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
    private Dominion dominion;
    private World physicsWorld;
    private Scheduler scheduler;
    private JFrame window;
    private Canvas canvas;
    private Entity player;
    private Entity camera;
    private Vec2 gravity = new Vec2(0, 9.8f * 2);
    private float PlayerSpeed = 10f;
    private float PlayerJumpForce = 12f;

    public GameEngine() {
        dominion = Dominion.create();
        physicsWorld = new World(gravity);
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
        bodyDef.position.set(3, 3);

        // Define the player's shape.
        PolygonShape shape = new PolygonShape();
        // 0.5 meter half-width and half-height for a 1x1 meter box.
        shape.setAsBox(0.5f, 0.5f);

        // Define fixture properties.
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.0f; // Reduce friction to prevent sticking
        fixtureDef.density = 1.0f;
        fixtureDef.restitution = 0.0f; // No bounciness
        // Create the player entity with its components.
        Entity player = dominion.createEntity(
                new TransformComponent(3, 3),
                new SpriteComponent(Color.BLUE, 32, 32),
                new PhysicsComponent(bodyDef, fixtureDef),
                new Tags.PlayerTag(),
                new MovementController(12, 12),
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
        System.out.println("Setting up window");
        window = new JFrame("2D Game Engine");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        window.setLocationRelativeTo(null);
        canvas = new Canvas();
        canvas.setSize(window.getSize());
        window.add(canvas);
        window.setVisible(true);

        System.out.println("Window setup complete");
    }

    private void setupCamera() {
        System.out.println("Setting up camera");
        // Create camera immediately with current canvas size
        camera = dominion.createEntity(
                new CameraComponent(canvas.getWidth(), canvas.getHeight())
        );
        // Optional: Add resize listener for dynamic updates
        window.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                camera.get(CameraComponent.class)
                        .setViewport(canvas.getWidth(), canvas.getHeight());
            }
        });
        System.out.println("Camera setup complete");
    }

    private void setupInput() {
        System.out.println("Setting up input");
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
        InputHandler.setup(canvas);
        System.out.println("Input setup complete");

    }
    private void setupSystems() {
        System.out.println("Setting up systems");
        RenderingSystem renderingSystem = new RenderingSystem(dominion, canvas, PIXELS_PER_METER);
        PhysicsSystem physicsSystem = new PhysicsSystem(
                dominion, physicsWorld, 1/60f, PIXELS_PER_METER
        );
        CameraSystem cameraSystem = new CameraSystem(dominion);
        CollisionSystem collisionSystem = new CollisionSystem(dominion);
        MovementSystem movementSystem = new MovementSystem(dominion, PIXELS_PER_METER);
        // Register the custom ContactListener
        physicsWorld.setContactListener(new GameContactListener(collisionSystem));
        scheduler.parallelSchedule(
                physicsSystem,
                collisionSystem,
                cameraSystem,
                movementSystem,
                renderingSystem
        );
        System.out.println("Systems setup complete");
    }

    public Entity createEntity(Object... components) {
        return dominion.createEntity(components);
    }
    private void createGround() {
        float groundY = 10f; // meters
        float groundHeight = 1f; // meters

        // Define the ground's physics body
        BodyDef groundDef = new BodyDef();
        groundDef.type = BodyType.STATIC;
        // The position is typically the center of the body. Adjust as needed.
        groundDef.position.set(0, groundY + groundHeight / 2f);

        // Create the ground entity with the required components.
        Entity groundEntity = dominion.createEntity(
                new TransformComponent(0, groundY),
                new SpriteComponent(Color.GREEN, 100 * 32, (int) (groundHeight * PIXELS_PER_METER)),
                new PhysicsComponent(groundDef, null), // We'll assign the fixture manually.
                new Tags.GroundTag()
        );

        // Create the physics body for the ground.
        Body body = physicsWorld.createBody(groundDef);
        // Create the ground shape.
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(50f, groundHeight / 2f);

        // Create the fixture on the body.
        Fixture fixture = body.createFixture(groundShape, 0.0f);
        // Set the fixture's user data to the same entity.
        fixture.setUserData(groundEntity);

        // Save the created body in the entity's PhysicsComponent.
        groundEntity.get(PhysicsComponent.class).setBody(body);
    }
    private void createObstacles() {
        // Create a box obstacle
        BodyDef boxDef = new BodyDef();
        boxDef.type = BodyType.STATIC;
        boxDef.position.set(5, 8);

        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(1, 1);

        FixtureDef boxFixture = new FixtureDef();
        boxFixture.shape = boxShape;

        dominion.createEntity(
                new TransformComponent(5 * PIXELS_PER_METER, 8 * PIXELS_PER_METER),
                new SpriteComponent(Color.RED, 64, 64),
                new PhysicsComponent(boxDef, boxFixture),
                new CollisionComponent()
        );
    }

    public void setCameraTarget(Entity target){
        camera.get(CameraComponent.class).follow(target);
    }
    public Dominion getDominion() {
        return dominion;
    }


    public void setDominion(Dominion dominion) {
        this.dominion = dominion;
    }

    public World getPhysicsWorld() {
        return physicsWorld;
    }

    public void setPhysicsWorld(World physicsWorld) {
        this.physicsWorld = physicsWorld;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public JFrame getWindow() {
        return window;
    }

    public void setWindow(JFrame window) {
        this.window = window;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public Entity getPlayer() {
        return player;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public Entity getCamera() {
        return camera;
    }

    public void setCamera(Entity camera) {
        this.camera = camera;
    }
}
