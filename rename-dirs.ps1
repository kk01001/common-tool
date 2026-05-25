# Batch rename directories from kk01001 to archer099
# Usage: Run .\rename-dirs.ps1 in the common-tool root directory

$rootPath = $PSScriptRoot
$oldName = "kk01001"
$newName = "archer099"

$dirs = Get-ChildItem -Path $rootPath -Directory -Recurse -Filter $oldName |
    Sort-Object { $_.FullName.Split([IO.Path]::DirectorySeparatorChar).Count } -Descending

if ($dirs.Count -eq 0) {
    Write-Host "No directories named '$oldName' found" -ForegroundColor Yellow
    exit
}

Write-Host "Found $($dirs.Count) directories to rename:`n" -ForegroundColor Cyan

$successCount = 0
$failCount = 0

foreach ($dir in $dirs) {
    $parentPath = $dir.Parent.FullName
    $newPath = Join-Path $parentPath $newName

    try {
        if (Test-Path $newPath) {
            Write-Host "[MERGE] $($dir.FullName)" -ForegroundColor Yellow
            Get-ChildItem -Path $dir.FullName | Move-Item -Destination $newPath -Force
            Remove-Item -Path $dir.FullName -Recurse -Force
        } else {
            Rename-Item -Path $dir.FullName -NewName $newName -Force
            Write-Host "[OK] $($dir.FullName) -> $newPath" -ForegroundColor Green
        }
        $successCount++
    } catch {
        Write-Host "[FAIL] $($dir.FullName): $_" -ForegroundColor Red
        $failCount++
    }
}

Write-Host "`nDone! Success: $successCount, Failed: $failCount" -ForegroundColor Cyan
