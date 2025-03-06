package org.caveman.core;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import dev.dominion.ecs.api.Scheduler;
import org.caveman.components.*;
import org.caveman.systems.*;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class GameEngine {
    public static final float PIXELS_PER_METER = 32.0f;
    private static final float PLAYER_SPEED = 10f;
    private static final float PLAYER_JUMP_FORCE = 12f;
    private static final Vec2 GRAVITY = new Vec2(0, 9.8f * 2);

    private final Dominion dominion;
    private final World physicsWorld;
    private final Scheduler scheduler;
    private JFrame window;
    private Canvas canvas;
    private Entity player;
    private Entity camera;

    public GameEngine() {
        dominion = Dominion.create();
        physicsWorld = new World(GRAVITY);
        scheduler = dominion.createScheduler();
        setupWindow();
        setupInput();
        setupCamera();
        setupSystems();
        createGround();
        createObstacles();
        createPlayer();
        scheduler.tickAtFixedRate(60);
    }

    private void createPlayer() {
        // Define the physics body for the player.
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.fixedRotation = true;
        // Set initial position in meters.
        bodyDef.position.set(3, -10);

        // Define the player's shape.
        PolygonShape shape = new PolygonShape();
        // 0.5 meter half-width and half-height for a 1x1 meter box.
        shape.setAsBox(1f, 1f);

        // Define fixture properties.
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.0f; // Reduce friction to prevent sticking
        fixtureDef.density = 1.0f;
        fixtureDef.restitution = 0.0f; // No bounciness
        // Create the player entity with its components.
        Entity player = dominion.createEntity(
                new TransformComponent(3, 3),
                new SpriteComponent(Color.BLUE, 1, 1),
                new PhysicsComponent(bodyDef, fixtureDef),
                new Tags.PlayerTag(),
                new MovementController(PLAYER_SPEED, PLAYER_JUMP_FORCE),
                new CollisionComponent()
        );

        // Create the physics body immediately.
        Body body = getPhysicsWorld().createBody(bodyDef);
        // Create the fixture and assign the player entity as its user data.
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(player);

        // Save the created physics body to the player's PhysicsComponent.
        player.get(PhysicsComponent.class).setBody(body);

        // Store the player for later reference and update the camera target.
        setCameraTarget(player);
    }


    private void setupWindow() {
        window = new JFrame("2D Game Engine");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(800, 600));
        window.add(canvas);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void setupCamera() {
        camera = dominion.createEntity(
                new CameraComponent(canvas.getWidth(), canvas.getHeight())
        );
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                camera.get(CameraComponent.class)
                        .setViewport(canvas.getWidth(), canvas.getHeight());
            }
        });
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

    private void createGround() {
        float groundY = 1; // meters
        float groundHeight = 1f; // meters

        // Define the ground's physics body
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyType.STATIC;
        // The position is typically the center of the body. Adjust as needed.
        groundDef.position.set(0, groundHeight);

        // Create the ground entity with the required components.
        Entity groundEntity = dominion.createEntity(
                new TransformComponent(0, groundY),
                new SpriteComponent(Color.GREEN, 100,3),
                new PhysicsComponent(groundDef, null), // We'll assign the fixture manually.
                new Tags.GroundTag()
        );

        // Create the physics body for the ground.
        Body body = physicsWorld.createBody(groundDef);
        // Create the ground shape.
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(100, groundHeight);

        // Create the fixture on the body.
        Fixture fixture = body.createFixture(groundShape, 0.0f);
        // Set the fixture's user data to the same entity.
        fixture.setUserData(groundEntity);

        // Save the created body in the entity's PhysicsComponent.
        groundEntity.get(PhysicsComponent.class).setBody(body);
    }

    private void createObstacles() {
        BodyDef boxDef = new BodyDef();
        int boxWidth = 2;
        int boxHeight = 2;
        boxDef.type = BodyType.STATIC;
        boxDef.position.set(5, -1.5f);

        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(boxWidth/4f, boxHeight/4f);

        FixtureDef boxFixture = new FixtureDef();
        boxFixture.shape = boxShape;

        var obs = dominion.createEntity(
                new TransformComponent(5, 10),
                new SpriteComponent(Color.RED, boxWidth,boxHeight),
                new PhysicsComponent(boxDef, boxFixture),
                new CollisionComponent(),
                new Tags.GroundTag()
        );

        // Create the physics body for the ground.
        Body body = physicsWorld.createBody(boxDef);


        // Create the fixture on the body.
        Fixture fixture = body.createFixture(boxShape, 0.0f);
        // Set the fixture's user data to the same entity.
        fixture.setUserData(obs);

        // Save the created body in the entity's PhysicsComponent.
        obs.get(PhysicsComponent.class).setBody(body);
    }

    public void setCameraTarget(Entity target) {
        camera.get(CameraComponent.class).follow(target);
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