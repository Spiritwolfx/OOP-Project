package com.Transformation.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
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
    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera();

        loadLevel("Test Map.tmx");

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
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        myPlayer.update(delta, myPhysics);


        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        renderer.setView(camera);
        renderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        myPlayer.draw(batch);
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
    public void loadLevel(String mapPath){

        //ensures no error occurs if this is the first map being loaded
        if (map != null) map.dispose();

        map = new TmxMapLoader().load(mapPath);

        //creating our player
        Texture playerTex = new Texture("ghost2.png");
        myPlayer = new Player(playerTex, 0, 0);

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
        myPlayer.sprite.setPosition(myPlayer.x, myPlayer.y);

        //creating our physics engine object
        myPhysics = new Physics(myPlayer, map);
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
        MapLayer collisionLayer = map.getLayers().get("Object Layer 1");
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
            if (!(item.userData.equals("wall")) && !(item.userData.equals("Player"))) {
                shapeRenderer.setColor(Color.RED); //using red for transformables
                shapeRenderer.rect(rect.x, rect.y, rect.w, rect.h);
                shapeRenderer.setColor(Color.GREEN);
                continue;
            }
            shapeRenderer.rect(rect.x, rect.y, rect.w, rect.h);
        }

        shapeRenderer.end();
    }
}
