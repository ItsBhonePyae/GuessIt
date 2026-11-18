# Lists every running java.exe with its role, memory and launch flags, so the
# GuessIt JVM can be told apart from IntelliJ's build server and Maven's JVMs.
# For the GuessIt process it also prints the JVM heap breakdown.
#
# Usage (run while the 3 GB reading is visible):
#   powershell -ExecutionPolicy Bypass -File .\tools\java-memory.ps1

$processes = Get-CimInstance Win32_Process -Filter "Name='java.exe'" | ForEach-Object {
    $command = [string]$_.CommandLine
    $role = if ($command -match 'Testing_for_bascially_everything\.([A-Za-z_0-9]+)') {
        "app:$($Matches[1])"
    }
    elseif ($command -match 'org\.jetbrains\.jps') {
        'IntelliJ build server'
    }
    elseif ($command -match 'maven|mvnw') {
        'Maven'
    }
    else {
        'other Java'
    }

    $process = Get-Process -Id $_.ProcessId -ErrorAction SilentlyContinue
    [pscustomobject]@{
        PID          = $_.ProcessId
        Role         = $role
        WorkingSetMB = [Math]::Round($_.WorkingSetSize / 1MB)
        PrivateMB    = if ($process) { [Math]::Round($process.PrivateMemorySize64 / 1MB) } else { 0 }
        PeakWSMB     = if ($process) { [Math]::Round($process.PeakWorkingSet64 / 1MB) } else { 0 }
        Xmx          = if ($command -match '-Xmx(\S+)') { $Matches[1] } else { 'DEFAULT (uncapped)' }
        JavaHome     = if ($_.ExecutablePath) { Split-Path $_.ExecutablePath -Parent } else { '' }
        Agents       = (($command | Select-String -AllMatches -Pattern '-javaagent:\S+|-agentlib:\S+' -ErrorAction SilentlyContinue).Matches.Value -join ' ')
    }
}

$processes | Sort-Object WorkingSetMB -Descending | Format-Table -AutoSize

$app = $processes | Where-Object { $_.Role -like 'app:*' } |
    Sort-Object WorkingSetMB -Descending | Select-Object -First 1

if (-not $app) {
    Write-Host "GuessIt is not running." -ForegroundColor Yellow
    return
}

Write-Host ""
Write-Host "GuessIt PID $($app.PID), Xmx=$($app.Xmx), agents=$(if ($app.Agents) { $app.Agents } else { 'none' })"
if ($app.Xmx -like 'DEFAULT*') {
    Write-Host "Warning: the heap cap is not applied. Use the 'SlidingSidebar_test' run configuration." -ForegroundColor Yellow
}

$jcmd = Join-Path $app.JavaHome 'jcmd.exe'
if (Test-Path $jcmd) {
    Write-Host ""
    Write-Host "JVM heap:"
    & $jcmd $app.PID GC.heap_info 2>$null | ForEach-Object { "  $_" }

    if ($app.Agents -match 'Profiler|JFR|coverage|jetbrains') {
        Write-Host ""
        Write-Host "An IDE agent is attached, so its buffers are included in the process memory." -ForegroundColor Yellow
    }
}
else {
    Write-Host "jcmd not found next to the running JVM." -ForegroundColor Yellow
}
