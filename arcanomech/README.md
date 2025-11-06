# Arcanomech

Arcanomech is a Fabric-based Minecraft mod that fuses magic and technology. This repository contains the initial project skeleton for iteration M0. It includes:

- Basic Gradle build powered by Fabric Loom.
- Core mod entrypoint and content registration for the Ether Crystal item and Mana Battery block.
- Resource pack stubs for localization, models, blockstates, and a sample recipe.

## Getting Started

1. Install a JDK 17+ environment.
2. Run `./gradlew genSources` to prepare mappings.
3. Use `./gradlew runClient` to launch a development client.

## Project Structure

```
src/main/java/com/example/arcanomech/       # Java sources
src/main/resources/assets/arcanomech/      # Mod assets (lang, models, blockstates)
src/main/resources/data/arcanomech/        # Data pack resources (recipes, loot tables)
```

## License

Released under the MIT License.
