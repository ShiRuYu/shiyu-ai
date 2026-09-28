[CmdletBinding()]
param(
    [string]$AppImageDirectory = (Join-Path $PSScriptRoot "..\dist\windows-x64\ShiYu"),
    [ValidateRange(1024, 65535)]
    [int]$Port = 19091
)

$ErrorActionPreference = "Stop"
$appRoot = (Resolve-Path -LiteralPath $AppImageDirectory).Path
$java = Join-Path $appRoot "runtime\bin\java.exe"
$javaw = Join-Path $appRoot "runtime\bin\javaw.exe"
$launcher = Join-Path $appRoot "ShiYu.exe"
$applicationDirectory = Join-Path $appRoot "app"

foreach ($requiredFile in @($launcher, $java, $javaw)) {
    if (!(Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
        throw "The app-image is missing a required executable: $requiredFile"
    }
}

$backendJars = @(Get-ChildItem -LiteralPath $applicationDirectory -Filter "shiyu-ai-bootstrap-*.jar" -File)
if ($backendJars.Count -ne 1) {
    throw "Expected exactly one backend bootstrap Jar in $applicationDirectory, found $($backendJars.Count)."
}

$tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
$appHome = Join-Path $tempRoot ("ShiYu-console-smoke-" + [guid]::NewGuid().ToString("N"))
$tempPrefix = $tempRoot.TrimEnd([System.IO.Path]::DirectorySeparatorChar, [System.IO.Path]::AltDirectorySeparatorChar) `
    + [System.IO.Path]::DirectorySeparatorChar
New-Item -ItemType Directory -Path $appHome | Out-Null

$process = $null
$client = [System.Net.Http.HttpClient]::new()
try {
    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $javaw
    $startInfo.WorkingDirectory = $appRoot
    $startInfo.UseShellExecute = $false
    $startInfo.CreateNoWindow = $true
    foreach ($argument in @(
        "-Dapp.home=$appHome",
        "-jar",
        $backendJars[0].FullName,
        "--spring.profiles.active=windows",
        "--server.port=$Port",
        "--shiyu.console.announce-startup-link=false"
    )) {
        $startInfo.ArgumentList.Add($argument)
    }
    $process = [System.Diagnostics.Process]::Start($startInfo)
    $baseUri = [Uri]"http://127.0.0.1:$Port/"
    $pageResponse = $null
    $deadline = [DateTime]::UtcNow.AddSeconds(90)
    while ([DateTime]::UtcNow -lt $deadline) {
        if ($process.HasExited) {
            throw "The packaged backend exited during startup (code $($process.ExitCode))."
        }
        try {
            $pageResponse = $client.GetAsync([Uri]::new($baseUri, "console/")).GetAwaiter().GetResult()
            if ($pageResponse.IsSuccessStatusCode) { break }
        }
        catch {
            # 内嵌数据库和应用上下文初始化期间继续轮询。
        }
        Start-Sleep -Milliseconds 500
    }
    if ($null -eq $pageResponse -or !$pageResponse.IsSuccessStatusCode) {
        throw "打包后的后端未能在 90 秒内提供 /console/。"
    }
    $html = $pageResponse.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    if ($html -notmatch '<div id="app"></div>') {
        throw "The packaged console HTML does not contain the Vue application mount point."
    }
    $assetMatch = [regex]::Match($html, 'src="([^"]+\.js)"')
    if (!$assetMatch.Success) { throw "The packaged console HTML does not reference its JavaScript bundle." }
    $assetUri = [Uri]::new($baseUri, $assetMatch.Groups[1].Value)
    $assetResponse = $client.GetAsync($assetUri).GetAwaiter().GetResult()
    if (!$assetResponse.IsSuccessStatusCode) { throw "The packaged console JavaScript bundle did not load." }
    $managementResponse = $client.GetAsync([Uri]::new($baseUri, "console/api/runtime")).GetAwaiter().GetResult()
    if ($managementResponse.StatusCode -ne [System.Net.HttpStatusCode]::Unauthorized) {
        throw "The management API must reject an unauthenticated request; got HTTP $([int]$managementResponse.StatusCode)."
    }
    Write-Output "Windows app-image smoke test passed: bundled Java 21, backend Jar, /console/ assets, and unauthenticated management API boundary."
}
finally {
    $client.Dispose()
    if ($null -ne $process) {
        if (!$process.HasExited) {
            $process.Kill()
            if (!$process.WaitForExit(15000)) { throw "Could not stop the packaged smoke-test backend process." }
        }
        $process.Dispose()
    }
    $resolvedHome = [System.IO.Path]::GetFullPath($appHome)
    if ($resolvedHome.StartsWith($tempPrefix, [System.StringComparison]::OrdinalIgnoreCase) -and ((Split-Path $resolvedHome -Leaf) -match '^ShiYu-console-smoke-[0-9a-f]{32}$')) {
        Remove-Item -LiteralPath $resolvedHome -Recurse -Force
    }
}
