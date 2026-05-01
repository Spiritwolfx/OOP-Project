package com.Transformation.game.Npcs;

import com.Transformation.game.Physics.Physics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.dongbat.jbump.Item;

public class NPC1 extends NPC{
    public enum State { IDLE,STANDING, WALKING }
    public State state = State.IDLE;

    public float targetX;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> idleAnim;

    public boolean wet = false;
    private Color tint = Color.WHITE.cpy(); // starts as normal white
    public Item<String> hitbox = new Item<>("NPC");


    // constructor that makes our animations as well
    public NPC1(float x, float y, String walkFile, String idleFile, Physics physics) {
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
        this.walkAnim = new Animation<>(0.1f, frames);

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
        this.idleAnim = new Animation<>(0.5f, frames);

        physics.world.add(this.hitbox, pos.x - 20 , pos.y , 60, 143);
    }

    /** handles npc movememnt */
    public void update(float delta, Physics phsyics) {
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

            //move hitbox
            phsyics.world.move(this.hitbox,pos.x - 20 , pos.y , phsyics.heistFilter);
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

        batch.setColor(tint); // to change color if npc is wet
        // draw the selected frame at your specific size (256x256)
        batch.draw(frame, pos.x - 128, pos.y, 256, 256);

        batch.setColor(Color.WHITE); //reset the batch color
    }

    public void setWet(boolean wet) {
        this.wet = wet;
        if (wet) {
            // tints the NPC to a darker, blueish hue to look "soaked"
            // (Red, Green, Blue, Alpha)
            this.tint.set(0.6f, 0.6f, 1.0f, 1f);
            System.out.println("NPC is now flammable!");
        } else {
            this.tint.set(Color.WHITE);
        }
    }
}
