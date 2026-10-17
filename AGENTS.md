# Project Rules: Vanilla Reforged (`better mod`)

These rules are mandatory and strictly enforced for all agent interactions in this repository.

---

## 1. Gravitas Reliability Invariants

1. **Ground before mutating**: Always read the relevant source files, configs, and tests before modifying anything. Never assume file content or structure.
2. **Never mark complete without fresh evidence**: A task or bugfix is not complete until verified by executing the appropriate Gradle build/test command after the mutation.
3. **Report actual results**: Every non-trivial response must clearly state:
   - **What Changed**: Specific files, lines, and behavioral delta.
   - **Evidence**: Actual CLI execution output (e.g., Gradle compilation/test results).
   - **Why**: Rationale behind key decisions.

---

## 2. Mandatory Versioning Policy

Every functional code modification, bug fix, utility enhancement, or new feature MUST be accompanied by a version bump in [`gradle.properties`](gradle.properties) under `mod_version`.

### Versioning Rules:
- **Bug Fixes & Utility Changes**:
  - Increments the **PATCH** / utility segment (e.g., `1.0.0` → `1.0.1`).
  - Required for: bug fixes, performance optimizations, utility refactors, code cleanups.
- **New Feature Additions**:
  - Increments the version number (e.g., if on `1.x`, a new feature addition bumps to `2.0` / major version increment as requested, or `1.1.0` if following standard minor feature increments).
  - Every new gameplay feature, new block/item, or expanded mechanic triggers a version bump before the build is finalized.
- **Verification Requirement**:
  - Always run `./gradlew build` (or `./gradlew check`) to verify that the build compiles and tests pass with the new version.

---

## 3. New Feature Prototyping & Isolation Policy

Before any major new feature or experimental system is merged into the main mod:
1. **Isolated Prototype Folder**: Create the new implementation in a dedicated sandbox or test module folder (e.g. `sandbox/<feature-name>` or experimental subproject) rather than modifying `src/main` directly.
2. **Dedicated Test JAR**: Build and output a separate, standalone test JAR (e.g., `<feature-name>-test.jar`) so the feature can be tested in isolation in Minecraft without destabilizing the main mod.
3. **Promotion to Main Mod**: Only after the feature is proven functional, tested, and approved will it be migrated/merged into the main mod (`src/main/...`).
4. **Final Integration**: Once merged into the main mod, update `mod_version` in `gradle.properties`, add JUnit/integration tests in `src/test`, and verify with `./gradlew build`.

