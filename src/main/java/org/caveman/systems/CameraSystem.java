package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import org.caveman.components.CameraComponent;
import org.caveman.components.TransformComponent;
import org.caveman.core.GameEngine;

public class CameraSystem implements Runnable {
    private final Dominion dominion;

    public CameraSystem(Dominion dominion) {
        this.dominion = dominion;
    }

    @Override
    public void run() {
        dominion.findEntitiesWith(CameraComponent.class)
                .stream()
                .forEach(entity -> {
                    CameraComponent camera = entity.comp();
                    if (camera.getTarget() != null && camera.getTarget().isEnabled()) {
                        TransformComponent targetTransform = camera.getTarget().get(TransformComponent.class);
                        if (targetTransform != null) {
                            // Convert target position to pixels for camera
                            float targetXPixels = targetTransform.getX() * GameEngine.PIXELS_PER_METER;
                            float targetYPixels = targetTransform.getY() * GameEngine.PIXELS_PER_METER;
                            camera.update(targetXPixels, targetYPixels);
                        }
                    }
                });
    }
}

