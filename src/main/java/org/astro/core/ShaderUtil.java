package org.astro.core;

import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ShaderUtil {
    public static int loadShader(String vertexPath, String fragmentPath) {
        String vertexSource = readShaderFile(vertexPath);
        String fragmentSource = readShaderFile(fragmentPath);

        int vertexShader = compileShader(vertexSource, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compileShader(fragmentSource, GL20.GL_FRAGMENT_SHADER);

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Program link error: " + GL20.glGetProgramInfoLog(program, 1024));
        }

        return program;
    }

    private static int compileShader(String source, int type) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);

        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader compile error: " + GL20.glGetShaderInfoLog(shader, 1024));
        }

        return shader;
    }

    private static String readShaderFile(String path) {
        try (InputStream inputStream = ShaderUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new RuntimeException("Shader file not found: " + path);
            }

            // Read the file from the InputStream
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            return scanner.useDelimiter("\\A").next(); // Read all the content
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader file: " + path, e);
        }
    }
}
