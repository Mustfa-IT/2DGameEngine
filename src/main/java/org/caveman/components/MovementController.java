package org.caveman.components;

public class MovementController {
    private float moveSpeed;
    private float jumpForce;
    private boolean justJumped;
    private boolean isGrounded;

    public MovementController(float moveSpeed, float jumpForce) {
        this.moveSpeed = moveSpeed;
        this.jumpForce = jumpForce;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }
    public boolean isJustJumped() {
        return justJumped;
    }

    public void setJustJumped(boolean justJumped) {
        this.justJumped = justJumped;
    }
    public float getJumpForce() {
        return jumpForce;
    }

    public void setJumpForce(float jumpForce) {
        this.jumpForce = jumpForce;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void setGrounded(boolean grounded) {
        isGrounded = grounded;
    }
}
