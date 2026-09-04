$src = Join-Path $PSScriptRoot 'build\libs\armorvisibility-1.0.0.jar'
$dest = Join-Path $env:APPDATA '.minecraft\mods'
New-Item -ItemType Directory -Path $dest -Force | Out-Null
Copy-Item -Path $src -Destination $dest -Force
$paths = @(
    'C:\Program Files\Minecraft Launcher\MinecraftLauncher.exe',
    'C:\Program Files (x86)\Minecraft Launcher\MinecraftLauncher.exe',
    'C:\Program Files\Minecraft\Minecraft Launcher\MinecraftLauncher.exe',
    'C:\Program Files (x86)\Minecraft\Minecraft Launcher\MinecraftLauncher.exe',
    (Join-Path $env:LOCALAPPDATA 'Programs\Minecraft Launcher\MinecraftLauncher.exe')
)
$started = $false
foreach ($p in $paths) {
    if (Test-Path $p) {
        Start-Process -FilePath $p
        Write-Output "LAUNCHER_STARTED:$p"
        $started = $true
        break
    }
}
if (-not $started) { Write-Output 'LAUNCHER_NOT_FOUND' }
