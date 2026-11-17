<#
    Launches the GuessIt JavaFX demo with low-latency G1 tuning.

    JVM flags cannot be applied from inside a Java class, so they live here and in
    the matching IntelliJ run configuration (SlidingSidebar_test).

    ParallelGCThreads / ConcGCThreads are derived from the machine's core count
    instead of being hardcoded, because a fixed value of 4 starves an 8+ core box
    and oversubscribes a dual-core laptop.

    Usage:
        powershell -ExecutionPolicy Bypass -File .\run-optimized.ps1
        powershell -ExecutionPolicy Bypass -File .\run-optimized.ps1 -SkipBuild

    Optional:
        -MainClass <name>   Defaults to Testing_for_bascially_everything.SlidingSidebar_test
#>
param(
    [string]$MainClass = 'Testing_for_bascially_everything.SlidingSidebar_test',
    [int]$MaxHeapMb = 512,
    [int]$InitialHeapMb = 128,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $root

try {
    if (-not $env:JAVA_HOME) {
        $env:JAVA_HOME = 'C:\Program Files\Java\jdk-24'
    }

    # A JDK that ships JavaFX (e.g. Liberica Full 26) puts its own modules ahead
    # of the Maven jars. JavaFX 26 leaks the decoded frame set on every media
    # pipeline restart (~165 MB per seek/replay, unbounded), so prefer a plain
    # JDK where the JavaFX 21 dependency jars are the ones that resolve.
    $bundledFx = & "$env:JAVA_HOME\bin\java.exe" --list-modules 2>$null |
        Where-Object { $_ -like 'javafx.base@*' } | Select-Object -First 1
    if ($bundledFx) {
        Write-Host "WARNING: $env:JAVA_HOME bundles $bundledFx, which wins over the JavaFX 21 jars." -ForegroundColor Yellow
        Write-Host '         Media playback can leak several GB. Point JAVA_HOME at a JDK without JavaFX (e.g. C:\Program Files\Java\jdk-24).' -ForegroundColor Yellow
    }

    if (-not $SkipBuild) {
        Write-Host 'Compiling...' -ForegroundColor Cyan
        & cmd /c "mvnw.cmd -q -DskipTests compile 2>&1" | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "Maven compile failed with exit code $LASTEXITCODE."
        }

        Write-Host 'Resolving dependencies...' -ForegroundColor Cyan
        & cmd /c "mvnw.cmd -q dependency:build-classpath -Dmdep.outputFile=target\classpath.txt 2>&1" | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "Dependency resolution failed with exit code $LASTEXITCODE."
        }
    }

    $depsFile = Join-Path $root 'target\classpath.txt'
    if (-not (Test-Path $depsFile)) {
        throw "Missing $depsFile. Run without -SkipBuild once."
    }

    $deps = (Get-Content $depsFile -Raw).Trim()
    $fxModules = @('javafx.controls', 'javafx.fxml', 'javafx.media', 'javafx.web', 'javafx.swing')

    # Only JavaFX jars belong on the module path; everything else stays on the
    # classpath so non-modular libraries (vlcj, JNA, AtlantaFX, ...) keep working.
    $modulePath = ($deps -split ';' |
        Where-Object { $_ -match 'javafx-[a-z]+-[\d.]+-win\.jar$' }) -join ';'

    $cores = [Environment]::ProcessorCount
    $parallelGc = [Math]::Max(2, [Math]::Min(8, [int][Math]::Floor($cores / 2)))
    $concurrentGc = [Math]::Max(1, [int][Math]::Floor($parallelGc / 4))

    $jvmArgs = @(
        # A hard ceiling stops the JVM from reserving/committing a 4 GB default
        # heap (25% of RAM) that Task Manager reports as multi-gigabyte usage.
        "-Xms${InitialHeapMb}m",
        "-Xmx${MaxHeapMb}m",
        '-XX:MaxMetaspaceSize=256m',
        '-XX:MaxDirectMemorySize=512m',
        '-XX:+UseG1GC',
        '-XX:MaxGCPauseMillis=30',
        '-XX:InitiatingHeapOccupancyPercent=45',
        '-XX:G1ReservePercent=15',
        # Idle periodic GC lets G1 hand committed heap back instead of keeping
        # a multi-gigabyte region set forever after a burst.
        '-XX:G1PeriodicGCInterval=30000',
        "-XX:ParallelGCThreads=$parallelGc",
        "-XX:ConcGCThreads=$concurrentGc",
        '--enable-native-access=javafx.graphics,javafx.media',
        "--module-path=$modulePath",
        "--add-modules=$($fxModules -join ',')",
        '-cp', "target\classes;$deps"
    )

    Write-Host "Starting $MainClass (heap=${InitialHeapMb}m/${MaxHeapMb}m, cores=$cores, ParallelGCThreads=$parallelGc, ConcGCThreads=$concurrentGc)" -ForegroundColor Green
    & "$env:JAVA_HOME\bin\java.exe" @jvmArgs $MainClass
}
finally {
    Pop-Location
}
