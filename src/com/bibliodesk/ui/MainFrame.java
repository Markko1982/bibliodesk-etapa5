package com.bibliodesk.ui;

import com.bibliodesk.app.Database;
import com.bibliodesk.model.User;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

/**
 * Janela principal da aplicação.
 * Também funciona como "roteador" de telas via CardLayout.
 */
public class MainFrame extends JFrame implements ScreenNavigator {

    private final Database db;
    private User currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final Map<String, JPanel> screens = new HashMap<>();

    public MainFrame(Database db) {
        this.db = db;

        setTitle("BiblioDesk");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);

        setContentPane(cards);
    }

    public Database getDb() {
        return db;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    /**
     * Devolve a interface de navegação para os painéis que só precisam
     * trocar de tela.
     */
    public ScreenNavigator getNavigator() {
        return this;
    }

    @Override
    public void addScreen(String name, JPanel panel) {
        screens.put(name, panel);
        cards.add(panel, name);
    }

    @Override
    public void showScreen(String name) {
        cardLayout.show(cards, name);

        JPanel panel = screens.get(name);
        if (panel instanceof Refreshable) {
            ((Refreshable) panel).onShow();
        }
    }
}
