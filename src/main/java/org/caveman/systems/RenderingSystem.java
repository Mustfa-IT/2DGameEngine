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
        canvas.createBufferStrategy(3);
        bufferStrategy = canvas.getBufferStrategy();
    }

    @Override
    public void run() {
        do {
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
        } while (bufferStrategy.contentsLost());
    }
    private void renderFrame(Graphics2D g) {
        // Get camera information
        CameraComponent camera = dominion.findEntitiesWith(CameraComponent.class)
                .stream()
                .findFirst()
                .map(Results.With1::comp)
                .orElseThrow();

        // Apply camera transform
        AffineTransform originalTransform = g.getTransform();
        g.scale(camera.getZoom(), camera.getZoom());
        g.translate(-camera.getX(), -camera.getY());

        // Clear background
        g.setColor(Color.DARK_GRAY);
        g.fillRect((int) camera.getX(), (int) camera.getY(),
                (int) (canvas.getWidth() / camera.getZoom()),
                (int) (canvas.getHeight() / camera.getZoom()));

        // Render all entities with transform and sprite
        dominion.findEntitiesWith(TransformComponent.class, SpriteComponent.class)
                .stream()
                .forEach(entity -> {
                    TransformComponent transform = entity.comp1();
                    SpriteComponent sprite = entity.comp2();
                    renderEntity(g, transform, sprite);
                });

        g.setTransform(originalTransform);  // Reset to screen coordinates
    }
    private void renderEntity(Graphics2D g, TransformComponent transform, SpriteComponent sprite) {
        // Convert physics meters to pixels and center sprite
        float x = transform.getX() * pixelsPerMeter;
        float y = transform.getY() * pixelsPerMeter;
        int width = sprite.getWidth();
        int height = sprite.getHeight();

        g.setColor(sprite.getColor());
        g.fillRect(
                (int)(x - width / 2f),  // Center X
                (int)(y - height / 2f), // Center Y
                width,
                height
        );
    }
}