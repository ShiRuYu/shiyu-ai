param(
    [string]$Output = "dist\offline-windows"
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
Push-Location $repo
try {
    mvn -Pprod,offline-models -DskipTests -Djacoco.skip=true clean package
    $target = Join-Path $repo $Output
    New-Item -ItemType Directory -Force -Path $target | Out-Null
    Copy-Item "shiyu-ai-bootstrap\target\shiyu-ai-bootstrap-*.jar" $target -Force
    foreach ($directory in @("data\db", "data\files", "data\index", "data\models", "data\backups")) {
        New-Item -ItemType Directory -Force -Path (Join-Path $target $directory) | Out-Null
    }
    Copy-Item "README.md" $target -Force
    Write-Host "Offline Windows package created at $target"
}
finally {
    Pop-Location
}
