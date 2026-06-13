---
name: check-skeleton-serializers
description: Use when the user asks to audit, verify, check, or fix serializer annotations for Skeleton classes in the PinFin model module. Covers any request about @file:UseSerializers correctness, missing or redundant serializers, skeleton serializer configuration, or pre-release serializer validation — in Russian or English.
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

## When to use

Any request about Skeleton serializers — audit, check, fix, verify, validate.
Examples: "проверь сериализаторы скелетонов", "check skeleton serializers",
"serializer audit before release", "предрелизная проверка".

## Type → Serializer mapping

| Property type          | Serializer class             | Import                                                        |
|------------------------|------------------------------|---------------------------------------------------------------|
| `MutableStateFlow<T>`  | `MutableStateFlowSerializer`  | `org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer` |
| `NonEmptyList<T>`      | `NonEmptyListSerializer`      | `arrow.core.serialization.NonEmptyListSerializer`               |
| `NonEmptySet<T>`       | `NonEmptySetSerializer`       | `arrow.core.serialization.NonEmptySetSerializer`                |
| `Either<L, R>`         | `EitherSerializer`            | `arrow.core.serialization.EitherSerializer`                     |
| `Option<T>`             | `OptionSerializer`            | `arrow.core.serialization.OptionSerializer`                     |

Required import for the annotation itself: `import kotlinx.serialization.UseSerializers`.

## Algorithm

### Phase 1 — Analyse

1. Find all `data class Skeleton` definitions under `model/src/commonMain/kotlin/`.
2. For each Skeleton, inspect **only the property declarations in the class body** (not companion objects, not nested types, not other classes in the file):
   - Scan the full property type including generic arguments for any of the five types above.
   - E.g. `MutableStateFlow<Option<T>>` requires both `MutableStateFlowSerializer` and `OptionSerializer`.
3. Also inspect the file's `@file:UseSerializers(...)` annotation (if present):
   - Which serializer classes are listed?

### Phase 2 — Report & Fix

For each file that HAS a Skeleton:

1. **Determine required serializers** — the set of serializer classes needed based on the Skeleton's own property types (including type arguments).
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

- All five types (`MutableStateFlow`, `NonEmptyList`, `NonEmptySet`, `Either`, `Option`) appearing in the property type **including generic arguments** matter. E.g. `MutableStateFlow<Option<T>>` counts as both.
- Nested Skeleton references (e.g. `OtherModel.Skeleton`) do **not** count — their serializers are handled by their own files.
- Trailing commas in the annotation argument list are project convention.
- Other `@Serializable` classes in the same file (e.g. sealed interfaces, other data classes) are ignored — only the Skeleton is checked.
- The annotation line is `@file:UseSerializers(` (opening paren on same line), then one serializer per line, then `)`.
