package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import org.caveman.components.CameraComponent;
import org.caveman.core.GameEngine;
import org.caveman.core.InputHandler;

import java.awt.event.KeyEvent;

public class CameraSystem implements Runnable {
    private final Dominion dominion;

    public CameraSystem(Dominion dominion) {
        this.dominion = dominion;
    }

    @Override
    public void run() {
        // Retrieve the elapsed time since the last update
        float deltaTime = (float) GameEngine.getDeltaTime();

        dominion.findEntitiesWith(CameraComponent.class)
                .forEach(entity -> {
                    CameraComponent camera = entity.comp();
                    if(InputHandler.isKeyPressed(KeyEvent.VK_UP)){
                        camera.setZoom(camera.getZoom() + 0.01f);
                    } else if(InputHandler.isKeyPressed(KeyEvent.VK_DOWN)){
                        camera.setZoom(camera.getZoom() - 0.01f);
                    }
                    // Update the camera (which handles smooth target following internally)
                    camera.update(deltaTime);
                });
    }
}
