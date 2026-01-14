package com.bibliodesk.ui;

import javax.swing.JPanel;

/**
 * Interface simples para navegação entre telas.
 */
public interface ScreenNavigator {

    void addScreen(String name, JPanel panel);

    void showScreen(String name);
}
