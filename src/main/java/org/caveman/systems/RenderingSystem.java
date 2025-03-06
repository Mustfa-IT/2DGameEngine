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
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

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

        // Render all entities with transform and sprite
        dominion.findEntitiesWith(TransformComponent.class, SpriteComponent.class)
                .stream()
                .forEach(entity -> {
                    TransformComponent transform = entity.comp1();
                    SpriteComponent sprite = entity.comp2();
                    renderEntity(g, transform, sprite);
                });

        g.setTransform(originalTransform);
    }
    private void renderEntity(Graphics2D g, TransformComponent transform, SpriteComponent sprite) {
        float x = transform.getX() * pixelsPerMeter;
        float y = transform.getY() * pixelsPerMeter;
        int width = sprite.getWidth();
        int height = sprite.getHeight();

        // Calculate top-left corner to center the sprite
        int drawX = (int) (x - width / 2);
        int drawY = (int) (y - height / 2);

        g.setColor(sprite.getColor());
        g.fillRect(drawX, drawY, width, height);
    }
}