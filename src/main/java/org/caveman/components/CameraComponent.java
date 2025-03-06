package org.caveman.components;

import dev.dominion.ecs.api.Entity;

public class CameraComponent {
    private float x, y;
    private float zoom = 1.0f;
    private Entity target;
    private final int viewportWidth;
    private final int viewportHeight;

    public CameraComponent(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    // Getters and setters
    public void follow(Entity target) {
        this.target = target;
    }

    public void update(float targetX, float targetY) {
        // Smooth camera follow
        float centerX = targetX - viewportWidth / (2 * zoom);
        float centerY = targetY - viewportHeight / (2 * zoom);
        this.x += (centerX - this.x) * 0.1f;
        this.y += (centerY - this.y) * 0.1f;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZoom() {
        return zoom;
    }

    public Entity getTarget() {
        return target;
    }

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void setViewport(int width, int height) {
        this.x = width / 2;
        this.y = height / 2;
    }
}

