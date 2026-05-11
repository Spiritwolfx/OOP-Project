package com.Transformation.game.Forms;

import com.Transformation.game.Player;

public class TableForm extends MimicForm {
    public TableForm(String name, float x, float y, float width, float height) {
        this.formName = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = 400f;
        this.weight = 1820f;
        this.textureName = "Assets/Assets/table_rect_0.png"; // actual bench sprite
        loadSprite(); // ADD THIS
    }

    @Override
    public void onTransform(Player player) {
        System.out.println("Transformed into " + formName);
    }
}
