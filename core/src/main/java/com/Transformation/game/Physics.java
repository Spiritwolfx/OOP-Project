package com.Transformation.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.dongbat.jbump.*;

import java.util.ArrayList;

/** contains all physics logic*/
public class Physics {
    public World<String> world = new World<>();

    /**custom collision filter to handle transformables as well as normal collidables*/
    public CollisionFilter heistFilter = new CollisionFilter() {
        @Override
        public Response filter(Item item, Item other) {
            // If it's not a wall, then it is a transformable (as we are the player, cannot collide with ourselves)
            if (other.userData.equals("wall")) return Response.slide;

            //ignoring transformables if we are ghost (BaseForm)
            if (item.userData.equals("BaseForm"))
                return null;

            return Response.slide;
        }
    };


    //constructor
    public Physics(Player myPlayer, TiledMap map){
        loadWalls(map);
        loadTransformables(map);

        //adding our player hitbox to our jbump world
        world.add(myPlayer.hitbox, myPlayer.x, myPlayer.y,
            myPlayer.getWidth(),myPlayer.getHeight());
    }

    /** to update hitbox to sprite size when player transforms */
    public void updateHitboxSize(Player myPlayer) {
        // remove the old hitbox
        world.remove(myPlayer.hitbox);

        // add the hitbox with new dimensions
        world.add(myPlayer.hitbox, myPlayer.x, myPlayer.y, myPlayer.getWidth(), myPlayer.getHeight());
    }

    /** gets all the collidable objects from our object layer*/
    public void loadWalls(TiledMap map) {
        //getting all objects from the object layer 1
        MapLayer collisionLayer = map.getLayers().get("Object Layer 1");
        MapObjects objects = collisionLayer.getObjects();

        //We need the map's total pixel height to flip the Y-axis
        //float mH = map.getProperties().get("height", Integer.class) * map.getProperties().get("tileheight", Integer.class);

        //looping through all the objects and adding to jbump
        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                //down-casting then getting our rectangle
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                // THE CRITICAL FLIP:
                // libGDX_Y = Total_Map_Height - Tiled_Y - Rectangle_Height
                //float visualY = mH - rect.y - rect.height;
                //System.out.println("Tiled Y: " + rect.y + " | Flipped Y: " + visualY + " | Map Total H: " + mH);
                world.add(new Item<>("wall"), rect.x, rect.y, rect.width, rect.height);

            }
        }
    }

    public void loadTransformables(TiledMap map){
        MapLayer transformablesLayer = map.getLayers().get("transformables");
        MapObjects objects = transformablesLayer.getObjects();

        //looping through all the objects and adding to jbump
        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {

                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                //getting and placing the formName from Tiled rectangle to our Item

                world.add(new Item<>(object.getProperties().get("formName",String.class)), rect.x, rect.y, rect.width, rect.height);

            }
        }
    }

    public String getNearbyTransformable(Player player) {
        // adding extra range around the player to make the search area
        float range = 25f;

        float searchX = player.x - range;
        float searchY = player.y - range;
        float searchW = player.getWidth() + (range * 2);
        float searchH = player.getHeight() + (range * 2);

        ArrayList<Item> items = new ArrayList<>();
        //creating a list of all rectangles in the search area
        world.queryRect(searchX, searchY, searchW, searchH,CollisionFilter.defaultFilter,items);

        String closestForm = null;
        float minDistanceSq = Float.MAX_VALUE; // assigning highest value possible to minimum distance (squared)

        // getting the center of the player
        float playerCenterX = player.x + (player.getWidth() / 2);
        float playerCenterY = player.y + (player.getHeight() / 2);

        for (Item item : items) {
            // filtering out the walls and the player itself
            if (!(item.userData.equals("wall")) && !(item.userData.equals(player.currForm.formName))) {

                // getting the rectangle of the item we got from our world.queryRect
                Rect rect = world.getRect(item);

                // getting center of this object
                float itemCenterX = rect.x + (rect.w / 2);
                float itemCenterY = rect.y + (rect.h / 2);

                // calculate squared distance (faster than Math.sqrt)
                float dx = playerCenterX - itemCenterX; // difference in x
                float dy = playerCenterY - itemCenterY; // difference in y
                float distSq = (dx * dx) + (dy * dy);

                // check if this is the closest one so far
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestForm = (String) item.userData;
                }
            }
        }

        return closestForm; // Returns the nearest string, or null if nothing was found
    }
}
