package com.bibliodesk.ui;

import com.bibliodesk.model.Profile;
import com.bibliodesk.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {

    private final MainFrame frame;

    private final JComboBox<String> filterProfile = new JComboBox<>(new String[]{
            "Todos", "ADMIN", "BIBLIOTECARIO", "LEITOR"
    });

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Nome", "Login", "Perfil", "Ativo"}, 0
    ) {
        @Override public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) return Boolean.class;
            return String.class;
        }
        @Override public boolean isCellEditable(int row, int col) {
            return col == 3; // só "Ativo" editável (checkbox)
        }
        @Override public void setValueAt(Object aValue, int row, int col) {
            super.setValueAt(aValue, row, col);
            if (col == 3) {
                User u = (User) getValueAt(row, 0); // vamos guardar o User no col 0? não.
            }
        }
    };

    private final JTable table = new JTable(model);

    // guarda os usuários correspondentes às linhas
    private List<User> currentRows = List.of();

    public UsersPanel(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top
        JPanel top = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Usuários");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        top.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(new JLabel("Filtrar perfil:"));
        right.add(filterProfile);
        top.add(right, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        filterProfile.addActionListener(e -> refresh());

        // Table
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Quando clicar no checkbox do "Ativo", atualiza o DB de verdade
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 3 && e.getFirstRow() >= 0 && e.getFirstRow() < currentRows.size()) {
                int row = e.getFirstRow();
                boolean active = Boolean.TRUE.equals(model.getValueAt(row, 3));
                User u = currentRows.get(row);
                frame.getDb().setUserActive(u.getId(), active);
            }
        });

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton novo = new JButton("Novo");
        JButton editar = new JButton("Editar");
        JButton excluir = new JButton("Excluir");
        JButton voltar = new JButton("Voltar ao menu");

        novo.addActionListener(e -> onNew());
        editar.addActionListener(e -> onEdit());
        excluir.addActionListener(e -> onDelete());
        voltar.addActionListener(e -> frame.getNavigator().showScreen("dashboard"));

        bottom.add(novo);
        bottom.add(editar);
        bottom.add(excluir);
        bottom.add(voltar);

        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);

        List<User> users = frame.getDb().getUsers();

        String fp = (String) filterProfile.getSelectedItem();
        if (fp != null && !"Todos".equals(fp)) {
            Profile p = Profile.valueOf(fp);
            users = users.stream().filter(u -> u.getProfile() == p).toList();
        }

        currentRows = users;

        for (User u : users) {
            model.addRow(new Object[]{
                    u.getName(),
                    u.getLogin(),
                    u.getProfile().name(),
                    u.isActive()
            });
        }
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentRows.size()) return null;
        return currentRows.get(row);
    }

    private void onNew() {
        UserFormDialog dlg = new UserFormDialog(frame, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            frame.getDb().addUser(
                    dlg.getNameValue(),
                    dlg.getLoginValue(),
                    dlg.getPasswordValue(),
                    dlg.getProfileValue(),
                    dlg.isActiveValue()
            );
            refresh();
        }
    }

    private void onEdit() {
        User selected = getSelectedUser();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }
        UserFormDialog dlg = new UserFormDialog(frame, selected);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            frame.getDb().updateUser(
                    selected.getId(),
                    dlg.getNameValue(),
                    dlg.getLoginValue(),
                    dlg.getPasswordValueOrNullIfEmpty(),
                    dlg.getProfileValue(),
                    dlg.isActiveValue()
            );
            refresh();
        }
    }

    private void onDelete() {
        User selected = getSelectedUser();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Excluir usuário \"" + selected.getName() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            frame.getDb().deleteUser(selected.getId());
            refresh();
        }
    }

    // -------- Dialog interno --------
    private static class UserFormDialog extends JDialog {
        private boolean saved = false;

        private final JTextField name = new JTextField(18);
        private final JTextField login = new JTextField(18);
        private final JPasswordField pass = new JPasswordField(18);
        private final JComboBox<Profile> profile = new JComboBox<>(Profile.values());
        private final JCheckBox active = new JCheckBox("Ativo");

        UserFormDialog(Frame owner, User existing) {
            super(owner, true);
            setTitle(existing == null ? "Novo usuário" : "Editar usuário");
            setSize(360, 260);
            setLocationRelativeTo(owner);

            JPanel p = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 6, 6, 6);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Nome:"), gbc);
            gbc.gridx = 1; p.add(name, gbc);

            gbc.gridx = 0; gbc.gridy = 1; p.add(new JLabel("Login:"), gbc);
            gbc.gridx = 1; p.add(login, gbc);

            gbc.gridx = 0; gbc.gridy = 2; p.add(new JLabel("Senha:"), gbc);
            gbc.gridx = 1; p.add(pass, gbc);

            gbc.gridx = 0; gbc.gridy = 3; p.add(new JLabel("Perfil:"), gbc);
            gbc.gridx = 1; p.add(profile, gbc);

            gbc.gridx = 1; gbc.gridy = 4; p.add(active, gbc);

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

        boolean isSaved() { return saved; }
        String getNameValue() { return name.getText().trim(); }
        String getLoginValue() { return login.getText().trim(); }
        String getPasswordValue() { return new String(pass.getPassword()); }
        String getPasswordValueOrNullIfEmpty() {
            String p = new String(pass.getPassword());
            return p.isBlank() ? null : p;
        }
        Profile getProfileValue() { return (Profile) profile.getSelectedItem(); }
        boolean isActiveValue() { return active.isSelected(); }
    }
}
