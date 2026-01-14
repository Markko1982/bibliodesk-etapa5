# BiblioDesk

## Status do projeto
Concluído (Etapa 5 – versionamento no GitHub)

## Tecnologias utilizadas
- Java (Desktop / Swing)
- Apache NetBeans IDE
- MySQL 8
- JDBC (MySQL Connector/J)
- Git e GitHub

## Time de desenvolvedores
- Marcus (Markko1982)

## Objetivo do software
Sistema desktop para gerenciamento de biblioteca, permitindo cadastrar usuários e livros, e registrar empréstimos e devoluções.

## Funcionalidades (requisitos)
- Autenticação de usuários por login e senha
- Cadastro, edição e listagem de usuários
- Cadastro, edição e listagem de livros
- Registro de empréstimos
- Registro de devoluções
- Controle de quantidade disponível de livros
- Consulta/listagem de empréstimos realizados

## Banco de dados (MySQL)
O script para criação das tabelas e inserção dos dados iniciais está em:

`database/01_create_and_seed.sql`

### Como criar e popular o banco
1. Abra o MySQL Workbench
2. Abra o arquivo `database/01_create_and_seed.sql`
3. Execute o script

### Logins de teste
- Admin: `admin` / `admin123`
- Bibliotecário: `biblio` / `biblio123`
- Leitor: `leitor` / `leitor123`

## Observações
- Caso o MySQL da máquina utilize senha diferente, ajuste as credenciais no arquivo:
  `src/com/bibliodesk/util/ConnectionFactory.java`

