package com.fleetstrike;

import com.fleetstrike.presentation.console.ConsoleGameController;
import com.fleetstrike.presentation.swing.FleetStrikeApp;

import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        boolean preferConsole = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                preferConsole = true;
                break;
            }
        }

        if (preferConsole || GraphicsEnvironment.isHeadless()) {
            ConsoleGameController controller = new ConsoleGameController();
            controller.start();
        } else {
            FleetStrikeApp.main(args);
        }
    }
}
