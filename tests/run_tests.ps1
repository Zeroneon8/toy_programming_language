# Ejecuta todos los casos .toy y compara la salida con su .expected.txt.
# Uso (desde la raiz del proyecto): powershell -ExecutionPolicy Bypass -File tests\run_tests.ps1

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$classPath = Join-Path $projectRoot "bin"

$toyFiles = @(Get-ChildItem -Path $PSScriptRoot -Filter *.toy | Sort-Object Name)
$total = $toyFiles.Count
$script:failures = 0

foreach ($file in $toyFiles) {
    $expectedPath = [System.IO.Path]::ChangeExtension($file.FullName, ".expected.txt")

    if (-not (Test-Path $expectedPath)) {
        Write-Host ("SKIP {0}: falta {1}" -f $file.Name, [System.IO.Path]::GetFileName($expectedPath))
        $script:failures++
        continue
    }

    $actual = (& java -cp $classPath Main $file.FullName 2>&1 | Out-String).TrimEnd()
    $expected = (Get-Content -Raw $expectedPath).TrimEnd()

    # Normaliza los finales de linea para comparar sin depender del sistema.
    $actual = $actual -replace "`r`n", "`n"
    $expected = $expected -replace "`r`n", "`n"

    if ($actual -eq $expected) {
        Write-Host ("OK   {0}" -f $file.Name)
    } else {
        Write-Host ("FAIL {0}" -f $file.Name)
        Write-Host "--- esperado ---"
        Write-Host $expected
        Write-Host "--- obtenido ---"
        Write-Host $actual
        $script:failures++
    }
}

Write-Host ""
Write-Host ("Tests: {0}   Fallos: {1}" -f $total, $script:failures)

if ($script:failures -gt 0) {
    exit 1
}
