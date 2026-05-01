package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class HairDryerForm extends MimicForm {

    public boolean isStart = false;
    public HairDryerForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 200f ;
        this.weight = 1200f;
        this.textureName = "HairDryer.png";
        loadSprite();
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
