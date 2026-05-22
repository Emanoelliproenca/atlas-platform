# ATLAS

Plataforma full stack ficticia da Atlas Labs para gestao operacional de contratos SaaS, servicos digitais, relacionamentos, documentacao e repasses internos.

Este repositorio e uma versao publica e ficticia criada para portfolio. Todos os nomes de organizacoes, contratos, servicos, URLs, arquivos, credenciais e dados demonstrativos foram inventados para simular um produto corporativo real sem expor informacoes de empresas ou ambientes internos.

## Problema Resolvido

Times de implantacao, suporte e operacoes corporativas costumam lidar com contratos, servicos contratados, versoes implantadas, setores responsaveis, documentacoes e repasses operacionais espalhados em planilhas, mensagens e ferramentas diferentes.

O ATLAS centraliza essa leitura em uma plataforma unica, permitindo consultar rapidamente quais servicos estao vinculados a cada contrato, quais versoes estao em uso, quais setores respondem por cada operacao e quais documentos sustentam o atendimento.

## Principais Funcionalidades

- Autenticacao com token Bearer e controle de sessao.
- Perfis de acesso para administrador e visualizador.
- CRUD de contratos SaaS com ativacao e inativacao logica.
- CRUD de servicos digitais e softwares associados.
- Gestao de relacionamentos entre contratos e servicos.
- Painel operacional com resumo, filtros e tabela consolidada.
- Filtros por contrato, servico, grupo, setor e status.
- Upload, abertura e download de arquivos vinculados a contratos.
- Importacao de planilhas Excel para contratos, servicos e relacionamentos.
- Area de repasses com orientacoes operacionais.
- Swagger/OpenAPI para exploracao da API.
- Testes automatizados no backend e frontend.

## Stack Utilizada

**Backend**

- Java 17
- Spring Boot 4
- Spring Web
- Spring Security
- Spring Data JPA
- Bean Validation
- PostgreSQL
- H2 para ambiente demo/testes
- Apache POI para leitura de Excel
- Springdoc OpenAPI / Swagger
- JUnit, Mockito e MockMvc

**Frontend**

- Angular 21
- TypeScript
- Angular Router
- Angular Forms
- HttpClient com interceptor de autenticacao
- Signals para estado local
- SCSS
- Vitest

## Arquitetura Resumida

```text
frontend/
  Angular SPA
  Rotas, componentes, services, guards e interceptor HTTP

src/main/java/com/atlas/platform/
  controller/   Endpoints REST
  service/      Regras de negocio e orquestracao
  repository/   Acesso a dados com Spring Data JPA
  model/        Entidades persistidas
  dto/          Contratos de entrada e saida da API
  mapper/       Conversao entre entidades e DTOs
  security/     Token de sessao e filtro Bearer
  config/       CORS, seguranca, OpenAPI e dados demo
```

O backend expoe uma API REST stateless protegida por token. O frontend consome essa API via `HttpClient`, injeta o token pelo interceptor e separa telas publicas, autenticadas e administrativas por rota/guard.

## Perfis de Acesso

| Perfil | Usuario | Senha | Permissoes |
| --- | --- | --- | --- |
| Administrador | `admin` | `admin123` no ambiente local/demo ou defina em `APP_SECURITY_ADMIN_PASSWORD` | Consulta, cria, edita, ativa/inativa e importa dados |
| Visualizador | `viewer` | `viewer123` no ambiente local/demo ou defina em `APP_SECURITY_VIEWER_PASSWORD` | Consulta paineis, detalhes, softwares e repasses |

## Como Rodar o Backend Demo

Requisitos:

- Java 17

Na raiz do projeto:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.main-class=com.atlas.platform.AtlasApplication
```

Backend demo:

```text
http://localhost:8091
```

Swagger:

```text
http://localhost:8091/swagger-ui.html
```

O perfil `demo` usa banco H2 em memoria e carrega dados ficticios automaticamente.

## Como Rodar o Frontend

Requisitos:

- Node.js 20+
- npm 10+

Em outro terminal:

```bash
cd frontend
npm install
npm start
```

Frontend:

```text
http://127.0.0.1:4311
```

## Deploy Gratuito

O projeto inclui configuracoes para deploy separado em Vercel, Render e Neon.

- Frontend: `frontend/vercel.json`
- Backend: `Dockerfile` e `render.yaml`
- Guia completo: `DEPLOY.md`

Variaveis principais:

```text
ATLAS_API_URL=https://sua-api.onrender.com
APP_CORS_ALLOWED_ORIGINS=https://seu-frontend.vercel.app
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST/DB?sslmode=require
SPRING_DATASOURCE_USERNAME=USUARIO
SPRING_DATASOURCE_PASSWORD=SENHA
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
APP_SECURITY_ADMIN_PASSWORD=SENHA_ADMIN
APP_SECURITY_VIEWER_PASSWORD=SENHA_VIEWER
SPRING_PROFILES_ACTIVE=seed
```

## Rodar com PostgreSQL

Para executar fora do modo demo, configure um PostgreSQL local e defina as variaveis:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/atlas_platform
export SPRING_DATASOURCE_USERNAME=atlas_app
export SPRING_DATASOURCE_PASSWORD=defina-uma-senha-local

export APP_SECURITY_ADMIN_USERNAME=admin
export APP_SECURITY_ADMIN_PASSWORD=defina-uma-senha-admin
export APP_SECURITY_VIEWER_USERNAME=viewer
export APP_SECURITY_VIEWER_PASSWORD=defina-uma-senha-viewer
export APP_SECURITY_SESSION_TTL_MINUTES=120
```

Depois:

```bash
./mvnw spring-boot:run
```

Backend padrao:

```text
http://localhost:8080
```

## Testes

Backend:

```bash
./mvnw test
```

Frontend:

```bash
cd frontend
npm test -- --watch=false
```

## Diferenciais Tecnicos

- Projeto full stack com separacao clara entre API REST e SPA Angular.
- Autenticacao propria com Bearer token, expiracao de sessao e persistencia segura do hash do token no backend.
- Controle de autorizacao por perfil, com permissoes diferentes para leitura e administracao.
- CRUD completo com validacoes, tratamento global de erros e respostas padronizadas.
- Modelagem relacional entre contratos, servicos, arquivos, repasses e sessoes autenticadas.
- Painel operacional com filtros combinados e agregacoes de resumo.
- Upload/download de arquivos usando endpoint dedicado e metadados persistidos.
- Importacao Excel com validacao de abas, linhas e relacionamentos.
- Ambiente demo independente de PostgreSQL usando H2 em memoria.
- Cobertura de testes em services, controllers, componentes e services Angular.
- README e dados preparados para publicacao em GitHub sem informacoes reais.

## Proximos Passos Planejados

- Adicionar paginacao e ordenacao server-side nas listagens administrativas.
- Evoluir importacao Excel com pre-visualizacao e relatorio detalhado antes da gravacao.
- Adicionar historico de alteracoes por contrato e relacionamento.
- Implementar dashboard com graficos de distribuicao por grupo, setor e servico.
- Criar pipeline CI para build, testes backend e testes frontend.
- Adicionar testes end-to-end para fluxo de login, consulta e cadastro.
- Externalizar anexos para storage dedicado em ambiente de producao.

## Seguranca e Dados Publicos

- Os dados demo usam dominios ficticios/reservados e textos genericos.
- O projeto nao contem dados reais de organizacoes, empresas, contratos, planilhas ou anexos.
- O frontend nao persiste senha nem token em `localStorage`.
- A sessao do usuario fica apenas em `sessionStorage`.
- O backend persiste somente o hash do token de sessao, nao o token bruto entregue ao navegador.
