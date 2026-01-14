package com.bibliodesk.ui;

import com.bibliodesk.app.Database;
import com.bibliodesk.model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class BooksFrame extends JFrame {

    private final Database db;
    private final DashboardFrameForm dashboard;

    private JTextField searchField;
    private DefaultTableModel model;
    private JTable table;

    public BooksFrame(Database db, DashboardFrameForm dashboard) {
        super("BiblioDesk - Livros");
        this.db = db;
        this.dashboard = dashboard;
        initUI();
        refreshTable();
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

        setSize(800, 500);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Topo com título e busca
        JLabel title = new JLabel("Livros");
        title.setFont(title.getFont().deriveFont(22f));
        JPanel top = new JPanel(new BorderLayout());
        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topLeft.add(title);
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.add(new JLabel("Buscar:"));
        searchField = new JTextField(25);
        topRight.add(searchField);
        JButton btSearch = new JButton("Filtrar");
        btSearch.addActionListener(e -> refreshTable());
        JButton btClear = new JButton("Limpar");
        btClear.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });
        topRight.add(btSearch);
        topRight.add(btClear);
        top.add(topLeft, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);
        content.add(top, BorderLayout.NORTH);

        // Tabela de livros
        model = new DefaultTableModel(new Object[]{"Título", "Autor", "ISBN", "Ano", "Qtd total", "Disponível"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(22);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        // Botões inferiores
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btNew = new JButton("Novo");
        JButton btEdit = new JButton("Editar");
        JButton btDelete = new JButton("Excluir");
        JButton btBack = new JButton("Voltar");
        btNew.addActionListener(e -> onNew());
        btEdit.addActionListener(e -> onEdit());
        btDelete.addActionListener(e -> onDelete());
        btBack.addActionListener(e -> {
            dispose();
            if (dashboard != null) {
                dashboard.returnToDashboard();
            }
        });
        bottom.add(btNew);
        bottom.add(btEdit);
        bottom.add(btDelete);
        bottom.add(btBack);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void refreshTable() {
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        model.setRowCount(0);
        List<Book> books = db.getBooks();
        for (Book b : books) {
            boolean match = q.isEmpty()
                    || b.getTitle().toLowerCase().contains(q)
                    || b.getAuthor().toLowerCase().contains(q)
                    || b.getIsbn().toLowerCase().contains(q);
            if (match) {
                model.addRow(new Object[]{
                        b.getTitle(),
                        b.getAuthor(),
                        b.getIsbn(),
                        b.getYear(),
                        b.getTotalQuantity(),
                        b.getAvailableQuantity()
                });
            }
        }
    }

    private Book getSelectedBookByTitleAuthor() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        String title = (String) model.getValueAt(row, 0);
        String author = (String) model.getValueAt(row, 1);
        for (Book b : db.getBooks()) {
            if (b.getTitle().equals(title) && b.getAuthor().equals(author)) {
                return b;
            }
        }
        return null;
    }

    private void onNew() {
        BookFormDialog dlg = new BookFormDialog(this, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            Book b = dlg.toBook();
            if (b != null) {
                db.addBook(
                        b.getTitle(),
                        b.getAuthor(),
                        b.getIsbn(),
                        b.getYear(),
                        b.getTotalQuantity(),
                        b.getAvailableQuantity()
                );
                refreshTable();
            }
        }
    }

    private void onEdit() {
        Book selected = getSelectedBookByTitleAuthor();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecione um livro na tabela.");
            return;
        }
        BookFormDialog dlg = new BookFormDialog(this, selected);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            Book edited = dlg.toBook();
            if (edited != null) {
                int total = edited.getTotalQuantity();
                int available = edited.getAvailableQuantity();
                if (available > total) {
                    available = total;
                }
                db.updateBook(
                        selected.getId(),
                        edited.getTitle(),
                        edited.getAuthor(),
                        edited.getIsbn(),
                        edited.getYear(),
                        total,
                        available
                );
                refreshTable();
            }
        }
    }

    private void onDelete() {
        Book selected = getSelectedBookByTitleAuthor();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecione um livro na tabela.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(
                this,
                "Excluir o livro \"" + selected.getTitle() + "\"?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION
        );
        if (ok == JOptionPane.YES_OPTION) {
            db.deleteBook(selected.getId());
            refreshTable();
        }
    }
}
