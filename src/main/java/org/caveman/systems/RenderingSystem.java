package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import org.caveman.components.CameraComponent;
import org.caveman.components.SpriteComponent;
import org.caveman.components.TransformComponent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

public class RenderingSystem implements Runnable {
    private final Dominion dominion;
    private final Canvas canvas;
    private final BufferStrategy bufferStrategy;
    private final float pixelsPerMeter;

    public RenderingSystem(Dominion dominion, Canvas canvas, float pixelsPerMeter) {
        this.dominion = dominion;
        this.canvas = canvas;
        this.pixelsPerMeter = pixelsPerMeter;
        canvas.setIgnoreRepaint(true);
        canvas.createBufferStrategy(3);
        bufferStrategy = canvas.getBufferStrategy();
    }

    @Override
    public void run() {
        do {
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            try {
                renderFrame(g);
            } finally {
                g.dispose();
            }
        } while (bufferStrategy.contentsRestored());

        bufferStrategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    private void renderFrame(Graphics2D g) {
        // Clear the background.
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Get the camera
        CameraComponent camera = dominion.findEntitiesWith(CameraComponent.class)
                .stream()
                .findFirst()
                .map(Results.With1::comp)
                .orElseThrow(() -> new RuntimeException("No camera entity found"));

        // Save the original transform.
        AffineTransform originalTransform = g.getTransform();

        // Compute canvas center
        float canvasCenterX = canvas.getWidth() / 2f;
        float canvasCenterY = canvas.getHeight() / 2f;

        // Build the new transform:
        // 1. Move the origin to the center of the canvas.
        // 2. Scale by the zoom factor.
        // 3. Translate the world so that the camera's center is at the origin.
        AffineTransform transform = new AffineTransform();
        transform.translate(canvasCenterX, canvasCenterY);
        transform.scale(camera.getZoom(), camera.getZoom());
        transform.translate(-camera.getPosition().x * pixelsPerMeter, -camera.getPosition().y * pixelsPerMeter);
        g.setTransform(transform);

        // Render all entities with TransformComponent and SpriteComponent
        dominion.findEntitiesWith(TransformComponent.class, SpriteComponent.class)
                .forEach(entity -> {
                    TransformComponent transformComponent = entity.comp1();
                    SpriteComponent sprite = entity.comp2();
                    renderEntity(g, transformComponent, sprite);
                });

        // Restore the original transform.
        g.setTransform(originalTransform);
    }


    private void renderEntity(Graphics2D g, TransformComponent transform, SpriteComponent sprite) {
        float x = transform.getX() * pixelsPerMeter;
        float y = transform.getY() * pixelsPerMeter;
        int width = sprite.getWidth();
        int height = sprite.getHeight();

        // Center the sprite on its transform's position
        int drawX = (int) (x - width / 2.0f);
        int drawY = (int) (y - height / 2.0f);

        g.setColor(sprite.getColor());
        g.fillRect(drawX, drawY, width, height);
    }
}
