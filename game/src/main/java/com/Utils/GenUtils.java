package com.Utils;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import com.collins.Shader.ShaderProgram;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;

import org.joml.Matrix4f;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.system.MemoryUtil.*;

public class GenUtils {

    // getResourcePath
    public static InputStream getResource(String path) {
        return GenUtils.class.getClassLoader().getResourceAsStream(path);
    }

    // read file as string
    public static String loadResourceAsString(String file) {
        String result = "";
        try {

            InputStream inputStream = getResource(file);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();

            result = stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //load shaders
    public static ShaderProgram loadShader(String vertexPath, String fragmentPath) throws Exception {
        String vertexShader = loadResourceAsString(vertexPath);
        String fragmentShader = loadResourceAsString(fragmentPath);



        ShaderProgram shader = new ShaderProgram();
        try {
            shader.createVertexShader(vertexShader);
            shader.createFragmentShader(fragmentShader);
            shader.link();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shader;
    }


}
