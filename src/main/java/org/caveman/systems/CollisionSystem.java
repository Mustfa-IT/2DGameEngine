package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.caveman.components.CollisionComponent;
import org.caveman.components.MovementController;
import org.caveman.components.Tags;
import org.jbox2d.dynamics.Fixture;

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

    public void handleCollision(Fixture fixtureA, Fixture fixtureB, boolean entering) {
        Object dataA = fixtureA.getUserData();
        Object dataB = fixtureB.getUserData();


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

        // Check for ground collisions on both fixtures.
        checkGroundCollision(entityA, entityB, entering);
        checkGroundCollision(entityB, entityA, entering);
    }

    private void checkGroundCollision(Entity entity, Entity other, boolean entering) {
        if (entity == null){
            System.out.println("Entity is null");
            return;
        }
        if (other == null){
            System.out.println("Other is null");
            return;
        }
        // If the entity is the player and the other entity is ground, update the grounded state.
        if (entity.has(Tags.PlayerTag.class) && other.has(Tags.GroundTag.class)) {
            MovementController controller = entity.get(MovementController.class);
            if (controller != null) {
                controller.setGrounded(entering);
            }
        }
    }
}
