package org.caveman.scenes;

import dev.dominion.ecs.api.Results;
import org.caveman.components.CameraComponent;
import org.caveman.components.SpriteComponent;
import org.caveman.components.TransformComponent;
import org.caveman.core.SceneBase;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GameScene extends SceneBase {
    @Override
    public void init() {

    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics2D g) {
        // Clear the entire screen (canvas coordinates)
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, CANVAS.getWidth(), CANVAS.getHeight());

        // Enable anti-aliasing and quality rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Get camera information
        CameraComponent camera = DOMINION.findEntitiesWith(CameraComponent.class)
                .stream()
                .findFirst()
                .map(Results.With1::comp)
                .orElseThrow();

        // Apply camera transform
        AffineTransform originalTransform = g.getTransform();
        g.scale(camera.getZoom(), camera.getZoom());
        g.translate(-camera.getX(), -camera.getY());

        // Render all entities with transform and sprite
        DOMINION.findEntitiesWith(TransformComponent.class, SpriteComponent.class)
                .forEach(result -> {
                    TransformComponent transform = result.comp1();
                    SpriteComponent sprite = result.comp2();
                    renderEntity(g, transform, sprite);
                });

        // Reset transform to screen coordinates
        g.setTransform(originalTransform);
    }

    private void renderEntity(Graphics2D g, TransformComponent transform, SpriteComponent sprite) {
        // Apply entity transform
        AffineTransform originalTransform = g.getTransform();
        g.translate(transform.getX(), transform.getY());
        g.rotate(transform.getRotation());

        // Render sprite
        g.setColor(sprite.getColor());
        g.drawRect(0, 0, sprite.getWidth(), sprite.getHeight());

        // Reset transform
        g.setTransform(originalTransform);
    }
}
