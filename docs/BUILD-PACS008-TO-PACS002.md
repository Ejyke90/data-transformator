# How to build Pacs008 → Pacs002 mapping (step-by-step)

This guide walks an engineer or an automation agent through turning the existing
Pacs008→Pacs009 implementation into a Pacs008→Pacs002 mapping. It follows a
conservative, iterative approach: start with a safe stub adapter, then implement
real mappings incrementally using MapStruct and the repository's tooling.

Files you'll reuse
- `service/src/main/java/org/translator/service/Pacs008ToPacs009Adapter.java` — working example to copy/adapt.
- `service/src/main/java/org/translator/service/Pacs008ToPacs002Adapter.java` — stub (may already exist).
- `mapper-core/` — MapStruct mappers and helpers live here.
- `pacs.002.001.15.xsd` — reference XSD for pacs.002 structure.
- `sample_pacs002.xml` — expected target fixture for assertions.
- `mapper-core/tools/` — inference scripts to generate candidate mappings.

## Overview (recommended flow)
1. Start with a stub adapter so the REST/dispatch path works and can be wire-tested.
2. Add a `Pacs008ToPacs002Mapper` MapStruct interface in `mapper-core` (initially empty).
3. Run the repo's mapping-matrix generator and inference scripts to propose candidate mappings.
4. Populate a few high-value `@Mapping` annotations (GrpHdr.MsgId, timestamps, amounts, EndToEndId, account ids/names).
5. Compile and run a local `MapperDiagnosticTest` to inspect results quickly.
6. Iterate until the endpoint returns a meaningful pacs.002 payload for your sample.
7. Add tests and CI assertions.

## Step-by-step instructions

### Prereqs
- Java 21 and the Gradle wrapper in the repo.
- Repo checked out at project root.

### 1) Verify (or create) the stub adapter
Confirm `Pacs008ToPacs002Adapter` exists and returns a small skeleton. If not present, create a minimal stub like:

```java
@Component
public class Pacs008ToPacs002Adapter implements MapperAdapter {
    @Override
    public boolean supports(String sourceType, String targetType) {
        return sourceType != null && sourceType.toLowerCase().contains("pacs.008")
            && targetType != null && targetType.toLowerCase().contains("pacs.002");
    }
    @Override
    public String map(String sourceXml) throws Exception {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<ns2:Document xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.01\">\n" +
               "  <Stub>mapped-from-pacs008</Stub>\n" +
               "</ns2:Document>";
    }
}
```

Compile and run service to verify the stub is reachable:

```bash
./gradlew :service:compileJava
./gradlew :service:bootRun
curl -X POST http://localhost:8080/transform-payment \
  -H "Content-Type: application/xml" \
  -H "X-Target-Message-Type: pacs.002" \
  --data-binary @sample_pacs008.xml
```

If you receive the stubbed pacs.002 response, the registry/dispatcher wiring is correct.

### 2) Create MapStruct mapper scaffold
Add a new mapper interface at `mapper-core/src/main/java/org/translator/mapper/Pacs008ToPacs002Mapper.java`.

Example scaffold:

```java
@Mapper(componentModel = "spring")
public interface Pacs008ToPacs002Mapper {
    // Replace Pacs00200115 with the actual target class if you generated it
    Pacs00200115 map(Pacs00800101 src);

    // Add helper methods and @AfterMapping for complex conversions
}
```

Notes:
- If Prowide models for pacs.002 are not available, either generate Java classes from the XSD or create a minimal target POJO for mapping and marshalling.

### 3) (Optional) Generate Java models from `pacs.002.001.15.xsd`
If you need full target model classes, generate them using `xjc` or a Maven/JAXB plugin and add generated sources to `mapper-core` or a new module.

### 4) Produce a mapping matrix / candidate mappings
Use the repository's inference tooling to get candidate mappings:
- `mapper-core/tools/infer_mappings.py` (strict)
- `mapper-core/tools/infer_mappings_relaxed.py` (heuristic)

Run the relaxed script to create `mapper-core/docs/mapping_matrix_candidates_relaxed.csv` and inspect suggested pairs.

### 5) Implement high-value mappings in the MapStruct mapper
Start with fields that matter most:
- `GrpHdr.MsgId`, `GrpHdr.CreDtTm`, `GrpHdr.TtlIntrBkSttlmAmt`
- Transaction-level `PmtId.EndToEndId`, amount, `Dbtr.Nm`, `DbtrAcct.Id` (IBAN/Othr), `Cdtr.Nm`, `CdtrAcct.Id`

Add `@Mapping` annotations and `@AfterMapping` helpers for nested/complex fields.

### 6) Compile and generate MapStruct implementation

```bash
./gradlew :mapper-core:compileJava
```
Check the generated `Pacs008ToPacs002MapperImpl` in `build/generated` or `build/classes`.

### 7) Replace stub with real adapter logic
In `Pacs008ToPacs002Adapter.map(...)`:
- Unmarshal source XML (same pattern as `Pacs008ToPacs009Adapter`).
- Call the mapper to obtain target model.
- Marshal the target model to XML using JAXB and the pacs.002 namespace.

### 8) Test locally
- Add/modify a diagnostic test mirroring `MapperDiagnosticTest` that maps `sample_pacs008.xml` → pacs.002 and asserts expected fields or prints the XML.
- Run unit tests:

```bash
./gradlew clean test
```

- Run the service and test REST endpoint:

```bash
./gradlew :service:bootRun
curl -X POST http://localhost:8080/transform-payment \
  -H "Content-Type: application/xml" \
  -H "X-Target-Message-Type: pacs.002" \
  --data-binary @sample_pacs008.xml
```

Validate returned XML against `sample_pacs002.xml`.

### 9) Iterate & expand mappings
Use candidate CSV rows to add lower-confidence mappings. For each change:
- Add `@Mapping` or `@AfterMapping` code.
- Compile MapStruct (fast feedback) and run `MapperDiagnosticTest`.
- Add unit/fragment tests as you stabilize mappings.

### 10) Production hardening & performance tips
- Cache `JAXBContext` instances per message type.
- Reuse `Marshaller`/`Unmarshaller` with ThreadLocal or pooling where safe.
- Use `MessageTypeUtils.normalize(...)` and `detectSourceTypeFromXml(...)` (utilities included in the repo).
- Expose `GET /mapping-capabilities` (already implemented) to list supported mappings.
- Add schema validation for marshalled outputs against `pacs.002.001.15.xsd` if strict compliance is required.

## Example code snippets

Unmarshal source (pattern used in existing adapter):

```java
JAXBContext jaxbCtx = JAXBContext.newInstance(Pacs00800101.class);
Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
javax.xml.transform.stream.StreamSource ss = new javax.xml.transform.stream.StreamSource(new java.io.StringReader(sourceXml));
JAXBElement<Pacs00800101> jel = unmarshaller.unmarshal(ss, Pacs00800101.class);
Pacs00800101 src = jel.getValue();
```

Marshal target (pacs.002):

```java
JAXBContext outCtx = JAXBContext.newInstance(Pacs00200115.class);
Marshaller marshaller = outCtx.createMarshaller();
marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
QName rootName = new QName("urn:iso:std:iso:20022:tech:xsd:pacs.002.001.01", "Document");
JAXBElement<Pacs00200115> root = new JAXBElement<>(rootName, Pacs00200115.class, mappedObject);
StringWriter sw = new StringWriter();
marshaller.marshal(root, sw);
String outXml = sw.toString();
```

## Practical AI / automation tips
- Use the stub-first approach to validate wiring before implementing mapping logic.
- Use inference scripts to propose candidate mappings; have the AI generate small, focused MapStruct patches (1–2 fields at a time) and run compile/test after each patch.
- Prefer small commits. Run `./gradlew :mapper-core:compileJava` after each patch to regenerate MapStruct impl and detect compile errors early.
- Maintain the mapping matrix CSV to track human decisions; let the AI suggest remaining high-confidence mappings.

## Checklist (quick)
- [ ] `./gradlew :mapper-core:compileJava` — MapStruct codegen succeeds
- [ ] `./gradlew :service:compileJava` — service compiles with adapter registered
- [ ] `./gradlew clean test` — unit tests pass (or at least no fatal errors)
- [ ] `curl POST` with `X-Target-Message-Type: pacs.002` returns expected structure
- [ ] Iterate mapping rules until coverage is acceptable

---

If you'd like, I can now:
- Generate a starter `Pacs008ToPacs002Mapper` interface with a first-pass set of `@Mapping` lines inferred from the relaxed CSV and the `sample_pacs002.xml`.
- Apply that skeleton to the repo and run the compile/test cycle and report results.

Tell me which follow-up you want next.
