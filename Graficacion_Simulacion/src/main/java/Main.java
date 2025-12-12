package org.example;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.event.*; // Importa MouseAdapter, KeyAdapter, etc.
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;

public class Main {

    public static void main(String[] args) {

        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        GLWindow window = GLWindow.create(caps);

        Renderer renderer = new Renderer();
        window.addGLEventListener(renderer);

        window.setSize(800, 600);
        window.setTitle("Casa en JOGL - Oscar");
        window.setVisible(true);

        FPSAnimator animator = new FPSAnimator(window, 60);
        animator.start();

        // --- Cierre de la aplicación ---
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(WindowEvent e) {
                animator.stop();
                System.exit(0);
            }
        });

        // --- Controles de cámara (Teclado) ---
        Camera cam = renderer.getCamera();

        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                float speed = 0.1f;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> cam.moveForward(speed);
                    case KeyEvent.VK_S -> cam.moveBackward(speed);
                    case KeyEvent.VK_A -> cam.moveLeft(speed);
                    case KeyEvent.VK_D -> cam.moveRight(speed);
                }
            }
        });

        // Control del mause
        window.addMouseListener(new MouseAdapter() {
            private int lastX = -1, lastY = -1;
            private boolean isCameraPaused = false; // Bandera para saber si pausamos

            // Detectar cuando PRESIONAMOS el botón
            @Override
            public void mousePressed(MouseEvent e) {
                // Si es el clic izquierdo (BUTTON1), pausamos la cámara
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isCameraPaused = true;
                }
            }

            // Detectar cuando SOLTAMOS el botón
            @Override
            public void mouseReleased(MouseEvent e) {
                // Al soltar el clic izquierdo, reactivamos la cámara
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isCameraPaused = false;
                    // Actualizamos lastX/Y para evitar un "salto" brusco al reactivar
                    lastX = e.getX();
                    lastY = e.getY();
                }
            }

            // Movimiento normal del mouse
            @Override
            public void mouseMoved(MouseEvent e) {
                handleCameraRotation(e);
            }

            // Movimiento mientras se presiona un botón (arrastrar)
            @Override
            public void mouseDragged(MouseEvent e) {
                handleCameraRotation(e);
            }

            // Lógica común para rotar
            private void handleCameraRotation(MouseEvent e) {
                if (lastX == -1) {
                    lastX = e.getX();
                    lastY = e.getY();
                    return;
                }

                // SI ESTÁ PAUSADO (Clic izquierdo apretado):
                // Solo actualizamos la posición 'lastX/Y' para que no se pierda la referencia,
                // pero NO llamamos a cam.addRotation.
                if (isCameraPaused) {
                    lastX = e.getX();
                    lastY = e.getY();
                    return;
                }

                // SI NO ESTÁ PAUSADO: Calculamos y rotamos
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;

                cam.addRotation(dx * 0.2f, dy * 0.2f);

                lastX = e.getX();
                lastY = e.getY();
            }
        });
    }
}