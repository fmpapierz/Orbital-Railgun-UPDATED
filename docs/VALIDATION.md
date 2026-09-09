# Validation matrix

## 2.0.3 targeting and camera shake results

All three loaders passed integrated-world testing on September 8, 2026. Logs: `.porting/range-shake-final.log` (Fabric/NeoForge) and `.porting/range-shake-forge-final.log` (Forge).

- Client and server raycasts found a block 480 blocks away at a 32-chunk render distance. The server accepted the shot and synchronized its exact target back to the client. The same block was outside the six-chunk test range.
- The distant test fixture keeps its chunk loaded, waits for the client to receive it, and holds use before sending the shot. Early test iterations exposed fixture unloading and use/shot timing issues; the final suite passed with those preconditions established.
- At impact, camera shake changed the projection. Disabling it or setting intensity to zero produced no change. Player position, yaw and pitch remained unchanged.
- The existing settings, pull immunity, bedrock protection, configurable crater, range reentry and lighting fade checks also passed.
- Ten unit tests passed. The separate publication copy also passed a clean three-loader build and release artifact audit.

The new controls and distant targeting are shown in [impact-shake-settings.png](config-validation/impact-shake-settings.png) and [render-distance-target.png](config-validation/render-distance-target.png). This remains integrated-server coverage, not a dedicated multiplayer test.

## 2.0.2 configuration and lighting regression results

Fabric, NeoForge and Forge passed real-client integrated-world tests on September 8, 2026. Logs: `.porting/config-all.log` (Fabric) and `.porting/config-neo-forge.log` (NeoForge/Forge). An initial NeoForge test assumed bedrock destruction started enabled; the smoke harness now establishes its boolean preconditions and restores the starting flags afterward.

- Actual numeric edit boxes and Apply buttons changed damage, effect range, crater radius, pull radius, cooldown and strike limit; server snapshots acknowledged every value. Particle and debug buttons also changed and restored correctly.
- A shot fired with radius 12 retained that radius after the setting changed back. Actual block removal preserved the bedrock column 13 blocks from the target.
- All five pull scenarios, both bedrock modes, range reentry, world loading and effect cleanup passed on every loader.
- Screenshots at full strength, halfway through the fade, just before removal and after removal show the beam and scene lighting blending back to normal. Representative images are in [config-validation](config-validation/).
- Eight unit tests passed, including expanded packet round trips, rejection of nonfinite/out-of-range values, configurable circle boundaries, and a monotonic smooth fade reaching zero before removal.

The final `clean build` and release artifact audit passed for all three 2.0.2 jars. These checks do not add dedicated-server or non-operator multiplayer coverage beyond the limitations below.

The three loaders were launched against Minecraft 26.2 with Java 25 and an RTX 4090. Each test created a disposable flat world and then exited after checking the server and client behavior.

| Loader | Loader version | Result | Checks |
| --- | --- | --- | --- |
| Fabric | 0.19.5 + Fabric API 0.159.0+26.2 | PASS (2.0.0) | world load, server validated shot, client packet, range leave/re-entry, bedrock removal, radius boundary, effect cleanup, settings screen |
| NeoForge | 26.2.0.82 | PASS (2.0.0) | same checks |
| Forge | 65.1.3 | PASS (2.0.0) | same checks |

The initial checks did not establish visual parity or verify entity damage. User testing found that the render pass sampled the hand depth buffer, obscuring terrain projection errors. The 2.0.1 regression suite captures the scope opening, aiming ring, strike phases, waves on raised terrain, and beam from two camera positions. It also clicks both server settings, verifies acknowledgements, checks creative/spectator/near/far survival pull and pull-off behavior, and exercises both bedrock settings against actual blocks. Results are recorded below after the runs complete.

The final release build was run after a clean, without smoke sources, and `scripts/Validate-Artifacts.ps1` confirmed the three jars contain their loader metadata, mixins, recipe, shaders, notices, and no smoke classes or smoke mixin names. The unit suite (`:common:test`) passed.

Smoke logs from the latest runs are kept in the local `.porting/` workspace cache, which is ignored by Git. Run `scripts/Smoke-Test.ps1` to reproduce them; it takes roughly two minutes per loader because the impact is intentionally tested at the original 700-tick timing.

The tests co-loaded GeckoLib 5.5.5 and Satin 4.0.0+26.2 on Fabric/NeoForge/Forge, and owo-lib 0.13.1+26.2 on Fabric. The railgun itself declares none of these as required dependencies. An Iris shaderpack and a Vulkan backend were not part of this validation matrix; the no-shader geometry fallback was tested.

## 2.0.1 visual and gameplay regression results

All three clients passed on September 8, 2026. These are integrated-server tests; separate dedicated-server multiplayer and non-operator UI flows have not been exercised.

| Check | Fabric | NeoForge | Forge |
| --- | --- | --- | --- |
| Scope opening line, rotating ring, transforming strike and terrain waves (screenshots) | Verified | Verified | Verified |
| Creative pull displacement | 0 | 0 | 0 |
| Spectator pull displacement | 0 | 0 | 0 |
| Survival 80 blocks from strike, horizontal displacement | 0 | 0 | 0 |
| Survival nearby, movement toward strike | 6.10 blocks | 6.02 blocks | 5.81 blocks |
| Nearby survival with pull OFF, horizontal displacement | 0 | 0 | 0 |
| Settings buttons and authoritative acknowledgements | PASS | PASS | PASS |
| Bedrock OFF retains bedrock while adjacent stone clears | PASS | PASS | PASS |
| Bedrock ON clears bedrock | PASS | PASS | PASS |
| World load, shot synchronization, range reentry, crater boundary and cleanup | PASS | PASS | PASS |

Pull probes run for 25 ticks. Their coordinates avoid the stone pillars used to test projected waves. Earlier probe failures were caused by teleporting inside those pillars, which triggered Minecraft's collision correction; the checks were rerun in clear space. Every asynchronous check must complete before the final PASS marker.

Logs: `.porting/visual-settings-fabric2.log`, `.porting/visual-settings-neoforge.log`, `.porting/visual-settings-forge.log`. The first successful Fabric run used the final gameplay/rendering code before the version-string and help-text update. Six unit tests passed, including settings-packet flag/permission round trips.

Screenshots are preserved in [visual-validation](visual-validation/): [opening line](visual-validation/01a-opening-line.png), [aiming ring](visual-validation/01c-aim-ring.png), [strike transformation](visual-validation/02b-strike-ring.png), [terrain waves](visual-validation/06-terrain-waves.png), [settings](visual-validation/05-config.png), [beam before moving](visual-validation/09-beam-before-move.png), and [beam after moving](visual-validation/10-beam-after-move.png).
