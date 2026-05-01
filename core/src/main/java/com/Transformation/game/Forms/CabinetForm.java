package com.Transformation.game.Forms;

import com.Transformation.game.Player;


public class CabinetForm extends MimicForm {
    public boolean doorOpen = false;

    public CabinetForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 0;
        this.weight = 7000000f;
        this.textureName = "cabinet.png"; // actual bench sprite
        loadSprite();
    }

    public void openCabinet(){
        this.textureName = "OpenCabinet.png";
        loadSprite();
        doorOpen = true;
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
