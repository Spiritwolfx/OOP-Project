package com.Transformation.game.Npcs;

import com.Transformation.game.Physics.Physics;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
public class NPC2 extends NPC {

    private Animation<TextureRegion> sleepAnim;
    private Animation<TextureRegion> shockAnim;
    private TextureRegion dead = new TextureRegion(new Texture("shocked.png"));

    public boolean shocked = false;


    public NPC2(float x, float y, String sleepFile) {
        this.pos = new Vector2(x, y);

        // Load the sleep animation sheet
        Texture sheet = new Texture(sleepFile);

        // Calculate frame dimensions based on the provided frameCount
        int frameWidth = sheet.getWidth() / 4;
        int frameHeight = sheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        // Flatten the 2D array into a 1D array for the Animation object
        TextureRegion[] frames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            frames[i] = tmp[0][i];
        }

        // Initialize the animation (0.2f is the frame duration, adjust for speed)
        this.sleepAnim = new Animation<>(0.5f, frames);

        sheet = new Texture("electric_explosion.png");

        // slice (10 frames wide, 1 frame high)
        frameWidth = sheet.getWidth() / 10;
        frameHeight = sheet.getHeight();
        tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        frames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            frames[i] = tmp[0][i];
        }

        // finally SHOCK animation done as well
        this.shockAnim = new Animation<>(0.1f, frames);

    }

    public void update(float delta,Physics physics) {
        stateTime += delta;
        // Since this NPC only sleeps, we don't need movement logic here
    }

    public void draw(SpriteBatch batch) {
        // Get the current frame of the sleep animation
        TextureRegion frame;


        if (this.shocked){
            frame = shockAnim.getKeyFrame(stateTime, true);
            batch.draw(dead,pos.x,pos.y,128,64);
            batch.draw(frame,pos.x - 64,pos.y-96,256,256);
        }
        else{
            frame = sleepAnim.getKeyFrame(stateTime, true);
            // draw the frame centered appropriately
            batch.draw(frame, pos.x , pos.y, 128, 64);
        }
    }

    public void setShocked(boolean isShocked){
        this.shocked = isShocked;
    }

}
