package com.Transformation.game;

import com.Transformation.game.Npcs.NPC;
import com.Transformation.game.Npcs.NPC1;
import com.Transformation.game.Forms.*;
import com.Transformation.game.Npcs.NPC2;
import com.Transformation.game.Physics.HitboxFactory;
import com.Transformation.game.Physics.Physics;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;

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

import java.util.Set;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class TransformationGame extends ApplicationAdapter {
    private SpriteBatch batch;

    private TiledMap map;
    private TiledMapRenderer renderer;
    private OrthographicCamera camera;

    private Physics myPhysics;
    private Player myPlayer;
    private ShapeRenderer shapeRenderer;
    private ParticleEffect particleEffect;
    private NPC npc;

    private int currentLevel;

    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        currentLevel = 0;
        loadLevel();


    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        myPlayer.update(delta, myPhysics, currentLevel);
        if (npc != null)
            npc.update(delta, myPhysics);

        if (currentLevel == 1 || currentLevel == 2)
            particleEffect.update(delta);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        switch(currentLevel){
            case 0:
                checkLevel0Conditions(myPhysics);
                break;
            case 1:
                checkLevel1Conditions();
                break;
            case 2:
                checkLevel2Conditions();
                break;
            case 3:
                System.out.println("Level 3");
                break;
            case 4:
                System.out.println("Level 4");
                break;
        }


        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        renderMap();

        batch.begin();
        if (npc != null)
            npc.draw(batch);
        for (MimicForm transformable : FormFactory.getAllForms()){
            transformable.draw(batch);
        }

        myPlayer.draw(batch);
        if (currentLevel == 1 || currentLevel == 2)
            particleEffect.draw(batch);
        batch.end();

        //showing all rectangles and other shapes in tiled vs in jbump
        showTiledShapes();
        showJbumpWorld();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }

    /*loads your level map and creates new jbump world**/
    public void loadLevel(){
        String mapPath = null;
        switch (currentLevel) {
            case 0:
                mapPath = "Assets/Assets/game.tmx";
                break;
            case 1:
                mapPath = "Assets/Assets/game_level_1.tmx";
                particleEffect = new ParticleEffect();

                // the second argument is the directory where the 'particle.png' is located
                particleEffect.load(Gdx.files.internal("bottle_shatter.p"), Gdx.files.internal(""));
                break;
            case 2:
                mapPath = "Assets/Assets/game_level_2tmx.tmx";
                particleEffect = new ParticleEffect();

                // the second argument is the directory where the 'particle.png' is located
                particleEffect.load(Gdx.files.internal("water_splash.p"), Gdx.files.internal(""));
                break;
            case 3:
                mapPath = "Assets/Assets/game_level_3.tmx";
                break;
            case 4:
                mapPath = "Assets/Assets/game_level_4.tmx";
                break;
        }

        //ensures no error occurs if this is the first map being loaded
        if (map != null) map.dispose();

        map = new TmxMapLoader().load(mapPath);

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

        if (currentLevel != 0) {
            //placing our npc at the NPC spawn point
            spawn = map.getLayers().get("NPC").getObjects().get(0);
            spawnX = spawn.getProperties().get("x", Float.class);
            spawnY = spawn.getProperties().get("y", Float.class);

            if (currentLevel == 1)
                npc = new NPC1(spawnX, spawnY, "LeftWalk.png", "LeftIdle.png", myPhysics);
            else if (currentLevel == 2){
                npc = new NPC2(spawnX,spawnY,"sleep.png");
            }
        }
    }

    public void checkLevel0Conditions(Physics physics){

        if ((myPlayer.x <= 500) && (myPlayer.x >= 455) && (myPlayer.y<= 65)){
            if (Gdx.input.isKeyPressed(Input.Keys.U)){
                physics.world.update(
                    myPlayer.hitbox,
                    421.7f,
                    364f,
                    myPlayer.getWidth(),
                    myPlayer.getHeight()
                );
                // 2. Synchronize the Player's visual coordinates
                myPlayer.x = 421.7f;
                myPlayer.y = 364f;
            }

        }
        if ((myPlayer.x <= 482) && (myPlayer.x >= 412) && (myPlayer.y > 300)){
            if (Gdx.input.isKeyPressed(Input.Keys.J)){
                physics.world.update(
                    myPlayer.hitbox,
                    472f,
                    65f,
                    myPlayer.getWidth(),
                    myPlayer.getHeight()
                );
                // 2. Synchronize the Player's visual coordinates
                myPlayer.x = 472f;
                myPlayer.y = 65f;
            }

        }
        if (myPlayer.y > 300){
            if (myPlayer.x < 349) {
                currentLevel = 1;
                loadLevel();
                return;
            }
            if (myPlayer.x > 510) {
                currentLevel = 2;
                loadLevel();
                return;
            }
        }
        if (myPlayer.y <= 65){
            if(myPlayer.x < 380){
                System.out.println("Level 3");
                return;
            }
            if (myPlayer.x >= 610){
                System.out.println("Level 4");
            }
        }
    }

    public void checkLevel1Conditions(){
        // retrieve the bottle and fuel form instances from the factory
        BottleForm bottle = (BottleForm) FormFactory.get("BottleForm");
        FuelForm fuel = (FuelForm) FormFactory.get("FuelForm");

        // exit if the bottle instance is missing
        if (bottle == null) return;

        NPC1 npc1 = (NPC1) npc;
        // if the bottle is not broken then
        if (!bottle.isBroken) {

            // if npc is not at target then check if bottle is on the ground (breaks on ground), if so move npc
            if ((npc1.targetX != -1) && (bottle.isTouchingGround(myPlayer, myPhysics))) {
                // position and trigger the glass breaking particle effect
                particleEffect.setPosition(bottle.x, bottle.y + 20);
                particleEffect.start();

                // update npc target coordinates and switch state to walking
                npc1.targetX = bottle.x;
                npc1.state = NPC1.State.WALKING;

                // remove the physical hitbox and clear the sprite for the bottle
                myPhysics.world.remove(HitboxFactory.getHitbox("BottleForm"));
                bottle.sprite = null;
            }
        }

        // if the fuel bottle is not broken yet then check for collisions
        if (!fuel.isBroken){
            fuel.checkHitNpc(myPlayer, myPhysics);

            // handle logic for when the fuel bottle breaks upon impact
            if (fuel.isBroken){
                // play breaking effect and set the npc status to wet
                particleEffect.setPosition(fuel.x, fuel.y + 20);
                particleEffect.start();
                npc1.setWet(true);

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
                if (Gdx.input.isKeyJustPressed(Input.Keys.O))
                    stove.openStove();
            }
            else{
                // if the conditions are met allow the player to ignite the fuel
                if (bottle.isBroken && npc1.wet){
                    // notify player to fire and check for key press
                    System.out.println("Press F to FiRe!!!!");
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        // set the fire target location and activate the fire state
                        stove.fireTargetx = bottle.x;
                        stove.setFire(true);
                    }
                }
            }

        }
    }

    public void checkLevel2Conditions(){
        // check if the player is currently in the cabinet form
        if (myPlayer.currForm.formName.equals("CabinetForm")) {

            CabinetForm cabinet = (CabinetForm) myPlayer.currForm;

            // handle the cabinet door state and input
            if (!cabinet.doorOpen) {
                // notify player to open the door and check for key press
                System.out.println("O for Open");
                if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
                    cabinet.openCabinet();
                }
            }
            return;

        }

        if (myPlayer.currForm instanceof HairDryerForm){
            HairDryerForm dryer = (HairDryerForm) myPlayer.currForm;
            NPC2 npc2 = (NPC2) npc;
            if (!dryer.isStart) {
                // notify player to open the door and check for key press
                System.out.println("S for Start");
                if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    dryer.isStart = true;
                }
            }

            if (myPlayer.x > 349 && myPlayer.y < 98){
                if (dryer.isStart){
                    npc2.setShocked(true);
                }
                particleEffect.setPosition(myPlayer.x,98);
                particleEffect.start();


                myPlayer.changeForm("BaseForm",myPhysics);

                // remove the physical hitbox and clear the sprite
                myPhysics.world.remove(HitboxFactory.getHitbox("HairDryerForm"));
                dryer.sprite = null;
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
}

