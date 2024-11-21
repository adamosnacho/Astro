package org.astro.core;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class Deserializer {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the .ser file: ");
        String filePath = scanner.nextLine();

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            Object obj = objectInputStream.readObject();
            System.out.println("Deserialized Object Structure:");
            printObjectTree(obj, "", new HashSet<>(), true);

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading the .ser file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recursively prints the object tree structure.
     *
     * @param obj        the object to inspect
     * @param indent     the indentation to use for printing
     * @param visited    a set of visited objects to avoid infinite loops
     * @param isLast     indicates if the current object is the last child
     */
    private static void printObjectTree(Object obj, String indent, Set<Object> visited, boolean isLast) {
        if (obj == null) {
            System.out.println(indent + "null");
            return;
        }

        Class<?> objClass = obj.getClass();
        visited.add(obj);  // Mark this object as visited

        String prefix = isLast ? "└── " : "├── ";
        String childIndent = isLast ? "    " : "│   ";

        // Handle primitive types and strings directly
        if (isPrimitiveOrWrapper(objClass) || obj instanceof String) {
            System.out.println(indent + prefix + objClass.getSimpleName() + ": " + obj);
            return;
        }

        // Handle arrays
        if (objClass.isArray()) {
            int length = Array.getLength(obj);
            System.out.println(indent + prefix + objClass.getSimpleName() + "[] with length " + length + ":");
            for (int i = 0; i < length; i++) {
                printObjectTree(Array.get(obj, i), indent + childIndent, visited, i == length - 1);
            }
            return;
        }

        // Handle collections
        if (obj instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) obj;
            System.out.println(indent + prefix + objClass.getSimpleName() + " (Collection) with " + collection.size() + " items:");
            List<?> items = new ArrayList<>(collection);
            for (int i = 0; i < items.size(); i++) {
                printObjectTree(items.get(i), indent + childIndent, visited, i == items.size() - 1);
            }
            return;
        }

        // Handle maps
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) obj;
            System.out.println(indent + prefix + objClass.getSimpleName() + " (Map) with " + map.size() + " entries:");
            List<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
            for (int i = 0; i < entries.size(); i++) {
                Map.Entry<?, ?> entry = entries.get(i);
                System.out.println(indent + childIndent + "Key:");
                printObjectTree(entry.getKey(), indent + childIndent + "    ", visited, i == entries.size() - 1);
                System.out.println(indent + childIndent + "Value:");
                printObjectTree(entry.getValue(), indent + childIndent + "    ", visited, i == entries.size() - 1);
            }
            return;
        }

        // Handle Java records
        if (objClass.isRecord()) {
            System.out.println(indent + prefix + objClass.getSimpleName() + " (Record):");
            for (RecordComponent component : objClass.getRecordComponents()) {
                try {
                    Method accessor = component.getAccessor();
                    Object value = accessor.invoke(obj);
                    System.out.println(indent + childIndent + component.getName() + ":");
                    printObjectTree(value, indent + childIndent + "    ", visited, true);
                } catch (Exception e) {
                    System.out.println(indent + childIndent + component.getName() + " (Error accessing field - " + e.getMessage() + ")");
                }
            }
            return;
        }

        // Handle regular objects using fields
        System.out.println(indent + prefix + objClass.getSimpleName() + " (Object):");
        Field[] fields = objClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                System.out.println(indent + childIndent + field.getName() + ":");
                printObjectTree(fieldValue, indent + childIndent + "    ", visited, i == fields.length - 1);
            } catch (IllegalAccessException e) {
                System.out.println(indent + childIndent + field.getName() + " (Error accessing field - " + e.getMessage() + ")");
            }
        }
    }

    /**
     * Checks if a class is a primitive type or its wrapper.
     *
     * @param clazz the class to check
     * @return true if the class is a primitive or wrapper, false otherwise
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class || clazz == Integer.class ||
                clazz == Character.class || clazz == Byte.class ||
                clazz == Short.class || clazz == Double.class ||
                clazz == Long.class || clazz == Float.class;
    }
}
