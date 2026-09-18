param([string]$Directory = (Join-Path $PSScriptRoot '../build/libs'))
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$loaders = @('fabric', 'neoforge', 'quilt')
if (Get-ChildItem -LiteralPath $Directory -Filter 'orbital-railgun-forge-*.jar') { $loaders += 'forge' }
foreach ($loader in $loaders) {
    $files = @(Get-ChildItem -LiteralPath $Directory -Filter "orbital-railgun-$loader-*.jar" | Where-Object Name -NotLike '*-sources.jar')
    if ($files.Count -ne 1) { throw "Expected exactly one release jar for $loader" }
    $jar = $files[0]
    $zip = [IO.Compression.ZipFile]::OpenRead($jar.FullName)
    try {
        $names = @($zip.Entries.FullName)
        $required = @('LICENSE', 'LICENSE-original.txt', 'NOTICE.md', 'pack.mcmeta',
            'io/github/kingironman2011/orbital_railgun_enhanced/OrbitalRailgun.class',
            'io/github/kingironman2011/orbital_railgun_enhanced/mixin/ServerMixin.class',
            'assets/orbital_railgun_enhanced/post_effect/strike.json',
            'data/orbital_railgun_enhanced/recipe/orbital_railgun.json')
        $required += switch ($loader) {
            fabric { 'fabric.mod.json' }
            quilt { 'fabric.mod.json' }
            neoforge { 'META-INF/neoforge.mods.toml' }
            forge { 'META-INF/mods.toml' }
        }
        foreach ($entry in $required) { if ($entry -notin $names) { throw "$($jar.Name) missing $entry" } }
        if ($loader -in @('fabric', 'quilt')) {
            $reader = [IO.StreamReader]::new($zip.GetEntry('fabric.mod.json').Open())
            try { $metadata = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
            if ($metadata.depends.minecraft -ne '26.3' -or $metadata.version -ne '2.1.0+26.3') {
                throw "$($jar.Name) has incorrect game/mod version metadata"
            }
            if ($loader -eq 'quilt' -and -not $metadata.depends.quilt_loader) {
                throw 'Quilt artifact must require Quilt Loader'
            }
        }
        if ($names | Where-Object { $_ -match '/smoke/|Smoke(Mixin|ServerMixin)' }) { throw 'Smoke classes in release jar' }
        $reader = [IO.StreamReader]::new($zip.GetEntry('orbital_railgun_enhanced.mixins.json').Open())
        try { $config = $reader.ReadToEnd() } finally { $reader.Dispose() }
        if ($config -match 'Smoke') { throw 'Smoke mixins in release configuration' }
        if ($loader -eq 'forge') {
            $reader = [IO.StreamReader]::new($zip.GetEntry('META-INF/MANIFEST.MF').Open())
            try { $manifest = $reader.ReadToEnd() } finally { $reader.Dispose() }
            if ($manifest -notmatch 'MixinConfigs: orbital_railgun_enhanced.mixins.json') { throw 'Forge MixinConfigs manifest missing' }
        }
    } finally { $zip.Dispose() }
    $hash = (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256).Hash
    Write-Output "$($jar.Name) SHA256=$hash"
}
