[CmdletBinding()]
param(
    [string]$Jdk21Home = $env:SHIYU_JDK21_HOME,
    [string]$OutputDirectory = "dist/windows-x64",
    [switch]$SkipMavenBuild
)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$pomPath = Join-Path $repoRoot "pom.xml"

if ([string]::IsNullOrWhiteSpace($Jdk21Home)) {
    throw "Set SHIYU_JDK21_HOME or pass -Jdk21Home with a Windows x64 JDK 21 installation."
}
$Jdk21Home = (Resolve-Path -LiteralPath $Jdk21Home).Path
$javaExe = Join-Path $Jdk21Home "bin\java.exe"
$jpackageExe = Join-Path $Jdk21Home "bin\jpackage.exe"
if (!(Test-Path -LiteralPath $javaExe -PathType Leaf) -or !(Test-Path -LiteralPath $jpackageExe -PathType Leaf)) {
    throw "The selected JDK does not contain bin\java.exe and bin\jpackage.exe."
}
if (![Environment]::Is64BitOperatingSystem) {
    throw "This first release target requires Windows x64."
}
$javaVersion = (& $javaExe -version 2>&1 | Out-String)
if ($LASTEXITCODE -ne 0 -or $javaVersion -notmatch 'version "21(?:\.|\+)') {
    throw "The packaging JDK must be Java 21. Detected: $javaVersion"
}

$maven = $null
if (!$SkipMavenBuild) {
    $maven = Get-Command mvn -ErrorAction SilentlyContinue
    if ($null -eq $maven) {
        throw "Maven is required to build the release artifacts. Install Maven or add mvn to PATH."
    }
}
$outputPath = [System.IO.Path]::GetFullPath((Join-Path $repoRoot $OutputDirectory))
$appImagePath = Join-Path $outputPath "ShiYu"
if (Test-Path -LiteralPath $appImagePath) {
    throw "Output already exists; choose a new OutputDirectory or move the existing release manually: $appImagePath"
}

$pom = [xml](Get-Content -LiteralPath $pomPath -Raw)
$version = $pom.project.properties.revision
if ([string]::IsNullOrWhiteSpace($version)) { throw "Could not read project revision from the root POM." }
$stagingPath = Join-Path ([System.IO.Path]::GetTempPath()) ("ShiYu-jpackage-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $stagingPath | Out-Null

try {
    if (!$SkipMavenBuild) {
        Push-Location $repoRoot
        try {
            & $maven.Source --batch-mode --no-transfer-progress `
                -pl modules/applications/shiyu-ai-bootstrap `
                -am '-DskipTests' '-Ddependency-check.skip=true' package
            if ($LASTEXITCODE -ne 0) { throw "Maven package failed with exit code $LASTEXITCODE." }
        }
        finally {
            Pop-Location
        }
    }

    $backendJar = Join-Path $repoRoot "modules\applications\shiyu-ai-bootstrap\target\shiyu-ai-bootstrap-$version.jar"
    $launcherJar = Join-Path $repoRoot "modules\applications\shiyu-runtime-console\target\shiyu-runtime-console-$version-launcher.jar"
    foreach ($artifact in @($backendJar, $launcherJar)) {
        if (!(Test-Path -LiteralPath $artifact -PathType Leaf)) { throw "Required release artifact is missing: $artifact" }
    }
    Copy-Item -LiteralPath $backendJar -Destination (Join-Path $stagingPath (Split-Path $backendJar -Leaf))
    Copy-Item -LiteralPath $launcherJar -Destination (Join-Path $stagingPath "runtime-console-launcher.jar")
    New-Item -ItemType Directory -Path $outputPath -Force | Out-Null

    & $jpackageExe `
        --type app-image `
        --name ShiYu `
        --app-version $version `
        --vendor ShiYu `
        --input $stagingPath `
        --main-jar runtime-console-launcher.jar `
        --main-class com.shiyu.ai.runtimeconsole.launcher.RuntimeConsoleLauncher `
        --dest $outputPath `
        --jlink-options "--strip-debug --no-header-files --no-man-pages" `
        --add-modules ALL-MODULE-PATH
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed with exit code $LASTEXITCODE." }
    if (!(Test-Path -LiteralPath (Join-Path $appImagePath "runtime\bin\javaw.exe") -PathType Leaf)) {
        throw "The generated runtime is missing javaw.exe, which the launcher requires for the backend child process."
    }
    if (!(Test-Path -LiteralPath (Join-Path $appImagePath "runtime\bin\java.exe") -PathType Leaf)) {
        throw "The generated runtime is missing java.exe, which is required for runtime diagnostics."
    }
    Write-Host "Windows app-image created: $appImagePath"
    Write-Host "This is a self-contained directory containing ShiYu.exe, its Java runtime, and application jars."
}
finally {
    $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
    $resolvedStage = [System.IO.Path]::GetFullPath($stagingPath)
    if ($resolvedStage.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase) -and ((Split-Path $resolvedStage -Leaf) -match '^ShiYu-jpackage-[0-9a-f]{32}$')) {
        Remove-Item -LiteralPath $resolvedStage -Recurse -Force
    }
}
