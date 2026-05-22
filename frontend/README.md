# ATLAS Frontend

Frontend Angular do ATLAS, uma plataforma ficticia de portfolio para gestao operacional de contratos SaaS e servicos digitais.

## Rodar localmente

```bash
npm install
npm start
```

O frontend sobe em `http://127.0.0.1:4311` e espera o backend demo em `http://localhost:8091`.

## Backend demo

Na raiz do projeto:

```bash
export APP_SECURITY_ADMIN_PASSWORD=escolha-uma-senha-demo-admin
export APP_SECURITY_VIEWER_PASSWORD=escolha-uma-senha-demo-viewer
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.main-class=com.atlas.platform.AtlasApplication
```

Credenciais demo:

- Admin: `admin` / valor de `APP_SECURITY_ADMIN_PASSWORD`
- Visualizador: `viewer` / valor de `APP_SECURITY_VIEWER_PASSWORD`

## Testes

```bash
npm test -- --watch=false
```

Todos os textos e dados usados pelo frontend sao ficticios e seguros para demonstracao publica.
