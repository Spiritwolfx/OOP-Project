package com.Transformation.game;

import com.Transformation.game.Animations.NPC;
import com.Transformation.game.Forms.*;
import com.Transformation.game.Physics.HitboxFactory;
import com.Transformation.game.Physics.Physics;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

import com.badlogic.gdx.utils.ScreenUtils;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Rect;

import java.text.Normalizer;
import java.util.Set;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class TransformationGame extends ApplicationAdapter {
    private enum GameState { INTRO, PLAYING, FAILED }
    private GameState gameState = GameState.INTRO;


    //if you want to set a level timer
//    private float failTimer = 0f; // counts time to detect failure
//    private static final float FAIL_TIME_LIMIT = 10f; // 2 minutes to complete level

    private SpriteBatch batch;

    private TiledMap map;
    private TiledMapRenderer renderer;
    private OrthographicCamera camera;

    private Physics myPhysics;
    private Player myPlayer;
    private ShapeRenderer shapeRenderer;
    private ParticleEffect glassBreak;
    private NPC npc;

    //HUD and text
    private BitmapFont font;
    private String hudMessage = "";
    private String failReason = "";

    private int currentLevel;

    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        currentLevel = 1;
        loadLevel();

        //load text
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);

        //calculating map size
        float mapWidth = map.getProperties().get("width", Integer.class) * map.getProperties().get("tilewidth", Integer.class);
        float mapHeight = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        //setting camera to show the entire map
        camera.setToOrtho(false, mapWidth, mapHeight);

        //center the camera on the map's middle
        camera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        camera.update();


        renderer = new OrthogonalTiledMapRenderer(map, 1f);
        shapeRenderer = new ShapeRenderer();

        glassBreak = new ParticleEffect();
        // The second argument is the directory where the 'particle.png' is located
        glassBreak.load(Gdx.files.internal("bottle_shatter.p"), Gdx.files.internal(""));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (gameState == GameState.INTRO) {
            drawIntroScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                gameState = GameState.PLAYING;
            }
            return; // don't update game logic during intro
        }

        if (gameState == GameState.FAILED) {
            drawFailScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                restartLevel();
            }
            return;
        }

        myPlayer.update(delta, myPhysics, currentLevel);
        npc.update(delta, myPhysics);


//        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        if (currentLevel == 1) {
            glassBreak.update(delta);
            checkLevel1Conditions();
            checkLevel1Fail();
        }


        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        renderMap();

        batch.begin();
        npc.draw(batch);
        for (MimicForm transformable : FormFactory.getAllForms()){
            transformable.draw(batch);
        }

        myPlayer.draw(batch);
        if (currentLevel == 1)
            glassBreak.draw(batch);
        batch.end();

        //showing all rectangles and other shapes in tiled vs in jbump
        showTiledShapes();
        showJbumpWorld();

        //draw hud messages
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.draw(batch, hudMessage, 20, Gdx.graphics.getHeight() - 20);
        batch.end();

    }

    private void checkLevel1Fail() {
        BottleForm bottle = (BottleForm) FormFactory.get("BottleForm");
        FuelForm fuel = (FuelForm) FormFactory.get("FuelForm");
        StoveForm stove = (StoveForm) FormFactory.get("StoveForm");

//        // fail if time runs out
//        if (failTimer >= FAIL_TIME_LIMIT) {
//            failReason = "You ran out of time!";
//            gameState = GameState.FAILED;
//            return;
//        }

        // bottle broke — wait for NPC to walk to it, then check if too far from stove
        if (bottle != null && bottle.isBroken && stove != null) {
            float bottleToStoveDist = Math.abs(bottle.x - stove.x);
            float npcToBottleDist = Math.abs(npc.getX() - bottle.x);

            // NPC has arrived at bottle position
            if (npcToBottleDist < 100f && bottleToStoveDist > 300f) {
                failReason = "The bottle was too far from the stove!\nThe detective investigated but smelled nothing.";
                gameState = GameState.FAILED;
                return;
            }
        }

        // fuel bottle hit ground but missed NPC
        if (fuel != null && fuel.isBroken && !npc.wet) {
            failReason = "The fuel bottle missed the detective!\nHe isn't flammable.";
            gameState = GameState.FAILED;
        }

        // fail if bottle dropped too far from stove
        if (bottle != null && bottle.isBroken) {
            if (stove != null) {
                float dist = Math.abs(bottle.x - stove.x);
                if (dist > 300f) { // too far from stove
                    failReason = "The bottle broke too far from the stove!\nThe detective won't investigate.";
                    gameState = GameState.FAILED;
                }
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }

    /*loads your level map and creates new jbump world**/
    public void loadLevel(){
        String mapPath = null;

        if (currentLevel== 1){
            mapPath = "Assets/Assets/game_level_1.tmx";
        }
        //ensures no error occurs if this is the first map being loaded
        if (map != null) map.dispose();

        map = new TmxMapLoader().load(mapPath);

        //creating our player
        myPlayer = new Player(0, 0);

        // Use point object instead of rectangle
        //int mapRows = map.getProperties().get("height", Integer.class);
        //int tileH = map.getProperties().get("tileheight", Integer.class);
        //float mapHeight = mapRows * tileH;

        //placing our player at the spawn point
        MapObject spawn = map.getLayers().get("spawn").getObjects().get(0);
        float spawnX = spawn.getProperties().get("x", Float.class);
        float spawnY = spawn.getProperties().get("y", Float.class);
        //float correctedY = mapHeight - spawnY - myPlayer.getHeight();

        myPlayer.x = spawnX;
        myPlayer.y = spawnY;


        //creating our physics engine object
        myPhysics = new Physics(myPlayer, map);

        //placing our npc at the NPC spawn point
        spawn = map.getLayers().get("NPC").getObjects().get(0);
        spawnX = spawn.getProperties().get("x", Float.class);
        spawnY = spawn.getProperties().get("y", Float.class);

        npc = new NPC(spawnX,spawnY,"LeftWalk.png","LeftIdle.png",myPhysics);

    }

    public void checkLevel1Conditions(){
        setMessage(""); // initialize messages

        // retrieve the bottle and fuel form instances from the factory
        BottleForm bottle = (BottleForm) FormFactory.get("BottleForm");
        FuelForm fuel = (FuelForm) FormFactory.get("FuelForm");

        // exit if the bottle instance is missing
        if (bottle == null) return;

        // if the bottle is not broken then
        if (!bottle.isBroken) {
            // if npc is not at target then check if bottle is on the ground (breaks on ground), if so move npc
            if ((npc.targetX != -1) && (bottle.isTouchingGround(myPlayer, myPhysics))) {
                // position and trigger the glass breaking particle effect
                glassBreak.setPosition(bottle.x, bottle.y + 20);
                glassBreak.start();

                // update npc target coordinates and switch state to walking
                npc.targetX = bottle.x;
                npc.state = NPC.State.WALKING;

                // remove the physical hitbox and clear the sprite for the bottle
                myPhysics.world.remove(HitboxFactory.getHitbox("BottleForm"));
                bottle.sprite = null;
            }
        }

        // if the fuel bottle is not broken yet then check for collisions
        if (!fuel.isBroken){
            fuel.checkHitNpc(npc, myPlayer, myPhysics);

            // handle logic for when the fuel bottle breaks upon impact
            if (fuel.isBroken){
                // play breaking effect and set the npc status to wet
                glassBreak.setPosition(fuel.x, fuel.y + 20);
                glassBreak.start();
                npc.setWet(true);

                // remove the fuel hitbox from the physics world and clear its sprite
                myPhysics.world.remove(HitboxFactory.getHitbox("FuelForm"));
                fuel.sprite = null;
            }
        }

        // check if the player is currently in the stove form
        if (myPlayer.currForm.formName.equals("StoveForm")){

            StoveForm stove = (StoveForm) myPlayer.currForm;

            // handle the stove door state and input
            if (!stove.doorOpen) {
                // notify player to open the door and check for key press
                System.out.println("O for Open");
                setMessage("Press O to open the stove door");
                if (Gdx.input.isKeyJustPressed(Input.Keys.O))
                    stove.open_stove();
            }
            else{
                // if the conditions are met allow the player to ignite the fuel
                if (bottle.isBroken && npc.wet){
                    // notify player to fire and check for key press
                    System.out.println("Press F to FiRe!!!!");
                    setMessage("Press F to ignite!");
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        // set the fire target location and activate the fire state
                        stove.fireTargetx = bottle.x;
                        stove.setFire(true);
                    }
                }
            }

        }
    }

    /** to view tiled rectangles */
    public void showTiledShapes(){
        // --- DEBUG BOXES SECTION ---
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(com.badlogic.gdx.graphics.Color.RED);

        // We need the map's total pixel height to flip the Y-axis
        //float mH = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        // Loop through every layer to find your collision boxes
        MapLayer collisionLayer = map.getLayers().get("Collision Layer");
        MapObjects objects = collisionLayer.getObjects();
        for (MapObject obj : objects) {
            if (obj instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) obj).getRectangle();

                // THE CRITICAL FLIP:
                // libGDX_Y = Total_Map_Height - Tiled_Y - Rectangle_Height
                //float visualY = mH - rect.y - rect.height ;

                //System.out.println("Map Height: " + mH);
                //System.out.println("rect y " + rect.y);
                //System.out.println("rect.height : " + rect.height);
                //System.out.println("visual y : " + visualY);

                shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);

            }
        }



        shapeRenderer.end();
    }

    /** to view all objects in our jbump world */
    private void showJbumpWorld() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN); //using green for physics world

        //getting all items currently in the jbump world
        Set<Item> items = myPhysics.world.getItems();

        for (Item<?> item : items) {
            //getting box of each item
            Rect rect = myPhysics.world.getRect(item);

            //rendering the object
            if (!(item.userData.equals("wall")) && !(item.userData.equals("BaseForm")) && !(item.userData.equals("floor"))) {
                shapeRenderer.setColor(Color.RED); //using red for transformables
                shapeRenderer.rect(rect.x, rect.y, rect.w, rect.h);
                shapeRenderer.setColor(Color.GREEN);
                continue;
            }
            shapeRenderer.rect(rect.x, rect.y, rect.w, rect.h);
        }

        shapeRenderer.end();
    }

    /** to render all the visible object layers from our Tiled map */
    public void renderMap(){
        batch.begin();

        for (MapLayer layer : map.getLayers()) {
            // checking if layer is visible
            if (layer.isVisible()) {

                // getting all our objects
                for (MapObject object : layer.getObjects()) {

                    if (object instanceof TiledMapTileMapObject) {
                        TiledMapTileMapObject tileObj = (TiledMapTileMapObject) object;

                        float width = tileObj.getProperties().get("width", Float.class);
                        float height = tileObj.getProperties().get("height", Float.class);

                        batch.draw(
                            tileObj.getTile().getTextureRegion(),
                            tileObj.getX(),
                            tileObj.getY(),
                            width,
                            height
                        );
                    }
                }
            }
        }

        batch.end();
    }

    private void drawIntroScreen() {
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // draw dark background box
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.8f));
        shapeRenderer.rect(50, 110, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 150);
        shapeRenderer.end();

        // draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(50, 110, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 150);
        shapeRenderer.end();

        // draw text
        batch.setProjectionMatrix(hudCamera.combined);
        int h = Gdx.graphics.getHeight();
        int w = Gdx.graphics.getWidth();

        batch.begin();
        font.setColor(Color.YELLOW);
        font.draw(batch, "LEVEL 1 - THE HAUNTING", w * 0.1f, h * 0.88f);

        font.setColor(Color.WHITE);
        font.draw(batch, "Your mission:", w * 0.1f, h * 0.78f);
        font.draw(batch, "1. Transform into the BOTTLE on the shelf", w * 0.1f, h * 0.70f);
        font.draw(batch, "   and drop it near the STOVE.", w * 0.1f, h * 0.64f);
        font.draw(batch, "2. The detective will walk over to investigate.", w * 0.1f, h * 0.57f);
        font.draw(batch, "3. Transform into the FUEL BOTTLE", w * 0.1f, h * 0.50f);
        font.draw(batch, "   and drop it ON the detective.", w * 0.1f, h * 0.44f);
        font.draw(batch, "4. Get into the STOVE and press O to open,", w * 0.1f, h * 0.37f);
        font.draw(batch, "   then press F to fire!", w * 0.1f, h * 0.31f);

        font.setColor(Color.GREEN);
        font.draw(batch, "Press ENTER to begin...", w * 0.1f, h * 0.15f);
        batch.end();

        font.setColor(Color.WHITE); // reset color
    }

    private void drawFailScreen() {
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.5f, 0, 0, 0.9f));
        shapeRenderer.rect(100, 100, Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - 200);
        shapeRenderer.end();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(Color.RED);
        font.draw(batch, "MISSION FAILED", 200, Gdx.graphics.getHeight() - 200);
        font.setColor(Color.WHITE);
        font.draw(batch, failReason, 150, Gdx.graphics.getHeight() - 300);
        font.setColor(Color.YELLOW);
        font.draw(batch, "Press R to restart", 150, 160);
        batch.end();

        font.setColor(Color.WHITE);
    }

    //method to set state of message
    public void setMessage(String message) {
        hudMessage = message;
    }

    private void restartLevel() {
//        failTimer = 0f;
        failReason = "";
        hudMessage = "";
        gameState = GameState.INTRO; // show intro again, or set to PLAYING to skip
        loadLevel();
        renderer = new OrthogonalTiledMapRenderer(map, 1f);
    }
}

