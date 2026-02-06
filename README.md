# 🧱 InfraStore (Estudo) — Spring Boot + Java + PostgreSQL + Gateway + HTML/JS + Tailwind

Este repositório é um ambiente **educativo** que simula um ecossistema de uma loja, com uma base sólida (backend + gateway + frontend) e uma camada avançada de **QA e Segurança (DevSecOps)**.

> ⚠️ **Não é produção.** Use apenas para estudo/lab. Evite expor serviços publicamente sem hardening e sem necessidade.
---

## ✅ Stack Base
### Backend (Store API)
- **Java + Spring Boot** (API e regras de negócio)

### Gateway (gateway-service)
- **Spring Cloud Gateway / Gateway Service** (borda do sistema)
  - Centraliza rotas para a API
  - Pode concentrar políticas de CORS, headers, rate limit e validações de borda (quando aplicável)

### Banco de Dados
- **PostgreSQL** (persistência)

### Frontend
- **HTML + JavaScript** (telas e consumo da API)
- **Tailwind CSS** (UI rápida, consistente e com pouco CSS manual)

---

## 🧩 Estrutura do Ecossistema (alto nível)
- `Store/` → API principal (Spring Boot + Postgres)
- `gateway-service/` → gateway / roteamento (borda)
- `nginx/` (se aplicável) → entrega do frontend e proxy (quando usado)
- `docker-compose.yml` → sobe o ecossistema de estudo
- `prints/` → evidências (dashboards/achados)

---

## 🔒 Camada Avançada: QA + Segurança (DevSecOps)

### 🔍 SAST — Auditoria e Análise Estática
- **Maven PMD Plugin**: auditoria de qualidade e más práticas no código Java  
  - *Priority 3 (média)*: robustez (ex.: variáveis mortas, `catch` vazio)  
  - *Priority 4 (baixa)*: legibilidade/polimento (ex.: imports inúteis, FQNs redundantes)
- **JaCoCo + SonarQube**: qualidade centralizada e **Security Hotspots** (Sonar padrão na porta **9000**)

📌 Nota técnica: atualmente o projeto **não possui testes unitários**. O foco do estudo é:
1) estabilizar API/arquitetura  
2) depois evoluir cobertura e testes unitários

---

### 🛡️ DAST — Testes Ofensivos
- **OWASP ZAP**: scans para identificar riscos de borda em:
  - `gateway-service` (camada de entrada)
  - frontend (headers/políticas)
  - API (inputs/validações)
- Aprendizados aplicados:
  - **Sanitização de dados no backend** (ex.: impedir payload/link malicioso em campos de input)
  - **Hardening de borda**: ausência de **CSP**, proteção contra **Clickjacking** e headers de segurança

---

## ⚙️ Automação
- **Docker**: ambiente reproduzível para QA (uso de `--no-cache` para reduzir interferência de cache)
- **Robot Framework (Web/UI)**: validação de fluxos e regras de negócio  
  - Observação: testes em modo **visual**, pois headless apresentou inconsistências (prioridade: fidelidade do cenário)

---

## ☁️ Cloudflare Tunnel (opcional)
Este ambiente pode estar configurado com **Cloudflare Tunnel** para acesso externo (ex.: demos e testes controlados), normalmente expondo a borda (Nginx/Gateway).

- Se **você NÃO quiser abrir acesso via tunnel**, **remova do `docker-compose.yml`** (ou do `.yml` da infra) o serviço/trecho relacionado ao **cloudflared** (tunnel) e/ou as variáveis associadas.
- Recomendação: mantenha o tunnel apenas quando necessário e, se possível, use allowlist/restrições.

---

## 🧰 Requisitos
- **RAM:** mínimo 8GB  
  > necessário para segurar **Docker + PostgreSQL + SonarQube + varreduras Maven** simultaneamente.

---

## 🔐 Variáveis de Ambiente — `.env.example`

Para manter o projeto reproduzível e evitar vazamento de credenciais, existe um arquivo modelo **`.env.example`**.  
Copie para `.env.example` e preencha localmente, em cada pasta existe a configuração que deve ficar o .env oficial:

```bash
cp .env.example .env
Exemplo de .env.example (sem segredos)
# Credenciais do Banco Docker
DB_NAME=storedb
DB_USER=storeuser
DB_PASSWORD=
DB_ROOT_PASSWORD=

# Segurança
JWT_SECRET=
JWT_EXPIRATION=

# E-mail (opcional)
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

# Credenciais do Banco Local (opcional)
LOCAL_DB_USER=root
LOCAL_DB_PASSWORD=

✅ Dica: mantenha .env fora do Git e versione apenas o .env.example.

▶️ Como executar (Docker)
Subir ambiente (build limpo)
docker compose build --no-cache
docker compose up -d
Acessos comuns
SonarQube: http://localhost:9000

Se você estiver usando Tunnel, o acesso pode estar externo — e isso é opcional (veja a seção “Cloudflare Tunnel”).

🧪 Auditorias e Relatórios
PMD (prioridades 3 e 4)
mvn -DskipTests pmd:check pmd:pmd
JaCoCo + SonarQube
mvn -DskipTests clean verify
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=SEU_TOKEN