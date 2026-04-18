package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class TestForm extends MimicForm{
    public TestForm() {
        this.formName = "Box";
        this.speed = 400f;
        this.weight = 700f;
        this.textureName = "ghost2.png"; // default player sprite
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed");
    }
}
