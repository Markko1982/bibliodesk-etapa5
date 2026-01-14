package com.bibliodesk.dao;

import com.bibliodesk.model.Profile;
import com.bibliodesk.model.User;
import com.bibliodesk.util.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private User map(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String login = rs.getString("login");
        String senhaHash = rs.getString("senha_hash");
        boolean ativo = rs.getInt("ativo") == 1;

        String perfilNome = rs.getString("perfil_nome"); // vem do JOIN
        Profile profile = Profile.valueOf(perfilNome);

        return new User(id, nome, login, senhaHash, profile, ativo);
    }

    public Optional<User> authenticate(String login, String passwordHash) {
        String sql = """
            SELECT u.id, u.nome, u.login, u.senha_hash, u.ativo, p.nome AS perfil_nome
            FROM usuario u
            JOIN perfil p ON p.id = u.perfil_id
            WHERE u.ativo = 1 AND u.login = ? AND u.senha_hash = ?
            LIMIT 1
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, passwordHash);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao autenticar usuário no banco: " + e.getMessage(), e);
        }
    }

    public List<User> findAll() {
        String sql = """
            SELECT u.id, u.nome, u.login, u.senha_hash, u.ativo, p.nome AS perfil_nome
            FROM usuario u
            JOIN perfil p ON p.id = u.perfil_id
            ORDER BY u.id
        """;

        List<User> out = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                out.add(map(rs));
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar usuários: " + e.getMessage(), e);
        }
    }

    public User insert(String name, String login, String passwordHash, Profile profile, boolean active) {
        String sql = """
            INSERT INTO usuario (nome, email, telefone, login, senha_hash, perfil_id, ativo)
            VALUES (?, NULL, NULL, ?, ?, (SELECT id FROM perfil WHERE nome = ?), ?)
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, login);
            ps.setString(3, passwordHash);
            ps.setString(4, profile.name());
            ps.setInt(5, active ? 1 : 0);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new User(id, name, login, passwordHash, profile, active);
                }
                throw new RuntimeException("Falha ao obter ID gerado do usuário.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao inserir usuário: " + e.getMessage(), e);
        }
    }

    public void updateWithoutPassword(int id, String name, String login, Profile profile, boolean active) {
        String sql = """
            UPDATE usuario
            SET nome = ?, login = ?, perfil_id = (SELECT id FROM perfil WHERE nome = ?), ativo = ?
            WHERE id = ?
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, login);
            ps.setString(3, profile.name());
            ps.setInt(4, active ? 1 : 0);
            ps.setInt(5, id);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar usuário: " + e.getMessage(), e);
        }
    }

    public void updateWithPassword(int id, String name, String login, String passwordHash, Profile profile, boolean active) {
        String sql = """
            UPDATE usuario
            SET nome = ?, login = ?, senha_hash = ?, perfil_id = (SELECT id FROM perfil WHERE nome = ?), ativo = ?
            WHERE id = ?
        """;

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, login);
            ps.setString(3, passwordHash);
            ps.setString(4, profile.name());
            ps.setInt(5, active ? 1 : 0);
            ps.setInt(6, id);

            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao atualizar usuário (com senha): " + e.getMessage(), e);
        }
    }

    public void setActive(int id, boolean active) {
        String sql = "UPDATE usuario SET ativo = ? WHERE id = ?";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ativar/desativar usuário: " + e.getMessage(), e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM usuario WHERE id = ?";

        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao deletar usuário: " + e.getMessage(), e);
        }
    }
}
