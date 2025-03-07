package org.caveman.systems;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Results;
import org.caveman.components.CameraComponent;
import org.caveman.components.LightEmitterComponent;
import org.caveman.components.SpriteComponent;
import org.caveman.components.TransformComponent;
import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.World;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

public class RenderingSystem implements Runnable {
    private final Dominion dominion;
    private final Canvas canvas;
    private final World world;
    private final BufferStrategy bufferStrategy;
    private final float pixelsPerMeter;

    public RenderingSystem(Dominion dominion, Canvas canvas,World world, float pixelsPerMeter) {
        this.dominion = dominion;
        this.canvas = canvas;
        this.pixelsPerMeter = pixelsPerMeter;
        this.world = world;
        canvas.setIgnoreRepaint(true);
        canvas.createBufferStrategy(3);
        bufferStrategy = canvas.getBufferStrategy();
    }

    @Override
    public void run() {
        do {
            Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
            renderFrame(g);
        } while (bufferStrategy.contentsRestored());

        bufferStrategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    public void renderFrame(Graphics2D g) {
        // Clear the background.
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Get the camera component (assumes there is at least one camera entity).
        CameraComponent camera = dominion.findEntitiesWith(CameraComponent.class)
                .stream()
                .findFirst()
                .map(entity -> entity.comp())
                .orElseThrow(() -> new RuntimeException("No camera entity found"));

        // Save the original transform.
        AffineTransform originalTransform = g.getTransform();

        // Compute canvas center.
        float canvasCenterX = canvas.getWidth() / 2f;
        float canvasCenterY = canvas.getHeight() / 2f;

        // Build the new transform: center, scale (zoom), and translate.
        AffineTransform transform = new AffineTransform();
        transform.translate(canvasCenterX, canvasCenterY);
        transform.scale(camera.getZoom(), camera.getZoom());
        transform.translate(-camera.getPosition().x * pixelsPerMeter, -camera.getPosition().y * pixelsPerMeter);
        g.setTransform(transform);

        // Only render entities that are in view.
        dominion.findEntitiesWith(TransformComponent.class, SpriteComponent.class)
                .forEach(entity -> {
                    TransformComponent transformComponent = entity.comp1();
                    SpriteComponent sprite = entity.comp2();
                    // Check if the entity's bounding box is in view.
                    if (camera.isInView(transformComponent.getX(), transformComponent.getY(),
                            sprite.getWidth(), sprite.getHeight(), pixelsPerMeter)) {
                        renderEntity(g, transformComponent, sprite);
                    }
                });

        // Render lighting and other effects.
        renderLighting(g,camera);

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

    private void renderLighting(Graphics2D g,CameraComponent camera) {
        // Compute the current time once per frame.
        long currentTimeMillis = System.currentTimeMillis();

        // Save the current composite and set additive blending for light effects.
        Composite originalComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        dominion.findEntitiesWith(TransformComponent.class, LightEmitterComponent.class)
                .forEach(entity -> {
                    TransformComponent transform = entity.comp1();
                    LightEmitterComponent light = entity.comp2();
                    if (!camera.isInView(transform.getX(), transform.getY(), light.getRange(), light.getRange(), pixelsPerMeter)) return;
                    // Get the light's world position (in meters) and convert to pixels.
                    float lightX_px = transform.getX() * pixelsPerMeter;
                    float lightY_px = transform.getY() * pixelsPerMeter;
                    Vec2 lightPos = new Vec2(transform.getX(), transform.getY()); // in meters

                    // Get the current intensity (which can be animated).
                    float intensity = light.getCurrentIntensity(currentTimeMillis);

                    int rays = light.getRays();
                    float angleStep = (float) (2 * Math.PI / rays);

                    // Build the light polygon from ray-cast results.
                    List<Point> polygonPoints = buildLightPolygon(light, lightPos, rays, angleStep);

                    // Create a polygon shape from the computed points.
                    Path2D.Float lightPolygon = new Path2D.Float();
                    if (!polygonPoints.isEmpty()) {
                        Point first = polygonPoints.get(0);
                        lightPolygon.moveTo(first.x, first.y);
                        for (int i = 1; i < polygonPoints.size(); i++) {
                            Point p = polygonPoints.get(i);
                            lightPolygon.lineTo(p.x, p.y);
                        }
                        lightPolygon.closePath();
                    }

                    // Create a radial gradient paint for a smooth light effect.
                    int centerX = (int) lightX_px;
                    int centerY = (int) lightY_px;
                    float radius = light.getRange() * pixelsPerMeter;
                    Color lightColor = light.getColor();
                    // The center color uses the animated intensity; the edge is transparent.
                    Color centerColor = new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), (int) (255 * intensity));
                    Color edgeColor = new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 0);
                    float[] dist = {0.0f, 1.0f};
                    Color[] colors = {centerColor, edgeColor};
                    RadialGradientPaint gradient = new RadialGradientPaint(new Point(centerX, centerY), radius, dist, colors);

                    g.setPaint(gradient);
                    g.fill(lightPolygon);
                });

        // Restore the original composite mode.
        g.setComposite(originalComposite);
    }

    /**
     * Builds the polygon representing the illuminated area for a given light.
     * For each ray, we use the physics world ray-cast to detect obstacles.
     */
    private List<Point> buildLightPolygon(LightEmitterComponent light, Vec2 lightPos, int rays, float angleStep) {
        List<Point> polygonPoints = new ArrayList<>();

        for (int i = 0; i < rays; i++) {
            float angle = i * angleStep;
            Vec2 dir = new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
            // Compute the ray's end point (if unobstructed) in meters.
            Vec2 endPoint = lightPos.add(dir.mul(light.getRange()));

            // Use jbox2d’s ray-cast to check for obstructions.
            RayCastCallbackImpl callback = new RayCastCallbackImpl();
            world.raycast(callback, lightPos, endPoint);
            Vec2 hitPoint = callback.hit ? callback.hitPoint : endPoint;

            // Convert the hit point to pixels.
            int pointX = (int) (hitPoint.x * pixelsPerMeter);
            int pointY = (int) (hitPoint.y * pixelsPerMeter);
            polygonPoints.add(new Point(pointX, pointY));
        }
        return polygonPoints;
    }

    // A simple implementation of the RayCastCallback that records the closest hit.
    private static class RayCastCallbackImpl implements RayCastCallback {
        public boolean hit = false;
        public Vec2 hitPoint = new Vec2();
        public float closestFraction = 1.0f;

        @Override
        public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
            if (fraction < closestFraction) {
                closestFraction = fraction;
                hitPoint.set(point);
                hit = true;
            }
            return fraction;
        }
    }
}
