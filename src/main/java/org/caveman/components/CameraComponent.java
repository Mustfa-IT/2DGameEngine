package org.caveman.components;

import dev.dominion.ecs.api.Entity;
import org.caveman.core.GameEngine;
import org.jbox2d.common.Vec2;

public class CameraComponent {
    private final float orthoWidth;  // World units (e.g., 40 meters)
    private final float orthoHeight; // World units (e.g., 30 meters)
    private final Vec2 position = new Vec2();
    private float zoom = 1.0f;
    private Entity target;

    public CameraComponent(float width, float height) {
        this.orthoWidth = width;
        this.orthoHeight = height;
    }

    public void update(float targetX, float targetY) {
        // Smooth camera follow
        float centerX = targetX - (orthoWidth * GameEngine.PIXELS_PER_METER) / (2 * zoom);
        float centerY = targetY - (orthoHeight * GameEngine.PIXELS_PER_METER) / (2 * zoom);
        this.position.x += (centerX - this.position.x) * 0.1f;
        this.position.y += (centerY - this.position.y) * 0.1f;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
    public float getOrthoWidth() { return orthoWidth; }
    public float getOrthoHeight() { return orthoHeight; }
    public Vec2 getPosition() { return position; }
    public void setPosition(Vec2 pos){
        position.set(pos);
    }
    public float getZoom() { return zoom; }

    public int getX() {
        return (int) (position.x - orthoWidth / 2);
    }

    public int getY() {
        return (int) (position.y - orthoHeight / 2);
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public Entity getTarget() {
        return target;
    }

}

