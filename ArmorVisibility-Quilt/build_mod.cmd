@echo off
setlocal
cd /d "%~dp0"
powershell -NoProfile -Command "$ErrorActionPreference='Stop'; $version='8.12'; $zipPath = Join-Path $env:TEMP (\"gradle-$version-bin.zip\"); $extractDir = Join-Path $env:TEMP (\"gradle-$version\"); if (-not (Test-Path $zipPath)) { Invoke-WebRequest -Uri \"https://services.gradle.org/distributions/gradle-$version-bin.zip\" -OutFile $zipPath }; if (-not (Test-Path $extractDir)) { Expand-Archive -Path $zipPath -DestinationPath $env:TEMP -Force; Rename-Item -Path (Join-Path $env:TEMP (\"gradle-$version\")) -NewName (\"gradle-$version\") -Force }; $gradleBat = Join-Path $extractDir 'bin\gradle.bat'; if (-not (Test-Path $gradleBat)) { throw \"Gradle not found at $gradleBat\" }; & $gradleBat build --console=plain"
