# Orbital Railgun Enhanced — Minecraft 26.2

A common-code port of [Mishkis/orbital-railgun](https://github.com/Mishkis/orbital-railgun) and [KingIronMan2011/orbital-railgun-enhanced](https://github.com/KingIronMan2011/orbital-railgun-enhanced), retaining their MIT notices and original assets.

## Install

Use **one** jar matching your loader, on the client and server. Minecraft **26.2** and **Java 25** are required.

| Loader | Tested version | Required additional mods | Jar in `build/libs/` |
| --- | --- | --- | --- |
| Fabric | 0.19.5 | Fabric API 0.159.0+26.2 | `orbital-railgun-fabric-2.0.4+26.2.jar` |
| Quilt | 0.31.0-beta.4 | Fabric API 0.159.0+26.2 | `orbital-railgun-quilt-2.0.4+26.2.jar` |
| NeoForge | 26.2.0.82 | None | `orbital-railgun-neoforge-2.0.4+26.2.jar` |
| Forge | 65.1.3 | None | `orbital-railgun-forge-2.0.4+26.2.jar` |

GeckoLib, owo-lib and Satin are **optional**. The mod uses Minecraft's item models, configuration screen, sound engine and post-processing API. Install compatible versions of those libraries if other mods need them. Do not install an old Minecraft version's library jar.

## Play

The railgun is in the Combat creative tab. Hold **Use / right click** to aim and press **Attack / left click** to fire at loaded terrain within your render distance, including diagonal and elevated views. The server independently validates the held railgun and traces the target using your requested view distance. Targeting does not generate unloaded chunks. Pull begins at 20 seconds; impact begins at 35 seconds. By default, the strike removes a radius-24 column through the world's entire build height, including bedrock. Block removal is spread across ticks to limit a single tick's workload. Effects finish at 53 seconds.

**Impact camera shake** and **Shake intensity** are local controls in Railgun settings. Shake defaults to ON at 35%, begins at impact, weakens with distance, and decays over three seconds. It changes only the rendered view, never player position or aiming rotation. The vertical blue particle stream has been removed; the terrain ring particles remain controlled by the Particles setting.

Crafting recipe, top to bottom:

| | | |
| --- | --- | --- |
| Glass pane | Netherite ingot | Emerald |
| Nether star | Netherite block | Netherite ingot |
| Lapis lazuli | Redstone | Empty |

**Photosensitivity:** the original effects include flashes, moving light and chromatic aberration. In the pause menu, open **Railgun settings** to disable shader effects or all visual effects and adjust the three sound volumes/toggles.

Client settings retain `config/orbital-railgun-enhanced.json5`. Server settings retain `config/orbital-railgun-enhanced-server-config.json`. Open **Railgun settings → Gameplay settings** to edit every original server setting, plus crater and pull radii. Numeric fields use an explicit Apply button. Changes require the world owner or operator permission and are synchronized to connected players.

| Setting | Default | Allowed values |
| --- | --- | --- |
| Strike damage | 20 | 0–100000 |
| Sound/effect range | 500 blocks | 0–2048 |
| Crater radius | 24 blocks | 1–64 |
| Pull radius | 24 blocks | 0–128 |
| Cooldown | 100 ticks | 0–72000 |
| Maximum active strikes | 10 | 1–100 |
| Particles / pull / destroy bedrock | ON | ON/OFF |
| Debug logging | OFF | ON/OFF |

Operator commands `/ore` and `/orbitalrailgun` support `help`, `radius`, `strikeRadius`, `pullRadius`, `debug`, `strikeDamage`, `cooldown`, `maxStrikes`, `particles`, `pull`, `bedrock`, and `reload`. `radius` retains its original meaning: horizontal sound/effect range. Crater radius is captured when a shot fires and also scales its aiming preview and visual effects. Existing strikes keep their original size when the config changes. During the last five seconds, strike lighting, beam and colour distortion smoothly blend back to the normal scene.

The **Enable pull** and **Destroy bedrock** buttons in Railgun settings control the server's `enablePull` and `destroyBedrock` options. Both default to ON. Operators can also use `/ore pull false` and `/ore bedrock false`. Pull only affects entities within the configured spherical pull radius around the strike point, and creative/spectator players are immune. With bedrock destruction OFF, other blocks still clear normally. These settings apply immediately, including to active strikes.

## Build and verify

Set `JAVA_HOME` to a JDK 25 installation, then run:

```powershell
.\gradlew.bat clean build
```

On this machine, the JDK is `C:\Program Files\Zulu\zulu-25`. The configured system `JAVA_HOME` may need overriding in the current shell.

The `common` module owns gameplay, rendering, assets and tests. `fabric`, `quilt`, `neoforge`, and `forge` provide their loaders' registry and networking adapters. Each output jar contains the common code. The Quilt build uses Quilt Loader's Fabric compatibility layer and requires Fabric API. `legacy/` preserves the previous port's sources for comparison; Gradle does not compile it.

Development clients: `./gradlew :fabric:runClient`, `:quilt:runClient`, `:neoforge:runClient`, or `:forge:runClient`.

For an automated real-client test, run `./scripts/Smoke-Test.ps1`. It creates disposable flat worlds, fires a railgun, checks range exit/reentry, verifies bedrock removal and the crater boundary, captures screenshots, then exits. Leave its test client controls alone while it runs. `-Loader fabric`, `quilt`, `neoforge`, or `forge` limits the run. Smoke code is opt-in and release packaging rejects `-PsmokeTest`.

See [validation results](docs/VALIDATION.md) and [porting decisions](docs/PORTING.md).

## Credits and license

Mishkis: original mod and assets. KingIronMan2011: enhanced fork. HyIsNoob: enhanced sound effects. MIT: [LICENSE](LICENSE), [original notice](LICENSE-original.txt), [NOTICE](NOTICE.md).

The separate `Satin API/` repository contains an optional LGPL port experiment. It is not needed to build or run this mod and is not a drop-in replacement for every historical Satin API.
