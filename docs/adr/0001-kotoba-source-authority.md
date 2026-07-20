# ADR 0001: Kotoba is the API catalog source authority

- Status: Accepted
- Date: 2026-07-21

## Context

The former CLJC catalog exposed unbounded host maps, sequences, and sets. Its
technical-standard citation has two topics, while the organization citation
has one, so topic cardinality and order must remain explicit.

## Decision

`src/association_facts.kotoba` is the sole production source. Both citations
retain every scalar field. Topic count plus indexed access preserves the
governance singleton and the ordered technical-standard/safety pair. The
bounded topic query returns ordered entry ids.

Unknown associations, fields, topics, negative indexes, and out-of-range
indexes return zero or typed option-none. The source declares no effects.
DataScript EDN remains a derived provider artifact.

CI executes reference semantics, restricted JavaScript, and instantiated typed
WebAssembly, and rejects production `.clj`, `.cljc`, and `.cljs` sources.

## Consequences

- Multi-topic entries remain complete without admitting host sets.
- The petroleum organization slug `api` stays distinct from software APIs.
- Clojure and the JVM are compiler/test hosts only.
