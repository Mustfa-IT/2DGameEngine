package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import org.caveman.components.CameraComponent;
import org.caveman.components.SpriteComponent;
import org.caveman.components.TransformComponent;
import org.caveman.scenes.SceneManager;

import javax.swing.*;
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
                    SceneManager.render(g);  // Use SceneManager to render
                } finally {
                    g.dispose();
                }
            } while (bufferStrategy.contentsRestored());

            bufferStrategy.show();
            Toolkit.getDefaultToolkit().sync();
        } while (bufferStrategy.contentsLost());
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