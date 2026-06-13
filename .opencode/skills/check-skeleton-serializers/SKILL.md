---
name: check-skeleton-serializers
description: Use ONLY when the user asks to verify or check that all Skeleton classes have correct @file:UseSerializers annotations before a release, or when the user mentions "Skeleton serializer check", "serializer audit", or "предрелизная проверка Skeleton". This skill audits all data class Skeleton definitions across the PinFin model module to ensure any Skeleton containing MutableStateFlow, NonEmptyList or NonEmptySet properties declares the corresponding serializers at file level.
---

# Check Skeleton Serializers

Audits all `data class Skeleton` definitions in the model module to verify that
files whose Skeleton properties include `MutableStateFlow`, `NonEmptyList` or
`NonEmptySet` declare the correct `@file:UseSerializers` annotation.

## Background

Without `@file:UseSerializers`, the kotlinx.serialization compiler plugin
silently falls back to `PolymorphicSerializer` for these types — which is
incorrect and will fail at runtime. The compiler does **not** emit an error
or warning, so this check is essential.

## Trigger phrases

- "проверь Skeleton перед релизом"
- "check skeleton serializers"
- "serializer audit"
- "предрелизная проверка"

## Algorithm

1. Search for all `data class Skeleton` definitions under `model/src/commonMain/kotlin/`.
2. For each Skeleton found, read only its property declarations (not companion objects):
   - Check if any property type is `MutableStateFlow<...>`.
   - Check if any property type is `NonEmptyList<...>`.
   - Check if any property type is `NonEmptySet<...>`.
3. Verify the file declares the corresponding serializers in `@file:UseSerializers`:

   | Property type          | Serializer class            | Import                                                       |
   |------------------------|-----------------------------|--------------------------------------------------------------|
   | `MutableStateFlow<T>`  | `MutableStateFlowSerializer` | `org.hnau.commons.kotlin.serialization.MutableStateFlowSerializer` |
   | `NonEmptyList<T>`      | `NonEmptyListSerializer`     | `arrow.core.serialization.NonEmptyListSerializer`              |
   | `NonEmptySet<T>`       | `NonEmptySetSerializer`      | `arrow.core.serialization.NonEmptySetSerializer`               |

4. If multiple types are used, all corresponding serializers must appear in the annotation.
5. If none of these types are used, the file does **not** need the annotation.

## Report format

Output a concise table:

```
| File | Uses MutableStateFlow | Uses NonEmptyList | Uses NonEmptySet | Has @file:UseSerializers | Issue |
|------|-----------------------|-------------------|------------------|--------------------------|-------|
```

Add a row only for files with issues. End with a summary: "X files checked, Y issues found."

## Key details

- Property types of other `data class Skeleton` (nested Skeleton references) do **not** count — only `MutableStateFlow`, `NonEmptyList` and `NonEmptySet` appearing directly as property types in the Skeleton itself matter.
- The annotation is `@file:UseSerializers` (with `import kotlinx.serialization.UseSerializers`).
- Trailing commas in the annotation argument list are project convention.
- Redundant annotations (present but unnecessary) should be noted as a warning, not an error.
