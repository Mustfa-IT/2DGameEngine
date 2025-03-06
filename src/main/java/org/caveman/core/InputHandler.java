package org.caveman.core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import java.awt.event.*;
import java.util.Collections;


public class InputHandler {
    private static final Set<Integer> pressedKeys = Collections.synchronizedSet(new HashSet<>());

    public static void setup(Component frame) {
        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!pressedKeys.contains(e.getKeyCode())) {
                    System.out.println("Key Pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
                    pressedKeys.add(e.getKeyCode());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // Optional: Handle typed events if needed
            }
        };

        frame.addKeyListener(keyListener);
    }

    public static boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public static void clearPressedKeys() {
        pressedKeys.clear();
    }
}
