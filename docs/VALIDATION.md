# Minecraft 26.3 validation

Validated locally on 2026-09-17, Windows, Zulu JDK 25, NVIDIA RTX 4090, OpenGL.

| Loader | Version | Compile/package | Real client + integrated server |
| --- | --- | --- | --- |
| Fabric | 0.19.5 / Fabric API 0.160.7+26.3 | PASS | PASS |
| NeoForge | 26.3.0.4-beta | PASS | PASS |
| Quilt | 0.31.0-beta.4 / Fabric API 0.160.7+26.3 | PASS | PASS |
| Forge | No upstream 26.3 release | BLOCKED | Not run |

## Build and artifact checks

- `gradlew.bat clean build`: successful.
- All 10 existing JUnit tests pass: 4 payload/settings tests and 6 targeting/strike-math tests.
- `scripts/Validate-Artifacts.ps1`: all three release jars pass. Common gameplay
  classes, mixins, assets, recipe, metadata and license notices are present.
  Smoke-test classes and mixin entries are absent.
- Release jars and SHA-256 checksums are in `build/libs/`.
- Explicit Forge tasks reject the unavailable dependency with an explanation.
  Supplying a 26.2 Forge dependency is also rejected, preventing a mislabeled jar.

## Gameplay checks

Each loader completed the full opt-in client smoke test and logged all 12 required
completion markers. This exercises world creation, item registration, recipe
loading, settings synchronization and permissions, shot networking, range exit
and reentry, creative/spectator pull immunity, survival pull and its toggle,
bedrock destruction and its toggle, crater radius, cleanup, camera shake, and a
480-block target with 32-chunk render distance.

Final successful logs contain no post-chain/shader compilation, mixin application,
or registry-loading failures. Impact screenshots were visually inspected for all
three loaders, along with Fabric's aiming view. Samples:

| Loader | Aiming | Impact |
| --- | --- | --- |
| Fabric | [Aiming](visual-validation/fabric-aim.png) | [Impact](visual-validation/fabric-impact.png) |
| NeoForge | [Aiming](visual-validation/neoforge-aim.png) | [Impact](visual-validation/neoforge-impact.png) |
| Quilt | [Aiming](visual-validation/quilt-aim.png) | [Impact](visual-validation/quilt-impact.png) |

Logs are retained locally in `build/validation/`; full screenshots and disposable
worlds are in each loader's `run/` directory. Repeat with
`scripts/Smoke-Test.ps1`, or use `-Loader fabric`, `neoforge`, or `quilt`.

## Limits

Runtime tests used Gradle development clients and their integrated servers.
Dedicated servers, remote multiplayer, Vulkan, other operating systems and
third-party modpacks were not tested. NeoForge and Quilt versions are beta
releases. Forge's preserved adapter still needs compilation and runtime testing
when a Minecraft 26.3 release becomes available; see [porting notes](PORTING.md).
