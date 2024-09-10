package org.astro.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ConsoleInputHandler extends Thread {
    private Lighting lighting;
    private GamePanel gp;

    public ConsoleInputHandler(Lighting lighting, GamePanel gp) {
        this.lighting = lighting;
        this.gp = gp;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("Enter command: ");
            String command;
            try {
                command = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Parse the command into tokens
            String[] tokens = parseCommand(command);

            // Pass the tokens to the game panel or other handler
            try {
                gp.runCommand(tokens);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private String[] parseCommand(String command) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;

        for (char c : command.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
                if (!inQuotes && currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0); // Clear the StringBuilder
                }
            } else if (c == ' ' && !inQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0); // Clear the StringBuilder
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
