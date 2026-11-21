# Contributing to DoorDasH

Thank you for your interest in improving **DoorDasH**!

## Architecture Overview

The codebase is split into two packages under `src/`:

- **`game.engine`** — Pure Java game simulation (`Board`, `Game`, `DataLoader`, `Constants`, `cards`, `cells`, `monsters`, `exceptions`, `interfaces`). Keep this package independent of JavaFX so engine logic stays cleanly testable.
- **`MainMenu`** — JavaFX presentation layer (`MainApp`, FXML views, controllers, renderers, animations, sound, and `assets/`).

Game data (`cards.csv`, `cells.csv`, `monsters.csv`) lives at the repository root and is loaded via relative file paths by `DataLoader`.

## Local Development Setup

### Run with Maven (Recommended)

```bash
mvn javafx:run
```

### Run Tests

```bash
mvn test
```

### Run in IntelliJ IDEA

1. Open the repository root in IntelliJ IDEA.
2. Set Project SDK to **JDK 25**.
3. Ensure the working directory in your `MainMenu.MainApp` run configuration points to the repository root (`Door_Dash/`).

## Pull Request Checklist

- Keep engine logic in `src/game/engine/` decoupled from JavaFX classes.
- Use exact, case-sensitive resource paths under `/MainMenu/assets/...` so builds work on Linux, macOS, and inside JAR packages.
- Add or update JUnit tests in `test/` for any changes to `game.engine`.
- Verify `mvn -B test` passes before opening a pull request.
