# Ejecuta los casos .toy y compara la salida con su .expected.txt.
#   - tests\*.toy         -> pruebas del analizador lexico (Main --tokens)
#   - tests\parser\*.toy  -> pruebas del analizador sintactico (Main)
# Uso (desde la raiz del proyecto): powershell -ExecutionPolicy Bypass -File tests\run_tests.ps1

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$classPath = Join-Path $projectRoot "bin"

$script:total = 0
$script:failures = 0

function Invoke-TestFolder {
    param(
        [string]$Folder,
        [string]$Title,
        [string[]]$MainArgs
    )

    Write-Host ("== {0} ==" -f $Title)

    $toyFiles = @(Get-ChildItem -Path $Folder -Filter *.toy | Sort-Object Name)

    foreach ($file in $toyFiles) {
        $script:total++
        $expectedPath = [System.IO.Path]::ChangeExtension($file.FullName, ".expected.txt")

        if (-not (Test-Path $expectedPath)) {
            Write-Host ("SKIP {0}: falta {1}" -f $file.Name, [System.IO.Path]::GetFileName($expectedPath))
            $script:failures++
            continue
        }

        $actual = (& java -cp $classPath Main @MainArgs $file.FullName 2>&1 | Out-String).TrimEnd()
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
}

Invoke-TestFolder -Folder $PSScriptRoot -Title "Analizador lexico" -MainArgs @("--tokens")
Invoke-TestFolder -Folder (Join-Path $PSScriptRoot "parser") -Title "Analizador sintactico" -MainArgs @()

Write-Host ("Tests: {0}   Fallos: {1}" -f $script:total, $script:failures)

if ($script:failures -gt 0) {
    exit 1
}
