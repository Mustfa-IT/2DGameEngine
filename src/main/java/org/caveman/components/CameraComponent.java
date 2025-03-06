package org.caveman.components;

import dev.dominion.ecs.api.Entity;
import org.caveman.core.GameEngine;
import org.jbox2d.common.Vec2;

/**
 * An improved camera component that supports smooth target following, proper zooming, and
 * automatic viewport adjustment on window resize.
 */
public class CameraComponent {
    private float orthoWidth;   // Base orthographic width in world units
    private float orthoHeight;  // Base orthographic height in world units
    private final Vec2 position = new Vec2(); // Current camera center position
    private float zoom = 1.0f;        // Current zoom level

    private Entity target;            // Optional target entity for following
    private float followSmoothing = 0.3f; // Smoothing factor for following target (0 = immediate; higher = slower)
    private float smoothTime = 0.6f; // Time to reach target (seconds)

    // Optional zoom limits
    private float minZoom = 0.5f;
    private float maxZoom = 2.0f;

    /**
     * Creates a new CameraComponent with the specified orthographic dimensions.
     *
     * @param width  the orthographic width (in world units)
     * @param height the orthographic height (in world units)
     */
    public CameraComponent(float width, float height) {
        this.orthoWidth = width ;
        this.orthoHeight = height;
    }

    /**
     * Updates the camera’s position. If a target is set, the camera will smoothly interpolate
     * toward the target’s position. This method should be called once per frame.
     *
     * @param deltaTime the time elapsed since the last update (in seconds)
     */
    public void update(float deltaTime) {
        if (target != null) {
            Vec2 targetPos = getTargetPosition(target);
            if (followSmoothing <= 0) {
                // Snap immediately to the target's position
                position.set(targetPos);
            } else {
                // Effective smooth time scales with followSmoothing
                float effectiveSmoothTime = smoothTime * followSmoothing;
                // Calculate interpolation factor using exponential decay for frame rate independence
                float t = 1 - (float) Math.exp(-deltaTime / effectiveSmoothTime);
                // Update the camera position towards the target
                position.x += ((targetPos.x) - position.x) * t;
                position.y += ((targetPos.y) - position.y) * t;
            }
        }
    }

    /**
     * Retrieves the target entity's position.
     * Adapt this method if you need a more complex extraction of the target’s world position.
     *
     * @param target the target entity
     * @return the target's position as a Vec2
     */
    private Vec2 getTargetPosition(Entity target) {
        TransformComponent transform = target.get(TransformComponent.class);
        return new Vec2(transform.getX(), transform.getY());
    }

    /**
     * Sets the camera's zoom level. The value is clamped between the configured minimum and maximum zoom.
     *
     * @param zoom the new zoom level
     */
    public void setZoom(float zoom) {
        this.zoom = clamp(zoom, minZoom, maxZoom);
    }

    /**
     * Returns the current zoom level.
     *
     * @return the zoom level
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Returns the base orthographic width (in world units).
     *
     * @return the orthographic width
     */
    public float getOrthoWidth() {
        return orthoWidth;
    }

    /**
     * Returns the base orthographic height (in world units).
     *
     * @return the orthographic height
     */
    public float getOrthoHeight() {
        return orthoHeight;
    }

    /**
     * Returns the camera's current center position.
     *
     * @return the camera position as a Vec2
     */
    public Vec2 getPosition() {
        return position;
    }

    /**
     * Sets the camera's center position directly.
     *
     * @param pos the new position
     */
    public void setPosition(Vec2 pos) {
        position.set(pos);
    }

    /**
     * Calculates the left X coordinate of the camera viewport based on the current position, zoom, and orthographic width.
     *
     * @return the left X coordinate (in world units)
     */
    public int getX() {
        // Effective viewport width = orthoWidth / zoom
        return (int) (position.x - (orthoWidth / zoom) / 2);
    }

    /**
     * Calculates the bottom Y coordinate of the camera viewport based on the current position, zoom, and orthographic height.
     *
     * @return the bottom Y coordinate (in world units)
     */
    public int getY() {
        // Effective viewport height = orthoHeight / zoom
        return (int) (position.y - (orthoHeight / zoom) / 2);
    }

    /**
     * Sets the target entity for the camera to follow.
     *
     * @param target the target entity
     */
    public void setTarget(Entity target) {
        this.target = target;
    }

    /**
     * Returns the target entity that the camera is currently following.
     *
     * @return the target entity, or null if no target is set
     */
    public Entity getTarget() {
        return target;
    }

    /**
     * Configures the smoothing factor for following a target.
     * A value of 0 will cause the camera to snap immediately, while higher values result in slower, smoother movement.
     *
     * @param smoothing the smoothing factor (recommended between 0 and 1)
     */
    public void setFollowSmoothing(float smoothing) {
        this.followSmoothing = smoothing;
    }

    /**
     * Returns the current smoothing factor for target following.
     *
     * @return the smoothing factor
     */
    public float getFollowSmoothing() {
        return followSmoothing;
    }

    /**
     * Sets the minimum allowed zoom level.
     *
     * @param minZoom the minimum zoom level
     */
    public void setMinZoom(float minZoom) {
        this.minZoom = minZoom;
        setZoom(zoom); // re-clamp current zoom if necessary
    }

    /**
     * Returns the minimum allowed zoom level.
     *
     * @return the minimum zoom level
     */
    public float getMinZoom() {
        return minZoom;
    }

    /**
     * Sets the maximum allowed zoom level.
     *
     * @param maxZoom the maximum zoom level
     */
    public void setMaxZoom(float maxZoom) {
        this.maxZoom = maxZoom;
        setZoom(zoom); // re-clamp current zoom if necessary
    }

    /**
     * Returns the maximum allowed zoom level.
     *
     * @return the maximum zoom level
     */
    public float getMaxZoom() {
        return maxZoom;
    }

    /**
     * Utility method to clamp a value between a minimum and maximum.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Optional: Adjusts the orthographic dimensions based on the window size to maintain a constant world view.
     * Call this method when the window is resized.
     *
     * @param windowWidth  the width of the window in pixels
     * @param windowHeight the height of the window in pixels
     * @param desiredWorldWidth the desired width of the view in world units
     */
    public void resize(int windowWidth, int windowHeight, float desiredWorldWidth) {
        float aspectRatio = (float) windowWidth / windowHeight;
        this.orthoWidth = desiredWorldWidth / aspectRatio;
        this.orthoHeight = desiredWorldWidth / aspectRatio;
    }

    // Optionally, add setters for orthoWidth and orthoHeight if you want manual control.
    public void setOrthoWidth(float orthoWidth) {
        this.orthoWidth = orthoWidth;
    }

    public void setOrthoHeight(float orthoHeight) {
        this.orthoHeight = orthoHeight;
    }
}
