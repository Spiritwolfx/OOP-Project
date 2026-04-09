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
    public static World<String> world = new World<>();

    /** gets all the collidable objects from our object layer*/
    public static void getWalls(TiledMap map) {
        //getting all objects from the object layer 1
        MapLayer collisionLayer = map.getLayers().get("Object Layer 1");
        MapObjects objects = collisionLayer.getObjects();

        //looping through all the objects and adding to jbump
        for (MapObject object : objects) {
            if (object instanceof RectangleMapObject) {
                //down-casting then getting our rectangle
                Rectangle rect = ((RectangleMapObject) object).getRectangle();

                world.add(new Item<>("Wall"), rect.x, rect.y, rect.width, rect.height);

            }
        }
    }

}
