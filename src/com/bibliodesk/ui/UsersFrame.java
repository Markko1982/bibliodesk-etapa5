package com.bibliodesk.ui;

import com.bibliodesk.app.Database;
import com.bibliodesk.model.Profile;
import com.bibliodesk.model.User;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class UsersFrame extends JFrame {

    private final Database db;
    private final DashboardFrameForm dashboard;

    private JComboBox<String> filterProfile;
    private DefaultTableModel model;
    private JTable table;
    private List<User> currentRows = new ArrayList<>();

    public UsersFrame(Database db, DashboardFrameForm dashboard) {
        super("BiblioDesk - Usuários");
        this.db = db;
        this.dashboard = dashboard;
        initUI();
        refresh();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                if (dashboard != null) {
                    dashboard.returnToDashboard();
                }
            }
        });

        setSize(720, 460);
        setLocationRelativeTo(null);
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Topo com título e filtro
        JPanel top = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("Usuários");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(new JLabel("Filtrar perfil:"));
        filterProfile = new JComboBox<>(new String[]{"Todos", "ADMIN", "BIBLIOTECARIO", "LEITOR"});
        filterProfile.addActionListener(e -> refresh());
        right.add(filterProfile);
        top.add(right, BorderLayout.EAST);
        content.add(top, BorderLayout.NORTH);

        // Tabela de usuários
        model = new DefaultTableModel(new Object[]{"Nome", "Login", "Perfil", "Ativo"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        table = new JTable(model);
        table.setRowHeight(22);

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 3) {
                    int row = e.getFirstRow();
                    if (row >= 0 && row < currentRows.size()) {
                        boolean active = Boolean.TRUE.equals(model.getValueAt(row, 3));
                        User u = currentRows.get(row);
                        db.setUserActive(u.getId(), active);
                    }
                }
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);

        // Botões inferiores
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton novo = new JButton("Novo");
        JButton editar = new JButton("Editar");
        JButton excluir = new JButton("Excluir");
        JButton voltar = new JButton("Voltar ao menu");

        novo.addActionListener(e -> onNew());
        editar.addActionListener(e -> onEdit());
        excluir.addActionListener(e -> onDelete());
        voltar.addActionListener(e -> {
            dispose();
            if (dashboard != null) {
                dashboard.returnToDashboard();
            }
        });

        bottom.add(novo);
        bottom.add(editar);
        bottom.add(excluir);
        bottom.add(voltar);

        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void refresh() {
        model.setRowCount(0);
        List<User> users = db.getUsers();
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
        UserFormDialog dlg = new UserFormDialog(this, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            db.addUser(
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
        UserFormDialog dlg = new UserFormDialog(this, selected);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            db.updateUser(
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
        int ok = JOptionPane.showConfirmDialog(
                this,
                "Excluir usuário \"" + selected.getName() + "\"?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION
        );
        if (ok == JOptionPane.YES_OPTION) {
            db.deleteUser(selected.getId());
            refresh();
        }
    }
}
