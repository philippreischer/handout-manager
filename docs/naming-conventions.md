# handout-manager — Naming Conventions Cheatsheet

**v0.2 · in Verwendung**

Analog zum A11yForge-Cheatsheet. Generische Konventionen übernommen, Domänen-Glossar neu auf dieses Projekt gemünzt.

---

## Grundregeln

- Alles auf **Englisch** (Bezeichner, Kommentare, Commits) — auch in SQL-Migrationen,
  Konfigurationsdateien und `.gitignore`. **Ausnahme:** Planungs- und Konzeptdokumente
  (`handout-manager-planning.md`, dieses Cheatsheet) sind auf Deutsch.
- Keine Abkürzungen außer etablierte: `id`, `url`, `html`, `jwt`, `api`, `dto`.
- Booleans mit Präfix: `is` / `has` / `should` / `can` — z. B. `isGameMaster`, `hasAccess`, `canView`.
- Domänenbegriffe konsistent über **alle** Schichten. Nie synonymisch mischen (siehe Glossar).

---

## Domänen-Glossar — feste Begriffe

Diese Begriffe konsistent über alle Schichten verwenden — Entity `Resource`, Tabelle `resource`, JSON `resource`, URL `/api/resources`. Synonyme strikt vermeiden.

| Begriff | Bedeutung | Verbotene Synonyme |
|---|---|---|
| **User** | Registriertes Konto. Kann je Projekt Game Master oder Player sein. | account, member, person |
| **Project** | Eine Spielrunde / Kampagne, von einem Game Master geführt. | campaign, game, table, session |
| **Membership** | Teilnahme eines Users an einem Project; trägt `role` und projektbezogenen `nickName`. | member, participant, enrollment |
| **MembershipRole** | Enum auf der Membership: `GAME_MASTER` / `PLAYER`. (System-/Admin-Rollen später separat am User als `UserRole`.) | rank, level, type |
| **Resource** | Ein Inhalt in einem Project: Notiz, Handout oder hochgeladene Datei (konsolidiert). | document, asset, item, file, handout |
| **ResourceType** | Enum: `NOTE` / `HANDOUT` / `FILE`. | kind, category, format |
| **Permission** | Explizite Freigabe (Whitelist), dass ein User eine Resource sehen darf. | access, grant, acl, whitelist |

Merke: **Handout** und **Note** sind *Werte* von `ResourceType`, keine eigenen Entitäten — die Entität heißt immer `Resource`.

---

## Java · Spring Boot

| Element | Regel | Beispiel |
|---|---|---|
| Klasse | PascalCase | `ResourceService` |
| Interface | PascalCase | `PermissionRepository` |
| Exception | PascalCase + `Exception` | `ResourceNotFoundException` |
| Konfigurationsklasse (Werte) | PascalCase + `Properties` | `JwtProperties` |
| Konfigurationsklasse (Beans) | PascalCase + `Config` | `SecurityConfig` |
| Enum-Typ | PascalCase | `ResourceType`, `MembershipRole` |
| Enum-Wert | UPPER_SNAKE | `GAME_MASTER` |
| Methode | camelCase, Verb zuerst | `grantPermission()` |
| Variable / Feld | camelCase | `passwordHash` |
| Konstante | UPPER_SNAKE | `MAX_UPLOAD_SIZE` |
| Package | lowercase | `at.handoutmanager.backend.resource` |
| Boolean | `is` / `has`-Präfix | `isGameMaster`, `hasAccess` |

- **Service-Methoden:** Verb zuerst — `createProject`, `uploadResource`, `grantPermission`, `revokePermission`, `findVisibleResources`.
- **Repository-Methoden:** Spring-Data-Konvention — `findByProjectId`, `existsByUserIdAndResourceId`, `findByEmail`.
- **`Properties` vs. `Config`:** `…Properties` ist ein typisiertes Abbild von
  Konfigurationswerten (`@ConfigurationProperties`) — reine Daten. `…Config` definiert Beans
  (`@Configuration`) — Verhalten. Die Trennung macht am Namen sichtbar, worum es sich handelt.
  **Kein `Util`-Suffix** für Klassen, die Konfiguration halten oder Beans sind: Ein `Util` ist
  ein zustandsloser statischer Helfer. `JwtService` heißt deshalb nicht `JwtUtil` — die Klasse
  hält Konfiguration und ist eine Spring-Bean.
- **DTOs:** Suffix `Request` / `Response` **+ `DTO`** — `ProjectCreateRequestDTO`, `ResourceResponseDTO`, `LoginRequestDTO`, `LoginResponseDTO`.

### Package-Struktur (feature-basiert)

```
at.handoutmanager.backend
├── user
├── project
├── membership
├── resource
├── permission
├── security      (JWT, SecurityConfig, Filter)
├── config        (app-weite Konfiguration, z. B. JPA Auditing)
└── common        (geteilte Exceptions, Hilfen)
```

---

## Vue 3 · Frontend

| Element | Regel | Beispiel |
|---|---|---|
| Komponenten-Datei | PascalCase.vue | `ResourceList.vue` |
| Komponenten-Tag | PascalCase | `<ResourceList />` |
| Composable | `use` + camelCase | `usePermissions()` |
| Pinia-Store | `use…Store` | `useAuthStore` |
| ref / reactive | camelCase | `selectedResource` |
| Props | camelCase | `defineProps<{ projectId: number }>` |
| Emits | kebab-case, Verb | `'permission-granted'`, `'resource-created'` |
| Event-Handler | `on` / `handle` + Verb | `handleResourceCreate()` |
| Konstante | UPPER_SNAKE | `DEFAULT_THEME` |
| Util-Datei | camelCase.ts | `formatDate.ts` |

- **Views** unter `views/`, wiederverwendbare Komponenten unter `components/`.
- Komponenten-Namen **mehrteilig** (Vue-Style-Guide) — `BaseButton`, nicht `Button`.

---

## PostgreSQL · Schema

| Element | Regel | Beispiel |
|---|---|---|
| Tabelle | snake_case, **Singular** | `resource` |
| Spalte | snake_case | `password_hash`, `created_at` |
| Primary Key | `id` | `id BIGINT PK` |
| Foreign Key (Spalte) | `<table>_id` | `project_id`, `user_id` |
| FK-Constraint | `fk_<table>_<col>` | `fk_resource_project_id` |
| Index | `idx_<table>_<col>` | `idx_resource_project_id` |
| Unique-Constraint | `uq_<table>_<col>` | `uq_membership_user_id_project_id` |
| Enum-Wert (DB) | UPPER_SNAKE als Text | `'GAME_MASTER'` |
| Flyway-Migration | `V<n>__<desc>.sql` | `V002__add_permission_table.sql` |

- **Tabellennamen Singular** — Hibernate/JPA mappt 1:1 auf Entitäten. Plural führt zu inkonsistenten Mappings.
- **Entity → Tabelle:** `User` → `user_account` (Ausnahme: `user` ist in PostgreSQL reserviert → `@Table(name = "user_account")`), `Project` → `project`, `Membership` → `membership`, `Resource` → `resource`, `Permission` → `permission`.

> **Abweichung von A11yForge (festgelegt):** Dort wurden Enum-Werte als lowercase snake_case (`'pending'`) gespeichert. Hier speichern wir sie als **UPPER_SNAKE** (`'GAME_MASTER'`), damit `@Enumerated(EnumType.STRING)` ohne `AttributeConverter` direkt passt.

---

## REST · JSON

| Element | Regel | Beispiel |
|---|---|---|
| Endpoint-Pfad | kebab-case, **Plural** | `/api/resources` |
| Pfad-Parameter | camelCase | `/api/projects/{projectId}` |
| Query-Parameter | camelCase | `?type=note&pageSize=20` |
| JSON-Property | camelCase | `"filePath": "…"` |
| Ressourcen-Verb | HTTP-Methode | `POST /api/resources`, `GET /api/resources/{id}` |

**camelCase für JSON:** Jackson + JS erwarten beides nativ camelCase. DB bleibt snake_case, das Mapping passiert sauber im JPA-Layer.

Beispiel-Endpoints:

```
POST /api/auth/register
POST /api/auth/login
GET  /api/projects
POST /api/projects
POST /api/projects/{projectId}/memberships
GET  /api/projects/{projectId}/resources
POST /api/projects/{projectId}/resources
POST /api/resources/{resourceId}/permissions
```

---

## Tests

| Element | Regel | Beispiel |
|---|---|---|
| JUnit 5 — Methode | `should<Outcome>_when<Condition>()` | `shouldDenyAccess_whenNoPermissionExists()` |
| Test-Klasse | `<ClassUnderTest>Test` | `ResourceServiceTest` |
| Integration-Test | `<…>IT` | `ResourceControllerIT` |
| Vitest — describe | `describe('ResourceList', …)` | |
| Vitest — it | `it('renders only visible resources', …)` | |
| Test-Datei (FE) | `ResourceList.spec.ts` | |

- Test-Variable: `sut` für „system under test" (Backend).
- Mocks mit Suffix: `permissionRepositoryMock`, `resourceServiceMock`.

---

## Git · Branches · Commits

| Branch-Typ | Format | Beispiel |
|---|---|---|
| Feature | `feature/<desc>` | `feature/resource-permissions` |
| Bugfix | `fix/<desc>` | `fix/jwt-refresh` |
| Chore | `chore/<desc>` | `chore/flyway-baseline` |
| Refactor | `refactor/<desc>` | `refactor/extract-auth-service` |
| Docs | `docs/<desc>` | `docs/naming-conventions` |

**Regel zur Reihenfolge:** Alles, was für *alle* Zweige gelten soll — Ignore-Regeln,
Formatierungs- und Editor-Konfiguration — gehört auf `main`, **bevor** abgezweigt wird. Sonst
erbt ein später abzweigender Branch die Regel nicht.

**Conventional Commits** — `<type>(<scope>): <description>`

| Typ | Beispiel |
|---|---|
| Feature | `feat(resource): add file upload endpoint` |
| Bugfix | `fix(auth): handle expired jwt on refresh` |
| Refactor | `refactor(permission): extract grant logic` |
| Docs | `docs(readme): document local docker setup` |
| Test | `test(permission): cover whitelist denial path` |
| Chore | `chore(config): split gitignore into root and backend` |

`chore` deckt alles ab, was weder Feature noch Bugfix ist: Build-Konfiguration, Abhängigkeiten,
Ignore-Regeln, Werkzeug-Einstellungen. (Der Branch-Typ `chore/` gab es von Anfang an, der
Commit-Typ fehlte.)

Gültige Scopes: `auth`, `user`, `project`, `membership`, `resource`, `permission`, `security`, `config`, `ui`, `theme`, `db`.

---

*handout-manager · Naming Conventions · v0.2*
