package org.caveman.core;

import dev.dominion.ecs.api.Dominion;
import org.jbox2d.dynamics.World;

import java.awt.*;

public abstract class SceneBase implements Scene {

    protected final GameEngine ENGINE;
    protected final Canvas CANVAS;
    protected final Dominion DOMINION;
    protected final World WORLD;

    public SceneBase() {
        this.ENGINE =GameEngine.getInstance();
        this.CANVAS = ENGINE.getCanvas();
        this.DOMINION = ENGINE.getDominion();
        this.WORLD = ENGINE.getPhysicsWorld();
    }

    @Override
    public void init() {

    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics2D g) {

    }
}
