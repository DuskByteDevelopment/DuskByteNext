# Fix: Re-copy gui folder from argon that was accidentally deleted
$srcRoot = "src\main\java\dev\duskbyte"
$refRoot = "reference\argon-main\src\main\java\dev\lvstrng\argon"

Write-Host "=== Fix: Re-copy gui ===" -ForegroundColor Yellow

# Re-copy gui
$guiSrc = Join-Path $refRoot "gui"
$guiDst = Join-Path $srcRoot "gui"
Copy-Item -Recurse -Force $guiSrc $guiDst
Write-Host "  Copied gui/"

# Also re-copy font (may have been deleted too)
$fontSrc = Join-Path $refRoot "font"
$fontDst = Join-Path $srcRoot "font"
if (Test-Path $fontSrc) {
    Copy-Item -Recurse -Force $fontSrc $fontDst
    Write-Host "  Copied font/"
}

# Replace package names in newly copied files
$javaFiles = Get-ChildItem -Path $guiDst -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
$count = 0
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $content = $content -replace 'dev\.lvstrng\.argon', 'dev.duskbyte'
    $content = $content -replace '\bArgon\b', 'DuskByte'
    $content = $content -replace 'EncryptedString\.of\("([^"]*)"\)', '"$1"'
    $content = $content -replace '(?m)^\s*import\s+dev\.duskbyte\.utils\.EncryptedString;\s*\r?\n', ''
    $content = $content -replace 'argonJar', 'duskByteJar'
    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
    $count++
}

$javaFiles2 = Get-ChildItem -Path $fontDst -Recurse -Filter "*.java" -ErrorAction SilentlyContinue
foreach ($file in $javaFiles2) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $content = $content -replace 'dev\.lvstrng\.argon', 'dev.duskbyte'
    $content = $content -replace '\bArgon\b', 'DuskByte'
    $content = $content -replace 'EncryptedString\.of\("([^"]*)"\)', '"$1"'
    $content = $content -replace '(?m)^\s*import\s+dev\.duskbyte\.utils\.EncryptedString;\s*\r?\n', ''
    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
    $count++
}

Write-Host "  Modified $count files"

# Verify
if (Test-Path "$guiDst\ClickGui.java") {
    Write-Host "  [OK] ClickGui.java exists" -ForegroundColor Green
} else {
    Write-Host "  [MISSING] ClickGui.java" -ForegroundColor Red
}

Write-Host "=== DONE ===" -ForegroundColor Green
