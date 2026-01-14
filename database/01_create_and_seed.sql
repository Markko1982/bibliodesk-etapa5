-- =========================
-- BiblioDesk - Etapa 4 (PI)
-- Banco de dados MySQL + tabelas + dados iniciais
-- =========================

CREATE DATABASE IF NOT EXISTS biblioteca_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE biblioteca_db;

-- Recria do zero para evitar restos de testes
DROP TABLE IF EXISTS emprestimo;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS livro;
DROP TABLE IF EXISTS perfil;

CREATE TABLE perfil (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(30) NOT NULL UNIQUE,
  descricao VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  email VARCHAR(120),
  telefone VARCHAR(30),
  login VARCHAR(50) NOT NULL UNIQUE,
  senha_hash CHAR(64) NOT NULL,
  perfil_id INT NOT NULL,
  ativo TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_usuario_perfil
    FOREIGN KEY (perfil_id) REFERENCES perfil(id)
) ENGINE=InnoDB;

CREATE TABLE livro (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titulo VARCHAR(200) NOT NULL,
  autor VARCHAR(120) NOT NULL,
  isbn VARCHAR(30) NOT NULL UNIQUE,
  ano INT,
  qtd_total INT NOT NULL,
  qtd_disponivel INT NOT NULL,
  CONSTRAINT chk_livro_qtd_total CHECK (qtd_total >= 0),
  CONSTRAINT chk_livro_qtd_disp CHECK (qtd_disponivel >= 0),
  CONSTRAINT chk_livro_qtd_limite CHECK (qtd_disponivel <= qtd_total)
) ENGINE=InnoDB;

CREATE TABLE emprestimo (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  livro_id INT NOT NULL,
  data_emprestimo DATE NOT NULL,
  data_prevista DATE NOT NULL,
  data_devolucao DATE NULL,
  status VARCHAR(20) NOT NULL,
  CONSTRAINT fk_emprestimo_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  CONSTRAINT fk_emprestimo_livro
    FOREIGN KEY (livro_id) REFERENCES livro(id),
  INDEX idx_emp_status (status),
  INDEX idx_emp_usuario (usuario_id),
  INDEX idx_emp_livro (livro_id)
) ENGINE=InnoDB;

-- =========================
-- SEED (dados iniciais)
-- =========================

INSERT INTO perfil (nome, descricao) VALUES
('ADMIN', 'Administrador do sistema'),
('BIBLIOTECARIO', 'Bibliotecário'),
('LEITOR', 'Leitor');

-- Senhas em SHA-256 (64 hex)
-- admin123  -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- biblio123 -> f967a64bffbe3f3a3e4fc807778dda8e965dc26a9b2db20ab705f6bc5543b9d7
-- leitor123 -> 5bb586e91c868fc9a5f274046be699d1ed7059b29e5195025a1882a17831152f

INSERT INTO usuario (nome, email, telefone, login, senha_hash, perfil_id, ativo) VALUES
('Admin', 'admin@local', NULL, 'admin',
 '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
 (SELECT id FROM perfil WHERE nome='ADMIN'), 1),

('Bibliotecário', 'biblio@local', NULL, 'biblio',
 'f967a64bffbe3f3a3e4fc807778dda8e965dc26a9b2db20ab705f6bc5543b9d7',
 (SELECT id FROM perfil WHERE nome='BIBLIOTECARIO'), 1),

('Leitor padrão', 'leitor@local', NULL, 'leitor',
 '5bb586e91c868fc9a5f274046be699d1ed7059b29e5195025a1882a17831152f',
 (SELECT id FROM perfil WHERE nome='LEITOR'), 1);

INSERT INTO livro (titulo, autor, isbn, ano, qtd_total, qtd_disponivel) VALUES
('Clean Code', 'Robert C. Martin', '9780132350884', 2008, 5, 3),
('Domain-Driven Design', 'Eric Evans', '9780321125217', 2003, 3, 2),
('Effective Java', 'Joshua Bloch', '9780134685991', 2018, 14, 4);

-- conferência rápida
SELECT * FROM perfil;
SELECT id, nome, login, ativo, perfil_id FROM usuario;
SELECT id, titulo, qtd_total, qtd_disponivel FROM livro;
