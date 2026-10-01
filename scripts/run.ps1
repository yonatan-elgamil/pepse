$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$jarPath = if ($env:DANOGL_JAR) { $env:DANOGL_JAR } else { Join-Path $repoRoot 'lib/DanoGameLab.jar' }

& (Join-Path $PSScriptRoot 'build.ps1')

Push-Location $repoRoot
try {
    $classesDir = Join-Path $repoRoot 'build/classes'
    & java -cp "$classesDir;$jarPath" pepse.PepseGameManager
    if ($LASTEXITCODE -ne 0) {
        throw "Game exited with code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
