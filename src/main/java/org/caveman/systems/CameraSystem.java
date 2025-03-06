package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import org.caveman.components.CameraComponent;
import org.caveman.components.TransformComponent;
import org.caveman.core.GameEngine;
import org.caveman.core.InputHandler;

import java.awt.event.KeyEvent;
public class CameraSystem implements Runnable {
    private Dominion dominion;

    public CameraSystem(Dominion dominion ){
        this.dominion = dominion;
    }

    @Override
    public void run() {
        dominion.findEntitiesWith(CameraComponent.class)
                .forEach(entity -> {
                    CameraComponent camera = entity.comp();
                    if (camera.getTarget() != null) {
                        TransformComponent target = camera.getTarget().get(TransformComponent.class);

                        // Increased speed factor (0.1f -> 0.3f for 30% per frame)
                        float lerpFactor = 0.9f; // Adjust this value (0.3 = fast, 0.1 = slow)

                        // Optional: For frame-rate independence
                         double deltaTime = GameEngine.getDeltaTime();
                         lerpFactor = 1 - (float) Math.pow(0.1f, deltaTime);

                        camera.getPosition().x += ((target.getX() * GameEngine.PIXELS_PER_METER) - camera.getPosition().x) * lerpFactor;
                        camera.getPosition().y += ((target.getY() * GameEngine.PIXELS_PER_METER) - camera.getPosition().y) * lerpFactor;
                    }
                });
    }
}

