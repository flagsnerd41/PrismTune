# Working with PrismTune as an AI agent

PrismTune is a clean, modern YouTube Music client for Android written in Kotlin. It follows Material 3 guidelines and prioritizes performance and clarity.

## Rules

1. Always pull the latest changes from `main` before starting work.
2. Commit messages: `type(scope): short description` (e.g. `feat(ui): simplify player`)
3. String edits go only in the default English strings file. Do not touch other language files.
4. Follow Kotlin and Android best practices.
5. Do **not** edit the database schema unless absolutely necessary.
6. Do **not** bump the version number.

## Building & testing

```bash
./gradlew :app:assembleFossDebug
```

Install the resulting APK and verify the change works before opening a PR.
