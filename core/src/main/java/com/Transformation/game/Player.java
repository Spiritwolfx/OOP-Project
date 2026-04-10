package com.Transformation.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dongbat.jbump.Item;

public class Player {

    //For shivam: edit this class for your logic, the texture is how you look
    //physics will handle the hitbox changes
    public Sprite sprite;

    //jbump item
    public Item<String> hitbox;

    //player sprite coordinates
    public float x;
    public float y;

    //constructor (texture is our player image)
    public Player(Texture texture,float startX, float startY){
        this.sprite = new Sprite(texture);
        this.sprite.setSize(64,64);
        this.x= startX;
        this.y= startY;
        this.sprite.setPosition(this.x, this.y);

        this.hitbox= new Item<>("Player");
    }

    /** use in render() to draw our player */
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public float getWidth() {
        return sprite.getWidth();
    }
    public float getHeight() {
        return sprite.getHeight();
    }

}

