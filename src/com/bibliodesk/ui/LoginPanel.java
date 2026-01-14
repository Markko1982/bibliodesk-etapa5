package com.bibliodesk.ui;

import com.bibliodesk.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final MainFrame frame;
    private final JTextField loginField = new JTextField(18);
    private final JPasswordField passField = new JPasswordField(18);
    private final JLabel msg = new JLabel(" ");

    public LoginPanel(MainFrame frame) {
        this.frame = frame;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel title = new JLabel("BiblioDesk - Login");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Login:"), gbc);

        gbc.gridx = 1;
        add(loginField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        add(passField, gbc);

        JButton entrar = new JButton("Entrar");
        entrar.addActionListener(e -> doLogin());

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(entrar, gbc);

        msg.setForeground(new Color(180, 0, 0));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(msg, gbc);
    }

    private void doLogin() {
        String login = loginField.getText().trim();
        String pass = new String(passField.getPassword());

        User u = frame.getDb().authenticate(login, pass).orElse(null);
        if (u == null) {
            msg.setText("Login/senha inválidos OU usuário inativo.");
            return;
        }

        frame.setCurrentUser(u);
        msg.setText(" ");
        passField.setText("");
        frame.getNavigator().showScreen("dashboard");
    }
}
