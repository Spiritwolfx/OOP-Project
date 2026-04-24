package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class StoveForm extends MimicForm {
    public boolean doorOpen = false;
    public StoveForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 0f;
        this.weight = 700f;
    }

    public void openDoor(){
        // set open door texture
        // load sprite
        doorOpen = true;
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
