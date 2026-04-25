package com.dadagm;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager uiManager = new UIManager();
            uiManager.createAndShowGUI();
        });
    }
}