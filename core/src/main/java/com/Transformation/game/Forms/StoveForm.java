package com.Transformation.game.Forms;

import com.Transformation.game.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StoveForm extends MimicForm {
    public boolean doorOpen = false;
    public boolean fire = false;
    public float fireTargetx;
    public float stateTime = 0;
    public int flameOffsetX;
    public int flameOffsetY;
    public int flameWidth;
    public int flameHeight;

    private Animation<TextureRegion> fireAnim;


    public StoveForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 0f;
        this.weight = 700000f;

        // In StoveForm constructor (or wherever you like)
        this.flameOffsetX = 70;     // move it to the right
        this.flameOffsetY = -20;    // move it up / down
        this.flameWidth   = 100;    // wider fire
        this.flameHeight  = 150;    // taller fire

        // loading our animations frames image
        Texture sheet = new Texture("fire.png");

        // slicing the frames it (8 frames wide, 1 frame high)
        int frameWidth = sheet.getWidth() / 8;
        int frameHeight = sheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight); // slicing into regions

        // placing our regions into an array
        TextureRegion[] frames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            frames[i] = tmp[0][i];
        }

        //finally, the animation is ready
        this.fireAnim = new Animation<TextureRegion>(0.1f, frames);
    }



    public void openStove(){
        this.textureName = "Assets/Assets/stove_open.png";
        loadSprite();
        doorOpen = true;
    }

    public void setFire(boolean fire){
        this.fire = fire;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // check if the main sprite exists before rendering
        if (sprite != null) sprite.draw(batch);

        // handle the fire animation logic if active
        if (fire){
            // capture the time elapsed since the last frame
            float delta = Gdx.graphics.getDeltaTime();

            // update the animation timer
            stateTime += delta;

            // retrieve the current frame of the fire animation
            TextureRegion frame = fireAnim.getKeyFrame(stateTime, true);

            // draw the animation frame at the specified offset and scale
            batch.draw(frame, x + flameOffsetX, y + flameOffsetY, flameWidth, flameHeight);
        }
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
