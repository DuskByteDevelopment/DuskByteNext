# DuskByte - Copy argon code and rename packages
# Run this from the project root: G:\Dev\DuskByteNext

$srcRoot = "src\main\java\dev\duskbyte"
$refRoot = "reference\argon-main\src\main\java\dev\lvstrng\argon"

# Step 1: Delete all existing source files
Write-Host "=== Step 1: Deleting existing source ===" -ForegroundColor Yellow
if (Test-Path $srcRoot) {
    Remove-Item -Recurse -Force $srcRoot
    Write-Host "  Deleted $srcRoot"
}

# Step 2: Copy all argon files
Write-Host "=== Step 2: Copying argon source ===" -ForegroundColor Yellow
Copy-Item -Recurse -Force $refRoot $srcRoot
Write-Host "  Copied to $srcRoot"

# Step 3: Replace package names in all .java files
Write-Host "=== Step 3: Replacing package names ===" -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path $srcRoot -Recurse -Filter "*.java"
$count = 0
foreach ($file in $javaFiles) {
    $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8

    # Replace package and import statements
    $content = $content -replace 'dev\.lvstrng\.argon', 'dev.duskbyte'

    # Replace EncryptedString.of("...") with just "..."
    $content = $content -replace 'EncryptedString\.of\("([^"]*)"\)', '$1'

    # Remove EncryptedString import lines
    $content = $content -replace '(?m)^\s*import dev\.duskbyte\.utils\.EncryptedString;\s*\r?\n', ''

    # Replace Argon references
    $content = $content -replace 'Argon\.INSTANCE', 'DuskByte.INSTANCE'
    $content = $content -replace 'import dev\.duskbyte\.Argon;', 'import dev.duskbyte.DuskByte;'

    Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
    $count++
}
Write-Host "  Modified $count Java files"

# Step 4: Create/update fabric.mod.json
Write-Host "=== Step 4: Updating fabric.mod.json ===" -ForegroundColor Yellow
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

# Step 5: Create duskbyte.mixins.json
Write-Host "=== Step 5: Creating duskbyte.mixins.json ===" -ForegroundColor Yellow
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

# Step 6: Update gradle.properties
Write-Host "=== Step 6: Updating gradle.properties ===" -ForegroundColor Yellow
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

# Step 7: Delete old DuskByteClient.java if exists
Write-Host "=== Step 7: Cleanup ===" -ForegroundColor Yellow
$oldEntry = "src\main\java\dev\duskbyte\DuskByteClient.java"
if (Test-Path $oldEntry) {
    Remove-Item -Force $oldEntry
    Write-Host "  Deleted DuskByteClient.java"
}

# Step 8: Delete old setting files that conflict
$oldSettings = @(
    "src\main\java\dev\duskbyte\setting",
    "src\main\java\dev\duskbyte\gui",
    "src\main\java\dev\duskbyte\hud",
    "src\main\java\dev\duskbyte\util"
)
foreach ($dir in $oldSettings) {
    if (Test-Path $dir) {
        Remove-Item -Recurse -Force $dir
        Write-Host "  Deleted $dir"
    }
}

Write-Host ""
Write-Host "=== DONE! ===" -ForegroundColor Green
Write-Host "Run: .\gradlew.bat build" -ForegroundColor Cyan
