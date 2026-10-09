# 26.2 port decisions

## Enhanced feature mapping

| Upstream feature | 26.2 implementation |
| --- | --- |
| Railgun model, texture, crafting, advancement, Combat tab | Original assets; Gecko geometry converted to native item-model elements |
| Scope zoom, aim overlay and target selection | Vanilla use/zoom behavior, original aim GLSL through native PostChain |
| Delayed orbital strike, pull and custom damage/death message | Shared server tick state with original 400/700/1060 tick timing |
| Bedrock destruction and 24-block circular column | Full build height, four Y layers per tick |
| Equip/scope/fire sounds, independent volumes and toggles | Original audio assets through Minecraft sounds; old config keys retained |
| Area listener and entering/exiting strike range | Horizontal distance, same dimension; start/stop packets and synchronized effect age on reentry |
| Client visual and shader toggles | Native pause-menu settings screen; simple geometry when shaders are disabled |
| Iris compatibility | Optional Iris API detection; suppress own full-screen pass while a shader pack is active |
| Server settings, operator commands, debug logging | Gson settings; `/ore` and `/orbitalrailgun`; range transitions and impact logs |
| Pull and bedrock controls (2.0.1) | Server-authoritative settings screen, host/operator permission checks, `/ore pull` and `/ore bedrock` |
| Translations/subtitles | All 15 language files and original sound subtitles retained |

## Deliberate changes

GeckoLib's static item geometry, owo's configuration UI and Satin's post processing are replaced by native Minecraft APIs. No obligatory library ports are needed. Config is available from the pause menu on all loaders; this build does not register a Mod Menu entry point.

Clients send only shooting intent. The server validates the held/used item, spectator state, cooldown, loaded target, world border and active-strike limit, and raycasts the target itself. Entities are queried during the strike rather than storing stale references. The pull calculation stays finite at distance 20. Live strike state clears when the server stops; strikes are not persisted across restarts.

Destroying all layers in a single tick could freeze a server, so the same crater is removed in bounded batches. The active-strike limit now includes the visual lifetime. Sound resumes from the start of its audio sample on reentry (as in the enhanced fork); the visual timeline resumes from its actual server age.

The original shader equations were moved to GLSL 330, std140 uniform blocks, native frame-graph post chains, and 26.2's reversed-depth reconstruction. The 2.0.1 pass executes before the world depth is cleared for the held item and uses the actual terrain projection, including view bobbing. Scope time counts from the start of item use. This restores the original opening line, rotating terrain crosshair, transforming strike marker and terrain waves. No direct OpenGL calls are made. Geometry and HUD fallback paths remain available. The active Iris-pack path and Vulkan backend have not been validated here; see the exact tested matrix in VALIDATION.md.

Pull is restricted to a 24-block sphere around the fixed strike point, independent of sound range. Creative and spectator players are immune; survival players and other entities remain affected when enabled. `enablePull` and `destroyBedrock` default to true, persist in the server config, and apply immediately to ongoing strikes. Disabling bedrock destruction skips bedrock while still removing other blocks.

Quilt has a dedicated adapter and artifact. It uses Quilt Loader's Fabric compatibility plugin with Fabric API because Quilted Fabric API has no 26.2 build. The Quilt development runs explicitly register the mod resources and Fabric API with Quilt Loader because Fabric Loom only emits Fabric Loader's discovery arguments.

Forge requires both pack metadata and a MixinConfigs manifest. Its development source output is explicitly merged through processResources so incremental builds retain both class files and resources. Mixin uses the JAVA_21 compatibility declaration accepted by all four loaders; Minecraft and the compiled code still require Java 25.

## Upstream sources

- https://github.com/Mishkis/orbital-railgun (MIT; upstream remote)
- https://github.com/KingIronMan2011/orbital-railgun-enhanced at ac99f3c (MIT; original checkout)
- The separate optional Satin experiment is based on https://github.com/Ladysnake/Satin branch 1.21.5, LGPL-3.0-or-later.
