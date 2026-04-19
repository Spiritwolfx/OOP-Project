package com.Transformation.game.Forms;

import com.Transformation.game.Physics.PropInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FormFactory {
    private static final Map<String, MimicForm> registry = new HashMap<>();

    // called once at level load by Physics.loadTransformables()
    public static void loadLevelRegistry(ArrayList<PropInstance> propsFromTiled) {
        registry.clear(); // wipe old level's forms

        for (PropInstance prop : propsFromTiled) {
            MimicForm newForm = null;

            if (prop.name.equals("Box")) {
                newForm = new TestForm(prop.name, prop.x, prop.y, prop.width, prop.height);
            }
            // add more forms here as you build them

            if (newForm != null) {
                registry.put(prop.name, newForm);
            }
        }
    }

    public static MimicForm get(String formName) {
        return registry.getOrDefault(formName, new BaseForm());
    }
}

