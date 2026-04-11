package com.Transformation.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Response;

public class Player {
    //current form of the player, object to store the weight and velocity
    MimicForm currForm;

    public Sprite sprite;

    // velocity variables
    public float velX;
    public float velY;

    //jbump item
    public Item<String> hitbox;

    //player sprite coordinates
    public float x;
    public float y;

    //flag to allow jumping
    public boolean isGrounded = false;

    //constructor (texture is our player image)
    public Player(Texture texture, float startX, float startY) {
        this.sprite = new Sprite(texture);
        this.currForm = new BaseForm();
        this.sprite.setSize(128, 128);
        this.x = startX;
        this.y = startY;
        this.sprite.setPosition(this.x, this.y);
        this.hitbox = new Item<>("Player");
    }

    public void changeForm(String formName) {
        this.currForm = FormFactory.get(formName);
        this.currForm.onTransform(this);

//        System.out.println("Pressed");

        // swap the sprite texture to match the new form
        Texture newTex = new Texture(currForm.textureName);
        sprite.setTexture(newTex);
    }

    public void update(float delta, Physics physics) {
        // x-axis movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) velX = -currForm.speed;
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velX = currForm.speed;
        else velX = 0; // no key = stop immediately

        // gravity — pulls player down every frame
        velY -= currForm.weight * delta;

        // jump — fires once per tap, not held
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isGrounded) {
            velY = 400f;
        }

        // transform — press E near a transformable object
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            String formName = physics.getNearbyTransformable(x, y, getWidth(), getHeight());
            if (formName != null) {
                changeForm(formName);
            }
        }

        // jbump handles collision — moves player to where it's allowed to go
        Response.Result result = physics.world.move(
            hitbox,
            x + velX * delta,  // where you WANT to go
            y + velY * delta,
            physics.heistFilter  // using our custom collisionFilter (see definition)
        );

        // update position to where jbump allowed us to go
        x = result.goalX;
        y = result.goalY;
        sprite.setPosition(x, y);

        // check if we landed on something below us
        isGrounded = false; // reset every frame
        for (int i = 0; i < result.projectedCollisions.size(); i++) {
            com.dongbat.jbump.Collision col = result.projectedCollisions.get(i);
            if (col.normal.y == 1) { // something hit from below = ground
                isGrounded = true;
                velY = 0; // stop falling
            }
        }


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
