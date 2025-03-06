package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.caveman.components.PhysicsComponent;
import org.caveman.components.SpriteComponent;
import org.caveman.components.TransformComponent;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;

/**
 * The PhysicsSystem class is responsible for managing the physics simulation
 * of entities within the game. It integrates with the Dominion ECS framework
 * and the JBox2D physics engine.
 */
public class PhysicsSystem implements Runnable {
    private final Dominion dominion;
    private final World world;
    private final float timeStep;
    private final float pixelsPerMeter;

    /**
     * Constructs a new PhysicsSystem.
     *
     * @param dominion      the Dominion ECS instance
     * @param world         the JBox2D world instance
     * @param timeStep      the time step for the physics simulation
     * @param pixelsPerMeter the conversion factor from pixels to meters
     */
    public PhysicsSystem(Dominion dominion, World world, float timeStep, float pixelsPerMeter) {
        this.dominion = dominion;
        this.world = world;
        this.timeStep = timeStep;
        this.pixelsPerMeter = pixelsPerMeter;
    }

    /**
     * Runs the physics simulation. This method is called periodically to
     * update the physics state of the entities.
     */
    @Override
    public void run() {
        // Create bodies for new entities
        dominion.findEntitiesWith(PhysicsComponent.class, TransformComponent.class)
                .stream()
                .filter(entity -> entity.comp1().getBody() == null)
                .forEach(entity -> initializePhysicsBody(entity.entity()));

        // Step the physics simulation
        world.step(timeStep, 6, 2);

        // Update transforms from physics bodies
        dominion.findEntitiesWith(PhysicsComponent.class, TransformComponent.class)
                .stream()
                .forEach(entity -> updateTransformFromBody(entity.entity()));

    }

    /**
     * Initializes the physics body for a given entity.
     *
     * @param entity the entity to initialize the physics body for
     */
    private void initializePhysicsBody(Entity entity) {
        PhysicsComponent physics = entity.get(PhysicsComponent.class);
        TransformComponent transform = entity.get(TransformComponent.class);
        SpriteComponent sprite = entity.get(SpriteComponent.class);
        BodyDef bodyDef = physics.getBodyDef();

        // Center the physics body at the transform position
        bodyDef.position.set(
                transform.getX() + (sprite.getWidth() / 2f) / pixelsPerMeter,
                transform.getY() + (sprite.getHeight() / 2f) / pixelsPerMeter
        );

        Body body = world.createBody(bodyDef);

        // Use the fixture definition if provided. This way we use the intended friction,
        // restitution, and other properties.
        if (physics.getFixtureDef() != null) {
            // Ensure the shape in the fixture def matches our sprite dimensions.
            if (physics.getFixtureDef().shape instanceof PolygonShape) {
                PolygonShape poly = (PolygonShape) physics.getFixtureDef().shape;
                poly.setAsBox(
                        (sprite.getWidth() / 2f) / pixelsPerMeter,
                        (sprite.getHeight() / 2f) / pixelsPerMeter
                );
            }
            body.createFixture(physics.getFixtureDef());
        } else {
            // Fallback: create a fixture with default density.
            System.out.println("No fixture def provided, creating default fixture");
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(
                    (sprite.getWidth() / 2f) / pixelsPerMeter,
                    (sprite.getHeight() / 2f) / pixelsPerMeter
            );
            body.createFixture(shape, 1.0f);
        }
        physics.setBody(body);
    }

    /**
     * Updates the transform component of an entity from its physics body.
     *
     * @param entity the entity to update the transform for
     */
    private void updateTransformFromBody(Entity entity) {
        PhysicsComponent physics = entity.get(PhysicsComponent.class);
        TransformComponent transform = entity.get(TransformComponent.class);
        Body body = physics.getBody();
        if(body == null) return;
        Vec2 position = body.getPosition();
        // Directly store physics coordinates (meters)
        transform.setX(position.x);
        transform.setY(position.y);
        transform.setRotation((float) Math.toDegrees(body.getAngle()));
    }
}