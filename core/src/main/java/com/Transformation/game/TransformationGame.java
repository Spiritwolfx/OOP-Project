package com.Transformation.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class TransformationGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;

    private TiledMapRenderer renderer;
    private OrthographicCamera camera;
    @Override
    public void create() {
        batch = new SpriteBatch();

        //setting up tiledMap and renderer and camera all to view the map and play
        TiledMap map = new TmxMapLoader().load("Test Map.tmx");
        renderer = new OrthogonalTiledMapRenderer(map,1f);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.update();


    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        camera.update();
        renderer.setView(camera);
        renderer.render();

    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
