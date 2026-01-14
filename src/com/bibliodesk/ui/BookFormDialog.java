package com.bibliodesk.ui;

import com.bibliodesk.model.Book;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo modal para cadastro e edição de livros. Possui campos para
 * título, autor, ISBN, ano, quantidade total e quantidade disponível.
 * O método {@link #toBook()} retorna uma instância de Book somente
 * quando o usuário confirma e os dados são válidos.
 */
public class BookFormDialog extends JDialog {
    private boolean saved = false;
    private final JTextField titleField = new JTextField(20);
    private final JTextField authorField = new JTextField(20);
    private final JTextField isbnField = new JTextField(15);
    private final JTextField yearField = new JTextField(6);
    private final JTextField totalField = new JTextField(6);
    private final JTextField availableField = new JTextField(6);

    public BookFormDialog(Frame owner, Book existing) {
        super(owner, true);
        setTitle(existing == null ? "Novo livro" : "Editar livro");
        setSize(380, 280);
        setLocationRelativeTo(owner);
        initUI(existing);
    }

    private void initUI(Book existing) {
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(new JLabel("Título:"));
        p.add(titleField);
        p.add(new JLabel("Autor:"));
        p.add(authorField);
        p.add(new JLabel("ISBN:"));
        p.add(isbnField);
        p.add(new JLabel("Ano:"));
        p.add(yearField);
        p.add(new JLabel("Qtd total:"));
        p.add(totalField);
        p.add(new JLabel("Disponível:"));
        p.add(availableField);

        if (existing != null) {
            titleField.setText(existing.getTitle());
            authorField.setText(existing.getAuthor());
            isbnField.setText(existing.getIsbn());
            yearField.setText(String.valueOf(existing.getYear()));
            totalField.setText(String.valueOf(existing.getTotalQuantity()));
            availableField.setText(String.valueOf(existing.getAvailableQuantity()));
        } else {
            yearField.setText(String.valueOf(java.time.LocalDate.now().getYear()));
            totalField.setText("1");
            availableField.setText("1");
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancelar");
        ok.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()
                    || authorField.getText().trim().isEmpty()
                    || isbnField.getText().trim().isEmpty()
                    || yearField.getText().trim().isEmpty()
                    || totalField.getText().trim().isEmpty()
                    || availableField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
                return;
            }
            try {
                int y = Integer.parseInt(yearField.getText().trim());
                int tot = Integer.parseInt(totalField.getText().trim());
                int av = Integer.parseInt(availableField.getText().trim());
                if (tot < 0 || av < 0) {
                    throw new NumberFormatException();
                }
                if (av > tot) {
                    JOptionPane.showMessageDialog(this, "Disponível não pode ser maior que Qtd total.");
                    return;
                }
                saved = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ano/Qtd total/Disponível inválidos.");
            }
        });
        cancel.addActionListener(e -> dispose());
        buttons.add(ok);
        buttons.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(p, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    public boolean isSaved() {
        return saved;
    }

    /**
     * Retorna um Book com ID 0 contendo os dados informados. O ID será atribuído
     * pelo {@link com.bibliodesk.app.Database} no momento do cadastro.
     */
    public Book toBook() {
        if (!saved) return null;
        try {
            int y = Integer.parseInt(yearField.getText().trim());
            int tot = Integer.parseInt(totalField.getText().trim());
            int av = Integer.parseInt(availableField.getText().trim());
            if (av > tot) av = tot;
            return new Book(0,
                    titleField.getText().trim(),
                    authorField.getText().trim(),
                    isbnField.getText().trim(),
                    y,
                    tot,
                    av);
        } catch (Exception e) {
            return null;
        }
    }
}