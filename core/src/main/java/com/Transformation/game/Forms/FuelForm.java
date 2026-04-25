package com.Transformation.game.Forms;

import com.Transformation.game.Animations.NPC;
import com.Transformation.game.Physics.HitboxFactory;
import com.Transformation.game.Physics.Physics;
import com.Transformation.game.Player;
import com.dongbat.jbump.Collision;
import com.dongbat.jbump.Response;

public class FuelForm extends MimicForm {

    public boolean isBroken = false; // is the bottle broken or not

    public FuelForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 200f ;
        this.weight = 1200f;
        this.textureName = "Assets/Assets/FuelBottle.png";
        loadSprite();
    }

    /** checks if the bottle is on the ground */
    public boolean isTouchingGround(Player player,Physics physics) {
        // trying to move the bottle 1 unit down to see if it collides with anthing
        Response.Result check = physics.world.check(
            HitboxFactory.getHitbox(this.formName),
            x,
            y - 1, // Subtract 1 if Y-up (LibGDX), Add 1 if Y-down
            physics.heistFilter
        );

        // checking the objects it collided with
        for (int i = 0; i < check.projectedCollisions.size(); i++) {
            Collision col = check.projectedCollisions.get(i);

            // checking if any of the objects was a floor
            String type = (String) col.other.userData;

            if (type.equals("floor") && col.normal.y == 1) {
                // can no longer move the bottle as it is broken
                this.speed = 0f;

                // bottle breaks
                isBroken = true;
                player.changeForm("BaseForm",physics);
            }

        }
        return isBroken;
    }

    public void checkHitNpc(NPC npc, Player player, Physics physics){
        // project the bottle's movement 1 unit down to check for collision
        Response.Result check = physics.world.check(
            HitboxFactory.getHitbox(this.formName),
            this.x,
            this.y - 1, // Checking downwards
            physics.heistFilter
        );

        // loop through what we might have hit
        for (int i = 0; i < check.projectedCollisions.size(); i++) {
            Collision col = check.projectedCollisions.get(i);

            // check if the 'other' object is the NPC
            String type = (String) col.other.userData;

            if (type != null && type.equals("NPC")) {
                System.out.println("HITTT - NPC Soaked!");

                this.speed = 0f;    // stop the bottle
                this.isBroken = true;
                player.changeForm("BaseForm",physics);

            }
        }


    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}

