## Data-Transformator

Data-Transformator is a Java-based, multi-module project that provides a focused ISO20022
payment message transformation capability: it maps pacs.008 (customer credit transfer)
messages to pacs.009 (financial institution credit transfer) messages.

This repository contains a working proof-of-concept (MVP) showing a compiled MapStruct
mapper and a small Spring Boot REST facade that accepts pacs.008 XML and returns a
mapped pacs.009 XML document.

Quick facts
- Language: Java 21 (toolchain configured in Gradle)
- Build: Gradle (wrapper included)
- Mapping: MapStruct (compile-time mappers)
- ISO20022 model: Prowide `pw-iso20022` (SRU2024) used as the canonical model types

Repository layout
- `mapper-core` — MapStruct mappers and mapping helpers. Contains `Pacs008ToPacs009Mapper`.
- `schema-generator` — placeholder module for future XSD → POJO generation tasks.
- `service` — Spring Boot application exposing a POST endpoint `/transform-payment` backed by the mapper.

What the MVP implements
- A MapStruct-based mapper (`mapper-core`) that performs conservative field mappings
  from Prowide pacs.008 types to pacs.009 types, with reflection-based helpers to
  handle some incompatible or optional structures.
- A Spring Boot `service` module with a single endpoint:
  - POST `/transform-payment` (Content-Type: application/xml) accepts a pacs.008 Document XML
    and returns the transformed pacs.009 Document XML (200 OK).
- An integration test (`TransformControllerIntegrationTest`) that programmatically builds
  a minimal pacs.008 message, posts it to the running Spring context, and asserts the
  response unmarshals to `Pacs00900101`.

How to build
1. Ensure Java 21 is installed and available.
2. From the repository root run:

```bash
./gradlew build
```

How to run the service locally
1. From the repo root run the Spring Boot app (service module):

```bash
./gradlew :service:bootRun
```

2. POST a pacs.008 XML Document to `http://localhost:8080/transform-payment` with header
   `Content-Type: application/xml`. The endpoint returns pacs.009 XML on success.

Design notes and scope
- The mapper is intentionally conservative: unmapped fields are reported at compile time
  but the mapper uses reflection and safe-copy helpers to avoid runtime failures when
  encountering optional or model-version-specific structures.
- The `schema-generator` module is a placeholder for future XJC or other POJO generation
  approaches; currently the project relies on the stable Prowide Java ISO20022 model.

When to present Data-Transformator as a tool vs. library vs. service
- Library/JAR (recommended for developer/engineering audience): the core mapping
  logic lives in `mapper-core` and is packaged as a JAR — this is the cleanest option
  for teams that want to embed transformation logic into existing applications or
  batch processing pipelines.
- Service (recommended for operational/delivery pilots): the `service` module provides
  a runnable Spring Boot service with a REST API. This is ideal for quick integration
  tests, demos, or as a small networked transformation microservice.
- Product/Executive framing: for Senior Executives, present Data-Transformator as a
  focused transformation capability that can be deployed either as an embeddable
  library (for integration into core payment platforms) or as a lightweight service
  (for staging, testing, or as an intermediary microservice). Emphasize that the
  current deliverable is an MVP: production hardening (security, observability,
  performance testing and full field-level mapping coverage) will be required prior
  to enterprise rollout.

Recommended next steps (short list)
- Harden the mapper: expand tests and mapping coverage for all required fields and
  message variants used by your operations.
- Add contract tests (sample messages + expected outputs) and property-based tests
  for corner cases (missing/optional fields, long strings, edge-case currencies).
- Add API validation, auth, logging, and metrics to the `service` module for production readiness.
- Decide on a deployment model: include `mapper-core` as a dependency in host
  applications (recommended) or run the `service` as a containerized microservice.

Contact & contribution
Raise issues or PRs in this repository for additional mapping rules or sample messages
to use as canonical test fixtures.

Additional developer notes — dispatcher & registry (how the translator works)
-----------------------------------------------------------------------

Library vs REST flow
- Library (embedding): import the `mapper-core` JAR and call mappers directly. For
  example, use the generated `Pacs008ToPacs009Mapper.INSTANCE.map(src)` to map
  in-memory Prowide model objects. This is the most efficient integration for
  batch jobs or host applications.
- REST endpoint (service): POST raw XML to `/transform-payment`. The controller
  accepts an optional HTTP header `X-Target-Message-Type` (e.g. `pacs.009`);
  when provided the controller delegates to the dispatcher which routes mapping
  requests using a registry of adapters.

Dispatcher / Registry flow (implemented)
- `TransformController` receives the XML payload and an optional header
  `X-Target-Message-Type`.
- If the header is present the controller delegates to `MessageMappingDispatcher`.
- `DefaultMessageMappingDispatcher` normalizes the requested target type and
  performs best-effort source-type detection (XML root namespace/name) using
  `MessageTypeUtils`.
- `MappingRegistry` holds discovered `MapperAdapter` Spring beans. Each
  `MapperAdapter` advertises which `(sourceType,targetType)` pairs it supports
  via `supports(String sourceType, String targetType)`.
- The registry finds a suitable adapter and the dispatcher invokes its `map(String sourceXml)`
  method. The adapter is responsible for unmarshalling the source XML, calling
  the concrete MapStruct mapper (or other logic), and returning marshalled
  target XML.

Why this design?
- Separation of concerns: the controller stays thin (HTTP concerns), the dispatcher
  focuses on routing, adapters encapsulate mapping logic and marshalling details.
- Extensibility: add a new adapter bean (for example `Pacs008ToPacs002Adapter`) to
  support new source/target pairs without changing controller or dispatcher code.
- Discoverability: adapters are simple Spring components and are auto-wired into
  the registry so adding new mappers is low-friction.

Usage examples (REST)
- Default behaviour (no header): legacy behaviour is preserved — controller will
  map pacs.008 -> pacs.009 using the original mapper.
- Explicit target header (preferred):

  curl -X POST http://localhost:8080/transform-payment \
    -H "Content-Type: application/xml" \
    -H "X-Target-Message-Type: pacs.009" \
    --data-binary @sample_pacs008.xml

  This instructs the service to normalize the header value and route to the
  appropriate adapter (if registered).

Scaling suggestions
- Registry & discovery: keep using a registry but extend it to support priorities
  and more flexible matching (namespaces, versions, message families).
- Adapter lifecycle: consider adding health checks per adapter and a lightweight
  capability endpoint (`GET /mapping-capabilities`) listing supported source/target
  mappings for operational visibility.
- Pluggable transformers: adopt an OSGi-like or plugin strategy for adapters if
  you expect third-parties to contribute mappers.
- Performance: cache JAXBContext and reuse marshallers/unmarshallers; measure
  and add a thread-pooled endpoint for high throughput.
- Testing: maintain a matrix of sample inputs and expected outputs. Automate
  contract tests that run on each adapter and the REST wrapper.

Files touched in this change
- `mapper-core/src/main/java/org/translator/mapper/MessageTypeUtils.java` — normalization
  and XML root-name detection utilities.
- `mapper-core/src/main/java/org/translator/mapper/MapperAdapter.java` — adapter interface.
- `service/src/main/java/org/translator/service/Pacs008ToPacs009Adapter.java` — adapter
  wrapping existing mapper.
- `service/src/main/java/org/translator/service/Pacs008ToPacs002Adapter.java` — stub adapter
  that demonstrates how to plug new targets.
- `service/src/main/java/org/translator/service/MappingRegistry.java` — registry of adapters.
- `service/src/main/java/org/translator/service/DefaultMessageMappingDispatcher.java` — dispatcher
  that uses the registry.

Next engineering steps (suggested)
1. Add a `GET /mapping-capabilities` endpoint returning the registry capabilities.
2. Add adapter-level unit/integration tests and example fixtures for pacs.002 if
   you plan to support it.
3. Replace heuristic detection with full XML schema/version negotiation if you
   require strict correctness between versions.

