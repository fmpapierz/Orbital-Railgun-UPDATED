# Minecraft 26.3 port

This release updates the Minecraft 26.2 port to Minecraft 26.3.
The new release version is 2.1.0+26.3.

## Shared changes

- Target Minecraft 26.3 and Java 25, using Gradle 9.6.0 and Loom 1.17.20.
- Move GPU buffer imports and the projection mixin descriptor to RenderPearl.
- Apply post effects at the start of `GameRenderer.render3dHud`, after terrain
  rendering and before the hand clears the world depth buffer.
- Declare shader input/output locations for the new SPIR-V shader compiler.
- Use `Entity.syncVelocity` to synchronize strike pull motion.
- Change the advancement's recipe-unlock condition to `recipes`.
- Update pack metadata for resource format 97.1 and data format 121.0.
- Update ModDevGradle to 2.0.147. Its JST 2.0.11 fixes the anonymous
  `HolderSet.contents` access-transformer error encountered with 2.0.146.

## Loader layout

`common` owns gameplay, configuration, assets, rendering and tests. `fabric` and
`neoforge` own their registry/network adapters. `quilt` compiles the shared Fabric
adapter against Fabric API and launches a real Quilt Loader runtime, with the
Fabric Loader runtime excluded. Its Fabric-format metadata requires Quilt Loader.
Install one loader jar only: the Fabric and Quilt jars use the same mod ID.

Quilt Standard Libraries are discontinued; this port uses Fabric API directly.
See the [Quilt FAQ](https://quiltmc.org/en/about/faq/).

## Forge availability

On 2026-09-17 the official [Forge Maven metadata](https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml)
contained no Minecraft 26.3 builds. The existing Forge adapter is preserved in
`forge/`, but it cannot be compiled or verified until Forge publishes 26.3.
`forge_version` is intentionally empty. The default build produces the three
available loader artifacts. Explicit Forge tasks fail with an explanation.

When a matching Forge release is available, set `forge_version` to its exact
`26.3-...` Maven version in `gradle.properties`. This enables the module and derives
its loader dependency bounds. Then compile, fix any Forge API changes and run
`scripts/Smoke-Test.ps1 -Loader forge` before distributing a Forge jar. Merely
enabling the module does not establish compatibility.

The `legacy/` archive remains available for reference and is excluded from the
build. Build caches, logs and development worlds are not tracked.
