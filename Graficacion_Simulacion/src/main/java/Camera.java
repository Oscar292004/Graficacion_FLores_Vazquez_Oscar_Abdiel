package org.example;

public class Camera {
    // Las hice privadas para una mejor práctica de programación
    private float x = 0, y = 1.7f, z = 5;
    private float pitch = 0, yaw = -90;

    // --- MÉTODOS GETTER AÑADIDOS ---
    // El Renderer necesita estos métodos para obtener la posición de la cámara.
    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    // --------------------------------

    public void moveForward(float speed) {
        x += Math.cos(Math.toRadians(yaw)) * speed;
        z += Math.sin(Math.toRadians(yaw)) * speed;
    }

    public void moveBackward(float speed) {
        x -= Math.cos(Math.toRadians(yaw)) * speed;
        z -= Math.sin(Math.toRadians(yaw)) * speed;
    }

    public void moveLeft(float speed) {
        x += Math.sin(Math.toRadians(yaw)) * speed;
        z -= Math.cos(Math.toRadians(yaw)) * speed;
    }

    public void moveRight(float speed) {
        x -= Math.sin(Math.toRadians(yaw)) * speed;
        z += Math.cos(Math.toRadians(yaw)) * speed;
    }

    public void addRotation(float dx, float dy) {
        yaw += dx;
        pitch -= dy;

        if (pitch > 89) pitch = 89;
        if (pitch < -89) pitch = -89;
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}