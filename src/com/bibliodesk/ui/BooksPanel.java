package com.bibliodesk.ui;

import com.bibliodesk.app.Database;
import com.bibliodesk.model.Book;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Tela de manutenção de livros.
 */
public class BooksPanel extends JPanel implements Refreshable {

    private final Database db;
    private final ScreenNavigator nav;

    private final JTextField searchField = new JTextField(25);
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{ "Título", "Autor", "ISBN", "Ano", "Qtd total", "Disponível" }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public BooksPanel(Database db, ScreenNavigator nav) {
        this.db = db;
        this.nav = nav;
        buildUI();
        refreshTable();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // topo com título e busca
        JLabel title = new JLabel("Livros");
        title.setFont(title.getFont().deriveFont(22f));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topLeft.add(title);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.add(new JLabel("Buscar:"));
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

        JPanel top = new JPanel(new BorderLayout());
        top.add(topLeft, BorderLayout.WEST);
        top.add(topRight, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // tabela
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // botões inferiores
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btNew = new JButton("Novo");
        JButton btEdit = new JButton("Editar");
        JButton btDelete = new JButton("Excluir");
        JButton btBack = new JButton("Voltar");

        btNew.addActionListener(e -> onNew());
        btEdit.addActionListener(e -> onEdit());
        btDelete.addActionListener(e -> onDelete());
        btBack.addActionListener(e -> nav.showScreen("dashboard"));

        bottom.add(btNew);
        bottom.add(btEdit);
        bottom.add(btDelete);
        bottom.add(btBack);

        add(bottom, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        String q = searchField.getText().trim().toLowerCase();
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
        if (row < 0) {
            return null;
        }
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
        BookForm form = new BookForm(null);
        int res = JOptionPane.showConfirmDialog(
                this, form, "Novo livro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        Book b = form.toBook();
        if (b == null) {
            return;
        }

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

    private void onEdit() {
        Book selected = getSelectedBookByTitleAuthor();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Selecione um livro na tabela.");
            return;
        }

        BookForm form = new BookForm(selected);
        int res = JOptionPane.showConfirmDialog(
                this, form, "Editar livro",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        Book edited = form.toBook();
        if (edited == null) {
            return;
        }

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
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }

        db.deleteBook(selected.getId());
        refreshTable();
    }

    @Override
    public void onShow() {
        refreshTable();
    }

    // --------- Form interno ---------

    private static class BookForm extends JPanel {
        private final JTextField title = new JTextField(20);
        private final JTextField author = new JTextField(20);
        private final JTextField isbn = new JTextField(15);
        private final JTextField year = new JTextField(6);
        private final JTextField total = new JTextField(6);
        private final JTextField available = new JTextField(6);

        BookForm(Book b) {
            setLayout(new GridLayout(0, 2, 8, 8));

            add(new JLabel("Título:"));
            add(title);
            add(new JLabel("Autor:"));
            add(author);
            add(new JLabel("ISBN:"));
            add(isbn);
            add(new JLabel("Ano:"));
            add(year);
            add(new JLabel("Qtd total:"));
            add(total);
            add(new JLabel("Disponível:"));
            add(available);

            if (b != null) {
                title.setText(b.getTitle());
                author.setText(b.getAuthor());
                isbn.setText(b.getIsbn());
                year.setText(String.valueOf(b.getYear()));
                total.setText(String.valueOf(b.getTotalQuantity()));
                available.setText(String.valueOf(b.getAvailableQuantity()));
            } else {
                year.setText("2025");
                total.setText("1");
                available.setText("1");
            }
        }

        Book toBook() {
            String t = title.getText().trim();
            String a = author.getText().trim();
            String i = isbn.getText().trim();
            String yStr = year.getText().trim();
            String totStr = total.getText().trim();
            String avStr = available.getText().trim();

            if (t.isEmpty() || a.isEmpty() || i.isEmpty()
                    || yStr.isEmpty() || totStr.isEmpty() || avStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                return null;
            }

            try {
                int y = Integer.parseInt(yStr);
                int tot = Integer.parseInt(totStr);
                int av = Integer.parseInt(avStr);

                if (tot < 0 || av < 0) {
                    throw new NumberFormatException();
                }
                if (av > tot) {
                    JOptionPane.showMessageDialog(this, "Disponível não pode ser maior que Qtd total.");
                    return null;
                }

                return new Book(0, t, a, i, y, tot, av);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ano/Qtd total/Disponível inválidos.");
                return null;
            }
        }
    }
}
