# JUNIE_CONTEXT.md — VGWM Data API

> Improved version aligned with the uploaded Lovable prototype, existing EzChatbot backend, and Kotlin competency assessment. This version preserves the original Data API architecture, keeps the broader backend priority-deadline rules, and stores AI analysis as one JSON column to avoid a wide nullable table.

**Audience:** Junie (JetBrains AI coding agent).
**Purpose:** This file is the authoritative guide for how code in this project must be written. Read this file fully before generating or modifying any code.

---

## 0. Prime Directives

These rules are absolute. They override language idioms, common shortcuts, and what "most Kotlin projects" do.

1. **This is a heavy object-oriented codebase.** Domain behavior, invariants, and state transitions live on domain objects. Application orchestration lives in services. If you catch yourself writing a `when` expression against a type/enum to select non-trivial behavior, stop and use polymorphism.
2. **Domain models are immutable.** Never add `var` to a domain class. All state changes return new instances via a private `copy()` method.
3. **Invariants are enforced by the objects themselves.** Use `private constructor` + factory methods + `require()` checks. A domain object must not be constructible in an invalid state.
4. **Repository interfaces live in `domain/`. Implementations live in `data/`.** The domain layer has zero dependencies on Exposed, Ktor, JDBC, or any framework.
5. **No anemic models.** If a class has only `val` fields and no methods beyond `equals`/`hashCode`, it is wrong unless it is explicitly a value object, DTO, or filter parameter object.
6. **All source code and comments are in English.** This is a school requirement. The Android UI will be bilingual (NL/EN) via resource files, but Kotlin code is English-only.
7. **Follow the package structure in Section 7 literally.** Do not invent new production packages without explicit approval. Do not move files between the specified locations. Test-only packages such as `test.fixture` are allowed when they keep tests readable.

If in doubt about a rule, stop and ask. Do not guess.

## 0.1 Agent Operating Rules

Junie must follow these rules when implementing code:

1. Do not generate broad scaffolding for multiple layers at once. Implement one explicit step from Section 16 at a time.
2. Prefer small, reviewable changes over large automatic rewrites.
3. After each task, report:
   - files created;
   - files modified;
   - design rules applied;
   - assumptions made;
   - unresolved questions or TODOs.
4. If a required domain rule is missing, do not invent it silently. Add a `TODO(requirement)` in the narrowest responsible location and report it.
5. If a technical decision conflicts with this document, stop and explain the conflict before changing direction.
6. Do not copy architecture, database design, class names, or domain boundaries from the Lovable prototype. The Lovable prototype is a UX and feature reference only.
7. Do not introduce helper classes, manager classes, utility classes, global functions, or generic `Utils` objects unless explicitly allowed.
8. Do not replace explicit domain concepts with generic maps, strings, or loosely typed structures.
9. Do not remove or weaken existing invariants to make implementation easier.
10. When uncertain, choose the design that keeps domain logic framework-independent and testable.
11. Preserve this document when editing it. Improvements must be additive or targeted replacements. Do not remove existing architecture rules, entities, endpoints, tests, or patterns unless the user explicitly requests removal.
12. When using uploaded reference projects, extract requirements from them, but do not blindly copy their implementation style.

13. Junie does not modify Gradle configuration. The following files are owned by the user and Junie must not edit, regenerate, or "tidy" them:
    - `build.gradle.kts` (root and any subprojects)
    - `settings.gradle.kts`
    - `gradle/libs.versions.toml`
    - `gradle.properties`
    - `gradle/wrapper/*`

    If a feature cannot be implemented without a missing dependency, Junie must stop and report:
    - which class, function, or framework feature could not be implemented;
    - which library and exact version would resolve it;
    - whether the version catalog already declares an alias for it;
    - a short justification (one or two lines).

    Junie waits for the user to add the dependency before continuing.
    Junie does not propose adding a dependency by writing a Gradle file "as a suggestion" — the report is text-only.

14. Junie does not run Gradle commands that mutate dependency state (`./gradlew --refresh-dependencies`, `./gradlew dependencies --write-locks`, `./gradlew wrapper --gradle-version=...`). Read-only commands like `./gradlew compileKotlin`, `./gradlew test`, and `./gradlew build` are allowed.

## 0.2 Domain-vs-Service Boundary

The phrase "behavior lives on objects" means domain behavior lives on domain objects. It does **not** mean that entities should call repositories, HTTP clients, file storage, JWT libraries, or AI services.

Domain objects own:
- invariants;
- state transitions;
- derived properties;
- validation that must always hold;
- behavior that depends only on the object's own state and value objects.

Services own:
- application orchestration;
- repository coordination;
- external integrations;
- authentication/token creation;
- file storage;
- AI calls;
- application-level transaction boundaries when multiple repositories must change atomically.

Routes own:
- HTTP parsing;
- authentication extraction;
- calling the correct service/repository;
- DTO mapping;
- returning responses.

Routes must not contain business rules. Services must not contain entity invariants or state-transition rules that belong on domain objects. Domain entities must not depend on infrastructure.

---

## 1. Project Overview

### What this is
A backend API for a **generic, company-agnostic inspection/observation application**. Employees walk an inspection "tour" through a company location, register observations with photos, and receive AI assistance to describe the photo and generate a full 5W1H analysis (Who/What/Where/When/Why/How), Root Cause Analysis, and Lean/Operational Excellence analysis.

This is the **Data API** — one of two backend services.

### The two-service architecture
```
Android app ──HTTPS+JWT──> Data API ──internal HTTP──> Chatbot API ──> Ollama
                               │
                               └──> SQLite (via Exposed)
```

- **Data API (this project)**: public service. Authentication, CRUD, business logic, persistence. Calls the Chatbot API server-side when AI analysis is needed.
- **Chatbot API (separate project, not in this repo)**: stateless AI proxy, listens on localhost only. Provides endpoints for photo analysis and 5W1H generation. Forked from an existing EzChatbot project.
- **Android app (separate project)**: talks only to the Data API.

### Company-agnostic framework
The code must not hardcode "De Heus", "VGWM", or any specific company terms. Company specifics live in:
- Seed data (rows in tables)
- A `CompanyInfo` singleton table with branding/labels
- Per-deployment configuration (`.env`)

One deployment serves one company. Multiple companies = multiple deployments. There is **no** multi-tenancy, no `Company` foreign key on tables.

### Use-case placeholder: De Heus Veghel
Seed data represents this example:
- 8 floors (Locations, hierarchical)
- Checkpoints per floor
- VGWM categories: Veiligheid (Safety), Gezondheid (Health), Welzijn (Wellbeing), Milieu (Environment)
- Observations with Priority levels and automatic deadlines
- External reference field for integration with maintenance systems (e.g., Ultimo)

### 1.1 Source artifact alignment

This Data API context is informed by three uploaded reference artifacts:

1. **Lovable prototype (`deheus-vgwm-main`)**
   - Treat as UX, terminology, seed-data, report/export, and feature reference.
   - Do not copy its frontend state-management architecture, Supabase dependency, React component structure, or TypeScript data model directly into the Kotlin backend.
   - Important concepts to preserve in the backend model:
     - VGWM category tags: V, G, W, M.
     - Observation types: unsafe situation, near miss, improvement point, good example, GMP+ deviation.
     - Full 5W1H: who, what, where, when, why, how.
     - Root Cause Analysis using 5 Why's.
     - Lean / Operational Excellence analysis: 8 Wastes, 6S, 3M, process focus, Gemba observation, PDCA.
     - Tour types include quarterly tours.
     - Ultimo is represented as an external reference/mock integration, not a real integration in the first version.

2. **Existing EzChatbot project**
   - Treat as a technical starting point for the separate Chatbot API only.
   - Its current `/chat` route, conversation memory, TTS/audio path, and general assistant workflow are not the target contract for VGWM.
   - For VGWM, the Chatbot API must be refactored into a stateless analysis service.

3. **Kotlin competency assessment**
   - Treat as evidence requirements for the school assessment.
   - Do not add artificial code only to tick boxes.
   - Prefer natural evidence through the domain model, repositories, strategies, factories, Ktor DSLs, Exposed objects, tests, and documentation.

---

### 1.2 AI analysis persistence rule

`AiAnalysis` is a structured domain value object, but it must be persisted in the observation table as **one nullable JSON column**, not as many nullable SQL columns.

Reason:
- AI analysis consists of nested sub-objects that should remain atomic.
- Some observation types may not need every analysis sub-object.
- The schema should tolerate future AI-output changes without adding many migrations.
- Mapper code must not require `!!` null assertions to reconstruct nested AI objects.
- The Data API does not need to filter or sort observations by individual AI-analysis fields in the first version.

Rules:
- Store the full serialized `AiAnalysis` object in `ai_analysis_json`.
- Store `ai_generated_at` separately only if useful for filtering, sorting, or display without deserializing JSON.
- Do not create columns such as `ai_who`, `ai_why_1`, `ai_pdca_plan`, etc.
- If future requirements need reporting on AI fields, consider SQLite JSON functions or a separate normalized analysis table after approval.

---

## 2. Stack & Versions

- **Kotlin** 2.0+
- **Ktor** 3.x (server-netty, content-negotiation, cors, status-pages, auth-jwt, call-logging, client-cio for outbound calls)
- **Exposed** latest (DAO flavor — use `IntEntity` / `UUIDEntity` / custom). NOT the DSL flavor.
- **SQLite** via `org.xerial:sqlite-jdbc`
- **HikariCP** for connection pooling
- **Koin** for dependency injection
- **kotlinx.serialization** for JSON (NOT Jackson, NOT Gson)
- **kotlinx.datetime** for `Instant`, `Duration` (NOT `java.time` in domain code)
- **kotlinx.coroutines** — all repository/service methods are `suspend`
- **JWT** via `com.auth0:java-jwt` or Ktor's `ktor-server-auth-jwt`
- **bcrypt** via `org.mindrot:jbcrypt` for password hashing
- **Dokka** for documentation generation

Testing:
- **kotlin.test** + **JUnit 5** for unit tests
- **Ktor `testApplication`** for integration tests
- **MockK** for mocking

### 2.1 Module assumption

The first version of the Data API is a **single Gradle module**. This matters because domain entities may expose `internal` rehydration factory methods for mapper classes.

If the project is later split into multiple Gradle modules, rehydration must be redesigned because Kotlin `internal` visibility is module-scoped. Do not silently make constructors public to solve that problem.

---

## 3. Object-Oriented Conventions

This section defines what "heavy OO" means for this project. These conventions are strict.

### 3.1 Two-level abstract hierarchy

All entities extend one of two abstract base classes:

```kotlin
abstract class Entity {
    abstract val id: EntityId
    
    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false
        if (this::class != other::class) return false
        return this.id == other.id
    }
    
    final override fun hashCode(): Int = id.hashCode()
}

abstract class AuditableEntity : Entity() {
    abstract val createdAt: Instant
    abstract val updatedAt: Instant
    
    /** Return a copy with updatedAt set to [now]. Used by repositories on save. */
    protected abstract fun touch(now: Instant): AuditableEntity
}
```

Rules:
- `AuditableEntity` is for entities with lifecycle (state changes over time): `Observation`, `Tour`, `User`.
- Plain `Entity` is for reference data (rarely changing, usually seeded): `Location`, `Checkpoint`, `Category`.
- `equals`/`hashCode` are `final` — never override in subclasses.
- `touch()` is `protected abstract` — every auditable subclass provides its own immutable update.

Implementation note:
- If the generic return type of `touch()` causes awkward casts or weak typing, prefer a simpler base class without `touch()` and add concrete internal methods such as `internal fun touched(now: Instant): Observation` on each auditable entity.
- Do not make `updatedAt` mutable to solve this.
- Do not expose a public `copy()` to solve this.

### 3.2 Typed IDs (value classes)

Never use raw `String` for entity IDs in domain code.

```kotlin
@JvmInline value class ObservationId(val value: String) {
    init { require(value.isNotBlank()) }
    companion object {
        fun generate(): ObservationId = ObservationId(UUID.randomUUID().toString())
    }
}
```

One value class per entity type: `ObservationId`, `TourId`, `UserId`, `LocationId`, `CheckpointId`, `CategoryId`.

The compiler now prevents passing a `UserId` where a `TourId` is expected. Runtime cost is zero (inlined to String).

**Caveat:** Exposed tables use `varchar("id", 36)` columns (raw String). The mapper layer (Section 6) converts between raw String (DB) and typed ID (domain).

### 3.3 Immutability with private `copy()`

Domain entities are immutable. Commands return new instances.

```kotlin
class Observation private constructor(
    override val id: ObservationId,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val description: String,
    val priority: Priority,
    val status: ObservationStatus,
    // ... other fields
) : AuditableEntity() {
    
    fun resolve(now: Instant = Clock.System.now()): Observation {
        require(status == ObservationStatus.OPEN || status == ObservationStatus.IN_PROGRESS) {
            "Cannot resolve observation in status $status"
        }
        return copy(status = ObservationStatus.RESOLVED, updatedAt = now)
    }
    
    override fun touch(now: Instant): Observation = copy(updatedAt = now)
    
    private fun copy(
        id: ObservationId = this.id,
        createdAt: Instant = this.createdAt,
        updatedAt: Instant = this.updatedAt,
        description: String = this.description,
        priority: Priority = this.priority,
        status: ObservationStatus = this.status,
        // ... etc
    ): Observation = Observation(id, createdAt, updatedAt, description, priority, status, /*...*/)
    
    companion object {
        fun create(/*...*/): Observation { /*...*/ }
        internal fun rehydrate(/*...*/): Observation { /*...*/ }
    }
}
```

Rules:
- Constructor is `private`.
- `copy()` is `private` — external code cannot use it.
- `companion object.create(...)` is the only way external code creates new instances. It validates and generates IDs.
- `companion object.rehydrate(...)` is `internal`, used only by mappers to reconstruct from DB without re-running validation.
- All state-changing methods (`resolve`, `assignTo`, `withXxx`) return a new instance via `copy()`.

**Do not** use Kotlin's `data class` for domain entities. `data class` auto-generates a public `copy()` that bypasses validation. Use a regular `class` with a private copy.

**Do** use `data class` for value objects (section 3.5) and DTOs (section 8).

### 3.4 Invariants via `require()`

Business rules that must always hold are enforced in:
1. The `create()` factory method (on initial construction).
2. Each command method (on state change).

```kotlin
fun addParticipant(userId: UserId, now: Instant = Clock.System.now()): Tour {
    require(status == TourStatus.NOT_STARTED) {
        "Cannot add participants after tour has started"
    }
    require(userId !in participants) {
        "User is already a participant"
    }
    return copy(participants = participants + userId, updatedAt = now)
}
```

`require()` throws `IllegalArgumentException`. `StatusPages` plugin maps this to HTTP 400.

### 3.5 Value objects (data class)

Value objects have no identity — two instances with the same field values are equal.

Use `data class` for:
- `FiveW1H(who, what, where, whenText, why, how)` — full 5W1H; Kotlin names stay English even when UI labels are Dutch.
- `RootCauseAnalysis(why1, why2, why3, why4, why5, rootCause, recommendedMeasure)`
- `PdcaCycle(plan, doStep, check, act)`
- `LeanAnalysis(wasteType, sixSCategory, threeM, processFocus, gembaObservation, pdca)`
- `AiAnalysis(photoDescription, fiveW1H, rootCauseAnalysis, leanAnalysis, generatedAt)`
- `TourCheckpointProgress(checkpointId, status, visitedAt, notes)`
- `Translations(en, nl)`
- Filter parameter objects like `ObservationFilter`

Rules:
- All fields are `val` (immutable).
- No identity. No ID field. No equals/hashCode override.
- Invariants in `init { require(...) }` if needed.

### 3.6 Sealed class with polymorphic behavior (Priority)

`Priority` is the canonical example of polymorphism in this project. It is **sealed class**, not enum, because each level has its own behavior (deadline calculation).

The backend intentionally keeps the broader deadline rules:
- Acute: 1 day
- High: 7 days
- Normal: 30 days
- Low: 90 days

The Lovable prototype used shorter demo deadlines. Do not copy those shorter values unless the user explicitly changes the backend policy later.

```kotlin
sealed class Priority(val code: String, val rank: Int) {
    abstract fun deadlineFrom(createdAt: Instant): Instant
    abstract val displayName: Translations

    object Acute : Priority("ACUTE", 0) {
        override fun deadlineFrom(createdAt: Instant) = createdAt + 1.days
        override val displayName = Translations(en = "Acute", nl = "Acuut")
    }
    object High : Priority("HIGH", 1) {
        override fun deadlineFrom(createdAt: Instant) = createdAt + 7.days
        override val displayName = Translations(en = "High", nl = "Hoog")
    }
    object Normal : Priority("NORMAL", 2) {
        override fun deadlineFrom(createdAt: Instant) = createdAt + 30.days
        override val displayName = Translations(en = "Normal", nl = "Normaal")
    }
    object Low : Priority("LOW", 3) {
        override fun deadlineFrom(createdAt: Instant) = createdAt + 90.days
        override val displayName = Translations(en = "Low", nl = "Laag")
    }

    companion object {
        val all: List<Priority> = listOf(Acute, High, Normal, Low)
        fun fromCode(code: String): Priority = all.firstOrNull { it.code == code }
            ?: throw IllegalArgumentException("Unknown priority code: $code")
    }
}
```

Usage: `observation.deadline = observation.priority.deadlineFrom(observation.createdAt)`.

**Anti-pattern — do not write this:**
```kotlin
// ❌ NO: when-expression against type discriminates without polymorphism
fun deadlineFor(priority: Priority, createdAt: Instant) = when(priority) {
    Priority.Acute -> createdAt + 1.days
    Priority.High -> createdAt + 7.days
    // ...
}
```

### 3.7 Enums for simple status types

For types that have labels but no per-value behavior, use `enum class`:

```kotlin
enum class ObservationStatus { 
    OPEN, IN_PROGRESS, RESOLVED, CANCELLED;
    
    val isTerminal: Boolean get() = this == RESOLVED || this == CANCELLED
}

enum class CheckpointStatus { NOT_VISITED, OK, NEEDS_ATTENTION, ACTION_REQUIRED }
enum class TourStatus { 
    NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED;
    val isTerminal: Boolean get() = this == COMPLETED || this == CANCELLED
    val allowsModification: Boolean get() = this == IN_PROGRESS
}
enum class TourType { DAILY, WEEKLY, MONTHLY, QUARTERLY, INCIDENT }
enum class ObservationType { UNSAFE, NEAR_MISS, IMPROVEMENT, GOOD_EXAMPLE, GMP_DEVIATION }
enum class UserRole { INSPECTOR, COORDINATOR, MANAGER, ADMIN }
```

Enums may have properties and methods, but they should be **trivial derivations** (like `isTerminal`). Non-trivial per-value behavior = use sealed class.

### 3.8 Aggregate roots

Some entities own other objects. The owning entity is the **aggregate root**; external code may only modify children through the root.

**Tour is an aggregate root.** It owns:
- `List<TourCheckpointProgress>` — composition, progress items exist only within a Tour.
- `Set<UserId>` — the participants (reference only; Users exist independently).

External code never constructs or mutates a `TourCheckpointProgress` directly. It calls `tour.updateCheckpoint(checkpointId, status, notes)`, which validates state and returns a new Tour.

**Observation is NOT a child of Tour.** It references Tour via `tourId: TourId?` but has its own lifecycle and repository.

### 3.9 Rich vs anemic: the test

Before committing a domain class, apply this test:
- **Does the class have any methods beyond getters, `equals`, `hashCode`, and `toString`?**
- **If no, and it is an entity (not a value object/DTO), it is anemic. Fix it.**

Ask: what does this object *do*? If the answer is "nothing, it just holds data and logic lives in services", the design is wrong for this project.

---

## 4. Domain Model — Complete Specification

This is the reference. All domain entities must match this exactly.

### 4.1 Entities (extend `AuditableEntity` or `Entity`)

**Observation** (AuditableEntity)
- `id: ObservationId`, `createdAt: Instant`, `updatedAt: Instant`
- `tourId: TourId?` — null if ad-hoc observation outside a tour
- `checkpointId: CheckpointId`
- `observerId: UserId` — who recorded the observation
- `assigneeId: UserId?` — who should address it (null initially)
- `categoryId: CategoryId` — VGWM category reference (V/G/W/M category row)
- `observationType: ObservationType` — unsafe, near miss, improvement, good example, or GMP+ deviation
- `description: String` (non-blank)
- `priority: Priority`
- `status: ObservationStatus` (default OPEN on creation)
- `photoPath: String?`
- `externalRef: String?` — e.g., Ultimo ticket ID
- `aiAnalysis: AiAnalysis?`
- Derived: `deadline: Instant` via `priority.deadlineFrom(createdAt)`
- Derived: `isOverdue: Boolean` — `!status.isTerminal && now > deadline`
- Commands: `resolve()`, `cancel()`, `assignTo(UserId)`, `withExternalRef(String)`, `withPhotoPath(String)`, `withAiAnalysis(AiAnalysis)`, `updateDescription(String)`

**Tour** (AuditableEntity) — Aggregate Root
- `id: TourId`, `createdAt: Instant`, `updatedAt: Instant`
- `type: TourType`
- `startedBy: UserId`
- `status: TourStatus` (default NOT_STARTED)
- `startedAt: Instant?`
- `endedAt: Instant?`
- `participants: Set<UserId>`
- `checkpointProgress: List<TourCheckpointProgress>` (composition)
- Derived: `allCheckpointsVisited`, `visitedCount`, `totalCheckpoints`, `duration`
- Queries: `progressFor(CheckpointId): TourCheckpointProgress?`, `hasParticipant(UserId): Boolean`
- Commands: `start()`, `addParticipant(UserId)`, `updateCheckpoint(CheckpointId, CheckpointStatus, notes)`, `endTour()`, `cancel()`

**User** (AuditableEntity)
- `id: UserId`, `createdAt: Instant`, `updatedAt: Instant`
- `name: String`, `email: String` (validated), `role: UserRole`, `passwordHash: PasswordHash`
- Queries: `canManageTours(): Boolean`, `canDeleteObservations(): Boolean`
- Commands: `changeRole(UserRole)`, `changePassword(PasswordHash)`, `updateProfile(name, email)`

**Location** (Entity)
- `id: LocationId`, `name: String`, `parentLocationId: LocationId?`, `orderIndex: Int`, `attentionNote: String?`
- Derived: `isTopLevel: Boolean`

**Checkpoint** (Entity)
- `id: CheckpointId`, `locationId: LocationId`, `name: String`, `attentionNote: String?`, `applicableCategories: Set<CategoryId>`
- Queries: `isCategoryApplicable(CategoryId): Boolean`

**Category** (Entity)
- `id: CategoryId`, `code: String` (e.g., "V", "G", "W", "M"), `displayName: Translations`, `colorHex: String?`

### 4.2 Value Objects (data class)

- `AiAnalysis(photoDescription: String?, fiveW1H: FiveW1H?, rootCauseAnalysis: RootCauseAnalysis?, leanAnalysis: LeanAnalysis?, generatedAt: Instant)`
- `FiveW1H(who: String, what: String, where: String, whenText: String, why: String, how: String)`
- `RootCauseAnalysis(why1: String, why2: String, why3: String, why4: String, why5: String, rootCause: String, recommendedMeasure: String)`
- `PdcaCycle(plan: String, doStep: String, check: String, act: String)`
- `LeanAnalysis(wasteType: String, sixSCategory: String, threeM: String, processFocus: String, gembaObservation: String, pdca: PdcaCycle)`
- `TourCheckpointProgress(checkpointId: CheckpointId, status: CheckpointStatus, visitedAt: Instant?, notes: String?)` — with `isVisited: Boolean` computed property
- `Translations(en: String, nl: String)` — with `forLanguage(code: String): String`
- `PasswordHash(value: String)` — `@JvmInline value class`, validates bcrypt/argon prefix in `init`

Kotlin property names remain English because all source code is English-only. DTOs may expose Dutch labels/fields when needed for the Android UI, PDF/Excel exports, or compatibility with the Lovable prototype terminology.

### 4.3 Enums

- `ObservationStatus`: OPEN, IN_PROGRESS, RESOLVED, CANCELLED (+ `isTerminal`)
- `TourStatus`: NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED (+ `isTerminal`, `allowsModification`)
- `CheckpointStatus`: NOT_VISITED, OK, NEEDS_ATTENTION, ACTION_REQUIRED
- `TourType`: DAILY, WEEKLY, MONTHLY, QUARTERLY, INCIDENT
- `ObservationType`: UNSAFE, NEAR_MISS, IMPROVEMENT, GOOD_EXAMPLE, GMP_DEVIATION
- `UserRole`: INSPECTOR, COORDINATOR, MANAGER, ADMIN

### 4.4 Sealed classes

- `Priority` — sealed class with `Acute`, `High`, `Normal`, `Low` singletons. Each overrides `deadlineFrom(Instant): Instant` and `displayName: Translations`.

---

## 5. Repositories

One repository interface per entity type. All interfaces live in `domain/repository/`. All implementations live in `data/repository/` and use Exposed DAO.

### 5.1 Interface conventions

```kotlin
interface ObservationRepository {
    suspend fun findById(id: ObservationId): Observation?
    suspend fun findAll(filter: ObservationFilter = ObservationFilter()): List<Observation>
    suspend fun save(observation: Observation): Observation
    suspend fun delete(id: ObservationId): Boolean
}

data class ObservationFilter(
    val tourId: TourId? = null,
    val checkpointId: CheckpointId? = null,
    val locationId: LocationId? = null,
    val status: ObservationStatus? = null,
    val priority: Priority? = null,
    val observationType: ObservationType? = null,
    val assigneeId: UserId? = null,
    val overdueOnly: Boolean = false,
)
```

Rules:
- All methods are `suspend`.
- `findById` returns nullable. No wrapper types (no `Result`, no `Optional`).
- `save` is upsert. Returns the saved entity (same instance or with generated ID).
- `delete` returns `Boolean` (true if something was deleted).
- Filtering via parameter object (`data class`), not via many `findByX` methods.
- **No repository returns DB entities (Exposed `Entity`). Only domain entities.**

### 5.2 Required repositories

- `ObservationRepository` + `ObservationFilter`
- `TourRepository` + `TourFilter`
- `UserRepository` (methods: `findById`, `findByEmail`, `save`, `delete`)
- `LocationRepository` (usually `findAll`, `findById`, `findChildren(LocationId)`)
- `CheckpointRepository` (`findById`, `findByLocation(LocationId)`, `findAll`)
- `CategoryRepository` (`findAll`, `findById`, `findByCode(String)`)

### 5.3 Implementation pattern (Exposed DAO)

See Section 6 for the mapper layer. Implementation skeleton:

```kotlin
class ExposedObservationRepository : ObservationRepository {
    
    override suspend fun findById(id: ObservationId): Observation? = newSuspendedTransaction {
        ObservationEntity.findById(id.value)?.let { ObservationMapper.toDomain(it) }
    }
    
    override suspend fun save(observation: Observation): Observation = newSuspendedTransaction {
        val existing = ObservationEntity.findById(observation.id.value)
        val entity = existing ?: ObservationEntity.new(observation.id.value) {}
        ObservationMapper.applyToEntity(observation, entity)
        ObservationMapper.toDomain(entity)
    }
    
    // ...
}
```

---

## 6. Persistence Layer (Exposed DAO)

### 6.1 Three-file pattern per entity

For every domain entity, there are three persistence-layer files:
1. **Table** — schema definition (`data/table/ObservationsTable.kt`)
2. **Entity** — Exposed DAO entity class (`data/entity/ObservationEntity.kt`)
3. **Mapper** — translates between `Entity` and domain class (`data/mapper/ObservationMapper.kt`)

### 6.1.1 Exposed DAO fallback rule

The required persistence style is Exposed DAO. However, Exposed custom String IDs can be type-sensitive depending on version and imports.

If `IdTable<String>` or custom string DAO IDs cause compiler/type problems:
1. Do not switch to raw SQL.
2. Do not leak Exposed DSL query code outside `data/`.
3. Do not replace typed domain IDs with raw strings.
4. Create a small persistence spike and report the exact compiler error before changing the persistence approach.
5. Any approved fallback must still keep repositories returning domain entities only.

### 6.1.2 Schema creation and persistence style

**Schema creation rule:**

- Schema creation runs once at application startup, inside a single `transaction { }` block.
- Use `SchemaUtils.create(...)` for initial creation, or `SchemaUtils.createMissingTablesAndColumns(...)` if columns may evolve during development.
- All `Table` objects must be passed in a single call so creation order is deterministic and the FK graph is satisfied.
- Do **not** use Flyway, Liquibase, hand-written SQL migration files, raw `exec("CREATE TABLE ...")`, or shell scripts for schema management in the first version.
- Production-grade migration tooling can be revisited later. The first version intentionally uses `SchemaUtils` for simplicity and to keep schema definitions in Kotlin.
- If `SchemaUtils.createMissingTablesAndColumns` produces unexpected ALTER statements during development, Junie must report the diff before re-running the application. Do not silently regenerate the database file.

Example (illustrative — Junie must not invent extra tables):

```kotlin
fun Application.configureDatabase() {
    val config = AppConfig.fromEnvironment()
    Database.connect(HikariDataSource(config.toHikariConfig()))

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            UsersTable,
            LocationsTable,
            CategoriesTable,
            CheckpointsTable,
            CheckpointCategoriesTable,
            ToursTable,
            TourParticipantsTable,
            TourCheckpointProgressTable,
            ObservationsTable,
            CompanyInfoTable,
        )
    }
}
```

**Persistence style rule (DAO-first):**

Repository implementations under `data/repository/` must use **Exposed DAO** as the default style. Do not create a new `data/repository/exposed/` package unless the user explicitly approves a package-structure change.

Allowed DAO operations:
- `EntityClass.new(id) { ... }` for inserts.
- `EntityClass.findById(id)` for lookups.
- `EntityClass.find { predicate }` for filtered reads.
- `EntityClass.all()` for unfiltered reads (only when truly needed).
- Mutating DAO entity properties followed by transaction commit, for updates.
- `entity.delete()` for deletes.
- DAO references and back-references for relationships (`referrersOn`, `via`, `optionalReferencedOn`).

DSL (`Table.select { }`, `Table.insert { }`, `Table.update { }`, `Table.deleteWhere { }`, `innerJoin`, etc.) is **not** the default tool. DSL is permitted only after Junie has stopped and reported:

- which repository method requires it;
- why DAO is insufficient (for example, a multi-table aggregation that DAO expresses awkwardly, or a measurable performance problem);
- the exact DSL snippet proposed;
- and the user has explicitly approved the deviation in writing.

Forbidden in all repositories regardless of style:
- `exec("...raw SQL...")`
- `TransactionManager.current().connection` access for hand-written JDBC
- DSL leaking outside `data/` (no `select { }` calls in services or routes)
- Mixing DAO and DSL inside the same repository method without justification

Returns from repository methods are always **domain entities**, never Exposed DAO entities. Mappers translate at the boundary.

### 6.2 Example: Observation

AI analysis persistence is intentionally JSON-based. Do **not** expand nested AI analysis fields into many nullable columns.

**Table** (`data/table/ObservationsTable.kt`):
```kotlin
object ObservationsTable : IdTable<String>("observations") {
    override val id = varchar("id", 36).entityId()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val tourId = varchar("tour_id", 36).nullable().references(ToursTable.id)
    val checkpointId = varchar("checkpoint_id", 36).references(CheckpointsTable.id)
    val observerId = varchar("observer_id", 36).references(UsersTable.id)
    val assigneeId = varchar("assignee_id", 36).nullable().references(UsersTable.id)
    val categoryId = varchar("category_id", 36).references(CategoriesTable.id)
    val observationType = enumerationByName("observation_type", 32, ObservationType::class)
    val description = text("description")
    val priorityCode = varchar("priority_code", 16)
    val status = enumerationByName("status", 16, ObservationStatus::class)
    val photoPath = varchar("photo_path", 512).nullable()
    val externalRef = varchar("external_ref", 128).nullable()

    // Store the complete nested AiAnalysis value object as JSON.
    // Do not split it into ai_who / ai_why_1 / ai_pdca_plan columns.
    val aiAnalysisJson = text("ai_analysis_json").nullable()
    val aiGeneratedAt = timestamp("ai_generated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
```

**Entity** (`data/entity/ObservationEntity.kt`):
```kotlin
class ObservationEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ObservationEntity>(ObservationsTable)

    var createdAt by ObservationsTable.createdAt
    var updatedAt by ObservationsTable.updatedAt
    var tourId by ObservationsTable.tourId
    var checkpointId by ObservationsTable.checkpointId
    var observerId by ObservationsTable.observerId
    var assigneeId by ObservationsTable.assigneeId
    var categoryId by ObservationsTable.categoryId
    var observationType by ObservationsTable.observationType
    var description by ObservationsTable.description
    var priorityCode by ObservationsTable.priorityCode
    var status by ObservationsTable.status
    var photoPath by ObservationsTable.photoPath
    var externalRef by ObservationsTable.externalRef
    var aiAnalysisJson by ObservationsTable.aiAnalysisJson
    var aiGeneratedAt by ObservationsTable.aiGeneratedAt
}
```

**Serializer helper** (`data/mapper/AiAnalysisJsonMapper.kt`):
```kotlin
object AiAnalysisJsonMapper {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun fromJson(raw: String?): AiAnalysis? =
        raw?.takeIf { it.isNotBlank() }
            ?.let { json.decodeFromString<AiAnalysisSurrogate>(it).toDomain() }

    fun toJson(analysis: AiAnalysis?): String? =
        analysis?.let { json.encodeToString(AiAnalysisSurrogate.fromDomain(it)) }
}
```

Use a small serializable surrogate if the domain value objects themselves are not annotated with `@Serializable`. This keeps domain modeling independent from wire/persistence concerns if desired.

**Mapper** (`data/mapper/ObservationMapper.kt`):
```kotlin
object ObservationMapper {

    fun toDomain(entity: ObservationEntity): Observation {
        val aiAnalysis = AiAnalysisJsonMapper.fromJson(entity.aiAnalysisJson)

        return Observation.rehydrate(
            id = ObservationId(entity.id.value),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            tourId = entity.tourId?.let { TourId(it) },
            checkpointId = CheckpointId(entity.checkpointId),
            observerId = UserId(entity.observerId),
            assigneeId = entity.assigneeId?.let { UserId(it) },
            categoryId = CategoryId(entity.categoryId),
            observationType = entity.observationType,
            description = entity.description,
            priority = Priority.fromCode(entity.priorityCode),
            status = entity.status,
            photoPath = entity.photoPath,
            externalRef = entity.externalRef,
            aiAnalysis = aiAnalysis,
        )
    }

    fun applyToEntity(domain: Observation, entity: ObservationEntity) {
        entity.createdAt = domain.createdAt
        entity.updatedAt = domain.updatedAt
        entity.tourId = domain.tourId?.value
        entity.checkpointId = domain.checkpointId.value
        entity.observerId = domain.observerId.value
        entity.assigneeId = domain.assigneeId?.value
        entity.categoryId = domain.categoryId.value
        entity.observationType = domain.observationType
        entity.description = domain.description
        entity.priorityCode = domain.priority.code
        entity.status = domain.status
        entity.photoPath = domain.photoPath
        entity.externalRef = domain.externalRef
        entity.aiAnalysisJson = AiAnalysisJsonMapper.toJson(domain.aiAnalysis)
        entity.aiGeneratedAt = domain.aiAnalysis?.generatedAt
    }
}
```

Rules:
- Mapper code must not use `!!` for AI-analysis reconstruction.
- Partial nested AI analysis is handled by the JSON object shape, not by dozens of independent nullable SQL fields.
- If `aiAnalysisJson` is corrupt or cannot be decoded, treat this as a data-integrity problem. Do not silently fabricate partial domain objects.
- If the team decides to annotate domain value objects with `@Serializable`, keep that decision consistent across all value objects and document it.

### 6.3 Tour aggregate — child table

Tour has `checkpointProgress: List<TourCheckpointProgress>`. Store as junction table `TourCheckpointProgressTable` with composite key `(tourId, checkpointId)`. Tour mapper loads all rows for a tour ID and maps to the list.

### 6.4 Transactions

All repository methods use `newSuspendedTransaction { ... }` from `exposed-jdbc`. Never start transactions outside repositories. Services compose repository calls; if multiple calls must be atomic, services use `newSuspendedTransaction` at the service level.

---

## 7. Package Structure

Follow this layout exactly. Package prefix: `nl.<chosen>.vgwm` (to be determined at project start — ask the user).

```
domain/
├── base/
│   ├── Entity.kt
│   └── AuditableEntity.kt
├── id/
│   ├── ObservationId.kt
│   ├── TourId.kt
│   ├── UserId.kt
│   ├── LocationId.kt
│   ├── CheckpointId.kt
│   └── CategoryId.kt
├── value/
│   ├── Priority.kt                     # sealed class
│   ├── ObservationStatus.kt            # enum
│   ├── TourStatus.kt                   # enum
│   ├── CheckpointStatus.kt             # enum
│   ├── TourType.kt                     # enum
│   ├── ObservationType.kt              # enum
│   ├── UserRole.kt                     # enum
│   ├── Translations.kt                 # data class
│   ├── PasswordHash.kt                 # value class
│   ├── FiveW1H.kt                      # data class
│   ├── RootCauseAnalysis.kt            # data class
│   ├── PdcaCycle.kt                    # data class
│   ├── LeanAnalysis.kt                 # data class
│   ├── AiAnalysis.kt                   # data class
│   └── TourCheckpointProgress.kt       # data class
├── entity/
│   ├── Observation.kt
│   ├── Tour.kt
│   ├── User.kt
│   ├── Location.kt
│   ├── Checkpoint.kt
│   └── Category.kt
├── repository/
│   ├── ObservationRepository.kt
│   ├── ObservationFilter.kt
│   ├── TourRepository.kt
│   ├── TourFilter.kt
│   ├── UserRepository.kt
│   ├── LocationRepository.kt
│   ├── CheckpointRepository.kt
│   └── CategoryRepository.kt
└── service/
    ├── ChatbotClient.kt                # interface (Strategy)
    ├── PhotoStorage.kt                 # interface (Strategy)
    ├── ObservationCreationService.kt
    ├── AuthService.kt
    └── PasswordHasher.kt               # interface

data/
├── table/
│   ├── ObservationsTable.kt
│   ├── ToursTable.kt
│   ├── TourParticipantsTable.kt
│   ├── TourCheckpointProgressTable.kt
│   ├── UsersTable.kt
│   ├── LocationsTable.kt
│   ├── CheckpointsTable.kt
│   ├── CheckpointCategoriesTable.kt    # junction
│   ├── CategoriesTable.kt
│   └── CompanyInfoTable.kt             # singleton row
├── entity/                              # Exposed DAO entities (one per Table)
├── mapper/                              # one per domain entity
├── repository/                          # ExposedXxxRepository implementations
├── external/
│   ├── HttpChatbotClient.kt            # implements ChatbotClient via Ktor client
│   └── LocalFilePhotoStorage.kt        # implements PhotoStorage to uploads/
├── security/
│   └── BcryptPasswordHasher.kt         # implements PasswordHasher
└── seed/
    └── DeHeusSeedData.kt               # placeholder seed

api/
├── dto/
│   ├── ObservationDto.kt               # data class, @Serializable
│   ├── TourDto.kt
│   ├── LocationDto.kt
│   ├── CheckpointDto.kt
│   ├── CategoryDto.kt
│   ├── UserDto.kt
│   ├── LoginDto.kt
│   ├── CompanyInfoDto.kt
│   └── mapper/                          # Domain ↔ DTO mappers
└── route/
    ├── AuthRoutes.kt
    ├── CompanyInfoRoutes.kt
    ├── LocationRoutes.kt
    ├── CheckpointRoutes.kt
    ├── CategoryRoutes.kt
    ├── UserRoutes.kt
    ├── ObservationRoutes.kt
    ├── TourRoutes.kt
    ├── PhotoRoutes.kt
    └── HealthRoutes.kt

plugins/
├── ConfigureRouting.kt
├── ConfigureSerialization.kt
├── ConfigureHTTP.kt                    # CORS, compression
├── ConfigureAuth.kt                    # JWT
├── ConfigureStatusPages.kt
└── ConfigureCallLogging.kt

config/
├── AppConfig.kt                        # loads from env
└── DatabaseConfig.kt                   # HikariCP + Exposed.connect

di/
└── AppModule.kt                        # Koin bindings

Application.kt                           # entry point
```

Production packages outside this structure require explicit approval. Test-only helper packages are allowed under the test source set when they improve readability, for example `test.fixture` or `test.fake`.

---

## 8. DTOs and Routes

### 8.1 DTO rules

DTOs are the wire format. Keep them separate from domain entities.

- Use `@Serializable data class`.
- All fields are primitives or other DTOs (no domain types, no typed IDs — use `String`).
- Mappers live in `api/dto/mapper/`.

```kotlin
@Serializable
data class ObservationDto(
    val id: String,
    val tourId: String?,
    val checkpointId: String,
    val observerId: String,
    val assigneeId: String?,
    val categoryId: String,
    val observationType: String,
    val description: String,
    val priority: String,                // priority code
    val status: String,
    val photoPath: String?,
    val externalRef: String?,
    val aiAnalysis: AiAnalysisDto?,
    val deadline: String,                // ISO-8601, derived
    val isOverdue: Boolean,              // derived
    val createdAt: String,
    val updatedAt: String,
)
```

### 8.2 Route handler rules

- Routes are thin. No business logic.
- Parse input to domain types (IDs, enums, Priority), call service/repository, map result to DTO, respond.
- Use `StatusPages` plugin for error handling — don't catch exceptions in routes.

### 8.3 Complete endpoint list

This is the complete target endpoint list. Do not scaffold all routes at once. Implement them in the phases below so every route can be tested and reviewed.

```
POST   /api/auth/login                  { email, password } → { token, expiresAt }
GET    /api/company-info                → CompanyInfoDto (no auth)
GET    /api/locations                   (JWT)
GET    /api/locations/{id}/children
GET    /api/checkpoints?locationId=
GET    /api/categories
GET    /api/users
GET    /api/observations?filters        → filter via query params
GET    /api/observations/{id}
POST   /api/observations                multipart: photo + metadata; calls Chatbot API
PATCH  /api/observations/{id}
DELETE /api/observations/{id}
GET    /api/tours?active=
GET    /api/tours/{id}
POST   /api/tours
PATCH  /api/tours/{id}/checkpoints/{cid}
POST   /api/tours/{id}/end
POST   /api/tours/{id}/cancel
GET    /api/photos/{uuid}               JWT-guarded binary
GET    /health
```

### 8.4 Endpoint implementation phases

The complete endpoint list above is the final target. Implement it in phases to prevent shallow scaffolding.

**Phase 1 — foundation and read-only reference data**
```
GET    /health
POST   /api/auth/login
GET    /api/company-info
GET    /api/locations
GET    /api/locations/{id}/children
GET    /api/checkpoints?locationId=
GET    /api/categories
```

**Phase 2 — observations and photo upload**
```
GET    /api/observations?filters
GET    /api/observations/{id}
POST   /api/observations
PATCH  /api/observations/{id}
DELETE /api/observations/{id}
GET    /api/photos/{uuid}
```

**Phase 3 — tours**
```
GET    /api/tours?active=
GET    /api/tours/{id}
POST   /api/tours
PATCH  /api/tours/{id}/checkpoints/{cid}
POST   /api/tours/{id}/end
POST   /api/tours/{id}/cancel
```

**Phase 4 — users and administrative polish**
```
GET    /api/users
```

Do not generate all phases in one Junie task unless the user explicitly asks for a broad scaffold.


---

### 8.5 Manual API testing with cURL and IntelliJ HTTP Client

Manual HTTP testing is allowed and encouraged while the Android app is not finished yet. This is especially useful for testing multipart photo uploads independently from the Android client.

Rules:
- Manual HTTP test files are non-production artifacts. Place them in `requests/` or `docs/http/`, not inside `src/main/kotlin`.
- Do not hardcode real JWT secrets, production tokens, personal file paths, or company credentials. Use placeholders such as `{{token}}`, `YOUR_JWT_TOKEN`, and `./sample-data/photo.jpg`.
- Manual requests do not replace integration tests. They are smoke-test helpers only.
- The multipart field names in cURL and `.http` files must match the actual route contract exactly. If the route changes, update the manual request examples in the same task.
- On Windows PowerShell, prefer `curl.exe` instead of `curl`, because `curl` may be an alias for `Invoke-WebRequest`.

Example cURL smoke test for observation creation with a photo:

```bash
curl -X POST http://localhost:8080/api/observations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "photo=@./sample-data/photo.jpg" \
  -F "checkpointId=checkpoint-123" \
  -F "observerId=user-123" \
  -F "categoryId=category-v" \
  -F "observationType=UNSAFE" \
  -F "description=Loose cable near the production line" \
  -F "priority=NORMAL"
```

Example Windows PowerShell variant:

```powershell
curl.exe -X POST "http://localhost:8080/api/observations" `
  -H "Authorization: Bearer YOUR_JWT_TOKEN" `
  -F "photo=@C:\Users\Freek\Pictures\test.jpg" `
  -F "checkpointId=checkpoint-123" `
  -F "observerId=user-123" `
  -F "categoryId=category-v" `
  -F "observationType=UNSAFE" `
  -F "description=Loose cable near the production line" `
  -F "priority=NORMAL"
```

Example IntelliJ HTTP Client request (`requests/observations.http`):

```http
POST http://localhost:8080/api/observations
Authorization: Bearer {{token}}
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="photo"; filename="photo.jpg"
Content-Type: image/jpeg

< ../sample-data/photo.jpg
--boundary
Content-Disposition: form-data; name="checkpointId"

checkpoint-123
--boundary
Content-Disposition: form-data; name="observerId"

user-123
--boundary
Content-Disposition: form-data; name="categoryId"

category-v
--boundary
Content-Disposition: form-data; name="observationType"

UNSAFE
--boundary
Content-Disposition: form-data; name="description"

Loose cable near the production line
--boundary
Content-Disposition: form-data; name="priority"

NORMAL
--boundary--
```

The examples are illustrative. Junie must align IDs, enum values, field names, and file paths with the actual seed data and implemented DTO/request classes.

## 9. Services

### 9.1 When to add a service

Add a service when:
- Orchestration involves multiple repositories.
- External integration is needed (Chatbot API, file storage).
- Cross-cutting concern (authentication, hashing).

Don't add a service for logic that fits on one domain entity.

Services are allowed to coordinate work, but they must not become an anemic-domain dumping ground.

A service may:
- call multiple repositories;
- call external systems;
- manage authentication/token creation;
- coordinate file storage;
- coordinate AI calls;
- start an application-level transaction.

A service may not:
- decide whether an `Observation` can be resolved;
- decide whether a `Tour` can be ended;
- calculate deadlines for `Priority`;
- mutate domain state directly;
- bypass domain factory methods or command methods.

### 9.2 Required services

**`AuthService`**
- `login(email, password): AuthToken` — verifies password, issues JWT.
- `verifyToken(token): UserId?`
- Uses `UserRepository`, `PasswordHasher`, JWT library.

**`ObservationCreationService`**
- Orchestrates: photo storage + Chatbot API call + repository save.
- See example in Section 10.

**`ChatbotClient`** (interface) + **`HttpChatbotClient`** (impl)
- `suspend fun analyzePhoto(imageBase64: String, context: ObservationAiContext): PhotoAnalysisResult`
- `suspend fun generateObservationAnalysis(context: ObservationAiContext, imageBase64: String?): GeneratedObservationAnalysis`
- Impl uses Ktor HTTP client to call `CHATBOT_API_URL` from env.
- The Data API must not depend on EzChatbot's old `/chat` route, `audioPath`, TTS, STT, or in-memory conversation state.
- The Chatbot API contract for VGWM is stateless request-in/response-out analysis.

Required result value objects:
- `PhotoAnalysisResult(description: String, leanAnalysis: LeanAnalysis?)`
- `GeneratedObservationAnalysis(fiveW1H: FiveW1H, rootCauseAnalysis: RootCauseAnalysis, leanAnalysis: LeanAnalysis?)`
- `ObservationAiContext` with checkpoint, location, observer/assignee display labels, observation type, description, and timestamp.

**`PhotoStorage`** (interface) + **`LocalFilePhotoStorage`** (impl)
- `suspend fun save(bytes: ByteArray): String` — returns path.
- `suspend fun load(path: String): ByteArray?`
- `suspend fun delete(path: String): Boolean`
- Impl writes to `UPLOAD_DIR` from env.

**`PasswordHasher`** (interface) + **`BcryptPasswordHasher`** (impl)
- `hash(raw: String): PasswordHash`
- `verify(raw: String, hash: PasswordHash): Boolean`

---

## 10. Example: ObservationCreationService

Demonstrates orchestration pattern:

```kotlin
class ObservationCreationService(
    private val observations: ObservationRepository,
    private val chatbot: ChatbotClient,
    private val photoStorage: PhotoStorage,
) {
    suspend fun createWithAiAnalysis(request: NewObservationRequest): Observation {
        // 1. Store photo if provided
        val photoPath = request.photoBytes?.let { photoStorage.save(it) }

        // 2. Build domain object (invariants enforced by factory)
        var observation = Observation.create(
            checkpointId = request.checkpointId,
            observerId = request.observerId,
            assigneeId = request.assigneeId,
            categoryId = request.categoryId,
            observationType = request.observationType,
            description = request.description,
            priority = request.priority,
            tourId = request.tourId,
        )
        if (photoPath != null) {
            observation = observation.withPhotoPath(photoPath)
        }

        // 3. Enrich with AI analysis if photo present
        if (request.photoBytes != null) {
            val imageBase64 = request.photoBytes.toBase64()
            val photoAnalysis = chatbot.analyzePhoto(
                imageBase64 = imageBase64,
                context = request.aiContext,
            )
            val generated = chatbot.generateObservationAnalysis(
                context = request.aiContext.withDescription(
                    "${request.description}\n\nPhoto analysis: ${photoAnalysis.description}"
                ),
                imageBase64 = imageBase64,
            )
            observation = observation.withAiAnalysis(
                AiAnalysis(
                    photoDescription = photoAnalysis.description,
                    fiveW1H = generated.fiveW1H,
                    rootCauseAnalysis = generated.rootCauseAnalysis,
                    leanAnalysis = generated.leanAnalysis ?: photoAnalysis.leanAnalysis,
                    generatedAt = Clock.System.now(),
                )
            )
        }

        // 4. Persist and return
        return observations.save(observation)
    }
}

data class NewObservationRequest(
    val checkpointId: CheckpointId,
    val observerId: UserId,
    val assigneeId: UserId? = null,
    val categoryId: CategoryId,
    val observationType: ObservationType,
    val description: String,
    val priority: Priority,
    val tourId: TourId? = null,
    val photoBytes: ByteArray? = null,
    val aiContext: ObservationAiContext,
)
```

Note: if `chatbot.analyzePhoto` or `chatbot.generateObservationAnalysis` fails, the service should log the failure but still save the observation **without** AI analysis. The user can request re-generation later via a separate endpoint. AI failure must not block observation creation.

## 11. Configuration (`.env`)

```
# Database
DB_URL=jdbc:sqlite:./vgwm.db
DB_USER=
DB_PASSWORD=

# JWT
JWT_SECRET=<generated-long-random-string>
JWT_ISSUER=vgwm-data-api
JWT_AUDIENCE=vgwm-android
JWT_EXPIRATION_HOURS=24

# Chatbot API (internal)
CHATBOT_API_URL=http://127.0.0.1:8081

# Photo storage
UPLOAD_DIR=./uploads

# Server
PORT=8080
```

`AppConfig` loads these at startup. Missing required vars = fail fast with a clear error.

---

## 11.1 Chatbot API refactor contract

The existing EzChatbot codebase may be reused only as a starting point for adapters and Ktor setup. For VGWM, it must be refactored toward this shape:

```
POST /api/ai/photo-description
POST /api/ai/observation-analysis
```

Allowed to reuse from EzChatbot:
- `LlmAdapter` concept.
- `LocalEchoLlmAdapter` for offline-safe tests.
- `OllamaLlmAdapter` concept for local models.
- provider selection via configuration.

Must not be part of the Data API contract:
- `/chat`
- `ChatWorkflow`
- global/shared `ConversationState`
- TTS/STT
- `audioPath`
- conversational memory

The Chatbot API should be stateless. Each request must include all context needed to produce the response.

Expected analysis output:
- photo description;
- full 5W1H;
- Root Cause Analysis / 5 Why's;
- Lean / Operational Excellence classification;
- PDCA proposal.

---

## 12. Testing

### 12.1 Requirements
- Minimum **3 unit tests** and **3 integration tests**. Aim for more where natural.
- All tests in English.
- Use `kotlin.test` assertions + JUnit 5 runner.
- Integration tests use Ktor `testApplication`.

### 12.2 Unit test targets (no framework, fast)

Write unit tests for:
1. **`Priority.deadlineFrom`** — verify each priority produces the broader backend deadline offset: Acute 1 day, High 7 days, Normal 30 days, Low 90 days.
2. **`Observation.resolve`** — verify invariant (cannot resolve a CANCELLED observation throws).
3. **`Tour.start`** — verify requires participants, requires NOT_STARTED state.
4. **`Tour.updateCheckpoint`** — verify state transitions.
5. **`ObservationMapper.toDomain`** — round-trip mapping preserves all fields, including observation type and JSON-backed `AiAnalysis`. The tests must use `Priority.Normal` / code `NORMAL`; do not keep older `MEDIUM` expectations.
6. **`FiveW1H`, `RootCauseAnalysis`, and `LeanAnalysis`** — verify value-object invariants where defined.

### 12.3 Integration test targets (with `testApplication`)

Write integration tests for:
1. **Login flow** — correct credentials return 200 + token; wrong credentials return 401.
2. **CRUD on observations** — POST creates, GET retrieves, PATCH updates, DELETE removes.
3. **Auth guard** — protected endpoint returns 401 without token.
4. **Observation creation with mocked Chatbot** — service injects `MockChatbotClient`, verify full `AiAnalysis` is stored and retrieved via `ai_analysis_json`: photo description, 5W1H, root cause, Lean/PDCA.
5. **Tour state machine** — start, update checkpoint, end; verify forbidden transitions return 400.

### 12.4 Test doubles

Use `MockK` for mocking interfaces. For repositories, prefer an `InMemoryObservationRepository` (a real implementation backed by a `MutableMap`) over mocks when testing services — this tests the contract, not the mock setup.

---

## 12.5 Kotlin competency evidence rules

The project must naturally demonstrate Kotlin competency without adding artificial or meaningless code.

Required evidence should come from real architecture:

- **program entry point / packages / imports**: `Application.kt`, Ktor module setup, package structure.
- **`val` and `var`**: `val` in domain; `var` only in Exposed DAO entities or framework-required mutable objects.
- **basic types and null safety**: DTO parsing, optional fields, config, JWT principal extraction.
- **safe calls and Elvis operator**: config loading, nullable AI analysis, optional authenticated principals.
- **control flow**: validation, status transitions, route parsing.
- **type checks and safe casts**: use safe casts where framework principal extraction requires runtime checks, e.g. `principal<JWTPrincipal>() as? JWTPrincipal` only if needed.
- **collections**: `List` for ordered checkpoint progress, `Set` for unique participants and applicable categories, `Map` only for lookups/configuration.
- **abstract classes**: `Entity`, `AuditableEntity`.
- **inheritance and overriding**: domain base classes, Exposed DAO classes, Ktor/plugin hooks where appropriate.
- **interfaces**: repositories, `ChatbotClient`, `PhotoStorage`, `PasswordHasher`.
- **polymorphism**: `Priority` sealed class and Strategy implementations.
- **constructors and `init` blocks**: typed IDs, value objects, config validation.
- **equality**: entity identity equality vs value-object structural equality, verified in tests.
- **data classes vs classes**: DTOs/value objects as data classes; entities as regular classes.
- **visibility**: private constructors, private copy, internal rehydrate, protected base behavior where justified.
- **member functions vs top-level functions**: domain behavior as members; Ktor plugin configuration as top-level functions.
- **default and named arguments**: factories and command methods where they improve readability.
- **extension functions**: allowed for narrow framework mapping helpers, authentication helpers, and encoding helpers.
- **companion objects**: `create`, `rehydrate`, ID generation, parsing functions.
- **object declarations**: Exposed tables and singleton mapper objects where appropriate.
- **delegated properties**: Koin injection or Exposed DAO fields where natural.
- **generics**: repository test helpers, typed IDs, or reusable test fixtures where useful.
- **exception handling**: centralized `StatusPages`; avoid local route try/catch.
- **function types / higher-order functions / lambdas / trailing lambdas / receiver lambdas**: Ktor DSL, Koin DSL, serialization config, test fixtures.
- **scope functions**: allowed when they improve readability; do not use them to make code cryptic.

Do not introduce fake endpoints, unused helpers, meaningless `Any`, artificial `Nothing`, or contrived overloads only to satisfy the assessment. If a competency cannot be demonstrated naturally, explain it in the report rather than damaging the codebase.

---

## 13. Documentation

### 13.1 KDoc
- Every public class and interface has a KDoc block.
- Every public function has a KDoc block explaining `what` and noteworthy invariants.
- Private functions may skip KDoc if the name and body are self-explanatory.
- Do not write KDoc that just restates the signature. Add value or omit.

### 13.2 Dokka
- `./gradlew dokkaHtml` must run cleanly.
- Output goes to `build/dokka/html/`.
- The final deliverable includes this generated documentation.

---

## 14. Design Patterns (explicit, for the report)

This project intentionally implements the following GoF-style patterns. Each must be preserved and visible in the final code.

### 14.1 Repository pattern
- Interfaces in `domain/repository/`.
- Implementations in `data/repository/` using Exposed DAO.
- Motivation: decouple business logic from persistence. Enables unit testing with `InMemoryXxxRepository`. Allows swapping SQLite for Postgres by changing only the Exposed connection string (the interface contract doesn't change).

### 14.2 Strategy pattern
- `ChatbotClient` interface in `domain/service/`.
- Implementations: `HttpChatbotClient` (production), `MockChatbotClient` (tests).
- Motivation: the Data API must not depend on a specific AI provider. The Chatbot API can be swapped (different model, different hosting) by providing a new `ChatbotClient` implementation without changing services or routes.
- `PhotoStorage` is a second instance of the same pattern (local disk now, S3 possible later).

### 14.3 Factory method (implicit)
- `Entity.create(...)` on each domain entity's companion object.
- Motivation: enforces invariants; callers cannot bypass validation by calling a public constructor.

Keep these patterns intact. If a refactor would collapse them, push back.

### 14.4 Adapter pattern / anti-corruption boundary

- `HttpChatbotClient` adapts the separate Chatbot API contract to Data API domain value objects.
- It protects the Data API from details of EzChatbot, Ollama, OpenAI, Gemini/Lovable, prompt formats, or tool-calling schemas.
- If the Chatbot API changes provider or endpoint shape, update the adapter, not domain entities or route handlers.

---

## 15. Anti-patterns — Explicit Don'ts

Do not write any of the following. If you find yourself writing one, stop and reconsider.

### 15.0 Service dumping ground
```kotlin
// ❌ WRONG
class ObservationService {
    fun resolve(observation: Observation): Observation {
        if (observation.status == ObservationStatus.CANCELLED) {
            throw IllegalArgumentException("Cannot resolve cancelled observation")
        }
        return observation.copy(status = ObservationStatus.RESOLVED)
    }
}
```
**Fix:** the invariant and transition belong on `Observation.resolve()`. Services may orchestrate but must not own entity state-transition rules.

### 15.1 Anemic domain model
```kotlin
// ❌ WRONG
data class Observation(val id: String, var status: String, var priority: String)

class ObservationService {
    fun resolve(o: Observation) { o.status = "RESOLVED" }
}
```
**Fix:** behavior on the object, immutability, validation.

### 15.2 `when` against type when polymorphism works
```kotlin
// ❌ WRONG
fun deadlineFor(p: Priority) = when(p) {
    is Priority.Acute -> ...
    is Priority.High -> ...
}
```
**Fix:** `p.deadlineFrom(createdAt)`.

### 15.3 Exposing Exposed entities outside the data layer
```kotlin
// ❌ WRONG
fun getObservation(id: String): ObservationEntity = ObservationEntity.findById(id)!!
```
**Fix:** repository returns domain `Observation`, mapper converts.

### 15.4 Raw String IDs in domain code
```kotlin
// ❌ WRONG
fun findObservation(id: String): Observation?
```
**Fix:** `fun findObservation(id: ObservationId): Observation?`

### 15.5 Public `copy()` via `data class` on entities
```kotlin
// ❌ WRONG
data class Tour(...) : AuditableEntity() { ... }
// External code can now bypass invariants: tour.copy(status = COMPLETED)
```
**Fix:** regular `class`, private `copy()`, public commands.

### 15.6 Business logic in routes
```kotlin
// ❌ WRONG
post("/observations") {
    val dto = call.receive<ObservationDto>()
    val obs = ...
    if (obs.priority == "ACUTE") { /* do extra stuff */ }
    // ...
}
```
**Fix:** routes parse/validate/call-service/respond. Logic goes in services or on domain objects.

### 15.7 Catching exceptions in routes
```kotlin
// ❌ WRONG
post("/observations") {
    try { ... } catch (e: IllegalArgumentException) { call.respond(HttpStatusCode.BadRequest) }
}
```
**Fix:** let exceptions propagate. `StatusPages` plugin maps them to HTTP responses centrally.

### 15.8 Using `java.time` in domain code
```kotlin
// ❌ WRONG
val now: LocalDateTime = LocalDateTime.now()
```
**Fix:** `kotlinx.datetime.Clock.System.now()` returning `Instant`.

### 15.9 Hardcoding company-specific labels
```kotlin
// ❌ WRONG
val VGWM_CATEGORIES = listOf("V", "G", "W", "M")
```
**Fix:** categories are rows in the `categories` table, seeded per deployment.

### 15.9.1 Copying prototype architecture into the backend
```kotlin
// ❌ WRONG
data class Action(
    val floorNumber: Int,
    val checkpointName: String,
    val resolved: Boolean,
)
```
**Fix:** Use proper backend domain entities and references: `Location`, `Checkpoint`, `Observation`, typed IDs, status enums, repositories, and mappers. The Lovable prototype is a UX/reference artifact only.

### 15.9.2 Depending on EzChatbot's old chat contract
```kotlin
// ❌ WRONG
chatbot.post("/chat", ChatRequest(text = "...")).audioPath
```
**Fix:** Use a stateless VGWM analysis contract through `ChatbotClient`, returning typed value objects for photo description, 5W1H, Root Cause Analysis, and Lean/PDCA.

### 15.9.3 Wide nullable AI-analysis table
```kotlin
// ❌ WRONG
object ObservationsTable : IdTable<String>("observations") {
    val aiWho = text("ai_who").nullable()
    val aiWhat = text("ai_what").nullable()
    val aiWhy1 = text("ai_why_1").nullable()
    val aiPdcaPlan = text("ai_pdca_plan").nullable()
    // ... many more nullable AI columns
}
```
**Fix:** store the complete nested `AiAnalysis` as one `ai_analysis_json` column plus optional `ai_generated_at`. This prevents a wide table, avoids fragile partial reconstruction, and keeps AI-output evolution manageable.

### 15.10 Storing derived values
```kotlin
// ❌ WRONG
class Observation(val deadline: Instant, ...) // deadline is always priority.deadlineFrom(createdAt)
```
**Fix:** computed property on the class.

---

## 16. Implementation Order

Implement in this order. Don't skip ahead — later steps depend on earlier ones.

1. **Gradle readiness check** — inspect whether required dependencies/plugins (Ktor, Kotlin, Serialization, Dokka, Exposed, Koin, JWT, bcrypt, testing libraries) are already present. Junie must not edit Gradle files; missing dependencies are reported to the user per Section 0.1.
2. **`Application.kt`** + `plugins/` skeletons — empty but wired.
3. **`config/`** — `AppConfig` loading from env, `DatabaseConfig` initializing HikariCP + Exposed.
4. **`domain/base/`** — `Entity`, `AuditableEntity`.
5. **`domain/id/`** — all value-class IDs.
6. **`domain/value/`** — enums first (including `ObservationType` and quarterly `TourType`), then `Priority` sealed class with broader backend deadlines, then value-object data classes including full 5W1H, Root Cause Analysis, Lean Analysis, and PDCA.
7. **`domain/entity/`** — `Location`, `Checkpoint`, `Category` first (simpler, no audit), then `User`, `Observation`, `Tour`.
8. **`domain/repository/`** — all interfaces.
9. **`data/table/`** — all table definitions.
10. **`data/entity/`** — Exposed entity classes.
11. **`data/mapper/`** — domain ↔ entity mappers.
12. **`data/repository/`** — Exposed implementations.
13. **`data/seed/`** — DeHeusSeedData, runs on first startup if DB empty.
14. **`domain/service/`** — service interfaces.
15. **`data/external/`**, **`data/security/`** — service implementations.
16. **`di/AppModule.kt`** — Koin wiring.
17. **`plugins/ConfigureAuth.kt`** — JWT setup.
18. **`api/dto/`** — DTOs + mappers.
19. **`api/route/`** — routes, one entity at a time. Start with `HealthRoutes`, then `AuthRoutes`, then read-only (`LocationRoutes`, `CategoryRoutes`, `CompanyInfoRoutes`), then CRUD (`ObservationRoutes`, `TourRoutes`).
20. **`test/unit/`** — unit tests alongside each domain class they cover.
21. **`test/integration/`** — integration tests for routes.

After each step, run `./gradlew build` and fix errors before moving on.

---

## 17. Questions to Ask Before Deviating

If you are about to:
- **Add a new package** not in Section 7 → ask first.
- **Use a `var` in a domain class** → don't. Find another way.
- **Add a public `copy()`** on an entity → don't. Add a specific command method instead.
- **Put business logic in a route handler** → move it to a service or onto the domain object.
- **Add a field that is always derived** → make it a computed property, not a constructor parameter.
- **Use `String` for an ID in domain code** → use the typed `XxxId` value class.
- **Import `java.time.*` in domain code** → use `kotlinx.datetime`.
- **Catch an exception to return an HTTP error** → let `StatusPages` handle it.

If you are unsure whether a deviation is justified, write a brief comment explaining the tradeoff and proceed, but flag it in the PR description / chat response so the user can review.

---

## 18. Final Checklist

Before declaring any file complete, verify:
- [ ] All code and comments in English.
- [ ] No `var` in domain classes (only in Exposed entities).
- [ ] No raw String IDs in domain code.
- [ ] No `data class` for auditable entities.
- [ ] Constructors are private on entities; factories on companion object.
- [ ] Commands return new instances via private `copy()`.
- [ ] Invariants enforced with `require()`.
- [ ] No business logic in routes.
- [ ] No Exposed entity leaks out of `data/`.
- [ ] `AiAnalysis` is stored as `ai_analysis_json`, not as many nullable SQL columns.
- [ ] KDoc on all public classes/functions.
- [ ] File is in the correct package per Section 7.
- [ ] Any deviation, assumption, TODO, or unresolved compiler issue has been reported after the task.
- [ ] Lovable prototype concepts are represented correctly without copying frontend architecture.
- [ ] EzChatbot-specific `/chat`, TTS, audio path, and conversation memory are not part of the Data API contract.
- [ ] Full AI analysis model supports 5W1H, Root Cause Analysis, Lean Analysis, and PDCA.
- [ ] Junie did not modify `build.gradle.kts`, `gradle/libs.versions.toml`, `settings.gradle.kts`, `gradle.properties`, or any wrapper file.
- [ ] All required dependencies were reported and added by the user before implementation, not introduced silently by Junie.
- [ ] Schema is created via `SchemaUtils.createMissingTablesAndColumns` or `SchemaUtils.create` inside a single startup transaction.
- [ ] Repository implementations use Exposed DAO; any DSL usage was reported and explicitly approved.
- [ ] Repository methods return domain entities only, never Exposed DAO entities.
- [ ] Manual cURL / IntelliJ HTTP Client examples, if present, use placeholders and match the implemented multipart field names.
