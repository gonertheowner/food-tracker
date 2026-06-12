# Testing Conventions

## Three-Layer Testing Strategy

Every feature is tested at three layers: controller, service, and repository. Each layer has its own scope, tooling, and test base class.

---

## Layer 1 — Controller Tests (Smoke / Integration)

**Goal:** Verify that HTTP contracts are correct — status codes, response body shape, and error formats.

**Tool:** `MockMvc` via `@WebMvcTest`

**Base class:** `AbstractControllerTest`

```java
@WebMvcTest(SomeController.class)
class SomeControllerTest extends AbstractControllerTest {
    @MockBean
    private SomeService someService;
    ...
}
```

**Rules:**
- Extend `AbstractControllerTest` which imports `SecurityConfig` and provides `MockMvc` + `ObjectMapper`.
- Declare `@WebMvcTest(YourController.class)` on the subclass.
- Mock the service layer with `@MockBean`.
- Only test **smoke scenarios**: happy path (2xx) and the most important error paths (400, 404).
- Assert status code, top-level JSON fields, and error response structure — not deep business logic.
- Use `jsonPath("$.field").exists()` and `jsonPath("$.field").value(expected)` for assertions.

**Error response shape to assert (400 validation):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": [{ "field": "name", "message": "must not be blank" }]
}
```

---

## Layer 2 — Service Tests (Unit)

**Goal:** Verify business logic with no Spring context, using pure Mockito mocks.

**Tool:** `@ExtendWith(MockitoExtension.class)`

**No base class** — these are plain unit tests.

```java
@ExtendWith(MockitoExtension.class)
class SomeServiceTest {
    @Mock SomeRepository someRepository;
    @InjectMocks SomeService someService;
    ...
}
```

**Rules:**
- Mock all dependencies with `@Mock`.
- Use `@InjectMocks` for the system under test.
- Test all branching conditions: happy path, not-found, access denied, etc.
- Use AssertJ: `assertThat(...).isEqualTo(...)` and `assertThatThrownBy(...).isInstanceOf(...)`.

---

## Layer 3 — Repository Tests (Integration with DB)

**Goal:** Verify that SQL queries, JPA mappings, and Liquibase migrations work against a real Postgres instance.

**Tool:** `@DataJpaTest` + Testcontainers (`PostgreSQLContainer`)

**Base class:** `AbstractDbTest`

```java
class SomeRepositoryTest extends AbstractDbTest {
    @Autowired SomeRepository someRepository;
    ...
}
```

**Rules:**
- Extend `AbstractDbTest` — it starts a shared `postgres:17` container per test class.
- The container uses `src/test/resources/sql/create-test-schema.sql` as init script to create the `food_tracker` schema before Liquibase runs.
- `@AutoConfigureTestDatabase(replace = NONE)` ensures the real Postgres container is used (not H2).
- `@DataJpaTest` wraps each test in a transaction that is rolled back → tests are isolated without manual cleanup.
- Test all custom query methods: save, find variants, delete.
- Avoid FK violations: products with `userId` must reference an existing user, or set `userId = null` for system products.

---

## Error Response DTO

`ErrorResponse` (in `com.foodtracker.dto`) is the single reusable error body:

| Field | Type | Present |
|---|---|---|
| `timestamp` | `Instant` | Always |
| `status` | `int` | Always |
| `error` | `String` | Always |
| `message` | `String` | Always |
| `path` | `String` | Always |
| `fieldErrors` | `List<FieldErrorDto>` | Only on 400 validation errors |

`GlobalExceptionHandler` maps exceptions to this DTO:
- `MethodArgumentNotValidException` → 400 + fieldErrors populated
- `ProductNotFoundException` (and future domain exceptions) → 404
- `Exception` (catch-all) → 500

---

## Dependency Matrix

| Layer | Spring Context | Database | MockMvc |
|---|---|---|---|
| Controller | `@WebMvcTest` slice | No | Yes |
| Service | None | No | No |
| Repository | `@DataJpaTest` slice | TestContainer | No |
