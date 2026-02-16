# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes. I would standardize on one consistent approach (e.g., repositories + explicit transactions) instead of mixing patterns.
Reasons:
- Consistency reduces cognitive load and bugs (one way to read/write, one place for query logic).
- Easier testing (mock repositories / use test DB consistently).
- Clearer transaction boundaries and less risk of side effects (especially around REST resources and external integrations).
I would do it incrementally: keep existing behavior, refactor the hottest paths first, and add tests while migrating.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI-first (generated code)
Pros: contract is explicit, easier client generation, consistent validation/docs, fewer breaking changes.
Cons: extra build/gen complexity, harder to customize, can feel heavy for small/simple endpoints.

Code-first (hand-written)
Pros: fastest to change, flexible, less tooling.
Cons: spec can drift, docs/clients may be outdated, harder to enforce consistency.

Choice: for a real product I’d prefer OpenAPI-first for all public APIs. For small/internal endpoints, code-first is acceptable, but I’d still generate/maintain an OpenAPI spec in CI to avoid drift.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Priority:
1) Unit tests for domain/use cases (validations, replace/archive rules, edge cases).
2) REST/API integration tests for critical endpoints (happy path + main failure modes).
3) A few end-to-end smoke tests (startup + basic flows).

Focus:
- Business rules and invariants (capacity, stock matching, uniqueness, location validation).
- Error handling and HTTP status mapping.

Keeping coverage effective:
- Keep the JaCoCo threshold in CI.
- Add tests for every bug fix.
- Prefer small, fast unit tests; use integration tests only where wiring/transactions matter.
```