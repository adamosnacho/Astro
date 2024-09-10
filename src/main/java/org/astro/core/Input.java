package org.astro.core;

import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class Input {
    public static Set<Integer> keys = new HashSet<>();
    public static MouseEvent mouse;
    public static boolean btn1Click = false;
    public static boolean btn2Click = false;
    public static boolean btn3Click = false;
    public static boolean btn1 = false;
    public static boolean btn2 = false;
    public static boolean btn3 = false;

    private static boolean btn1Processed = false;
    private static boolean btn2Processed = false;
    private static boolean btn3Processed = false;

    public static void MousePressed() {
        if (mouse.getButton() == MouseEvent.BUTTON1 && !btn1Processed) {
            btn1Click = true;
            btn1Processed = true;
        }

        if (mouse.getButton() == MouseEvent.BUTTON2 && !btn2Processed) {
            btn2Click = true;
            btn2Processed = true;
        }

        if (mouse.getButton() == MouseEvent.BUTTON3 && !btn3Processed) {
            btn3Click = true;
            btn3Processed = true;
        }


        if (mouse.getButton() == MouseEvent.BUTTON1) btn1 = true;
        if (mouse.getButton() == MouseEvent.BUTTON2) btn2 = true;
        if (mouse.getButton() == MouseEvent.BUTTON3) btn3 = true;
    }

    public static void MouseReleased() {
        if (mouse.getButton() == MouseEvent.BUTTON1) {
            btn1Processed = false;
        }
        if (mouse.getButton() == MouseEvent.BUTTON2) {
            btn2Processed = false;
        }
        if (mouse.getButton() == MouseEvent.BUTTON3) {
            btn3Processed = false;
        }

        if (mouse.getButton() == MouseEvent.BUTTON1) btn1 = false;
        if (mouse.getButton() == MouseEvent.BUTTON2) btn2 = false;
        if (mouse.getButton() == MouseEvent.BUTTON3) btn3 = false;
    }

    public static void update() {
        btn1Click = false;
        btn2Click = false;
        btn3Click = false;
    }
}
