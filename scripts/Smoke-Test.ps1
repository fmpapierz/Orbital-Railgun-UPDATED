param([ValidateSet('all', 'fabric', 'neoforge', 'forge')][string]$Loader = 'all')
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $root
try {
    $loaders = if ($Loader -eq 'all') { @('fabric', 'neoforge', 'forge') } else { @($Loader) }
    $logDir = New-Item -ItemType Directory -Path 'build/validation' -Force
    foreach ($name in $loaders) {
        $log = Join-Path $logDir.FullName "$name-client.txt"
        & './gradlew.bat' ":${name}:runClient" '-PsmokeTest' 2>&1 | Tee-Object -FilePath $log
        if ($LASTEXITCODE -ne 0) { throw "$name client failed. See $log" }
        foreach ($marker in @('WORLD_LOADED', 'SETTINGS_VERIFIED', 'CONFIG_ALL_VERIFIED', 'CONFIG_RADIUS_VERIFIED', 'CAMERA_SHAKE_VERIFIED', 'RENDER_DISTANCE_TARGET_VERIFIED', 'SHOT_SYNCED', 'RANGE_REENTRY_VERIFIED', 'PULL_VERIFIED', 'BEDROCK_TOGGLE_VERIFIED', 'IMPACT_VERIFIED', 'PASS')) {
            if (-not (Select-String -LiteralPath $log -Pattern "ORE_SMOKE_$marker" -Quiet)) {
                throw "$name did not reach $marker. See $log"
            }
        }
    }
    # Switching off the property invalidates compilation/resources; preserve test logs/worlds.
    & './gradlew.bat' 'build'
    if ($LASTEXITCODE -ne 0) { throw 'Release build failed' }
    & (Join-Path $PSScriptRoot 'Validate-Artifacts.ps1')
} finally { Pop-Location }
