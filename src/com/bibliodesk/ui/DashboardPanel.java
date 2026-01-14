package com.bibliodesk.ui;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel(MainFrame frame) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Dashboard / Menu");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 2, 12, 12));

        JButton users = new JButton("Usuários");
        users.addActionListener(e -> frame.getNavigator().showScreen("users"));

        JButton books = new JButton("Livros");
        books.addActionListener(e -> frame.getNavigator().showScreen("books"));

        JButton loans = new JButton("Empréstimos/Devolução");
        loans.addActionListener(e -> frame.getNavigator().showScreen("loans"));

        JButton sair = new JButton("Sair");
        sair.addActionListener(e -> {
            frame.setCurrentUser(null);
            frame.getNavigator().showScreen("login");
        });

        center.add(users);
        center.add(books);
        center.add(loans);
        center.add(sair);

        add(center, BorderLayout.CENTER);
    }
}
