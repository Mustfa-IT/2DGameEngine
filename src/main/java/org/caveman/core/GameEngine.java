package org.caveman.core;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Scheduler;
import org.caveman.components.*;
import org.caveman.systems.*;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameEngine {
    public static final float PIXELS_PER_METER = 32.0f;
    public static final Vec2 UP_VEC = new Vec2(0,1);
    private static final float PLAYER_SPEED = 10f;
    private static final float PLAYER_JUMP_FORCE = 12f;
    private static final Vec2 GRAVITY = new Vec2(0, 9.8f * 2);
    final float DESIRED_WORLD_WIDTH = 20.0f;

    private static final Dominion dominion = Dominion.create();
    private static final World physicsWorld = new World(GRAVITY);
    private static final Scheduler scheduler = dominion.createScheduler();
    private JFrame window;
    private Canvas canvas;
    private Entity player;
    private Entity camera;

    public GameEngine() {
        setupWindow();
        setupInput();
        setupCamera();
        setupSystems();
        createGround();
        createObstacles();
        createPlayer();
        scheduler.tickAtFixedRate(60);
    }

    public static double getDeltaTime() {
        return scheduler.deltaTime();
    }

    private void setupWindow() {
        window = new JFrame("2D Game Engine");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(800, 600));
        window.setLayout(new BorderLayout()); // Add layout manager
        window.add(canvas, BorderLayout.CENTER); // Add to center
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void setupCamera() {
        camera = dominion.createEntity(
                new CameraComponent(canvas.getWidth() , canvas.getHeight())
        );

        // Add a listener to update the camera size when the window is resized
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                CameraComponent camera = GameEngine.this.camera.get(CameraComponent.class);
                // Pass the canvas dimensions and the desired world width
                camera.resize(canvas.getWidth() , canvas.getHeight(), DESIRED_WORLD_WIDTH);
            }
        });

        camera.get(CameraComponent.class).setZoom(1.0f);
    }

    private void setupInput() {
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
        InputHandler.setup(canvas);
    }

    private void setupSystems() {
        RenderingSystem renderingSystem = new RenderingSystem(dominion, canvas, PIXELS_PER_METER);
        PhysicsSystem physicsSystem = new PhysicsSystem(
                dominion, physicsWorld, 1/60f, PIXELS_PER_METER
        );
        CameraSystem cameraSystem = new CameraSystem(dominion);
        CollisionSystem collisionSystem = new CollisionSystem(dominion);
        MovementSystem movementSystem = new MovementSystem(dominion, PIXELS_PER_METER);

        physicsWorld.setContactListener(new GameContactListener(collisionSystem));

        scheduler.parallelSchedule(
                movementSystem,    // Process input first
                physicsSystem,     // Update physics
                collisionSystem,   // Handle collisions
                cameraSystem,      // Update camera
                renderingSystem    // Render last
        );
    }
    private void createPlayer() {
        player = GameObjectFactory.createPlayer(
                dominion,
                physicsWorld,
                PLAYER_SPEED,
                PLAYER_JUMP_FORCE,
                -3f, // Body X in meters
                -3f, // Body Y in meters
                1, // Transform X in pixels
                1, // Transform Y in pixels
                Color.BLUE
        );
        setCameraTarget(player);
    }
    private void createGround() {
        GameObjectFactory.createGround(
                dominion,
                physicsWorld,
                0f,
                10f,
                100f,
                1f,
                Color.pink
        );
        GameObjectFactory.createGround(
                dominion,
                physicsWorld,
                0f,
                7f,
                10f,
                1f,
                Color.pink
        );
    }

    private void createObstacles() {
        GameObjectFactory.createObstacle(
                dominion,
                physicsWorld,
                10f,
                4f,
                1f,1f,
                Color.red
        );
    }

    public void setCameraTarget(Entity target) {
        camera.get(CameraComponent.class).setTarget(target);
    }

    // Getters (Removed unnecessary setters)
    public Dominion getDominion() { return dominion; }
    public World getPhysicsWorld() { return physicsWorld; }
    public Scheduler getScheduler() { return scheduler; }
    public JFrame getWindow() { return window; }
    public Canvas getCanvas() { return canvas; }
    public Entity getPlayer() { return player; }
    public Entity getCamera() { return camera; }
}