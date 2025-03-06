package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.caveman.components.MovementController;
import org.caveman.components.PhysicsComponent;
import org.caveman.components.Tags;
import org.caveman.core.InputHandler;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.awt.event.KeyEvent;

public class MovementSystem implements Runnable {
    private final Dominion dominion;
    private final float pixelsPerMeter;

    public MovementSystem(Dominion dominion, float pixelsPerMeter) {
        this.dominion = dominion;
        this.pixelsPerMeter = pixelsPerMeter;
    }

    @Override
    public void run() {
        dominion.findEntitiesWith(Tags.PlayerTag.class, MovementController.class, PhysicsComponent.class)
                .stream()
                .forEach(entity -> {
                    MovementController controller = entity.comp2();
                    PhysicsComponent physics = entity.comp3();
                    handleMovement(entity.entity(), controller, physics);
                    handleJump(entity.entity(), controller, physics);
                });
    }

    private void handleMovement(Entity entity, MovementController controller, PhysicsComponent physics) {
        float moveDirection = 0f;
        if(InputHandler.isKeyPressed(KeyEvent.VK_A)) moveDirection -= 1;
        if(InputHandler.isKeyPressed(KeyEvent.VK_D)) moveDirection += 1;

        Body body = physics.getBody();
        if(body == null) return;
        Vec2 velocity = body.getLinearVelocity();

        float targetSpeed = moveDirection * controller.getMoveSpeed();
        float speedDifference = targetSpeed - velocity.x;
        float force = body.getMass() * speedDifference / (1/60f);

        body.applyForceToCenter(new Vec2(force, 0));
    }

    private void handleJump(Entity entity, MovementController controller, PhysicsComponent physics) {
        if(InputHandler.isKeyPressed(KeyEvent.VK_SPACE) && controller.isGrounded()) {
            System.out.println("Jumping");
            Body body = physics.getBody();
            body.applyLinearImpulse(
                    new Vec2(0, body.getMass() * -controller.getJumpForce()),
                    body.getWorldCenter()
            );
            controller.setGrounded(false);
        }

    }
}