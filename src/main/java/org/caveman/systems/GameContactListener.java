package org.caveman.systems;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

public class GameContactListener implements ContactListener {
    private final CollisionSystem collisionSystem;

    public GameContactListener(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    @Override
    public void beginContact(Contact contact) {
        collisionSystem.handleCollision(contact, true);
    }

    @Override
    public void endContact(Contact contact) {
        collisionSystem.handleCollision(contact, false);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        // No pre-solve logic needed for this example.
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        // No post-solve logic needed for this example.
    }
}