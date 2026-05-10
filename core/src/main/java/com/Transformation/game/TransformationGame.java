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

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

    private enum GameState { INTRO, PLAYING, TRANSITION, LEVEL_INFO, FAILED, WIN }
    private GameState gameState = GameState.PLAYING;

    private int targetLevel = -1;
    private float transitionTimer = 0f;

    private float level2Timer = 40f; // 60 second countdown
    private float shockDelay = 3f;    // seconds to wait after shock
    private float shockTimer = 0f;    // counts up after shock

    private String failReason = "";
    private String winMessage = "";

    private BitmapFont font;

    private OrthographicCamera hudCamera;

    private static final float SPLASH_RADIUS = 80f;   // pixels – how close the fuel must land to the NPC

    @Override
    public void create() {
        batch = new SpriteBatch();

        font = new BitmapFont(); // font loader

        camera = new OrthographicCamera();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        currentLevel = 0;
        loadLevel();


    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        if (gameState == GameState.TRANSITION) {
            transitionTimer -= delta;
            drawTransitionScreen();

            // Once the timer hits 0, load the level or go back to playing
            if (transitionTimer <= 0) {
                if (targetLevel == 1 || targetLevel == 2) {
                    currentLevel = targetLevel;
                    loadLevel();
                    gameState = GameState.LEVEL_INFO;
                } else {
                    gameState = GameState.PLAYING;
                }
            }
            return; // Stop rendering the rest of the game while transitioning
        }

        if (gameState == GameState.LEVEL_INFO) {
            drawLevelInfoScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                gameState = GameState.PLAYING;
            }
            return;
        }

        if (gameState == GameState.FAILED) {
            drawFailScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                gameState = GameState.PLAYING;
                // Notice we do NOT set currentLevel = 0 here anymore.
                // It stays on the current level, and we just reload it:
                loadLevel();
            }
            return;
        }

        if (gameState == GameState.WIN) {
            drawWinScreen();
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                currentLevel = 0;          // go back to hub
                gameState = GameState.PLAYING;
                loadLevel();
            }
            return;
        }

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
                checkLevel1Fail();
                break;
            case 2:
                checkLevel2Conditions();
                checkLevel2Fail();
                checkLevel2Win();
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
//        for (MimicForm transformable : FormFactory.getAllForms()){
//            transformable.draw(batch);
//        }
        for (MimicForm transformable : FormFactory.getAllForms()){
            // Only draw if the sprite exists to prevent crashes
            if (transformable.sprite != null) {
                transformable.draw(batch);
            }
        }

        myPlayer.draw(batch);
        if (currentLevel == 0) {
            font.setColor(Color.WHITE);
            font.getData().setScale(2f); //make text slightly bigger

            // --- Room Labels ---
            // Level 1 (Upper Left)
            drawOutlinedText(batch, font, "Level 1", 160, 490, Color.WHITE);

            // Level 2 (Upper Right)
            drawOutlinedText(batch, font, "Level 2", 700, 490, Color.WHITE);

            // Level 3 (Lower Left - Coming Soon)
            drawOutlinedText(batch, font, "Level 3", 160, 220, Color.LIGHT_GRAY);
            font.getData().setScale(1.2f); // Shrink font for the subtext
            drawOutlinedText(batch, font, "(Soon)", 164, 185, Color.GRAY);
            font.getData().setScale(2f); // Reset back to big

            // Level 4 (Lower Right - Coming Soon)
            drawOutlinedText(batch, font, "Level 4", 670, 220, Color.LIGHT_GRAY);
            font.getData().setScale(1.2f); // Shrink font for the subtext
            drawOutlinedText(batch, font, "(Soon)", 674, 185, Color.GRAY);


            font.getData().setScale(1.3f);
            // Near bottom stairs
            if ((myPlayer.x <= 500) && (myPlayer.x >= 455) && (myPlayer.y <= 65)) {
                drawOutlinedText(batch, font, "Press U to go UP stairs", myPlayer.x - 60, myPlayer.y + 100, Color.WHITE);
            }
            // Near top stairs
            if ((myPlayer.x <= 482) && (myPlayer.x >= 412) && (myPlayer.y > 300)) {
                drawOutlinedText(batch, font, "Press J to go DOWN stairs", myPlayer.x - 70, myPlayer.y - 20, Color.WHITE);
            }
            font.getData().setScale(1f); // Reset scale
        }
        // Stove hints in Level 1
        if (currentLevel == 1 && myPlayer.currForm.formName.equals("StoveForm")) {
            StoveForm stove = (StoveForm) myPlayer.currForm;
            BottleForm bottle = (BottleForm) FormFactory.get("BottleForm");
            FuelForm fuel = (FuelForm) FormFactory.get("FuelForm");
            NPC1 npc1 = (NPC1) npc;

            if (bottle != null && fuel != null) {
                font.getData().setScale(1.2f);
                if (!stove.doorOpen) {
                    drawOutlinedText(batch, font, "Press O to Open",
                        stove.x + 30, stove.y + 120, Color.WHITE);
                } else if (bottle.isBroken && npc1.wet) {
                    drawOutlinedText(batch, font, "Press F to Fire",
                        stove.x + 30, stove.y + 120, Color.ORANGE);
                }
                font.getData().setScale(1f); // reset
            }
        }


        if (currentLevel == 1 || currentLevel == 2)
            particleEffect.draw(batch);
        batch.end();

        // --- HUD drawing (screen space) ---
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        float screenWidth  = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float refHeight = 720f;                           // [CHANGE] base resolution for scaling, increase = bigger text
        float fontScale = screenHeight / refHeight;       // dynamic scale factor (keep this formula)

        // 1. Timer background box (top‑right)
        if (currentLevel == 2) {
            // [CHANGE] box dimensions relative to screen
            float boxW = screenWidth * 0.21f;              // width: 25% of screen width
            float boxH = screenHeight * 0.05f;            // height: 5% of screen height
            float boxX = screenWidth - boxW - screenWidth * 0.02f; // X: 2% margin from right
            float boxY = screenHeight - boxH - screenHeight * 0.02f; // Y: 2% margin from top

            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(new Color(0, 0, 0, 0.5f));
            shapeRenderer.rect(boxX, boxY, boxW, boxH);
            shapeRenderer.end();
        }

        // 2. HUD text
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        if (currentLevel == 2) {
            // ---- Timer text (inside the box) ----
            String timerText = "Time: " + (int) level2Timer + "s";
            GlyphLayout layout = new GlyphLayout(font, timerText);
            Color timerColor = level2Timer <= 10 ? Color.RED : Color.WHITE;
            font.setColor(timerColor);

            font.getData().setScale(fontScale * 2.4f);     // [CHANGE] multiplier adjusts timer size

            // [CHANGE] text position
            float timerTextX = screenWidth - screenWidth * 0.1f - layout.width; // X: right‑aligned
            float timerTextY = screenHeight - screenHeight * 0.03f;              // Y: down from top edge

            font.draw(batch, timerText, timerTextX, timerTextY);
            font.setColor(Color.WHITE);

            // ---- hints ----
            font.getData().setScale(fontScale * 2f);     // [CHANGE] multiplier for hint size (0.7 = relatively smaller)

            float hintX = screenWidth * 0.02f;             // [CHANGE] horizontal position (2% from left)
            float hintY = screenHeight * 0.1f;            // [CHANGE] vertical position (4% from top)

            if (myPlayer.currForm.formName.equals("CabinetForm")) {
                CabinetForm cabinet = (CabinetForm) myPlayer.currForm;
                if (!cabinet.doorOpen) {
                    drawOutlinedText(batch, font, "Press O to Open", hintX, hintY, Color.WHITE);
                }
            } else if (myPlayer.currForm.formName.equals("HairDryerForm")) {
                HairDryerForm dryer = (HairDryerForm) myPlayer.currForm;
                if (!dryer.isStart) {
                    drawOutlinedText(batch, font, "Press S to Start", hintX, hintY, Color.WHITE);
                }
            }
            font.getData().setScale(1f); // reset
        }
        batch.end();

        // Reset back to world camera for debug shapes
        batch.setProjectionMatrix(camera.combined);
        //showing all rectangles and other shapes in tiled vs in jbump
        showTiledShapes();
        showJbumpWorld();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();

        if (font != null) { //dispose font
            font.dispose();
        }
    }

    /*loads your level map and creates new jbump world**/
    public void loadLevel(){
        npc = null;
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

                level2Timer = 40f;
                shockTimer = 0f;
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

        // spawn player on top of stairs
        if (currentLevel == 0) {
            myPlayer.x = 421.7f;
            myPlayer.y = 364f;
        } else {
            myPlayer.x = spawnX;
            myPlayer.y = spawnY;

        }


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

    public void checkLevel0Conditions(Physics physicsEngine){
        if ((myPlayer.x <= 500) && (myPlayer.x >= 455) && (myPlayer.y <= 65)){
            if (Gdx.input.isKeyPressed(Input.Keys.U)){
                physicsEngine.world.update(myPlayer.hitbox, 421.7f, 364f, myPlayer.getWidth(), myPlayer.getHeight());
                myPlayer.x = 421.7f;
                myPlayer.y = 364f;
            }
        }
        if ((myPlayer.x <= 482) && (myPlayer.x >= 412) && (myPlayer.y > 300)){
            if (Gdx.input.isKeyPressed(Input.Keys.J)){
                physicsEngine.world.update(myPlayer.hitbox, 472f, 65f, myPlayer.getWidth(), myPlayer.getHeight());
                myPlayer.x = 472f;
                myPlayer.y = 65f;
            }
        }

        if (myPlayer.y > 300){
            if (myPlayer.x < 349) {
                startTransition(1);
            }
            if (myPlayer.x > 510) {
                startTransition(2);
            }
        }

        if (myPlayer.y <= 65){
            if(myPlayer.x < 380){
                // Bump player right so they don't infinitely trigger Level 3
                myPlayer.x = 385;
                physicsEngine.world.update(myPlayer.hitbox, myPlayer.x, myPlayer.y, myPlayer.getWidth(), myPlayer.getHeight());
                startTransition(3);
            }
            if (myPlayer.x >= 610){
                // Bump player left so they don't infinitely trigger Level 4
                myPlayer.x = 600;
                physicsEngine.world.update(myPlayer.hitbox, myPlayer.x, myPlayer.y, myPlayer.getWidth(), myPlayer.getHeight());
                startTransition(4);
            }
        }
    }

    public void checkLevel1Conditions(){
        BottleForm bottle = (BottleForm) FormFactory.get("BottleForm");
        FuelForm fuel = (FuelForm) FormFactory.get("FuelForm");

        // Exit if either form is missing to prevent NullPointerExceptions
        if (bottle == null || fuel == null) return;

        NPC1 npc1 = (NPC1) npc;
        // if the bottle is not broken then
        if (!bottle.isBroken) {

            // Removed targetX restriction so NPC always investigates
            if (bottle.isTouchingGround(myPlayer, myPhysics)) {
                particleEffect.setPosition(bottle.x, bottle.y + 20);
                particleEffect.start();

                npc1.targetX = bottle.x;
                npc1.state = NPC1.State.WALKING;

                myPhysics.world.remove(HitboxFactory.getHitbox("BottleForm"));
                bottle.sprite = null;
            }
        }

        if (!fuel.isBroken){
            fuel.checkHitNpc(myPlayer, myPhysics);

            if (!fuel.isBroken) // only check floor if NPC wasn't hit
                fuel.isTouchingGround(myPlayer, myPhysics);

            if (fuel.isBroken){
                particleEffect.setPosition(fuel.x, fuel.y + 20);
                particleEffect.start();

                //wet the NPC ONLY if the fuel hit him
                if (fuel.brokenOnNPC) {
                    npc1.setWet(true);
                }

                myPhysics.world.remove(HitboxFactory.getHitbox("FuelForm"));
                fuel.sprite = null;
            }

        }

        if (myPlayer.currForm.formName.equals("StoveForm")){
            StoveForm stove = (StoveForm) myPlayer.currForm;

            if (!stove.doorOpen) {
                System.out.println("O for Open");
                if (Gdx.input.isKeyJustPressed(Input.Keys.O))
                    stove.openStove();
            }
            else{
                if (bottle.isBroken && npc1.wet){
                    System.out.println("Press F to FiRe!!!!");
                    if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        stove.fireTargetx = bottle.x;
                        stove.setFire(true);
                    }
                }
            }
        }
    }

    public void checkLevel1Fail() {
        FuelForm fuel = (FuelForm) FormFactory.get("FuelForm");
        BottleForm bottle = (BottleForm) FormFactory.get("BottleForm");
        StoveForm stove = (StoveForm) FormFactory.get("StoveForm");

        if (fuel == null || bottle == null) {
            System.out.println("FAIL CHECK: fuel=" + fuel + " bottle=" + bottle);
            return;
        }

        NPC1 npc1 = (NPC1) npc;

        // Debug print (optional)
        System.out.println("fuel.isBroken=" + fuel.isBroken
            + " | npc1.wet=" + npc1.wet
            + " | npc1.pos.x=" + npc1.pos.x
            + " | currForm=" + myPlayer.currForm.formName);

        // 1. Fuel wasted? (broke on ground, not directly on NPC)
        if (fuel.isBroken && !fuel.brokenOnNPC) {
            float dist = Math.abs(fuel.x - npc1.pos.x);

            if (dist > SPLASH_RADIUS) {
                // Too far – mission fails
                triggerFail("The fuel bottle smashed on the floor!\nHe needs to be soaked to catch fire.");
                return;
            } else {
                // Close enough – splash the NPC and keep going
                if (!npc1.wet) {
                    npc1.setWet(true);
                }
                // Do NOT fail – the level can still be won
            }
        }

        // 2. Bottle too far from the stove? (original check)
        if (bottle.isBroken && stove != null && !npc1.wet) {
            float bottleToStoveDist = Math.abs(bottle.x - stove.x);
            if (npc1.state == NPC1.State.STANDING && bottleToStoveDist > 300f) {
                triggerFail("The bottle was too far from the stove!\nThe person investigated but smelled nothing.");
                return;
            }
        }

        // 3. Fire missed the NPC? (now checks actual flame rectangle)
        if (myPlayer.currForm.formName.equals("StoveForm")) {
            stove = (StoveForm) myPlayer.currForm;
            if (stove.fire && stove.stateTime > 1.0f) {
                if (!isNpcInFlame(stove, npc1)) {
                    triggerFail("The fire missed the house member!\nHe wasn't standing close enough.");
                    return;
                } else {
                    // NPC caught in the fire – level complete!
                    triggerWin("The person went up in flames!\nVengeance is sweet.");
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

    public void checkLevel2Fail() {
        HairDryerForm dryer = (HairDryerForm) FormFactory.get("HairDryerForm");
        if (dryer == null) return;

        NPC2 npc2 = (NPC2) npc;

        // countdown timer
        level2Timer -= Gdx.graphics.getDeltaTime();
        if (level2Timer <= 0f && !npc2.shocked) {
            triggerFail("You ran out of time!\nThe person woke up before you could act.");
            return;
        }

        // fail if dryer was dropped in tub but wasn't turned on first
        if (dryer.sprite == null && !npc2.shocked && !dryer.isStart) {
            triggerFail("The hair dryer wasn't turned on!\nPress S before dropping it in the tub.");
            return;
        }
    }

    public void checkLevel2Win() {
        NPC2 npc2 = (NPC2) npc;
        if (npc2.shocked) {
            shockTimer += Gdx.graphics.getDeltaTime();
            if (shockTimer >= shockDelay) {
                triggerWin("The person got fried in the bathtub!\nVengeance has been served.");
            }
        } else {
            shockTimer = 0f;   // safety reset if shock is ever undone
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

    private void startTransition(int level) {
        targetLevel = level;
        transitionTimer = 2.0f; // Displays the screen for 2 seconds
        gameState = GameState.TRANSITION;
    }

    private void drawTransitionScreen() {
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Draw a solid black background for the cutscene
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        // Determine what text to show based on the level
        String mainText = "";
        String subText = "";

        if (targetLevel == 1 || targetLevel == 2) {
            mainText = "ENTERING LEVEL " + targetLevel;
            subText = "Get Ready...";
        } else {
            mainText = "LEVEL " + targetLevel;
            subText = "Coming Soon!";
        }

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(3.0f);

        // Draw the text roughly in the center
        font.draw(batch, mainText, screenWidth * 0.38f, screenHeight * 0.55f);

        font.getData().setScale(1.5f);
        font.setColor(Color.YELLOW);
        font.draw(batch, subText, screenWidth * 0.4f, screenHeight * 0.42f);

        batch.end();
        font.setColor(Color.WHITE); // reset color
    }

    // Helper method to draw text with a black outline for better legibility
    private void drawOutlinedText(SpriteBatch batch, BitmapFont font, String text, float x, float y, Color mainColor) {
        // Draw the black outline by offsetting the text 2 pixels in every direction
        font.setColor(Color.BLACK);
        font.draw(batch, text, x - 2, y); // left
        font.draw(batch, text, x + 2, y); // right
        font.draw(batch, text, x, y - 2); // down
        font.draw(batch, text, x, y + 2); // up

        // Draw the actual colored text on top
        font.setColor(mainColor);
        font.draw(batch, text, x, y);
    }

    private void drawLevelInfoScreen() {
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();
        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.5f);
        if (currentLevel == 1) {
            font.draw(batch, "LEVEL 1 MISSION", screenWidth * 0.35f, screenHeight * 0.8f);
            font.getData().setScale(1.6f);
            font.setColor(Color.YELLOW);
            font.draw(batch, "- Use the table to get to the bottle.", screenWidth * 0.1f, screenHeight * 0.6f);
            font.draw(batch, "- Get the NPC to the stove.", screenWidth * 0.1f, screenHeight * 0.5f);
            font.draw(batch, "- Drench him with the petrol bottle.", screenWidth * 0.1f, screenHeight * 0.4f);
            font.draw(batch, "- Open and fire on the stove.", screenWidth * 0.1f, screenHeight * 0.3f);
            font.draw(batch, "- Press E to transform into objects.", screenWidth * 0.1f, screenHeight * 0.2f);
        } else if (currentLevel == 2) {
            font.draw(batch, "LEVEL 2 MISSION", screenWidth * 0.35f, screenHeight * 0.8f);
            font.getData().setScale(1.6f);
            font.setColor(Color.YELLOW);
            font.draw(batch, "- Kill the person using the environment.", screenWidth * 0.1f, screenHeight * 0.6f);
            font.draw(batch, "- Press E to transform into objects.", screenWidth * 0.1f, screenHeight * 0.5f);
        }
        font.getData().setScale(1.7f);
        font.setColor(Color.LIGHT_GRAY);
        font.draw(batch, "Press ENTER to start...", screenWidth * 0.35f, screenHeight * 0.1f);
        batch.end();
    }

    public void triggerFail(String reason) {
        failReason = reason;
        gameState = GameState.FAILED;
    }

    public void triggerWin(String message) {
        winMessage = message;
        gameState = GameState.WIN;
    }

    private void drawWinScreen() {
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Green translucent overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.0f, 0.3f, 0.0f, 0.8f)); // dark green
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Main WIN text
        font.getData().setScale(3.0f);
        drawOutlinedText(batch, font, "LEVEL COMPLETE!", screenWidth * 0.3f, screenHeight * 0.7f, Color.GREEN);

        // Custom message
        font.getData().setScale(1.5f);
        drawOutlinedText(batch, font, winMessage, screenWidth * 0.25f, screenHeight * 0.5f, Color.WHITE);

        // Return to hub instruction
        font.getData().setScale(1.2f);
        drawOutlinedText(batch, font, "Press ENTER to return to hub", screenWidth * 0.33f, screenHeight * 0.35f, Color.LIGHT_GRAY);

        batch.end();
        font.getData().setScale(1f);   // reset scale
    }

    private void drawFailScreen() {
        OrthographicCamera hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Enable transparency for the red overlay
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        Gdx.gl.glBlendFunc(Gdx.gl.GL_SRC_ALPHA, Gdx.gl.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0.3f, 0.0f, 0.0f, 0.8f)); // Dark translucent red
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Main FAILED text
        font.getData().setScale(3.0f);
        drawOutlinedText(batch, font, "MISSION FAILED", screenWidth * 0.35f, screenHeight * 0.7f, Color.RED);

        // The specific reason you passed into triggerFail()
        font.getData().setScale(1.5f);
        drawOutlinedText(batch, font, failReason, screenWidth * 0.25f, screenHeight * 0.5f, Color.WHITE);

        // Restart Instructions
        font.getData().setScale(1.2f);
        drawOutlinedText(batch, font, "Press R to Restart Level", screenWidth * 0.38f, screenHeight * 0.35f, Color.LIGHT_GRAY);

        batch.end();

        // Reset scale for the rest of the game
        font.getData().setScale(1f);
    }

    private boolean isNpcInFlame(StoveForm stove, NPC1 npc1) {
        // Flame rectangle (same as draw() in StoveForm)
        float flameX = stove.x + stove.flameOffsetX;
        float flameY = stove.y + stove.flameOffsetY;
        float flameW = stove.flameWidth;
        float flameH = stove.flameHeight;

        // NPC hitbox (same as in NPC1 constructor)
        float npcX = npc1.pos.x + npc1.hitboxOffsetX;
        float npcY = npc1.pos.y + npc1.hitboxOffsetY;
        float npcW = npc1.hitboxWidth;
        float npcH = npc1.hitboxHeight;

        // AABB overlap test
        return flameX < npcX + npcW
            && flameX + flameW > npcX
            && flameY < npcY + npcH
            && flameY + flameH > npcY;
    }
}

