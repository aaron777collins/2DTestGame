package com.collins;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.Utils.GenUtils;
import com.collins.Shader.ShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Game {

    // The window handle
    private long window;
    // width and height of window
    private int width = 800;
    private int height = 600;

    int vaoId;
    int vboId;

    ShaderProgram shaderProgram;

    /**
     * Field of View in Radians
     */
    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    private Matrix4f projectionMatrix;

    public void run() throws Exception {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void setKeyCallbacks() {
        // Setup a key callback. It will be called every time a key is pressed, repeated
        // or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }

    private void sync(double loopStartTime) {
        float loopSlot = 1f / 50;
        double endTime = loopStartTime + loopSlot;
        while(getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {}
        }
    }

    // This function returns the time in seconds since the program started
    private double getTime() {
        return System.nanoTime() / 1000000000.0;
    }

    private void init() throws Exception {

        float aspectRatio;

        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
        throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
        throw new RuntimeException("Failed to create the GLFW window");

        // set key callbacks
        setKeyCallbacks();

        // Get the thread stack and push a new frame
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2);

                aspectRatio = (float) pWidth.get(0) / pHeight.get(0);
            } // the stack frame is popped automatically
            // Make the OpenGL context current
            glfwMakeContextCurrent(window);
            // Enable v-sync
            glfwSwapInterval(1);

            shaderProgram = GenUtils.loadShader("vertex.vs", "fragment.fs");


            // Make the window visible
            glfwShowWindow(window);

            projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio,
                Z_NEAR, Z_FAR);



            float[] vertices = new float[]{
                0.0f,  0.5f, 0.0f,
               -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
           };

           FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
verticesBuffer.put(vertices).flip();


            // Bind to the VAO
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);
            glEnableVertexAttribArray(0);

            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            memFree(verticesBuffer);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

           // Unbind the VBO
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Unbind the VAO
            glBindVertexArray(0);

            if (verticesBuffer != null) {
                MemoryUtil.memFree(verticesBuffer);
            }

        }

        private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        double current = getTime();
        double secsPerUpdate = 1.0d / 30.0d;
        double previous = getTime();
        double steps = 0.0;


        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {

            double loopStartTime = getTime();
            double elapsed = loopStartTime - previous;
            previous = current;
            steps += elapsed;

            //handleInput();

            while (steps >= secsPerUpdate) {
              //updateGameState();
              steps -= secsPerUpdate;
            }

            render();
            sync(current);

            // glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // render();

            // glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void render() {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        shaderProgram.bind();

        // Bind to the VAO
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);

        // Draw the vertices
        glDrawArrays(GL_TRIANGLES, 0, 3);

        // Restore state
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        shaderProgram.unbind();

        // // Set the viewport to the dimensions of the window
        // GL11.glViewport(0, 0, width, height);

        // // Set the current matrix mode to GL_PROJECTION
        // GL11.glMatrixMode(GL11.GL_PROJECTION);

        // // Load the identity matrix
        // GL11.glLoadIdentity();

        // // Create a Matrix4f object for the perspective projection matrix
        // Matrix4f projMatrix = new Matrix4f();

        // // Set the perspective projection matrix using the perspective method
        // projMatrix.perspective((float) Math.toRadians(45.0f), (float) width / (float) height, 0.1f, 100.0f);

        // // Convert the Matrix4f object to a float array
        // float[] projArray = new float[16];
        // projMatrix.get(projArray);

        // // Apply the perspective projection matrix using the glMultMatrix function
        // GL11.glMultMatrixf(projArray);

        // // Set the current matrix mode to GL_MODELVIEW
        // GL11.glMatrixMode(GL11.GL_MODELVIEW);

        // // Load the identity matrix
        // GL11.glLoadIdentity();

        // // Create a Matrix4f object for the camera transformation matrix
        // Matrix4f camMatrix = new Matrix4f();

        // // Set the camera position and orientation using the translate and rotate
        // // methods
        // camMatrix.translate(5.0f, 0.0f, 0.0f);
        // camMatrix.rotate((float) Math.toRadians(0.0f), 1.0f, 0.0f, 0.0f);

        // // Convert the Matrix4f object to a float array
        // float[] camArray = new float[16];
        // camMatrix.get(camArray);

        // // Apply the camera transformation matrix using the glMultMatrix function
        // GL11.glMultMatrixf(camArray);

        // // Enable the depth buffer
        // GL11.glEnable(GL11.GL_DEPTH_TEST);

        // // Clear the depth buffer
        // GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // // Begin drawing the cube
        // GL11.glBegin(GL11.GL_TRIANGLES);

        // // Set the color of the front face to red
        // glColor3f(1.0f, 0.0f, 0.0f);

        // // Specify the vertices of the front face
        // glVertex3f(-0.5f, 0.5f, 0.5f);
        // glVertex3f(-0.5f, -0.5f, 0.5f);
        // glVertex3f(0.5f, -0.5f, 0.5f);
        // glVertex3f(0.5f, -0.5f, 0.5f);
        // glVertex3f(0.5f, 0.5f, 0.5f);
        // glVertex3f(-0.5f, 0.5f, 0.5f);

        // // Set the color of the back face to green
        // glColor3f(0.0f, 1.0f, 0.0f);

        // // Specify the vertices of the back face
        // glVertex3f(-0.5f, 0.5f, -0.5f);
        // glVertex3f(0.5f, 0.5f, -0.5f);
        // glVertex3f(0.5f, -0.5f, -0.5f);
        // glVertex3f(0.5f, -0.5f, -0.5f);
        // glVertex3f(-0.5f, -0.5f, -0.5f);
        // glVertex3f(-0.5f, 0.5f, -0.5f);

        // // Set the color of the left face to blue
        // glColor3f(0.0f, 0.0f, 1.0f);

        // // Specify the vertices of the left face
        // glVertex3f(-0.5f, 0.5f, 0.5f);
        // glVertex3f(-0.5f, 0.5f, -0.5f);
        // glVertex3f(-0.5f, -0.5f, -0.5f);
        // glVertex3f(-0.5f, -0.5f, -0.5f);
        // glVertex3f(-0.5f, -0.5f, 0.5f);
        // glVertex3f(-0.5f, 0.5f, 0.5f);

        // // Set the color of the right face to yellow
        // glColor3f(1.0f, 1.0f, 0.0f);

        // // Specify the vertices of the right face
        // glVertex3f(0.5f, 0.5f, 0.5f);
        // glVertex3f(0.5f, -0.5f, 0.5f);
        // glVertex3f(0.5f, -0.5f, -0.5f);
        // glVertex3f(0.5f, -0.5f, -0.5f);
        // glVertex3f(0.5f, 0.5f, -0.5f);
        // glVertex3f(0.5f, 0.5f, 0.5f);

        // // Set the color of the top face to cyan
        // glColor3f(0.0f, 1.0f, 1.0f);

        // // Specify the vertices of the top face
        // glVertex3f(-0.5f, 0.5f, 0.5f);
        // glVertex3f(0.5f, 0.5f, 0.5f);
        // glVertex3f(0.5f, 0.5f, -0.5f);
        // glVertex3f(0.5f, 0.5f, -0.5f);
        // glVertex3f(-0.5f, 0.5f, -0.5f);
        // glVertex3f(-0.5f, 0.5f, 0.5f);

        // // Set the color of the bottom face to magenta
        // glColor3f(1.0f, 0.0f, 1.0f);

        // // Specify the vertices of the bottom face
        // glVertex3f(-0.5f, -0.5f, 0.5f);
        // glVertex3f(-0.5f, -0.5f, -0.5f);
        // glVertex3f(0.5f, -0.5f, -0.5f);
        // glVertex3f(0.5f, -0.5f, -0.5f);
        // glVertex3f(0.5f, -0.5f, 0.5f);
        // glVertex3f(-0.5f, -0.5f, 0.5f);

        // // End drawing the cube
        // glEnd();




        glfwSwapBuffers(window); // swap the color buffers

    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    // get window
    public long getWindow() {
        return window;
    }

    public static void main(String[] args) throws Exception {
        try {
            new Game().run();
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
