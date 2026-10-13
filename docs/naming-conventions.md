# handout-manager — Naming Conventions Cheatsheet

**v0.5 · in Verwendung**

Analog zum A11yForge-Cheatsheet. Generische Konventionen übernommen, Domänen-Glossar neu auf dieses Projekt gemünzt.

---

## Grundregeln

- Alles auf **Englisch** (Bezeichner, Kommentare, Commits) — auch in SQL-Migrationen,
  Konfigurationsdateien und `.gitignore`. **Ausnahme:** Planungs- und Konzeptdokumente
  (`handout-manager-planning.md`, dieses Cheatsheet) sind auf Deutsch.
- Keine Abkürzungen außer etablierte: `id`, `url`, `html`, `jwt`, `api`, `dto`.
- Booleans mit Präfix: `is` / `has` / `should` / `can` — z. B. `isGameMaster`, `hasAccess`, `canView`.
- Domänenbegriffe konsistent über **alle** Schichten. Nie synonymisch mischen (siehe Glossar).
- **Kommentare halten Randbedingungen fest, nicht was der Code tut.** Ein Kommentar, der die
  Zeile paraphrasiert, ist Ballast — der Code sagt es schon. Wertvoll ist ein Kommentar, der
  eine nicht offensichtliche Reihenfolge, eine Framework-Eigenheit oder einen Grund *gegen* die
  naheliegende Lösung festhält: genau das bewahrt davor, den Fehler in drei Monaten erneut zu
  bauen. Maßstab: Würde ein Aufräumen ohne diesen Kommentar etwas kaputtmachen? Dann bleibt er.

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
| Fabrik für ein technisches Artefakt | PascalCase + `Factory` | `RefreshCookieFactory` |
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
- **Eine `…Factory` liegt bei dem, was sie baut, nicht bei dem, der sie benutzt.**
  `RefreshCookieFactory` baut Refresh-Cookies und gehört deshalb nach `security`, obwohl
  `AuthController` und `UserController` sie aufrufen. Dieselbe Regel entschied, dass
  `CookieProperties` von `auth` nach `security` wanderte: Die Fabrik hängt davon ab, und läge
  die Konfiguration weiter in `auth`, zeigte die Abhängigkeit von der Mechanik zum
  Anwendungsfall — also gegen die dokumentierte Richtung.
- **DTOs:** Suffix `Request` / `Response` **+ `DTO`** — `ProjectCreateRequestDTO`, `ResourceResponseDTO`, `LoginRequestDTO`, `LoginResponseDTO`.
  DTOs leben auf der **Controller-Ebene**, nicht im Service: Ein Service nimmt Parameter
  entgegen, damit seine Signatur nicht von der HTTP-API abhängt. Braucht ein Service ein
  zusammengesetztes Ergebnis, ist das ein internes Record ohne `DTO`-Suffix (z. B. `AuthTokens`).

### Package-Struktur (feature-basiert)

```
at.handoutmanager.backend
├── auth           (Anwendungsfall: Controller, DTOs, AuthService)
├── user
├── project
├── membership
├── resource
├── permission
├── security       (Mechanik: JWT, SecurityConfig, Filter, RefreshToken, Cookie-Fabrik)
├── config         (app-weite Konfiguration, z. B. JPA Auditing)
└── common         (geteilte Exceptions, Hilfen, GlobalExceptionHandler)
```

**`auth` vs. `security`:** `auth` ist der Anwendungsfall (anmelden, registrieren, Token
erneuern, abmelden), `security` die Mechanik dahinter (Token bauen und prüfen, Requests filtern,
Spring Security konfigurieren). Derselbe Schnitt wie zwischen Controller und Service, eine Ebene
höher. Beide sind gültige Commit-Scopes.

---

## Vue 3 · Frontend

| Element | Regel | Beispiel |
|---|---|---|
| Komponenten-Datei | PascalCase.vue | `ResourceList.vue` |
| Komponenten-Tag | PascalCase | `<ResourceList />` |
| Unterordner in `components/` | nach Rolle, lowercase | `components/auth/`, `components/layout/`, `components/settings/` |
| Composable | `use` + camelCase | `usePermissions()` |
| Pinia-Store — Export | `use…Store` | `useAuthStore` |
| Pinia-Store — Datei | `<bereich>Store.ts` | `authStore.ts`, `userStore.ts` |
| Pinia-Store — id | der Bereich, ohne Suffix | `defineStore('auth', …)` |
| ref / reactive | camelCase | `selectedResource` |
| Props | camelCase | `defineProps<{ projectId: number }>` |
| Emits | kebab-case, Verb | `'permission-granted'`, `'resource-created'` |
| Event-Handler | `on` / `handle` + Verb | `handleResourceCreate()` |
| Router-Meta-Feld | camelCase | `requiresAuth`, `guestOnly` |
| Konstante | UPPER_SNAKE | `DEFAULT_THEME` |
| Util-Datei | camelCase.ts | `formatDate.ts` |

- **Store-Datei gegen Store-Export:** Die Datei heißt `authStore.ts`, nicht `useAuthStore.ts` —
  ein `use`-Präfix beschreibt eine Funktion, keine Datei. Der Export behält es.
  *Beim Umbenennen beachten:* Pfade, die als Zeichenkette in `vi.mock('…')` stehen, fasst kein
  IDE-Rename an. Ein `type-check` findet das nicht, die Vitest-Tests schon.
- **Views** unter `views/`, wiederverwendbare Komponenten unter `components/`, dort nach Rolle
  gruppiert. Eine View ist der **Rahmen** (Layout, Seitenstruktur), die Komponente der
  **Inhalt** — `AuthView.vue` hält die zentrierte Karte, `LoginForm.vue` das Formular darin.
  Hat eine Seite mehrere unabhängige Formulare, bekommt **jedes seine eigene Komponente**
  (`components/settings/PasswordSection.vue`): Dann hält jede Datei genau einen Satz
  Zustände — `password`, `isSubmitting`, `errorMessage` — statt ein halbes Dutzend mit
  Präfixen (`passwordLoading`, `userNameLoading`) in einer Datei. Nebeneffekt: jeder Abschnitt
  ist einzeln montierbar und damit einzeln testbar.
- **`id`-Attribute bleiben seitenweit eindeutig**, auch wenn die Formulare in verschiedenen
  Dateien liegen. Zweimal dasselbe `id` auf einer Seite, und das `for` eines Labels zeigt auf
  das falsche Feld — beim Klicken kaum zu merken, bei Screenreadern sofort.
- Komponenten-Namen **mehrteilig** (Vue-Style-Guide) — `BaseButton`, nicht `Button`.
- **`api/`-Dateien:** `httpClient.ts` (die Axios-Instanz), `interceptors.ts` (exportiert
  `setupInterceptors()`, aufgerufen aus `main.ts`), `errorMessage.ts` (`ProblemDetail` → Text
  für die Anzeige). Die Interceptors liegen **bewusst nicht** in `httpClient.ts`: Der Auth-Store
  importiert den Client, und ein Interceptor im Client müsste den Store importieren — das wäre
  ein Zirkelimport. Eine eigene Datei, die beide kennt, löst das auf.
- **Ein Store ruft einen anderen nur innerhalb einer Action auf** — `const authStore =
  useAuthStore()` im Funktionskörper, nicht auf Modulebene. Sobald sich zwei Stores
  gegenseitig importieren, werten die Module sich im Kreis aus, und je nachdem welches zuerst
  dran ist, ist das Gegenstück noch `undefined`. Im Funktionskörper wird es erst beim Aufruf
  aufgelöst.
- **Router-Meta-Felder werden typisiert** — `declare module 'vue-router'` mit einem erweiterten
  `RouteMeta`-Interface (Module Augmentation). Ohne das sind Meta-Felder vom Typ `unknown` und
  ein Tippfehler (`requiresAith`) ergibt keine Fehlermeldung, sondern eine stillschweigend
  offene Route.
- **Request-Typen nur, wenn der Body woanders entsteht als dort, wo er typisiert ist.** Baut
  eine Action ihn aus ihren eigenen typisierten Parametern (`{ password, userName }`), ist ein
  `ChangeUserNameRequest`-Interface eine zweite Kopie derselben Information — ein Tippfehler
  fiele schon an den Parametern auf. Erst wenn eine Komponente ein Objekt befüllt und
  durchreicht, verdient der Typ einen Namen.

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
| CHECK-Constraint | `ck_<table>_<beschreibung>` | `ck_refresh_token_revoked` |
| Enum-Wert (DB) | UPPER_SNAKE als Text | `'GAME_MASTER'` |
| Flyway-Migration | `V<n>__<desc>.sql` | `V002__add_permission_table.sql` |

- **Tabellennamen Singular** — Hibernate/JPA mappt 1:1 auf Entitäten. Plural führt zu inkonsistenten Mappings.
- **Entity → Tabelle:** `User` → `user_account` (Ausnahme: `user` ist in PostgreSQL reserviert → `@Table(name = "user_account")`), `Project` → `project`, `Membership` → `membership`, `Resource` → `resource`, `Permission` → `permission`, `RefreshToken` → `refresh_token`.
- **Kein eigener Index auf einer UNIQUE-Spalte** — PostgreSQL legt dafür bereits einen an.
  Ein zusätzlicher kostet Schreibleistung ohne Gewinn.
- **CHECK-Constraints beschreiben die Bedingung, nicht die Spalten.** `ck_refresh_token_revoked`
  sichert, dass `revoked_at` und `revoked_reason` nur gemeinsam gesetzt sind — der Name nennt
  das Thema, nicht die Spaltenliste, die sonst unlesbar lang würde.
- **Eine Migration, die eine Spalte nachrüstet, füllt Bestandsdaten vor dem Constraint.**
  Reihenfolge: Spalte anlegen, vorhandene Zeilen auf einen sinnvollen Wert setzen, dann den
  CHECK ergänzen. Umgekehrt scheitert die Migration an genau den Daten, die sie reparieren soll.

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
| Eigener Datensatz | `/me` statt der eigenen ID | `GET /api/users/me` |
| Teiländerung eines Feldes | `PATCH <ressource>/<feld>` | `PATCH /api/users/me/username` |
| Fehlerantwort | `ProblemDetail` | `{"status": 409, "title": …, "detail": …}` |

**camelCase für JSON:** Jackson + JS erwarten beides nativ camelCase. DB bleibt snake_case, das Mapping passiert sauber im JPA-Layer.

**`/me` statt der eigenen ID:** Der Client muss seine ID nicht kennen, und der Server nimmt sie
aus dem Token statt aus dem Pfad — damit kann niemand durch Ändern der ID auf ein fremdes Konto
zielen. Gängige Praxis (GitHub, Spotify, Google).

**Feldbezogene Unterpfade bei Teiländerungen.** `PATCH /api/users/me/username` und
`PATCH /api/users/me/password` statt eines gemeinsamen `PATCH /api/users/me` mit optionalen
Feldern. Gründe: Jeder Endpunkt hat eine eigene, vollständig verpflichtende Validierung statt
„irgendeines dieser Felder"; die Antworten dürfen sich unterscheiden (die Passwortänderung gibt
ein neues Token zurück, die Namensänderung das Profil); und im Log ist ablesbar, *was* geändert
wurde. Preis: mehr Endpunkte.

**Fehlerformat einheitlich `ProblemDetail`** (RFC 9457), zentral im `GlobalExceptionHandler`
unter `common`. Kein eigenes Fehler-DTO — sonst hätte man zwei Formate im System, weil
Framework-Fehler ohnehin `ProblemDetail` liefern. Validierungsfehler ergänzen ein
`errors`-Objekt (Feldname → Meldung) über `setProperty`.

**Fehlermeldungen nach außen generisch, im Log genau.** Eine Antwort darf nicht verraten, ob
eine E-Mail registriert ist oder ob ein Token existiert hat. Die Unterscheidung gehört ins Log.

**401 und 403 sind nicht austauschbar.** 401 heißt „hol dir ein neues Token", 403 heißt „das
darfst du nicht". Die Unterscheidung ist keine Stilfrage: Der Response-Interceptor im Frontend
löst bei 401 einen Refresh aus. Ein falsch eingetipptes *aktuelles* Passwort mit 401 zu
beantworten würde also einen Refresh anstoßen, der ein Refresh-Token verbraucht, ohne dass
irgendetwas abgelaufen war. Deshalb gibt es `PasswordMismatchException` (403) getrennt von
`InvalidCredentialsException` (401).

**Erfolgsantworten tragen keine Meldungstexte.** Ein 204 oder ein 200 mit Nutzdaten genügt; die
Formulierung für den Benutzer gehört ins Frontend. Eine Bestätigung über einen Seitenwechsel
hinweg läuft über einen Query-Parameter (`/login?deleted=1`), nicht über Store-Zustand — sie
übersteht damit das Zurücksetzen des Stores, das zur Löschung ja gerade gehört.

Beispiel-Endpoints:

```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
GET    /api/users/me
PATCH  /api/users/me/username
PATCH  /api/users/me/password
DELETE /api/users/me
GET    /api/projects
POST   /api/projects
POST   /api/projects/{projectId}/memberships
GET    /api/projects/{projectId}/resources
POST   /api/projects/{projectId}/resources
POST   /api/resources/{resourceId}/permissions
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
| Test-Ordner (FE) | `__tests__` neben dem Getesteten | `src/api/__tests__/` |

- Test-Klassen liegen **spiegelbildlich** zum Getesteten (gleiches Package unter `src/test/java`),
  sind package-private (kein `public`).
- **Frontend-Tests** folgen derselben Idee: ein `__tests__`-Ordner direkt neben der geprüften
  Datei, nicht gesammelt unter `src/components/__tests__/` wie im `create-vue`-Beispiel.
- Test-Variable: `sut` für „system under test" (Backend).
- Mocks mit Suffix: `permissionRepositoryMock`, `resourceServiceMock`.
- **Testdaten als Konstanten** am Kopf der Klasse (`EMAIL`, `PASSWORD`, `ACCESS_TOKEN`), nicht
  als Literale verstreut. Grund: Stub und Aufruf können nicht auseinanderlaufen. Konstanten für
  Fehlerfälle tragen ihre Absicht im Wert (`WRONG_PASSWORD = "wrong-password"`), nicht nur im
  Namen.
- **Aufbau immer gleich:** erst alle `when`, dann der Aufruf, dann `verify` / `assertThat`.
- **Mocke Verhalten, nicht Daten.** Reine Datenhalter mit `new` erzeugen. `ArgumentCaptor`
  verwenden, um zu prüfen was der Code *erzeugt* hat, statt was der Mock zurückgibt.
- **Eine Hilfsmethode, die einen Parameter ignoriert, ist eine Falle.** `tokenWith(expiresAt,
  revokedAt)`, das intern `Instant.now()` benutzt, liefert für jeden Aufruf dasselbe Objekt und
  täuscht Abdeckung vor. Besser mehrere sprechende Helfer ohne Parameter — `activeToken()`,
  `expiredToken()`, `revokedToken(reason)` — als einer, der alles kann.
- **Ein Test, der nicht hält was sein Name sagt, ist schlechter als keiner.** Beim Schreiben
  lieber löschen als „irgendwie grün" stehen lassen: beim Lesen glaubt man sonst, der Fall sei
  abgedeckt.
- **Jeden neuen Test einmal absichtlich brechen.** Die geprüfte Bedingung im Produktionscode
  entfernen, den Test rot sehen, zurücknehmen. Grün allein heißt nur „nicht widerlegt" — erst
  das gesehene Rot beweist, dass der Test beobachtet, was draufsteht.

### Backend-Tests — Werkzeuge

- **`@WebMvcTest` scannt keine `@Component`s.** Jede Sicherheitskomponente, die der Controller
  oder die Kette braucht, muss in `@Import` stehen — auch Hilfsklassen wie
  `RefreshCookieFactory`. Fehlt eine, scheitert der ganze Kontext mit „required a bean of type …
  that could not be found", und zwar in *jedem* Test der Klasse gleichzeitig.
- **`@WithMockUser` passt nicht zu `@AuthenticationPrincipal Long`.** Es legt einen String in
  den SecurityContext. Für einen `Long` als Principal baut man die Authentifizierung selbst:
  `request.with(authentication(new UsernamePasswordAuthenticationToken(USER_ID, null, List.of())))`.
- **`ReflectionTestUtils.setField`** gibt einer Entität im Test eine generierte ID oder einen
  `@CreatedDate`-Zeitstempel, ohne dafür Setter zu öffnen, die es im Produktionscode nicht
  geben soll.
- **`InOrder`**, wenn die *Reihenfolge* die Aussage ist — etwa dass das Passwort geprüft wird,
  bevor Tokens widerrufen werden. Ein `verify` ohne Ordnung würde auch die vertauschte Variante
  durchlassen.
- **`verify(mock, never())`** ist oft der eigentliche Beweis: Dass bei falschem Passwort das
  *neue* nicht einmal gehasht wird, zeigt, dass die Prüfung vor der Arbeit steht.
- **Secret für Tests über Surefire**, nicht über eine Test-`application.properties`: siehe
  Planungsdatei, Abschnitt CI.

### Frontend-Tests — Werkzeuge

- **Mocke so weit unten, wie das geprüfte Verhalten reicht.** Zwei Ebenen stehen zur Wahl:
  - **Axios-Adapter ersetzen** (`client.defaults.adapter = …`), wenn der Prüfling *in* der
    Axios-Kette liegt — die Interceptors laufen dann darum herum und werden mitgeprüft.
    Wichtig: Ein selbstgeschriebener Adapter muss bei Fehlerstatus **werfen**. Das Auswerten von
    `validateStatus` machen nur die eingebauten Adapter; eine zurückgegebene 401 landet sonst im
    Erfolgszweig.
  - **Modul ersetzen** (`vi.mock('@/api/httpClient', …)`), wenn der Prüfling *über* Axios liegt
    — Stores, Komponenten. Was Axios unterwegs tut, ist dort nicht Teil der Aussage, und ein
    Mock mit vier `vi.fn()` ist weniger Maschinerie, die mit rot werden kann.
- **Gemockte Module brauchen `vi.hoisted`.** Vitest zieht jeden `vi.mock`-Aufruf an den
  Dateianfang, noch vor die Imports. Eine gewöhnliche `const`-Variable ist dort noch nicht
  deklariert; `vi.hoisted` wandert mit nach oben. Der Schlüssel in der Mock-Fabrik heißt
  `default` für einen Default-Export (`httpClient`) und sonst wie der benannte Export
  (`useRouter`).
- **Modul-Singletons im `beforeEach` zurücksetzen.** `client` lebt einmal pro Testlauf:
  `client.interceptors.request.clear()` und `.response.clear()`, sonst stapelt jeder Test einen
  weiteren Interceptor obendrauf. Ebenso `setActivePinia(createPinia())` — ohne laufende
  Vue-App gibt es keine Pinia-Instanz.
- **Komponententest: Pinia zweimal bereitstellen.** `setActivePinia(pinia)` für die Aufrufe des
  Tests selbst, `mount(X, { global: { plugins: [pinia] } })` für die Komponente, die ihren Store
  über Vues Injection bekommt. Dieselbe Instanz, zwei Zugänge — sonst prüft der Test an einem
  zweiten, leeren Store.
- **`flushPromises()` nach jedem ausgelösten Event**, das eine `async`-Funktion anstößt. Der
  Test ist schneller als das `await`; ohne diese Zeile prüft man einen Zustand, der noch nicht
  eingetreten ist. Häufigster Grund für scheinbar grundlos rote Tests.
- **`setValue` statt `element.value = …`** — es löst zusätzlich das Event aus, das `v-model`
  braucht.
- **jsdom erzwingt `required` und `minlength` nicht** bei einem direkt ausgelösten
  Submit-Event. HTML-Validierung lässt sich damit nicht prüfen; verbindlich ist ohnehin die
  Bean-Validation im Backend, die ihren eigenen Test hat.

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
erbt ein später abzweigender Branch die Regel nicht. Dasselbe gilt für Änderungen, die nicht zum
aktuellen Feature gehören (etwa Testumbenennungen in einem bereits gemergten Bereich).

**Conventional Commits** — `<type>(<scope>): <description>`

| Typ | Beispiel |
|---|---|
| Feature | `feat(resource): add file upload endpoint` |
| Bugfix | `fix(auth): handle expired jwt on refresh` |
| Refactor | `refactor(permission): extract grant logic` |
| Docs | `docs(readme): document local docker setup` |
| Test | `test(permission): cover whitelist denial path` |
| Chore | `chore(config): split gitignore into root and backend` |
| CI | `ci(github): run backend and frontend jobs on pull requests` |

`chore` deckt alles ab, was weder Feature noch Bugfix ist: Build-Konfiguration, Abhängigkeiten,
Ignore-Regeln, Werkzeug-Einstellungen. (Der Branch-Typ `chore/` gab es von Anfang an, der
Commit-Typ fehlte.) `ci` ist davon abgegrenzt und gilt nur für die Pipeline selbst — Workflows,
Caches, Jobs.

**Betreffzeile kurz halten**, höchstens eine knappe Zeile Rumpf dahinter. Was die Entscheidung
*begründet*, gehört in die Planungsdatei, nicht in eine Commit-Nachricht, die niemand wieder
liest.

Gültige Scopes: `auth`, `user`, `project`, `membership`, `resource`, `permission`, `security`, `settings`, `home`, `config`, `github`, `ui`, `theme`, `db`.

**`ui` vs. Feature-Scope:** Was zu einem Anwendungsfall gehört, trägt dessen Scope — ein
Login-Formular ist `auth`, nicht `ui`. `ui` ist für anwendungsweite Oberfläche ohne fachliche
Zugehörigkeit: Layout, Navigation, `App.vue`.

---

*handout-manager · Naming Conventions · v0.5*
