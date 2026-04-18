package com.Transformation.game.Forms;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FormFactory {
    private static final Map<String, Supplier<MimicForm>> registry = new HashMap<>();


    static {
        // register your forms here as you add them
        registry.put("BaseForm",
            () -> new BaseForm());
        registry.put("Box",
            () -> new TestForm()); // random testForm for checking
    }

    public static MimicForm get(String formName) {
        Supplier<MimicForm> supplier = registry.get(formName);
        if (supplier != null) return supplier.get();
        return new BaseForm(); // fallback to default
    }
}
