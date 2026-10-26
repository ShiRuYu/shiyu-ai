[CmdletBinding()]
param(
    [ValidateSet('baseline', 'upgrade-quota', 'upgrade-conversation-idempotency', 'upgrade-generation-postprocessing', 'dump', 'restore')]
    [string]$Action = 'baseline',
    [ValidateSet('postgresql', 'mysql')]
    [string]$Provider = 'postgresql',
    [string]$File = '.\shiyu-db.dump'
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

function Require-Command([string]$Name) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' was not found on PATH. Install the $Provider client tools first."
    }
}

function Invoke-Postgres {
    param([string]$Operation)
    if ([string]::IsNullOrWhiteSpace($env:PGDATABASE)) {
        throw 'PGDATABASE must be provided by the deployment environment.'
    }
    if ($Operation -in @('baseline', 'upgrade-quota', 'upgrade-conversation-idempotency', 'upgrade-generation-postprocessing')) {
        Require-Command 'psql'
    } elseif ($Operation -eq 'dump') {
        Require-Command 'pg_dump'
    } else {
        Require-Command 'pg_restore'
    }

    if ($Operation -eq 'baseline') {
        & psql --set ON_ERROR_STOP=1 --single-transaction `
            --file (Join-Path $repoRoot 'scripts/database/postgresql/baseline-schema.sql') `
            --file (Join-Path $repoRoot 'scripts/database/postgresql/baseline-seed.sql')
    } elseif ($Operation -eq 'upgrade-quota') {
        & psql --set ON_ERROR_STOP=1 --single-transaction `
            --file (Join-Path $repoRoot 'scripts/database/postgresql/20261024_quota_state.sql')
    } elseif ($Operation -eq 'upgrade-conversation-idempotency') {
        & psql --set ON_ERROR_STOP=1 --single-transaction `
            --file (Join-Path $repoRoot 'scripts/database/postgresql/20261024_conversation_idempotency.sql')
    } elseif ($Operation -eq 'upgrade-generation-postprocessing') {
        & psql --set ON_ERROR_STOP=1 --single-transaction `
            --file (Join-Path $repoRoot 'scripts/database/postgresql/20261024_generation_postprocessing.sql')
    } elseif ($Operation -eq 'dump') {
        & pg_dump --format=custom --file $File
    } else {
        & pg_restore --exit-on-error --clean --if-exists --dbname $env:PGDATABASE $File
    }
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL $Operation failed with exit code $LASTEXITCODE." }
}

function Invoke-MySqlFile([string]$Database, [string]$Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "MySQL input file '$Path' does not exist."
    }

    $mysqlPath = (Get-Command 'mysql' -ErrorAction Stop).Source
    $databaseArgument = '"' + $Database.Replace('"', '\"') + '"'
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $mysqlPath
    $startInfo.Arguments = "--binary-mode --default-character-set=utf8mb4 --database=$databaseArgument"
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardInput = $true
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    if (-not $process.Start()) { throw 'Unable to start the MySQL client.' }
    $outputTask = $process.StandardOutput.ReadToEndAsync()
    $errorTask = $process.StandardError.ReadToEndAsync()
    try {
        $input = [System.IO.File]::OpenRead($Path)
        try {
            $input.CopyTo($process.StandardInput.BaseStream)
        } finally {
            $input.Dispose()
        }
    } finally {
        $process.StandardInput.Close()
    }
    $process.WaitForExit()
    [Console]::Out.Write($outputTask.GetAwaiter().GetResult())
    [Console]::Error.Write($errorTask.GetAwaiter().GetResult())
    return $process.ExitCode
}

function Invoke-MySql {
    if ([string]::IsNullOrWhiteSpace($env:MYSQL_DATABASE)) {
        throw 'MYSQL_DATABASE must be provided by the deployment environment.'
    }
    if ($Action -eq 'dump') {
        Require-Command 'mysqldump'
        & mysqldump --single-transaction --routines --triggers --hex-blob --result-file=$File $env:MYSQL_DATABASE
        if ($LASTEXITCODE -ne 0) { throw "MySQL $Action failed with exit code $LASTEXITCODE." }
        return
    }

    Require-Command 'mysql'
    if ($Action -eq 'upgrade-quota') {
        $upgradeFile = Join-Path $repoRoot 'scripts/database/mysql/20261024_quota_state.sql'
    } elseif ($Action -eq 'upgrade-conversation-idempotency') {
        $upgradeFile = Join-Path $repoRoot 'scripts/database/mysql/20261024_conversation_idempotency.sql'
    } elseif ($Action -eq 'upgrade-generation-postprocessing') {
        $upgradeFile = Join-Path $repoRoot 'scripts/database/mysql/20261024_generation_postprocessing.sql'
    } else {
        $upgradeFile = $File
    }
    $exitCode = Invoke-MySqlFile $env:MYSQL_DATABASE $upgradeFile
    if ($exitCode -ne 0) { throw "MySQL $Action failed with exit code $exitCode." }
}

if ($Action -eq 'baseline' -and $Provider -ne 'postgresql') {
    throw 'The checked-in baseline is PostgreSQL. Provision MySQL from an approved MySQL dump, then set the baseline marker before switching the application provider.'
}

if ($Provider -eq 'postgresql') { Invoke-Postgres $Action } else { Invoke-MySql }
