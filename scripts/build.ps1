$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$jarPath = if ($env:DANOGL_JAR) { $env:DANOGL_JAR } else { Join-Path $repoRoot 'lib/DanoGameLab.jar' }

if (-not (Test-Path -LiteralPath $jarPath -PathType Leaf)) {
    throw 'Missing DanoGameLab.jar. Add it to lib/ or set DANOGL_JAR to its absolute path.'
}

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    throw 'javac was not found. Install JDK 17 and add its bin directory to PATH.'
}

Push-Location $repoRoot
try {
    $sourceFiles = @(Get-ChildItem -LiteralPath (Join-Path $repoRoot 'src') -Recurse -Filter '*.java' -File |
        Sort-Object FullName)
    if ($sourceFiles.Count -eq 0) {
        throw 'No Java source files were found under src/.'
    }

    $classesDir = Join-Path $repoRoot 'build/classes'
    if (Test-Path -LiteralPath $classesDir) {
        Remove-Item -LiteralPath $classesDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

    $sourcePaths = @($sourceFiles | ForEach-Object {
        '"' + $_.FullName.Substring($repoRoot.Length + 1).Replace('\', '/') + '"'
    })
    [System.IO.File]::WriteAllLines(
        (Join-Path $repoRoot 'build/sources.txt'),
        [string[]] $sourcePaths,
        [System.Text.UTF8Encoding]::new($false)
    )

    & javac --release 17 -encoding UTF-8 -cp $jarPath -d 'build/classes' '@build/sources.txt'
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed (exit code $LASTEXITCODE)."
    }
    Write-Host 'Build successful: build/classes'
}
finally {
    Pop-Location
}
