package com.bibliodesk.ui;

import com.bibliodesk.model.Profile;
import com.bibliodesk.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para cadastro e edição de usuários. Permite informar nome,
 * login, senha, perfil e se o usuário está ativo. Ao confirmar, as
 * informações inseridas são disponibilizadas via getters.
 */
public class UserFormDialog extends JDialog {
    private boolean saved = false;
    private final JTextField name = new JTextField(18);
    private final JTextField login = new JTextField(18);
    private final JPasswordField pass = new JPasswordField(18);
    private final JComboBox<Profile> profile = new JComboBox<>(Profile.values());
    private final JCheckBox active = new JCheckBox("Ativo");

    public UserFormDialog(Frame owner, User existing) {
        super(owner, true);
        setTitle(existing == null ? "Novo usuário" : "Editar usuário");
        setSize(360, 260);
        setLocationRelativeTo(owner);
        initUI(existing);
    }

    private void initUI(User existing) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        p.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        p.add(name, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        p.add(new JLabel("Login:"), gbc);
        gbc.gridx = 1;
        p.add(login, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        p.add(new JLabel("Senha:"), gbc);
        gbc.gridx = 1;
        p.add(pass, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        p.add(new JLabel("Perfil:"), gbc);
        gbc.gridx = 1;
        p.add(profile, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        p.add(active, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancelar");

        ok.addActionListener(e -> {
            if (name.getText().trim().isEmpty() || login.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e Login são obrigatórios.");
                return;
            }
            if (existing == null && new String(pass.getPassword()).isBlank()) {
                JOptionPane.showMessageDialog(this, "Senha é obrigatória no cadastro.");
                return;
            }
            saved = true;
            dispose();
        });

        cancel.addActionListener(e -> dispose());
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);

        if (existing != null) {
            name.setText(existing.getName());
            login.setText(existing.getLogin());
            profile.setSelectedItem(existing.getProfile());
            active.setSelected(existing.isActive());
        } else {
            active.setSelected(true);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public String getNameValue() {
        return name.getText().trim();
    }

    public String getLoginValue() {
        return login.getText().trim();
    }

    public String getPasswordValue() {
        return new String(pass.getPassword());
    }

    public String getPasswordValueOrNullIfEmpty() {
        String p = new String(pass.getPassword());
        return p.isBlank() ? null : p;
    }

    public Profile getProfileValue() {
        return (Profile) profile.getSelectedItem();
    }

    public boolean isActiveValue() {
        return active.isSelected();
    }
}