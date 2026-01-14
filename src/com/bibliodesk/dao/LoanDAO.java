package com.bibliodesk.dao;

import com.bibliodesk.model.Book;
import com.bibliodesk.model.Loan;
import com.bibliodesk.model.Profile;
import com.bibliodesk.model.User;
import com.bibliodesk.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    public List<Loan> findAll() {
        String sql = """
            SELECT
                e.id AS emp_id,
                e.data_emprestimo,
                e.data_devolucao,

                u.id AS user_id,
                u.nome AS user_nome,
                u.login AS user_login,
                u.senha_hash,
                u.ativo,
                p.nome AS perfil_nome,

                b.id AS book_id,
                b.titulo,
                b.autor,
                b.isbn,
                b.ano,
                b.qtd_total,
                b.qtd_disponivel
            FROM emprestimo e
            JOIN usuario u ON u.id = e.usuario_id
            JOIN perfil p ON p.id = u.perfil_id
            JOIN livro b ON b.id = e.livro_id
            ORDER BY e.id DESC
        """;

        List<Loan> out = new ArrayList<>();

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // User completo (o model exige todos os campos)
                Profile profile = Profile.valueOf(rs.getString("perfil_nome"));
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("user_nome"),
                        rs.getString("user_login"),
                        rs.getString("senha_hash"),
                        profile,
                        rs.getInt("ativo") == 1
                );

                // Book completo
                Book book = new Book(
                        rs.getInt("book_id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        rs.getInt("ano"),
                        rs.getInt("qtd_total"),
                        rs.getInt("qtd_disponivel")
                );

                LocalDate loanDate = rs.getDate("data_emprestimo").toLocalDate();
                Loan loan = new Loan(rs.getInt("emp_id"), user, book, loanDate);

                Date dev = rs.getDate("data_devolucao");
                if (dev != null) {
                    loan.setReturnDate(dev.toLocalDate());
                }

                out.add(loan);
            }

            return out;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar empréstimos: " + e.getMessage(), e);
        }
    }

    public Loan createLoan(User user, Book book, LocalDate loanDate) {
        String sqlDec = """
            UPDATE livro
            SET qtd_disponivel = qtd_disponivel - 1
            WHERE id = ? AND qtd_disponivel > 0
        """;

        String sqlIns = """
            INSERT INTO emprestimo (usuario_id, livro_id, data_emprestimo, data_prevista, data_devolucao, status)
            VALUES (?, ?, ?, ?, NULL, ?)
        """;

        LocalDate dueDate = loanDate.plusDays(7); // simples e suficiente para preencher data_prevista
        String status = "ABERTO";

        try (Connection c = ConnectionFactory.getConnection()) {
            c.setAutoCommit(false);

            // 1) decrementar disponibilidade (só se tiver > 0)
            try (PreparedStatement psDec = c.prepareStatement(sqlDec)) {
                psDec.setInt(1, book.getId());
                int rows = psDec.executeUpdate();
                if (rows != 1) {
                    c.rollback();
                    throw new IllegalStateException("Livro indisponível.");
                }
            }

            // 2) inserir empréstimo
            int newId;
            try (PreparedStatement psIns = c.prepareStatement(sqlIns, Statement.RETURN_GENERATED_KEYS)) {
                psIns.setInt(1, user.getId());
                psIns.setInt(2, book.getId());
                psIns.setDate(3, Date.valueOf(loanDate));
                psIns.setDate(4, Date.valueOf(dueDate));
                psIns.setString(5, status);

                psIns.executeUpdate();

                try (ResultSet keys = psIns.getGeneratedKeys()) {
                    if (!keys.next()) {
                        c.rollback();
                        throw new RuntimeException("Falha ao obter ID do empréstimo.");
                    }
                    newId = keys.getInt(1);
                }
            }

            c.commit();
            return new Loan(newId, user, book, loanDate);

        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar empréstimo: " + e.getMessage(), e);
        }
    }

    public void registerReturn(int loanId, int bookId, LocalDate returnDate) {
        String sqlUpd = """
            UPDATE emprestimo
            SET data_devolucao = ?, status = ?
            WHERE id = ? AND data_devolucao IS NULL
        """;

        String sqlInc = """
            UPDATE livro
            SET qtd_disponivel = qtd_disponivel + 1
            WHERE id = ? AND qtd_disponivel < qtd_total
        """;

        try (Connection c = ConnectionFactory.getConnection()) {
            c.setAutoCommit(false);

            int updated;
            try (PreparedStatement ps = c.prepareStatement(sqlUpd)) {
                ps.setDate(1, Date.valueOf(returnDate));
                ps.setString(2, "DEVOLVIDO");
                ps.setInt(3, loanId);
                updated = ps.executeUpdate();
            }

            // se já estava devolvido, não faz nada
            if (updated == 1) {
                try (PreparedStatement ps2 = c.prepareStatement(sqlInc)) {
                    ps2.setInt(1, bookId);
                    ps2.executeUpdate();
                }
            }

            c.commit();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao registrar devolução: " + e.getMessage(), e);
        }
    }
}
