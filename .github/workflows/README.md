# CI

The build workflow builds all three 26.2 loader artifacts and uploads them for review. It does not publish releases. CodeQL uses JDK 25 and the same root build task. Real graphical world tests run locally via scripts/Smoke-Test.ps1.
