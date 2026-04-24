package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class BottleForm extends MimicForm {
    public BottleForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 200f ;
        this.weight = 1200f;
        this.textureName = "Assets/Assets/Bottle1.png"; // actual bench sprite
        loadSprite(); // ADD THIS
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
