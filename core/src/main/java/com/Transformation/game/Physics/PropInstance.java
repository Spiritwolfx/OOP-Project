package com.Transformation.game.Physics;


/**class to help create transformable objects in our level (used by physics)*/
public class PropInstance {

    public String name;
    public float x, y, width, height; //coordinates and dimensions

    //constructor
    public PropInstance(String name, float x, float y, float width, float height) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
