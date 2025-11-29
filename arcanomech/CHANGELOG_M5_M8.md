# M5–M8 Changelog

## Changes
- Fixed resource loading across spell, workbench recipe, aspect, and altar structure managers to avoid holding `Resource` objects open and ensure proper stream handling.
- Migrated arcane workbench and crusher recipes onto registered recipe types with data-pack driven serializers and updated block entities to consume them through the vanilla recipe manager.
- Added example workbench and crusher datapack entries demonstrating the expected six-slot workbench layout and crusher work time field.
- Completed mana network balancing with cached components, cable-backed topology, and per-tick redistribution respecting `Balance` IO limits; the balancer now tracks per-edge budgets to prevent teleporting flow and `/am mana info` reports component fill, target ratio, and IO budgets.
- Added a server-only ritual runtime driven by `data/arcanomech/altar/rituals/*.json`, validating pedestals, consuming inputs and mana from the connected network, and issuing rewards with logging.
- Added optional EMI recipe categories for arcane workbench and crusher recipes using existing serializers and items as icons, moved behind a client-only entrypoint.
- Documented mana network behavior in `AGENTS.md` and expanded datapack progression (altars, loot tables, advancements, rituals) under `data/arcanomech`.
- Normalized root build tooling with a Gradle 8.10.2 wrapper, multi-project aggregator tasks, and CI workflow runnable from the repository root.
- Aligned plugin management to resolve Fabric Loom directly from Fabric Maven with shared repositories for all modules and CI builds.

## Deviations
- None.
