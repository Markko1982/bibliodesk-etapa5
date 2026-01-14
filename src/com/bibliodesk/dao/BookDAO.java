package com.bibliodesk.dao;

import com.bibliodesk.model.Book;
import com.bibliodesk.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDAO {

    private Book map(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        String title = rs.getString("titulo");
        String author = rs.getString("autor");
        String isbn = rs.getString("isbn");
        int year = rs.getInt("ano");
        int total = rs.getInt("qtd_total");
        int available = rs.getInt("qtd_disponivel");

        return new Book(id, title, author, isbn, year, total, available);
    }

    public List<Book> findAll() {
        String sql = """
            SELECT id, titulo, autor, isbn, ano, qtd_total, qtd_disponivel
            FROM livro
            ORDER BY id
        """;

        List<Book> out = new ArrayList<>();

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar livros: " + e.getMessage(), e);
        }
    }

    public Optional<Book> findById(int id) {
        String sql = """
            SELECT id, titulo, autor, isbn, ano, qtd_total, qtd_disponivel
            FROM livro
            WHERE id = ?
            LIMIT 1
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar livro por id: " + e.getMessage(), e);
        }
    }

    public Book insert(String title, String author, String isbn, int year, int totalQty, int availableQty) {
        if (availableQty > totalQty) availableQty = totalQty;

        String sql = """
            INSERT INTO livro (titulo, autor, isbn, ano, qtd_total, qtd_disponivel)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setInt(4, year);
            ps.setInt(5, totalQty);
            ps.setInt(6, availableQty);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new Book(id, title, author, isbn, year, totalQty, availableQty);
                }
                throw new RuntimeException("Falha ao obter ID gerado do livro.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir livro: " + e.getMessage(), e);
        }
    }

    public void update(int id, String title, String author, String isbn, int year, int totalQty, int availableQty) {
        if (availableQty > totalQty) availableQty = totalQty;

        String sql = """
            UPDATE livro
            SET titulo = ?, autor = ?, isbn = ?, ano = ?, qtd_total = ?, qtd_disponivel = ?
            WHERE id = ?
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, isbn);
            ps.setInt(4, year);
            ps.setInt(5, totalQty);
            ps.setInt(6, availableQty);
            ps.setInt(7, id);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar livro: " + e.getMessage(), e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM livro WHERE id = ?";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar livro: " + e.getMessage(), e);
        }
    }

    // usado em empréstimo: só decrementa se tiver disponível
    public boolean decrementAvailableIfPossible(int bookId) {
        String sql = """
            UPDATE livro
            SET qtd_disponivel = qtd_disponivel - 1
            WHERE id = ? AND qtd_disponivel > 0
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            return rows == 1;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao decrementar disponibilidade: " + e.getMessage(), e);
        }
    }

    // usado em devolução: só incrementa se não ultrapassar total
    public boolean incrementAvailableIfPossible(int bookId) {
        String sql = """
            UPDATE livro
            SET qtd_disponivel = qtd_disponivel + 1
            WHERE id = ? AND qtd_disponivel < qtd_total
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            return rows == 1;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao incrementar disponibilidade: " + e.getMessage(), e);
        }
    }
}
