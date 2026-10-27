$ErrorActionPreference = 'Stop'
$previousDatabase = $env:PGDATABASE
$env:PGDATABASE = 'disposable_test_db'
$global:testPsqlCalls = 0
$global:testPsqlFailFirst = $true
$global:testPsqlArguments = @()

function psql {
    $global:testPsqlCalls++
    $global:testPsqlArguments = @($args)
    $global:LASTEXITCODE = if ($global:testPsqlFailFirst -and $global:testPsqlCalls -eq 1) { 1 } else { 0 }
}

try {
    $failed = $false
    try {
        & (Join-Path $PSScriptRoot '..\migrate.ps1') -Action baseline -Provider postgresql
    } catch {
        $failed = $true
    }
    if (-not $failed) {
        throw 'Migration must stop when the schema statement fails, even if seed would succeed.'
    }
    if ($global:testPsqlCalls -ne 1) {
        throw "Migration continued after schema failure: $global:testPsqlCalls psql calls."
    }

    $global:testPsqlCalls = 0
    $global:testPsqlFailFirst = $false
    & (Join-Path $PSScriptRoot '..\migrate.ps1') -Action baseline -Provider postgresql
    if ($global:testPsqlCalls -ne 1) {
        throw "Successful migration must use one transaction: $global:testPsqlCalls psql calls."
    }
    if ('--single-transaction' -notin $global:testPsqlArguments) {
        throw 'Baseline migration must use a PostgreSQL transaction.'
    }
    $files = $global:testPsqlArguments | Where-Object { $_ -like '*baseline-*.sql' }
    if ($files.Count -ne 2) {
        throw 'Baseline migration must include schema and seed scripts.'
    }
    $global:testPsqlCalls = 0
    & (Join-Path $PSScriptRoot '..\migrate.ps1') -Action upgrade-agent-version-delete-flag -Provider postgresql
    if ($global:testPsqlCalls -ne 1 -or '--single-transaction' -notin $global:testPsqlArguments) {
        throw 'Agent version delete-flag migration must use one PostgreSQL transaction.'
    }
    if (-not ($global:testPsqlArguments | Where-Object { $_ -like '*20261027_version_numeric_delete_flag.sql' })) {
        throw 'Agent version delete-flag migration must execute the matching upgrade resource.'
    }
} finally {
    $env:PGDATABASE = $previousDatabase
    Remove-Variable -Name testPsqlCalls -Scope Global
    Remove-Variable -Name testPsqlFailFirst -Scope Global
    Remove-Variable -Name testPsqlArguments -Scope Global
}
