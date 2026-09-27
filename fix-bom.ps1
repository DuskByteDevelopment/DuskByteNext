# Byte-level BOM removal - works regardless of PowerShell encoding
$srcRoot = "src\main\java\dev\duskbyte"

Write-Host "=== Byte-level BOM removal ===" -ForegroundColor Yellow
$count = 0
Get-ChildItem -Path $srcRoot -Recurse -Filter "*.java" | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $newBytes = New-Object byte[] ($bytes.Length - 3)
        [Array]::Copy($bytes, 3, $newBytes, 0, $bytes.Length - 3)
        [System.IO.File]::WriteAllBytes($_.FullName, $newBytes)
        $count++
    }
}
Write-Host "  Fixed $count Java files with BOM"

# Fix JSON too
$jsonCount = 0
Get-ChildItem -Path "src\main\resources" -Recurse -Filter "*.json" -ErrorAction SilentlyContinue | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $newBytes = New-Object byte[] ($bytes.Length - 3)
        [Array]::Copy($bytes, 3, $newBytes, 0, $bytes.Length - 3)
        [System.IO.File]::WriteAllBytes($_.FullName, $newBytes)
        $jsonCount++
    }
}
Write-Host "  Fixed $jsonCount JSON files with BOM"

Write-Host "=== Verify: checking for remaining BOM ===" -ForegroundColor Cyan
$remaining = 0
Get-ChildItem -Path $srcRoot -Recurse -Filter "*.java" | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $remaining++
        Write-Host "  STILL HAS BOM: $($_.FullName)" -ForegroundColor Red
    }
}
if ($remaining -eq 0) {
    Write-Host "  All clean!" -ForegroundColor Green
}

Write-Host "=== DONE ===" -ForegroundColor Green
Write-Host "Next: .\gradlew.bat build" -ForegroundColor Cyan
