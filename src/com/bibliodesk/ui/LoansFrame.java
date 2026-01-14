package com.bibliodesk.ui;

import com.bibliodesk.app.Database;
import com.bibliodesk.model.Book;
import com.bibliodesk.model.Loan;
import com.bibliodesk.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LoansFrame extends JFrame {

    private final Database db;
    private final DashboardFrameForm dashboard;

    private JComboBox<User> cbUser;
    private JComboBox<Book> cbBook;
    private JTextField tfDate;
    private DefaultTableModel model;
    private JTable table;
    private List<Loan> currentLoans;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LoansFrame(Database db, DashboardFrameForm dashboard) {
        super("BiblioDesk - Empréstimos/Devolução");
        this.db = db;
        this.dashboard = dashboard;
        initUI();
        onShow();
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

        // deixa a janela mais larga e impede que fique menor que isso,
        // para não esconder o botão "Registrar empréstimo"
        setSize(1050, 600);
        setMinimumSize(new Dimension(1050, 600));
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ---- topo: título + formulário de empréstimo ----
        JLabel title = new JLabel("Empréstimos / Devoluções");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.NORTH);

        // formulário superior
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.add(new JLabel("Usuário:"));
        cbUser = new JComboBox<>();
        cbUser.setPreferredSize(new Dimension(180, 26));
        form.add(cbUser);

        form.add(new JLabel("Livro:"));
        cbBook = new JComboBox<>();
        cbBook.setPreferredSize(new Dimension(220, 26));
        form.add(cbBook);

        form.add(new JLabel("Data (dd/MM/aaaa):"));
        tfDate = new JTextField(8);
        form.add(tfDate);

        JButton btEmprestar = new JButton("Registrar empréstimo");
        btEmprestar.addActionListener(e -> registerLoan());
        form.add(btEmprestar);

        top.add(form, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        // ---- tabela de empréstimos ----
        model = new DefaultTableModel(
                new Object[]{"Usuário", "Livro", "Data empréstimo", "Devolvido em"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(22);
        content.add(new JScrollPane(table), BorderLayout.CENTER);

        // ---- botões inferiores ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btDevolver = new JButton("Registrar devolução");
        btDevolver.addActionListener(e -> registerReturn());
        JButton btVoltar = new JButton("Voltar");
        btVoltar.addActionListener(e -> {
            dispose();
            if (dashboard != null) {
                dashboard.returnToDashboard();
            }
        });
        bottom.add(btDevolver);
        bottom.add(btVoltar);
        content.add(bottom, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void reloadUsersAndBooks() {
        cbUser.removeAllItems();
        for (User u : db.getUsers()) {
            if (u.isActive()) {
                cbUser.addItem(u);
            }
        }
        cbBook.removeAllItems();
        for (Book b : db.getBooks()) {
            if (b.getAvailableQuantity() > 0) {
                cbBook.addItem(b);
            }
        }
    }

    private void reloadLoansTable() {
        List<Loan> loans = db.getLoans();
        currentLoans = loans;
        model.setRowCount(0);
        for (Loan l : loans) {
            String dataEmp = l.getLoanDate().format(fmt);
            String dataDev = l.isReturned() && l.getReturnDate() != null
                    ? l.getReturnDate().format(fmt)
                    : "-";
            model.addRow(new Object[]{
                    l.getUser().getName(),
                    l.getBook().getTitle(),
                    dataEmp,
                    dataDev
            });
        }
    }

    public void onShow() {
        reloadUsersAndBooks();
        reloadLoansTable();
        if (tfDate.getText().isBlank()) {
            tfDate.setText(LocalDate.now().format(fmt));
        }
    }

    private LocalDate parseDateField() {
        String text = tfDate.getText().trim();
        if (text.isEmpty()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(text, fmt);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Data inválida. Use o formato dd/MM/aaaa.");
            return null;
        }
    }

    private void registerLoan() {
        User u = (User) cbUser.getSelectedItem();
        Book b = (Book) cbBook.getSelectedItem();
        if (u == null || b == null) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário e um livro.");
            return;
        }
        LocalDate date = parseDateField();
        if (date == null) return;
        try {
            db.addLoan(u, b, date);
            JOptionPane.showMessageDialog(this, "Empréstimo registrado com sucesso!");
            reloadUsersAndBooks();
            reloadLoansTable();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void registerReturn() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentLoans.size()) {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo na tabela.");
            return;
        }
        Loan loan = currentLoans.get(row);
        if (loan.isReturned()) {
            JOptionPane.showMessageDialog(this, "Este empréstimo já foi devolvido.");
            return;
        }
        LocalDate date = LocalDate.now();
        db.returnLoan(loan, date);
        JOptionPane.showMessageDialog(this, "Devolução registrada com sucesso!");
        reloadUsersAndBooks();
        reloadLoansTable();
    }
}
