param(
    [Parameter(Mandatory = $true)][string]$Jar,
    [string]$NestedDir
)

# Extracts META-INF/jars/* from the given jar into a nested dir and removes them
# from the jar, so ZKM never opens nested jar classes (avoids the mixin
# version-mismatch FATAL in ZKM's trim stage).
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression

$Jar = (Resolve-Path $Jar).Path
if (-not $NestedDir) {
    $NestedDir = Join-Path ([IO.Path]::GetDirectoryName($Jar)) (($([IO.Path]::GetFileName($Jar))) + '.nested')
}
if (-not [IO.Path]::IsPathRooted($NestedDir)) {
    $NestedDir = Join-Path (Get-Location).Path $NestedDir
}
New-Item -Force -ItemType Directory -Path $NestedDir | Out-Null

$zip = [IO.Compression.ZipFile]::Open($Jar, [IO.Compression.ZipArchiveMode]::Update)
try {
    $targets = @($zip.Entries | Where-Object { $_.FullName -like 'META-INF/jars/*' -and $_.Length -gt 0 })

    foreach ($e in $targets) {
        $rel = $e.FullName.Replace('/', [IO.Path]::DirectorySeparatorChar)
        $dest = Join-Path $NestedDir $rel
        $parent = [IO.Path]::GetDirectoryName($dest)
        if (-not (Test-Path $parent)) { New-Item -Force -ItemType Directory -Path $parent | Out-Null }

        $in = $e.Open()
        $out = [IO.File]::Create($dest)
        $in.CopyTo($out)
        $out.Dispose()
        $in.Dispose()
        Write-Host "  extracted: $($e.FullName)"
    }

    foreach ($e in $targets) { $e.Delete() }

    if ($targets.Count -eq 0) { Write-Host '  no nested jars found' }
    else { Write-Host "  stripped $($targets.Count) nested jar(s)" }
} finally {
    $zip.Dispose()
}
