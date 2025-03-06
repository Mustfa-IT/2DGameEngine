package org.caveman.scenes;


import org.caveman.core.Scene;

import java.awt.*;

public class MenuScene implements Scene {
    @Override
    public void init() {
        System.out.println("Menu Scene Initialized");
    }

    @Override
    public void update() {
        // Menu logic (e.g., handling button clicks)
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 800, 600);
        g.setColor(Color.WHITE);
        g.drawString("Main Menu", 350, 300);
    }
}
