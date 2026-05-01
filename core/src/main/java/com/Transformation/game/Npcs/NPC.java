package com.Transformation.game.Npcs;

import com.Transformation.game.Physics.Physics;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class NPC {
    public Vector2 pos;
    public float stateTime = 0;

    public abstract void update(float delta, Physics physics);
    public abstract void draw(SpriteBatch batch);

    public float getX() { return pos.x; }
    public float getY() { return pos.y; }
}
