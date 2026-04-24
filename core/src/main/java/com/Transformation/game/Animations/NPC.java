package com.Transformation.game.Animations;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class NPC {
    public enum State { IDLE,STANDING, WALKING }
    public State state = State.IDLE;

    private Vector2 pos;
    private float stateTime = 0;
    public float targetX;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> idleAnim;

    // constructor that makes our animations as well
    public NPC(float x, float y, String walkFile, String idleFile) {
        this.pos = new Vector2(x, y);

        // loading our animations frames image
        Texture sheet = new Texture(walkFile);

        // slicing the frames it (10 frames wide, 1 frame high)
        int frameWidth = sheet.getWidth() / 10;
        int frameHeight = sheet.getHeight();

        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight); // slicing into regions

        // placing our regions into an array
        TextureRegion[] frames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            frames[i] = tmp[0][i];
        }

        //finally, the animation is ready
        this.walkAnim = new Animation<TextureRegion>(0.1f, frames);

        // now do the same for IDLE animation
        sheet = new Texture(idleFile);

        // slice (11 frames wide, 1 frame high)
        frameWidth = sheet.getWidth() / 11;
        frameHeight = sheet.getHeight();
        tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        // placing into array, skipping the first frame and last three frames for smooth animation
        frames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            frames[i] = tmp[0][i+1];
        }

        // finally IDLE animation done as well
        this.idleAnim = new Animation<TextureRegion>(0.5f, frames);
    }

    /** handles npc movememnt */
    public void update(float delta) {
        stateTime += delta;

        if (state == State.WALKING && targetX != -1) {
            // Move toward target
            if (pos.x > targetX + 5) pos.x -= 60 * delta;
            else if (pos.x < targetX - 5) pos.x += 60 * delta;
            else {
                // reached target position
                pos.x = targetX;
                state = State.STANDING;
                targetX = -1;

            }
        }
    }

    /** renders our NPC */
    public void draw(SpriteBatch batch) {
        TextureRegion frame;

        // pick the correct frame based on the current state
        if (state == State.WALKING) {
            frame = walkAnim.getKeyFrame(stateTime, true);
        } else {
            // This handles STANDING and any other state (if we add more)
            frame = idleAnim.getKeyFrame(stateTime, true);
        }

        // draw the selected frame at your specific size (256x256)
        batch.draw(frame, pos.x - 128, pos.y, 256, 256);
    }
}
