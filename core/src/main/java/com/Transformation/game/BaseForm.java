package com.Transformation.game;

public class BaseForm extends MimicForm {
    public BaseForm() {
        this.formName = "BaseForm";
        this.speed = 200f;
        this.weight = 1000f;
        this.textureName = "ghost2.png"; // default player sprite
    }

    @Override
    public void onTransform(Player player) {
        // default form, nothing special happens
    }
}
