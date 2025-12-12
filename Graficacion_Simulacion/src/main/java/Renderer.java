package org.example;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.File;
import java.io.IOException;

public class Renderer implements GLEventListener {

    private final Camera camera = new Camera();
    private final GLU glu = new GLU();

    private Texture wallTex, floorTex, roofTex;
    // Color de las ventanas
    private final float[] WINDOW_COLOR = {0.3f, 0.5f, 1.0f, 0.5f};

    // Posición de la "luz" (El Sol) para calcular la sombra
    // Está en X=10, Y=10, Z=5
    private final float[] lightPos = {10.0f, 10.0f, 5.0f};

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        try {
            wallTex = TextureIO.newTexture(new File("src/main/resources/wall.jpg"), true);
            floorTex = TextureIO.newTexture(new File("src/main/resources/floor.jpg"), true);
            roofTex = TextureIO.newTexture(new File("src/main/resources/roof.jpg"), true);
        } catch (IOException e) {
            System.err.println("Error cargando texturas: " + e.getMessage());
        }

        gl.glEnable(GL2.GL_TEXTURE_2D);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // 1. Limpiar pantalla y dibujar cielo
        gl.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();
        updateCamera(glu); // Metodo auxiliar para limpiar el código

        // 2. Dibujar el Suelo (Lo dibujamos primero para que las sombras caigan sobre él)
        drawExterior(gl);

        // 3. Dibujar la Escena REAL (Casa y Árboles con colores y texturas)
        gl.glPushMatrix();
        drawSceneObjects(gl, true); // true = con texturas y colores
        gl.glPopMatrix();

        // 4. Dibujar las SOMBRAS (La misma escena, pero aplastada y negra)
        // Desactivamos texturas y profundidad para la sombra
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_BLEND); // Para que la sombra sea medio transparente
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.5f); // Color Negro semitransparente

        gl.glPushMatrix();
        // Matriz mágica para sombras (Proyecta todo al piso Y=0.01)
        applyShadowMatrix(gl, lightPos);

        // Dibujamos los objetos de nuevo (pero ahora se verán negros y aplastados)
        drawSceneObjects(gl, false); // false = sin texturas, solo geometría
        gl.glPopMatrix();

        // Restaurar estado
        gl.glDisable(GL2.GL_BLEND);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glColor3f(1,1,1);
    }

    // Metodo que agrupa TODO lo que debe tener sombra (Casa + Árboles)
    private void drawSceneObjects(GL2 gl, boolean useTextures) {
        // Dibujar Casa
        drawHouse(gl, useTextures);

        // Dibujar Árbol 1 (A la izquierda)
        gl.glPushMatrix();
        gl.glTranslatef(-6, 0, -2); // Posición
        drawTree(gl, useTextures);
        gl.glPopMatrix();

        // Dibujar Árbol 2 (A la derecha atrás)
        gl.glPushMatrix();
        gl.glTranslatef(5, 0, 4);   // Posición
        gl.glScalef(1.2f, 1.5f, 1.2f); // Este árbol es más grande
        drawTree(gl, useTextures);
        gl.glPopMatrix();
    }

    private void drawTree(GL2 gl, boolean useTextures) {
        // Si usamos texturas, las desactivamos temporalmente para usar colores sólidos
        if(useTextures) gl.glDisable(GL2.GL_TEXTURE_2D);

        // --- TRONCO (Marrón) ---
        if(useTextures) gl.glColor3f(0.55f, 0.27f, 0.07f);
        // Cilindro aproximado con un cubo alargado
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3f(-0.3f, 0, 0.3f);
        gl.glVertex3f( 0.3f, 0, 0.3f);
        gl.glVertex3f( 0.3f, 2, 0.3f);
        gl.glVertex3f(-0.3f, 2, 0.3f);

        gl.glVertex3f(-0.3f, 0, -0.3f);
        gl.glVertex3f( 0.3f, 0, -0.3f);
        gl.glVertex3f( 0.3f, 2, -0.3f);
        gl.glVertex3f(-0.3f, 2, -0.3f);

        gl.glVertex3f(-0.3f, 0, 0.3f);
        gl.glVertex3f(-0.3f, 0, -0.3f);
        gl.glVertex3f(-0.3f, 2, -0.3f);
        gl.glVertex3f(-0.3f, 2, 0.3f);

        gl.glVertex3f(0.3f, 0, 0.3f);
        gl.glVertex3f(0.3f, 0, -0.3f);
        gl.glVertex3f(0.3f, 2, -0.3f);
        gl.glVertex3f(0.3f, 2, 0.3f);
        gl.glEnd();

        // HOJAS (Verde Oscuro)
        if(useTextures) gl.glColor3f(0.1f, 0.5f, 0.1f);
        // Pirámide
        gl.glBegin(GL2.GL_TRIANGLES);
        // Frente
        gl.glVertex3f(-1.5f, 2, 1.5f);
        gl.glVertex3f( 1.5f, 2, 1.5f);
        gl.glVertex3f( 0, 5, 0); // Punta
        // Atrás
        gl.glVertex3f(-1.5f, 2, -1.5f);
        gl.glVertex3f( 1.5f, 2, -1.5f);
        gl.glVertex3f( 0, 5, 0);
        // Izquierda
        gl.glVertex3f(-1.5f, 2, -1.5f);
        gl.glVertex3f(-1.5f, 2, 1.5f);
        gl.glVertex3f( 0, 5, 0);
        // Derecha
        gl.glVertex3f( 1.5f, 2, -1.5f);
        gl.glVertex3f( 1.5f, 2, 1.5f);
        gl.glVertex3f( 0, 5, 0);
        gl.glEnd();

        // Base de las hojas (tapa de abajo)
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3f(-1.5f, 2, 1.5f);
        gl.glVertex3f( 1.5f, 2, 1.5f);
        gl.glVertex3f( 1.5f, 2, -1.5f);
        gl.glVertex3f(-1.5f, 2, -1.5f);
        gl.glEnd();

        // Reactivar texturas si estaban activas
        if(useTextures) {
            gl.glColor3f(1,1,1);
            gl.glEnable(GL2.GL_TEXTURE_2D);
        }
    }

    // Esta función aplica matemáticas de matrices para "aplastar" la geometría contra el suelo
    // simulando una sombra proyectada desde la posición de la luz.
    private void applyShadowMatrix(GL2 gl, float[] lightPos) {
        float floorY = 0.01f; // Altura del piso (un poquito arriba para que no parpadee)
        float[] shadowMat = new float[16];
        float dot = lightPos[1] - floorY; // Usamos Y como eje principal

        shadowMat[0] = dot;
        shadowMat[4] = -lightPos[0];
        shadowMat[8] = 0;
        shadowMat[12] = 0;

        shadowMat[1] = 0;
        shadowMat[5] = 0;
        shadowMat[9] = 0;
        shadowMat[13] = 0; // Aplasta en Y

        shadowMat[2] = 0;
        shadowMat[6] = -lightPos[2];
        shadowMat[10] = dot;
        shadowMat[14] = 0;

        shadowMat[3] = 0;
        shadowMat[7] = -1;
        shadowMat[11] = 0;
        shadowMat[15] = lightPos[1];

        gl.glMultMatrixf(shadowMat, 0);
    }

    private void updateCamera(GLU glu) {
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();
        float lookX = (float)(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        float lookY = (float)(Math.sin(Math.toRadians(pitch)));
        float lookZ = (float)(Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        glu.gluLookAt(camera.getX(), camera.getY(), camera.getZ(),
                camera.getX() + lookX, camera.getY() + lookY, camera.getZ() + lookZ, 0, 1, 0);
    }

    private void drawExterior(GL2 gl) {
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glColor3f(0.1f, 0.6f, 0.1f); // Verde
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3f(-50, 0, -50);
        gl.glVertex3f( 50, 0, -50);
        gl.glVertex3f( 50, 0,  50);
        gl.glVertex3f(-50, 0,  50);
        gl.glEnd();
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glColor3f(1, 1, 1);
    }

    // Modificado para recibir "useTextures"
    private void drawHouse(GL2 gl, boolean useTextures) {
        if(useTextures && floorTex != null) floorTex.bind(gl);

        // PAREDES
        gl.glBegin(GL2.GL_QUADS);
        // Trasera
        gl.glTexCoord2f(0, 0.75f); gl.glVertex3f(-2, 1.5f, 2);
        gl.glTexCoord2f(1, 0.75f); gl.glVertex3f( 2, 1.5f, 2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 2, 2, 2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-2, 2, 2);

        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 0, 2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 2, 0, 2);
        gl.glTexCoord2f(1, 0.75f); gl.glVertex3f( 2, 1.5f, 2);
        gl.glTexCoord2f(0, 0.75f); gl.glVertex3f(-2, 1.5f, 2);
        // Izquierda
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 0,  2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(-2, 0, -2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f(-2, 2, -2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-2, 2,  2);
        // Derecha
        gl.glTexCoord2f(0, 0); gl.glVertex3f(2, 0, -2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(2, 0,  2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f(2, 2,  2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(2, 2, -2);
        // Frontal (Puerta)
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 0, -2);
        gl.glTexCoord2f(0.4f, 0); gl.glVertex3f(-0.5f, 0, -2);
        gl.glTexCoord2f(0.4f, 1); gl.glVertex3f(-0.5f, 2, -2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-2, 2, -2);

        gl.glTexCoord2f(0.6f, 0); gl.glVertex3f( 0.5f, 0, -2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 2, 0, -2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 2, 2, -2);
        gl.glTexCoord2f(0.6f, 1); gl.glVertex3f( 0.5f, 2, -2);

        gl.glTexCoord2f(0.4f, 0.75f); gl.glVertex3f(-0.5f, 1.5f, -2);
        gl.glTexCoord2f(0.6f, 0.75f); gl.glVertex3f( 0.5f, 1.5f, -2);
        gl.glTexCoord2f(0.6f, 1);     gl.glVertex3f( 0.5f, 2.0f, -2);
        gl.glTexCoord2f(0.4f, 1);     gl.glVertex3f(-0.5f, 2.0f, -2);
        gl.glEnd();

        // VENTANAS (Solo si usamos texturas, no queremos sombra de ventanas)
        if(useTextures) {
            gl.glDisable(GL2.GL_TEXTURE_2D);
            gl.glColor4f(WINDOW_COLOR[0], WINDOW_COLOR[1], WINDOW_COLOR[2], WINDOW_COLOR[3]);
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3f(-1.5f, 0.5f, 2.01f); gl.glVertex3f(-0.5f, 0.5f, 2.01f);
            gl.glVertex3f(-0.5f, 1.5f, 2.01f); gl.glVertex3f(-1.5f, 1.5f, 2.01f);
            gl.glVertex3f( 0.5f, 0.5f, 2.01f); gl.glVertex3f( 1.5f, 0.5f, 2.01f);
            gl.glVertex3f( 1.5f, 1.5f, 2.01f); gl.glVertex3f( 0.5f, 1.5f, 2.01f);
            gl.glEnd();
            gl.glEnable(GL2.GL_TEXTURE_2D);
            gl.glColor3f(1,1,1);
        }

        // PISO
        if(useTextures && wallTex != null) wallTex.bind(gl);
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 0.001f, -2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 2, 0.001f, -2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 2, 0.001f,  2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f(-2, 0.001f,  2);
        gl.glEnd();

        // TECHO
        if(useTextures && roofTex != null) roofTex.bind(gl);
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 2, -2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 2, 2, -2);
        gl.glTexCoord2f(0.5f, 1); gl.glVertex3f( 0, 3.5f, -2);

        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 2, 2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 2, 2, 2);
        gl.glTexCoord2f(0.5f, 1); gl.glVertex3f( 0, 3.5f, 2);
        gl.glEnd();

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex3f(-2, 2, -2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f(-2, 2,  2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 0, 3.5f, 2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f( 0, 3.5f, -2);

        gl.glTexCoord2f(0, 0); gl.glVertex3f( 2, 2, -2);
        gl.glTexCoord2f(1, 0); gl.glVertex3f( 2, 2,  2);
        gl.glTexCoord2f(1, 1); gl.glVertex3f( 0, 3.5f, 2);
        gl.glTexCoord2f(0, 1); gl.glVertex3f( 0, 3.5f, -2);
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL2 gl = drawable.getGL().getGL2();
        if (h == 0) h = 1;
        float aspect = (float) w / h;
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(60, aspect, 0.1, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }
    public Camera getCamera() { return camera; }
}