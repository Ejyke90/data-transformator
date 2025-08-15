# AI-driven Implementation Guide — Pacs008ToPacs009Mapper

Purpose
- Provide a step-by-step, testable implementation plan an AI (or developer) can follow to create the `Pacs008ToPacs009Mapper` MapStruct mapper used in this repository.
- Capture the full context required: data models, dependencies, mapping strategy, edge cases, tests, and verification steps.

High-level plan
1. Prepare environment & dependencies
2. Create minimal mapper skeleton and build to generate MapStruct artifacts
3. Implement core field mappings (group header + basic transaction fields)
4. Add remittance handling and structured fallbacks
5. Add conservative helpers (party, account, IBAN heuristic)
6. Add @AfterMapping fixes for payment type and identifiers
7. Add unit tests and integration test runs
8. Iterate until mapping is complete and tests pass

Checklist (each item = one testable iteration)
- [ ] Env: Java 21 + Gradle build succeeds for `:mapper-core` (compiles annotation processors)
- [ ] Skeleton: MapStruct interface exists and MapStruct generated implementation compiles
- [ ] GH: GroupHeader2 -> GroupHeader4 mapping passes a unit test asserting core fields copied
- [ ] TX: CreditTransferTransactionInformation2 -> CreditTransferTransactionInformation3 basic fields mapped (endToEndId, instrId, amount)
- [ ] Remit: Unstructured + structured remittance is consolidated into target `ustrd` list via named helper
- [ ] Party/account: Party and account copies preserve name and IBAN where possible (unit test asserts IBAN heuristic)
- [ ] AfterMapping: Payment type, postal addresses and common IDs copied conservatively
- [ ] Integration: `service` integration test posts programmatic pacs.008 and unmarshals pacs.009 successfully

Environment & dependencies (what AI must know)
- Java 21 toolchain via Gradle.
- MapStruct (version used in this repo). MapStruct requires an annotation processor at compile time.
- Prowide ISO20022 model: `com.prowidesoftware:pw-iso20022:SRU2024` (provides `Pacs00800101`, `Pacs00900101` and related dic classes).
- Jakarta JAXB API + runtime for marshalling/unmarshalling in tests and runtime.
- The project layout: `mapper-core` is a Java module that MapStruct will compile into a JAR; `service` uses it.

Minimal contract (what the mapper must do)
- Input: `Pacs00800101` (Prowide model)
- Output: `Pacs00900101` (Prowide model)
- Error modes: null/empty inputs -> return null or safe empty target; unknown or incompatible fields -> skip with warnings
- Success criteria: key identifiers (GrpHdr.MsgId, GrpHdr.CreDtTm, PmtId.EndToEndId) appear in the mapped `Pacs00900101` and at least one of `GrpHdr` or `CdtTrfTxInf` exists in output (integration test asserts this)

AI prompt templates (what to feed the AI)
- Minimal skeleton prompt (create interface):
  "Create a MapStruct mapper interface `Pacs008ToPacs009Mapper` that maps `Pacs00800101` -> `Pacs00900101`. Include an `INSTANCE` field via `Mappers.getMapper(...)`. Start by mapping the GroupHeader `GroupHeader2` -> `GroupHeader4` and a single transaction mapping `CreditTransferTransactionInformation2` -> `CreditTransferTransactionInformation3` for fields: `pmtId.endToEndId`, `pmtId.instrId`, `intrBkSttlmAmt` and add `@Mapping` annotations. Use `unmappedTargetPolicy = ReportingPolicy.WARN`."

- Remittance helper prompt:
  "Add a default method named `remittanceInformation1ToUstrd` annotated with `@Named("remittanceToUstrd")` that accepts `RemittanceInformation1` and returns `RemittanceInformation2`. Copy `ustrd` if present and extract `addtlRmtInf` from structured `strd` elements into the target unstructured list. Make it robust using reflection so it tolerates absent methods across model versions."

- AfterMapping prompt:
  "Add an `@AfterMapping` method `afterTransactionMap` that copies payment type info (`pmtTpInf`) conservatively using reflection, copies postal addresses for debtor/creditor if present, and attempts to copy common identification fields from `pmtId` to the target.`"

Mapping strategy & implementation notes (key decisions)
- Be conservative: prefer to copy compatible fields directly and use reflection for optional/variant fields.
- Use MapStruct generated methods for straightforward one-to-one mappings; implement complex conversions as `default` methods in the interface to keep everything in one place and accessible to generated code.
- For remittance, consolidate both `ustrd` and `strd.addtlRmtInf` into the target `ustrd` list. If structured fields provide typed info (creditor reference, amounts), extract the first structured entry fields into corresponding target setters when available.
- Use an IBAN heuristic in account mapping: inspect `Othr` id entries and set IBAN on the target if the string looks like an IBAN.

Testable unit tests (per iteration)
1. Compile check (iteration 1): run

```bash
./gradlew :mapper-core:compileJava
```

Expected: compilation succeeds and MapStruct generated sources exist under `build/generated/sources/annotationProcessor`.

2. Unit test for group header mapping (iteration 2): write a JUnit test that instantiates `GroupHeader2`, sets `msgId`, `creDtTm`, `nbOfTxs`, calls `Pacs008ToPacs009Mapper.INSTANCE.map` for a `Pacs00800101` with that group header, and asserts the target `GrpHdr` fields equal the source.

3. Unit test for transaction mapping (iteration 3): create `CreditTransferTransactionInformation2` with `pmtId.endToEndId`, `intrBkSttlmAmt`, `pmtId.instrId`, call mapping, assert target fields.

4. Remittance test (iteration 4): construct `RemittanceInformation1` with `ustrd` and `strd` entries, map via helper and assert target `ustrd` contains both original `ustrd` and `strd.addtlRmtInf` strings.

5. IBAN heuristic test (iteration 5): create `CashAccount7` with `Id.Othr.Id = "NL91ABNA0417164300"`, map and assert target account has IBAN set (or its id contains IBAN string depending on target model API).

6. Integration test (iteration 6): run `:service:test` which contains `TransformControllerIntegrationTest` and should pass. Run:

```bash
./gradlew :service:test --no-daemon --info
```

Expected: test passes, and the integration test prints request and response XML showing mapping result.

Quality gates & validation
- Build: `./gradlew build` (should pass)
- Lint/Typecheck: ensure MapStruct warnings are reviewed; unmappedTargetPolicy=WARN helps find gaps.
- Unit tests: implement the tests above and ensure they pass
- Integration test: `TransformControllerIntegrationTest` must pass and assert mapped `Pacs00900101` not null

Edge cases to cover
- Missing optional fields (nulls) — mapper should not NPE.
- Different Prowide model versions — reflection guards prevent NoSuchMethodException
- Large or multiple remittance entries — ensure `ustrd` concatenation or preserving list elements
- Incompatible list vs single-element fields — use defensive checks and attempt to add rather than assign

Developer hints for the AI (practical prompts & constraints)
- Ask the AI to prefer `default` methods inside the interface for helpers so MapStruct generated code can call them.
- Provide concrete examples (small sample objects or XML snippets) and expected output for each iteration.
- Require that all reflection usage is wrapped in try/catch and fails silently (log or ignore) to keep mapping robust.

Quality-of-output checks (what to assert programmatically)
- MapStruct generated implementation exists and class loads: `Mappers.getMapper(Pacs008ToPacs009Mapper.class)` returns non-null.
- For each unit test, assert that mapped fields equal expected values.
- For integration test: unmarshal response into `Pacs00900101` using typed JAXB unmarshal and assert `out != null` and `out.getGrpHdr() != null || !out.getCdtTrfTxInf().isEmpty()`.

Documentation & collaboration
- Add Javadoc and short comments on each helper method describing purpose and safe-fail behavior.
- Keep `unmappedTargetPolicy = ReportingPolicy.WARN` while iterating to surface missing mappings.
- Maintain a mapping matrix (CSV) listing source field -> target field -> covered (yes/no) to drive test creation.

Maintenance & next steps after AI creates mapper
- Review MapStruct warnings and expand mappings until the mapping matrix is green.
- Add more contract tests for real-world message variants encountered in production.
- Consider extracting complex helpers into a small helper class if the interface becomes too large.

Appendix: useful commands

```bash
# build mapper-core and run its tests
./gradlew :mapper-core:build

# run service integration tests
./gradlew :service:test

# run full project build
./gradlew build
```



