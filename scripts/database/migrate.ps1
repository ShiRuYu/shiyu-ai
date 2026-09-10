[CmdletBinding()]
param(
    [ValidateSet('baseline', 'dump', 'restore')]
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
    Require-Command $(if ($Operation -eq 'baseline') { 'psql' } elseif ($Action -eq 'dump') { 'pg_dump' } else { 'pg_restore' })
    if ($Operation -eq 'baseline') {
        & psql --set ON_ERROR_STOP=1 --file (Join-Path $repoRoot 'scripts/database/postgresql/baseline-schema.sql')
        & psql --set ON_ERROR_STOP=1 --file (Join-Path $repoRoot 'scripts/database/postgresql/baseline-seed.sql')
    } elseif ($Action -eq 'dump') {
        & pg_dump --format=custom --file $File
    } else {
        & pg_restore --exit-on-error --clean --if-exists --dbname $env:PGDATABASE $File
    }
    if ($LASTEXITCODE -ne 0) { throw "PostgreSQL $Operation failed with exit code $LASTEXITCODE." }
}

function Invoke-MySql {
    if ([string]::IsNullOrWhiteSpace($env:MYSQL_DATABASE)) {
        throw 'MYSQL_DATABASE must be provided by the deployment environment.'
    }
    if ($Action -eq 'dump') {
        Require-Command 'mysqldump'
        & mysqldump --single-transaction --routines --triggers --hex-blob --result-file=$File $env:MYSQL_DATABASE
    } else {
        Require-Command 'mysql'
        & mysql $env:MYSQL_DATABASE --binary-mode --execute "source $File"
    }
    if ($LASTEXITCODE -ne 0) { throw "MySQL $Action failed with exit code $LASTEXITCODE." }
}

if ($Action -eq 'baseline' -and $Provider -ne 'postgresql') {
    throw 'The checked-in baseline is PostgreSQL. Provision MySQL from an approved MySQL dump, then set the baseline marker before switching the application provider.'
}

if ($Provider -eq 'postgresql') { Invoke-Postgres $Action } else { Invoke-MySql }
