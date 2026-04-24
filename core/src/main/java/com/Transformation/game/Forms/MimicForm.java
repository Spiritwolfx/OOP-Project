package com.Transformation.game.Forms;

import com.Transformation.game.Player;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class MimicForm {
    public float speed;
    public float weight;
    public String formName; //name of the form e.g. "Box"
    public String textureName; //filename of the sprite e.g. "ghost2.png"
    public float x, y, width, height; //details of the form when changing forms
    public Sprite sprite; //sprite reference

    //called once when form is created
    public void loadSprite() {
        if (textureName != null) {
            Texture tex = new Texture(textureName);
            sprite = new Sprite(tex);
            sprite.setSize(width, height);
            sprite.setPosition(x, y);
        }
    }

    //draws the form at its current position
    public void draw(SpriteBatch batch) {
        if (sprite != null) sprite.draw(batch);
    }

    //each form must define what happens when you transform into it
    public abstract void onTransform(Player player);
}
