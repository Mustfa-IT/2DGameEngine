package org.caveman.core;

import org.caveman.scenes.GameScene;
import org.caveman.scenes.MenuScene;

public class SceneFactory {
    public static Scene gameScene() {
        return new GameScene();
    }

    public static Scene menuScene() {
        return new MenuScene();
    }
}
