---
name: check-skeleton-serializers
description: Use ONLY when the user asks to verify or check that all Skeleton classes have correct @file:UseSerializers annotations before a release, or when the user mentions "Skeleton serializer check", "serializer audit", or "предрелизная проверка Skeleton". This skill audits all data class Skeleton definitions across the PinFin model module to ensure any Skeleton containing MutableStateFlow, NonEmptyList, NonEmptySet or Either properties declares the corresponding serializers at file level — and vice versa, removes redundant serializers.
---

# Check Skeleton Serializers

Audits all `data class Skeleton` definitions in the model module, verifying
that `@file:UseSerializers` contains **exactly** the serializers needed for
the Skeleton's property types — no missing, no redundant.

## Background

Without `@file:UseSerializers`, the kotlinx.serialization compiler plugin
silently falls back to `PolymorphicSerializer` for these types — which is
incorrect and will fail at runtime. The compiler does **not** emit an error
or warning.

## Trigger phrases

- "проверь Skeleton перед релизом"
- "check skeleton serializers"
- "serializer audit"
- "предрелизная проверка"

## Type → Serializer mapping

| Property type          | Serializer class            | Import |
|------------------------|-----------------------------|--------|
| `MutableStateFlow<T>`  | `MutableStateFlowSerializer` | `org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer` |
| `NonEmptyList<T>`      | `NonEmptyListSerializer`     | `arrow.core.serialization.NonEmptyListSerializer` |
| `NonEmptySet<T>`       | `NonEmptySetSerializer`      | `arrow.core.serialization.NonEmptySetSerializer` |
| `Either<L, R>`         | `EitherSerializer`           | `arrow.core.serialization.EitherSerializer` |

Required import for the annotation itself: `import kotlinx.serialization.UseSerializers`.

## Algorithm

### Phase 1 — Analyse

1. Find all `data class Skeleton` definitions under `model/src/commonMain/kotlin/`.
2. For each Skeleton, inspect **only the property declarations in the class body** (not companion objects, not nested types, not other classes in the file):
   - Are any of the four types above used directly?
3. Also inspect the file's `@file:UseSerializers(...)` annotation (if present):
   - Which serializer classes are listed?

### Phase 2 — Report & Fix

For each file that HAS a Skeleton:

1. **Determine required serializers** — the set of serializer classes needed based on the Skeleton's own property types.
2. **Determine declared serializers** — the set actually listed in `@file:UseSerializers`.
3. **Diff them:**
   - `required \ declared` = **missing** → must be added
   - `declared \ required` = **redundant** → must be removed
   - Empty diff both ways = **correct** → no action

**Automatically fix every issue:**
- Add missing serializer classes to `@file:UseSerializers` (trailing comma convention).
- Remove redundant serializer classes from `@file:UseSerializers`.
- If the annotation ends up empty after removing all serializers, delete the whole `@file:UseSerializers` block and remove unused imports (`UseSerializers` and all serializer imports).

### Phase 3 — Verify

Re-read each modified file to confirm the annotation is correct. Report a final summary table.

## Report format

```
| File | Missing | Redundant | Action |
|------|---------|-----------|--------|
```

End with: "X files checked, Y fixed, Z already correct."

## Key details

- Only `MutableStateFlow`, `NonEmptyList`, `NonEmptySet` and `Either` appearing **directly** as property types in the `data class Skeleton` itself matter. Nested Skeleton references do not count.
- Trailing commas in the annotation argument list are project convention.
- Other `@Serializable` classes in the same file (e.g. sealed interfaces, other data classes) are ignored — only the Skeleton is checked.
- The annotation line is `@file:UseSerializers(` (opening paren on same line), then one serializer per line, then `)`.
