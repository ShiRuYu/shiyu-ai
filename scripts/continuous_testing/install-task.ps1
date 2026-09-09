param(
  [ValidateSet('Install','Remove')][string]$Action = 'Install',
  [string]$TaskName = 'ShiYu-ContinuousTesting'
)
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$python = (Get-Command python).Source
$run = "-m scripts.continuous_testing.daemon"
if ($Action -eq 'Install') {
  $action = New-ScheduledTaskAction -Execute $python -Argument $run -WorkingDirectory $repo
  $trigger = New-ScheduledTaskTrigger -AtLogOn
  $settings = New-ScheduledTaskSettingsSet -RestartCount 3 -RestartInterval (New-TimeSpan -Minutes 1) -ExecutionTimeLimit ([TimeSpan]::Zero)
  Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Settings $settings -Description 'ShiYu recoverable continuous testing daemon' -Force
} else { Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false }
