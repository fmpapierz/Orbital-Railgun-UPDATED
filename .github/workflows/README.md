# CI

The build workflow builds the available Minecraft 26.3 artifacts (Fabric, NeoForge, Quilt) and uploads them for review. Forge is disabled until an upstream 26.3 release exists. Nothing is published automatically. CodeQL uses JDK 25 and the root build task. Real client tests run locally through scripts/Smoke-Test.ps1.
