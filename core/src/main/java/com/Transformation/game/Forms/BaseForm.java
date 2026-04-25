package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class BaseForm extends MimicForm {
    public BaseForm() {
        this.formName = "BaseForm";
        this.speed = 200f;
        this.weight = 1000f;
        this.textureName = "ghost2cropped.png";// default player sprite
        loadSprite();
    }

    @Override
    public void onTransform(Player player) {
        // default form, nothing special happens
    }
}
