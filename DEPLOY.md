# Deploy gratuito do ATLAS

Este guia deixa o projeto pronto para um deploy separado:

- Frontend Angular no Vercel.
- Backend Spring Boot no Render.
- PostgreSQL no Neon.

## 1. Banco PostgreSQL no Neon

Crie um banco no Neon e copie a string JDBC do projeto.

Use formato JDBC no backend:

```text
jdbc:postgresql://HOST/DB?sslmode=require
```

Variaveis para o Render:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST/DB?sslmode=require
SPRING_DATASOURCE_USERNAME=USUARIO_NEON
SPRING_DATASOURCE_PASSWORD=SENHA_NEON
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
```

Para demo publica sem ferramenta de migracao, `update` e a opcao mais pratica: o Hibernate cria/ajusta as tabelas no banco vazio do Neon sem apagar dados. Para um produto real em producao, prefira migrations versionadas e `validate`.

## 2. Backend no Render

O backend possui `Dockerfile` e `render.yaml`.

Variaveis obrigatorias:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
APP_SECURITY_ADMIN_PASSWORD
APP_SECURITY_VIEWER_PASSWORD
APP_CORS_ALLOWED_ORIGINS
```

Variaveis recomendadas:

```text
SPRING_PROFILES_ACTIVE=prod,seed
APP_SECURITY_ADMIN_USERNAME=admin
APP_SECURITY_VIEWER_USERNAME=viewer
APP_SECURITY_SESSION_TTL_MINUTES=120
APP_SOFTWARE_SYNC_MANIFEST_URL=https://softwares.example.invalid/manifest.json
SPRINGDOC_SWAGGER_UI_ENABLED=true
SPRINGDOC_API_DOCS_ENABLED=true
```

Use `prod,seed` no deploy com PostgreSQL para carregar dados ficticios sem trocar o banco para H2. Para producao real sem seed, use apenas `prod`. O perfil `demo` continua reservado para execucao local com H2 em memoria.

`APP_SOFTWARE_SYNC_MANIFEST_URL` e opcional. Quando preenchida, `POST /softwares/sincronizar` consome um manifesto JSON externo real neste formato:

```json
[
  {
    "nome": "Sistema Operacional",
    "descricaoSoftware": "Descricao curta",
    "versaoReferencia": "2026.5",
    "ativo": true
  }
]
```

Configure `APP_CORS_ALLOWED_ORIGINS` com a URL final do frontend no Vercel:

```text
https://SEU-FRONTEND.vercel.app
```

O Spring usa `server.port=${PORT:8091}`, entao funciona com a porta injetada pelo Render e continua rodando localmente em `8091`.

## 3. Frontend no Vercel

No Vercel, use a pasta `frontend` como raiz do projeto.

Configure:

```text
Build Command: npm run build:deploy
Output Directory: dist/atlas-frontend/browser
```

Variavel obrigatoria:

```text
ATLAS_API_URL=https://SUA-API.onrender.com
```

O script `npm run build:deploy` gera `public/atlas-config.js` com a URL da API. Em desenvolvimento, o valor padrao continua sendo `http://localhost:8091`.

## 4. Validacao local

Backend:

```bash
./mvnw test
```

Frontend:

```bash
cd frontend
npm run build
npm run build:deploy
```

## Checklist antes de publicar

- Definir senhas do admin/viewer no Render, nunca no codigo.
- Definir `APP_CORS_ALLOWED_ORIGINS` com a URL real do Vercel.
- Definir `ATLAS_API_URL` no Vercel com a URL real do Render.
- Conferir Swagger em `/swagger-ui.html`.
- Conferir login com `admin` e `viewer`.
