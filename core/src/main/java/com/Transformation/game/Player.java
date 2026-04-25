package com.Transformation.game;

import com.Transformation.game.Forms.BaseForm;
import com.Transformation.game.Forms.FormFactory;
import com.Transformation.game.Forms.MimicForm;
import com.Transformation.game.Physics.Physics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dongbat.jbump.Item;
import com.dongbat.jbump.Response;

public class Player {
    //current form of the player, object to store the weight and velocity
    public MimicForm currForm;

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
    public Player(float startX, float startY) {
        this.currForm = new BaseForm();
        this.sprite = new Sprite(new Texture(this.currForm.textureName));
        this.sprite.setSize(64, 64);
        this.x = startX;
        this.y = startY;
        this.sprite.setPosition(this.x, this.y);
        this.hitbox = new Item<>(this.currForm.formName);
    }

    public void changeForm(String formName, Physics physics) {
        //if untransforming, move ghost to where the form was
        if (formName.equals("BaseForm")) {
            this.currForm.x = this.x;
            this.currForm.y = this.y;
            this.sprite.setPosition(this.x, this.y);
        } else {
            MimicForm targetForm = FormFactory.get(formName);
            this.x = targetForm.x;
            this.y = targetForm.y;
            this.sprite.setPosition(this.x, this.y);

//            //put the box back in jbump at its new position
//            physics.world.add(
//                new Item<>(this.currForm.formName),
//                this.currForm.x,
//                this.currForm.y,
//                this.currForm.width,
//                this.currForm.height
//            );
//        } else {
//            // remove static box from jbump — player becomes the box
//            physics.removeTransformable(formName);
        }

        this.currForm = FormFactory.get(formName);
        this.currForm.onTransform(this);
        physics.updateHitbox(this);

        if (currForm.textureName != null) {
            Texture newTex = new Texture(currForm.textureName);
            sprite.setTexture(newTex);
        }
//        sprite.setSize(currForm.width, currForm.height); // match sprite size to form
    }

    public void update(float delta, Physics physics, int currentLevel) {

        // x-axis movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) velX = -currForm.speed;
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)|| Gdx.input.isKeyPressed(Input.Keys.D)) velX = currForm.speed;
        else velX = 0; // no key = stop immediately

        // gravity — pulls player down every frame
        velY -= currForm.weight * delta;

        // jump — fires once per tap, not held
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) && isGrounded) {
            velY = 400f;
        }



        // jbump handles collision — moves player to where it's allowed to go
        Response.Result result = physics.world.move(
            hitbox,
            x + velX * delta,  // where you WANT to go
            y + velY * delta ,
            physics.heistFilter // using our custom collisionFilter (see definition)
        );

        // update position to where jbump allowed us to go
        this.x = result.goalX;
        this.y = result.goalY;
        sprite.setPosition(x, y);

        //update current form's position to match player
        if (!currForm.formName.equals("BaseForm")) {
            currForm.x = x;
            currForm.y = y;
            if (currForm.sprite != null)
                currForm.sprite.setPosition(x, y);
        }

        // check if we landed on something below us
        isGrounded = false; // reset every frame
        for (int i = 0; i < result.projectedCollisions.size(); i++) {
            com.dongbat.jbump.Collision col = result.projectedCollisions.get(i);
            if (col.normal.y == 1) { // something hit from below = ground
                isGrounded = true;
                velY = 0; // stop falling
            }
        }
        // transform — press E near a transformable object
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            String formName = physics.getNearbyTransformable(this,currentLevel);
            System.out.println("Form Name:" + formName);
            if (!currForm.formName.equals("BaseForm") && formName == null) {
                // if player is not in baseForm then change to baseForm
                if (isGrounded)
                    changeForm("BaseForm", physics);
            }
            else if (formName != null) {
                // else put player in nearest transformable form
                    changeForm(formName, physics);
            }
        }


    }

    /** use in render() to draw our player */
    public void draw(SpriteBatch batch) {
        if (currForm.formName.equals("BaseForm")) {
            sprite.draw(batch); // draw ghost
        } //else {
            //currForm.draw(batch); // draw the form's own sprite
        //}
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }
}
