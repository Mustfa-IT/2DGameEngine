package org.caveman.core;

import java.awt.*;
/**
 * Represents a scene in the application.
 * Provides methods to initialize, update, and render the scene.
 * */
public interface Scene {
    void init();
    void update();
    void render(Graphics2D g);
}
