package org.caveman.components;

import java.awt.*;

public class SpriteComponent {
    private Color color;
    private int width, height;

    public SpriteComponent(Color color, int width, int height) {
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
