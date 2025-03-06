package org.caveman.core;

import dev.dominion.ecs.api.Dominion;
import dev.dominion.ecs.api.Entity;
import org.caveman.components.*;
import org.caveman.components.MovementController;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.awt.*;

import static org.caveman.core.GameEngine.PIXELS_PER_METER;

public class GameObjectFactory {

    public static Entity createPlayer(Dominion dominion, World physicsWorld, float speed, float jumpForce,
                                      float posX, float posY, float width, float height,
                                      Color color) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(posX, posY);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.0f;
        fixtureDef.density = 1.0f;
        fixtureDef.restitution = 0.0f;

        Entity player = dominion.createEntity(
                new TransformComponent(posX, posY),
                new SpriteComponent(color, (int) ((width) * PIXELS_PER_METER), (int) ((height) * PIXELS_PER_METER)),
                new PhysicsComponent(bodyDef, fixtureDef),
                new Tags.PlayerTag(),
                new MovementController(speed, jumpForce),
                new CollisionComponent()
        );

        Body body = physicsWorld.createBody(bodyDef);
        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData(player);

        player.get(PhysicsComponent.class).setBody(body);

        return player;
    }

    public static Entity createGround(
            Dominion dominion,
            World physicsWorld,
            float posX,
            float posY,
            float width,
            float height,
            Color color
    ) {
        return createObstacle(
                dominion,
                physicsWorld,
                posX,
                posY,
                width,
                height,
                color
        );
    }

    public static Entity createObstacle(
            Dominion dominion,
            World physicsWorld,
            float posX,
            float posY,
            float sizeX,
            float sizeY,
            Color color
    ) {
        BodyDef boxDef = new BodyDef();
        boxDef.type = BodyType.STATIC;
        boxDef.position.set(posX, posY); // Set body's position to desired location


        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(sizeX/2, sizeY/2);

        FixtureDef boxFixture = new FixtureDef();
        boxFixture.shape = boxShape;

        Entity obstacle = dominion.createEntity(
                new TransformComponent(posX/2, posY/2),
                new SpriteComponent(color, (int) (sizeX *PIXELS_PER_METER), (int) (sizeY * PIXELS_PER_METER)),
                new PhysicsComponent(boxDef, boxFixture),
                new CollisionComponent(),
                new Tags.GroundTag()
        );

        Body body = physicsWorld.createBody(boxDef);
        Fixture fixture = body.createFixture(boxFixture);
        fixture.setUserData(obstacle);

        obstacle.get(PhysicsComponent.class).setBody(body);

        return obstacle;
    }
}