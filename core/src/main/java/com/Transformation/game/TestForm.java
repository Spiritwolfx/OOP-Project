package com.Transformation.game;

public class TestForm extends MimicForm{
    public TestForm() {
        this.speed = 300f;
        this.weight = 1000f;
        this.textureName = "ghost2.png"; // default player sprite
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed");
    }
}
