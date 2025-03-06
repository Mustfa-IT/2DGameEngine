package org.caveman.scenes;

import org.caveman.core.Scene;
import org.caveman.core.SceneFactory;

import java.awt.Graphics2D;

public class SceneManager {
    private static Scene currentScene;

    public static void setScene(Scene scene) {
        currentScene = scene;
        currentScene.init();
    }

    public static void update() {
        if (currentScene != null) {
            currentScene.update();
        }
    }

    public static void render(Graphics2D g) {
        if (currentScene != null) {
            currentScene.render(g);
        }
    }
}