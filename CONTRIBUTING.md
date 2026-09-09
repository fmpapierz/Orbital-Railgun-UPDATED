# Development

Use JDK 25 and the checked-in Gradle 9.5.1 wrapper. Build with `./gradlew clean build`; run focused unit tests with `./gradlew :common:test`.

Shared code belongs in `common/src/main/java`; assets/data in `common/src/main/resources`. Keep loader APIs inside the three loader modules and use `Platform` for networking. Do not load client classes from a dedicated-server initializer.

When changing networking or effects, run the real client checks in `scripts/Smoke-Test.ps1` and inspect the screenshots and logs. The harness is opt-in under `common/src/smoke` and must never ship. `scripts/Validate-Artifacts.ps1` checks release contents.

Keep MIT notices with upstream-derived assets and source. The optional Satin repository is separately licensed LGPL and does not supply code to the railgun build. `legacy/` is an excluded comparison archive.

Java sources use Google Java Format 1.36.1. Build versions are pinned in the Gradle files. Keep README and the validation matrix accurate; a compiled jar alone is not evidence of a successful world launch.
