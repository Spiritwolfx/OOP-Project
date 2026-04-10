package com.Transformation.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.World;

/** contains all physics logic*/
public class Physics {
    public World<String> world = new World<>();

    //constructor
    public Physics(Player myPlayer, TiledMap map){
        getWalls(map);

        //adding our player hitbox to our jbump world
        world.add(myPlayer.hitbox, myPlayer.x, myPlayer.y,
            myPlayer.getWidth(),myPlayer.getHeight());
    }

    /** gets all the collidable objects from our object layer*/
    public void getWalls(TiledMap map) {
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
                world.add(new Item<>("Wall"), rect.x, rect.y, rect.width, rect.height);

            }
        }
    }

}
