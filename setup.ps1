# DuskByte - Copy argon code, rename EVERYTHING to duskbyte
# Run from: G:\Dev\DuskByteNext

$srcRoot = "src\main\java\dev\duskbyte"
$refRoot = "reference\argon-main\src\main\java\dev\lvstrng\argon"

# Step 1: Delete all existing source
Write-Host "=== Step 1: Delete old source ===" -ForegroundColor Yellow
if (Test-Path $srcRoot) {
    Remove-Item -Recurse -Force $srcRoot
    Write-Host "  Deleted $srcRoot"
}

# Step 2: Copy all argon source
Write-Host "=== Step 2: Copy argon source ===" -ForegroundColor Yellow
Copy-Item -Recurse -Force $refRoot $srcRoot
Write-Host "  Copied to $srcRoot"

# Step 3: Rename files that contain "Argon" in filename
Write-Host "=== Step 3: Rename files ===" -ForegroundColor Yellow
Get-ChildItem -Path $srcRoot -Recurse -Filter "*Argon*" | ForEach-Object {
    $newName = $_.Name -replace 'Argon', 'DuskByte'
    $newName = $newName -replace 'argon', 'duskbyte'
    Rename-Item $_.FullName -NewName $newName
    Write-Host "  $($_.Name) -> $newName"
}
Get-ChildItem -Path $srcRoot -Recurse -Filter "*argon*" | ForEach-Object {
    $newName = $_.Name -replace 'argon', 'duskbyte'
    $newName = $newName -replace 'Argon', 'DuskByte'
    Rename-Item $_.FullName -NewName $newName
    Write-Host "  $($_.Name) -> $newName"
}

# Step 4: Replace ALL text in ALL files
Write-Host "=== Step 4: Replace all content ===" -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path $srcRoot -Recurse -Filter "*.java"
$count = 0
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $original = $content

    # Package/import: dev.lvstrng.argon -> dev.duskbyte
    $content = $content -replace 'dev\.lvstrng\.argon', 'dev.duskbyte'

    # Class names and references
    $content = $content -replace '\bArgon\b', 'DuskByte'

    # EncryptedString.of("...") -> "..."
    $content = $content -replace 'EncryptedString\.of\("([^"]*)"\)', '"$1"'

    # Remove EncryptedString imports
    $content = $content -replace '(?m)^\s*import\s+dev\.duskbyte\.utils\.EncryptedString;\s*\r?\n', ''

    # File/class naming: argonJar -> duskByteJar, etc
    $content = $content -replace 'argonJar', 'duskByteJar'
    $content = $content -replace 'argon-', 'duskbyte-'

    # Config/file paths
    $content = $content -replace '"argon"', '"duskbyte"'
    $content = $content -replace "'argon'", "'duskbyte'"

    if ($content -ne $original) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
        $count++
    }
}
Write-Host "  Modified $count files"

# Also fix json and properties files
Write-Host "  Fixing JSON/properties files..." -ForegroundColor Cyan
$jsonFiles = Get-ChildItem -Path "src\main\resources" -Recurse -Filter "*.json" -ErrorAction SilentlyContinue
foreach ($file in $jsonFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
    $content = $content -replace 'dev\.lvstrng\.argon', 'dev.duskbyte'
    $content = $content -replace 'Argon', 'DuskByte'
    $content = $content -replace 'argon', 'duskbyte'
    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
}

# Step 5: Create/update fabric.mod.json
Write-Host "=== Step 5: fabric.mod.json ===" -ForegroundColor Yellow
$fabricJson = @'
{
  "schemaVersion": 1,
  "id": "duskbyte",
  "version": "DuskByte-1337",
  "name": "DuskByte",
  "description": "DuskByte Client - Inspired by Argon",
  "authors": ["DuskByte"],
  "license": "GPL-3.0",
  "environment": "client",
  "entrypoints": {
    "client": [
      "dev.duskbyte.DuskByte"
    ]
  },
  "mixins": [
    "duskbyte.mixins.json"
  ],
  "depends": {
    "fabricloader": ">=0.16.0",
    "minecraft": "~1.21",
    "java": ">=21",
    "fabric-api": "*"
  }
}
'@
Set-Content -Path "src\main\resources\fabric.mod.json" -Value $fabricJson -Encoding UTF8

# Step 6: Create duskbyte.mixins.json
Write-Host "=== Step 6: duskbyte.mixins.json ===" -ForegroundColor Yellow
$mixinJson = @'
{
  "required": true,
  "minVersion": "0.8",
  "package": "dev.duskbyte.mixin",
  "compatibilityLevel": "JAVA_21",
  "mixins": [
    "ClientConnectionMixin",
    "EndCrystalItemMixin",
    "ItemStackMixin",
    "LivingEntityAccessor",
    "WorldAccessor"
  ],
  "client": [
    "BufferRendererAccessor",
    "CameraMixin",
    "ClientPlayerEntityMixin",
    "ClientPlayerInteractionManagerAccessor",
    "ClientPlayerInteractionManagerMixin",
    "FrustumAccessor",
    "GameRendererMixin",
    "HandledScreenMixin",
    "InGameHudMixin",
    "KeyBindingAccessor",
    "KeyBindingMixin",
    "KeyboardMixin",
    "MinecraftClientAccessor",
    "MinecraftClientMixin",
    "MouseHandlerAccessor",
    "MouseMixin",
    "OtherClientPlayerEntityAccessor",
    "ScreenMixin",
    "ShaderProgramAccessor",
    "WorldRendererAccessor"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
'@
Set-Content -Path "src\main\resources\duskbyte.mixins.json" -Value $mixinJson -Encoding UTF8

# Step 7: Update gradle.properties
Write-Host "=== Step 7: gradle.properties ===" -ForegroundColor Yellow
$gradleProps = @'
org.gradle.jvmargs=-Xmx1G

# Fabric Properties
minecraft_version=1.21.1
yarn_mappings=1.21.1+build.3
loader_version=0.16.12

# Mod Properties
mod_version=DuskByte-1337
maven_group=dev.duskbyte
archives_base_name=duskbyte

# Dependencies
fabric_version=0.102.0+1.21.1
'@
Set-Content -Path "gradle.properties" -Value $gradleProps -Encoding UTF8

# Step 8: Cleanup old conflicting files
Write-Host "=== Step 8: Cleanup ===" -ForegroundColor Yellow
$oldPaths = @(
    "src\main\java\dev\duskbyte\DuskByteClient.java",
    "src\main\java\dev\duskbyte\setting",
    "src\main\java\dev\duskbyte\gui",
    "src\main\java\dev\duskbyte\hud",
    "src\main\java\dev\duskbyte\util",
    "src\main\java\dev\duskbyte\mixin\ChatMixin.java",
    "src\main\java\dev\duskbyte\mixin\ClientPlayNetworkHandlerMixin.java"
)
foreach ($p in $oldPaths) {
    if (Test-Path $p) {
        Remove-Item -Recurse -Force $p
        Write-Host "  Deleted $p"
    }
}

# Step 9: Verify key files exist
Write-Host ""
Write-Host "=== Verification ===" -ForegroundColor Cyan
$keyFiles = @(
    "src\main\java\dev\duskbyte\DuskByte.java",
    "src\main\java\dev\duskbyte\module\Module.java",
    "src\main\java\dev\duskbyte\module\ModuleManager.java",
    "src\main\java\dev\duskbyte\module\Category.java",
    "src\main\java\dev\duskbyte\module\setting\Setting.java",
    "src\main\java\dev\duskbyte\event\EventManager.java",
    "src\main\java\dev\duskbyte\gui\ClickGui.java",
    "src\main\java\dev\duskbyte\utils\RenderUtils.java",
    "src\main\resources\fabric.mod.json",
    "src\main\resources\duskbyte.mixins.json"
)
foreach ($f in $keyFiles) {
    if (Test-Path $f) {
        Write-Host "  [OK] $f" -ForegroundColor Green
    } else {
        Write-Host "  [MISSING] $f" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== DONE! ===" -ForegroundColor Green
Write-Host "Next: .\gradlew.bat build" -ForegroundColor Cyan
