package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.caveman.components.CollisionComponent;
import org.caveman.components.MovementController;
import org.caveman.components.Tags;
import org.caveman.core.GameEngine;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.contacts.Contact;

public class CollisionSystem implements Runnable {
    private final Dominion dominion;

    public CollisionSystem(Dominion dominion) {
        this.dominion = dominion;
    }

    @Override
    public void run() {
        dominion.findEntitiesWith(CollisionComponent.class)
                .forEach(entity -> {
                    CollisionComponent collision = entity.comp();
                    collision.reset();
                });
    }

    public void handleCollision(Contact contact, boolean entering) {
        Object dataA = contact.getFixtureA().getUserData();
        Object dataB = contact.getFixtureB().getUserData();


        if (dataA == null) {
            System.out.println("Data A is null");
            return;
        }
        if (dataB == null) {
            System.out.println("Data B is null");
            return;
        }
        Entity entityA = (Entity) dataA;
        Entity entityB = (Entity) dataB;
        entityA.get(CollisionComponent.class).setColliding(true);
        entityB.get(CollisionComponent.class).setColliding(true);


        entityA.get(CollisionComponent.class).setOtherEntity(entityB);
        entityB.get(CollisionComponent.class).setOtherEntity(entityA);
        // Check for ground collisions on both fixtures.
        checkGroundCollision(contact,entityA, entityB, entering);
        checkGroundCollision(contact,entityB, entityA, entering);
    }

    private void checkGroundCollision(Contact contact ,Entity entity, Entity other, boolean entering) {
        if (!entering) return;
        if (entity.has(Tags.PlayerTag.class) && other.has(Tags.GroundTag.class)) {
            MovementController controller = entity.get(MovementController.class);
            if (controller != null && !controller.isJustJumped()) {
                WorldManifold worldManifold = new WorldManifold();
                contact.getWorldManifold(worldManifold);
                Vec2 normal = worldManifold.normal;
                float dot = Vec2.dot(normal, GameEngine.UP_VEC);
                if (dot < -0.9f) {
                    controller.setGrounded(true);
                }
            }
        }
    }
}
