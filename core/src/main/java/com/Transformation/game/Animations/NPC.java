package com.Transformation.game.Animations;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class NPC {
    public enum State { IDLE,STANDING, WALKING }
    public State state = State.WALKING;

    private Vector2 pos;
    private float stateTime = 0;
    public float targetX;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> idleAnim;

    // CONSTRUCTOR: Now it does the slicing for you!
    public NPC(float x, float y, String walkFile, String idleFile) {
        this.pos = new Vector2(x, y);

        // 1. Load the image
        Texture sheet = new Texture(walkFile);

        // 2. Slice it (12 frames wide, 1 frame high)
        int frameWidth = sheet.getWidth() / 10;
        int frameHeight = sheet.getHeight();
        TextureRegion[][] tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        // 3. Put into a flat array for the Animation object
        TextureRegion[] frames = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            frames[i] = tmp[0][i];
        }

        this.walkAnim = new Animation<TextureRegion>(0.1f, frames);

        // 1. Load the image
        sheet = new Texture(idleFile);

        // 2. Slice it (12 frames wide, 1 frame high)
        frameWidth = sheet.getWidth() / 11;
        frameHeight = sheet.getHeight();
        tmp = TextureRegion.split(sheet, frameWidth, frameHeight);

        // 3. Put into a flat array for the Animation object
        frames = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            frames[i] = tmp[0][i+1];
        }

        this.idleAnim = new Animation<TextureRegion>(0.5f, frames);
    }

    public void update(float delta) {
        stateTime += delta;
        if (state == State.WALKING && targetX != -1) {
            // Move toward target
            if (pos.x > targetX + 5) pos.x -= 60 * delta;
            else if (pos.x < targetX - 5) pos.x += 60 * delta;
            else {
                // Arrived!
                pos.x = targetX;
                state = State.STANDING; // Or a new 'INVESTIGATING' state
                targetX = -1;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame;

        // Pick the correct frame based on the current state
        if (state == State.WALKING) {
            frame = walkAnim.getKeyFrame(stateTime, true);
        } else {
            // This handles STANDING and any other state (like sitting)
            frame = idleAnim.getKeyFrame(stateTime, true);
        }

        // Draw the selected frame at your specific size (256x256)
        batch.draw(frame, pos.x - 128, pos.y, 256, 256);
        // offsetting the pos.x as draws from bottom left corner of frame
    }
}
