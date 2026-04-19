package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class TestForm extends MimicForm {
    public TestForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 400f;
        this.weight = 700f;
        this.textureName = "ghost2.png";
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
