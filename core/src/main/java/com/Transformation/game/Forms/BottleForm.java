package com.Transformation.game.Forms;

import com.Transformation.game.Physics.HitboxFactory;
import com.Transformation.game.Physics.Physics;
import com.Transformation.game.Player;
import com.dongbat.jbump.Collision;
import com.dongbat.jbump.Response;

public class BottleForm extends MimicForm {

    public boolean isBroken = false; // is the bottle broken or not

    public BottleForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 200f ;
        this.weight = 1200f;
        this.textureName = "Assets/Assets/Bottle1.png";
        loadSprite();
    }

    /** checks if the bottle is on the ground */
    public boolean isTouchingGround(Physics physics) {
        // trying to move the bottle 1 unit down to see if it collides with anthing
        Response.Result check = physics.world.check(
            HitboxFactory.getHitbox("BottleForm"),
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
            }

        }
        return isBroken;
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
