package com.Transformation.game.Physics;

import com.dongbat.jbump.Item;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** stores all our hitboxes in current level into a registry for easy access*/
public class HitboxFactory {
    // key: formName, value: the unique jbump Item
    private static final Map<String, Item<String>> hitboxRegistry = new HashMap<>();

    /** takes list of hitboxes and adds them to our registry*/
    public static void loadHitboxes(ArrayList<Item<String>> hitboxes) {
        hitboxRegistry.clear(); //clearing the registry at start of each level

        for (Item<String> hitbox : hitboxes) {
            //System.out.println(hitbox.userData); // for debugging purposes
            hitboxRegistry.put(hitbox.userData, hitbox);
        }
    }

    /** loads an individual hitbox into the registry */
    public static void loadIndividualHitbox(Item<String> hitbox){
        hitboxRegistry.put(hitbox.userData, hitbox);
        System.out.println(hitbox.userData);
    }

    /** returns the hitbox of the formName it gets in the parameter */
    public static Item<String> getHitbox(String formName) {
        return hitboxRegistry.get(formName);
    }
}
