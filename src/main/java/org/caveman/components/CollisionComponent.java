package org.caveman.components;

import dev.dominion.ecs.api.Entity;

public class CollisionComponent {
    private boolean colliding;
    private Entity otherEntity;

    public void reset() {
        colliding = false;
        otherEntity = null;
    }

    public boolean isColliding() {
        return colliding;
    }

    public void setColliding(boolean colliding) {
        this.colliding = colliding;
    }

    public Entity getOtherEntity() {
        return otherEntity;
    }

    public void setOtherEntity(Entity otherEntity) {
        this.otherEntity = otherEntity;
    }
}