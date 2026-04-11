package com.Transformation.game;

public abstract class MimicForm {
    public float speed;
    public float weight;
    public String textureName; //filename of the sprite e.g. "ghost2.png"

    //each form must define what happens when you transform into it
    public abstract void onTransform(Player player);
}
