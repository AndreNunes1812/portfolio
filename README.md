# Portfolio Backend

API REST para gestão de portfólio de projetos: cadastro de projetos com membros e gerente, transição de status, exclusão condicionada, mock de API externa de membros e relatório agregado do portfólio.

## Stack

| Tecnologia | Uso |
|------------|-----|
| **Java 21** | Linguagem |
| **Spring Boot 3.2.5** | Aplicação, Web, JPA, Validação, Segurança |
| **PostgreSQL** | Banco de dados relacional |
| **Flyway** | Migrações de schema (`classpath:db/migration`) |
| **SpringDoc OpenAPI 2.5** | Documentação OpenAPI 3 e Swagger UI |
| **Lombok** | Redução de boilerplate em entidades e serviços |
| **JaCoCo** | Cobertura de testes (regra mínima de **70%** de linhas no pacote `br.com.portfolio.service`) |

## Pré-requisitos

- **JDK 21** (compatível com o `java.version` do `pom.xml`)
- **Maven 3.8+**
- **PostgreSQL** acessível com banco e usuário configurados (veja [Configuração do banco](#configuração-do-banco))

## Configuração do banco

O datasource padrão está em `src/main/resources/application.yml`:

- URL JDBC: `jdbc:postgresql://localhost:5432/dbportfolio`
- Usuário e senha conforme o arquivo (ajuste para seu ambiente local ou produção).

**Recomendação:** em produção, não versionar credenciais; use variáveis de ambiente ou um segredo externo e sobrescreva `spring.datasource.*` conforme a [documentação do Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html).

Crie o banco e o usuário no PostgreSQL, por exemplo:

```sql
CREATE USER portfolio WITH PASSWORD 'sua_senha';
CREATE DATABASE dbportfolio OWNER portfolio;
```

As tabelas são criadas pelo **Flyway** na subida da aplicação (`V2__portfolio_schema.sql`: `membro`, `projeto`, `projeto_membro`).

## Como executar

Na raiz do projeto (`backend/`):

```bash
mvn spring-boot:run
```

A API sobe em **http://localhost:8080** (porta definida em `server.port`).

### Perfis Spring

Existem arquivos opcionais:

- `application-dev.yml` — mais log (`DEBUG`) e SQL Hibernate visível
- `application-prod.yml` — níveis de log mais contidos

Ative um perfil, por exemplo:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Autenticação

A API usa **HTTP Basic** (Spring Security):

- Usuário: `admin`
- Senha: `admin` (codificada com `{noop}` no `InMemoryUserDetailsManager`)

Rotas públicas (sem credenciais): `/`, Swagger/OpenAPI e `/error`. Demais endpoints exigem o header `Authorization: Basic ...`.

O Swagger UI permite informar usuário e senha para testar os endpoints protegidos.

## Documentação interativa (OpenAPI)

| Recurso | Caminho |
|---------|---------|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

A descrição da API (título, versão, esquema `basicAuth`) está em `OpenApiConfig`.

## Endpoints principais

Base URL: `http://localhost:8080`

### Raiz

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/` | Metadados do serviço (nome, link da documentação, tipo de auth) |

### Projetos (`/api/v1/projetos`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/v1/projetos` | Criar projeto (status inicial em análise; regras de membros/gerente) |
| GET | `/api/v1/projetos` | Listar com paginação; filtros opcionais `nome` (contém) e `status` |
| GET | `/api/v1/projetos/{id}` | Buscar por id (inclui classificação de risco calculada) |
| PUT | `/api/v1/projetos/{id}` | Atualizar projeto |
| PATCH | `/api/v1/projetos/{id}/status` | Alterar status (sequência ou cancelamento, conforme regras de negócio) |
| DELETE | `/api/v1/projetos/{id}` | Excluir (bloqueado para certos status, ex.: iniciado, em andamento, encerrado) |

Paginação padrão: `size=20`, ordenação padrão por `id` (via `Pageable` do Spring Data).

### Membros — API externa mockada (`/api/external/members`)

Simula integração externa para cadastro e consulta de membros (nome e atribuição), usados na composição de projetos.

| Método | Caminho | Descrição |
|--------|---------|-----------|
| POST | `/api/external/members` | Criar membro |
| GET | `/api/external/members` | Listar com paginação |
| GET | `/api/external/members/{id}` | Buscar por id |

### Relatórios (`/api/v1/relatorios`)

| Método | Caminho | Descrição |
|--------|---------|-----------|
| GET | `/api/v1/relatorios/portfolio` | Resumo: quantidades e orçamento por status, média de duração dos encerrados, total de membros únicos alocados |

## Domínio — status do projeto

Ordem lógica dos valores em `StatusProjeto`:

1. `EM_ANALISE` → `ANALISE_REALIZADA` → `ANALISE_APROVADA` → `INICIADO` → `PLANEJADO` → `EM_ANDAMENTO` → `ENCERRADO`
2. `CANCELADO` em fluxos específicos (regras implementadas em `ProjetoService` / `StatusProjetoValidator`)

Exclusão de projeto não é permitida nos status `INICIADO`, `EM_ANDAMENTO` e `ENCERRADO`.

## Testes e qualidade

```bash
mvn test
```

Após os testes, o relatório JaCoCo fica em `target/site/jacoco/index.html`. O build falha no goal `jacoco:check` se a cobertura de **linhas** do pacote `br.com.portfolio.service` ficar abaixo de **70%**.

## Estrutura do código (visão geral)

```
src/main/java/br/com/portfolio/
├── PortfolioApplication.java      # Entry point
├── config/                        # Security, OpenAPI
├── controller/                    # REST: projetos, membros externos, relatórios, raiz
├── domain/                        # Entidades JPA e enums
├── dto/                           # Request/response
├── exception/                     # Exceções e handler global
├── repository/                    # Spring Data JPA (+ specifications)
└── service/                       # Regras de negócio e mapeamento

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/                  # Scripts Flyway
```

## Build do JAR

```bash
mvn -DskipTests package
```

Artefato gerado: `target/portfolio-backend-1.0.0.jar` (versão conforme `pom.xml`).

```bash
java -jar target/portfolio-backend-1.0.0.jar
```

## Licença e versão

Versão da API documentada no OpenAPI: **1.0.0** (`artifactId` / `version` no `pom.xml`). Ajuste a licença do repositório conforme a política do seu projeto, se aplicável.
