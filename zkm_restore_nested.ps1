param(
    [Parameter(Mandatory = $true)][string]$Jar,
    [string]$NestedDir
)

# Re-inserts <Jar>.nested/META-INF/jars/*.jar entries back into the jar.
Add-Type -AssemblyName System.IO.Compression.FileSystem

$Jar = (Resolve-Path $Jar).Path
if (-not $NestedDir) { $NestedDir = "$Jar.nested" }
if (-not (Test-Path $NestedDir)) { Write-Host '  no nested jars to restore'; exit 0 }

$zip = [IO.Compression.ZipFile]::Open($Jar, [IO.Compression.ZipArchiveMode]::Update)
try {
    Get-ChildItem -Recurse -File $NestedDir | ForEach-Object {
        $rel = $_.FullName.Substring($NestedDir.Length + 1).Replace([IO.Path]::DirectorySeparatorChar, '/')
        $entry = $zip.CreateEntry($rel)
        $src = [IO.File]::OpenRead($_.FullName)
        $dst = $entry.Open()
        $src.CopyTo($dst)
        $dst.Dispose(); $src.Dispose()
        Write-Host "  restored: $rel"
    }
} finally {
    $zip.Dispose()
}
