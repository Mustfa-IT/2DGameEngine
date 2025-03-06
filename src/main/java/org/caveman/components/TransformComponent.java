package org.caveman.components;

public class TransformComponent {
    private float x, y, rotation;

    public TransformComponent(float x, float y) {
        this.x = x;
        this.y = y;
        this.rotation = 0;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
