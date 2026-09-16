# EnderChest

This project is a fork and reconstruction of [EnderChest by fernsehheft](https://modrinth.com/plugin/enderchest), based on `EnderChest-2.4.0.jar`.

## Layout

- `src/main/java` — Java source, arranged by package.
- `src/main/resources` — plugin descriptor and default configuration.
- `reference/original` — the unmodified source JAR.
- `reference/original-meta` — metadata extracted from that JAR; it is reference material, not build input.
- `reference/embedded-annotations` — IDE annotations accidentally shaded into the original JAR; excluded from the plugin source build.
- `docs` — decompilation notes.

## Build

Use JDK 25 and Paper 1.21.11, then run:

```powershell
gradle build
```

The deployable shaded plugin JAR is written to `build/libs/EnderChest-2.4.0.jar`.

This project targets Paper 1.21.11. User-editable messages use MiniMessage;
legacy `&`/`§` values are migrated automatically on startup.

## Reconstruction note

This is reconstructed source, not the original source distribution. The original JAR and extracted reference material are retained under `reference/`; the build uses maintained source dependencies rather than compiling the original shaded libraries.
