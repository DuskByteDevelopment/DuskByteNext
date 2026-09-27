param(
    [Parameter(Mandatory = $true)][string]$Jar
)

# Extracts META-INF/jars/* from the given jar into <Jar>.nested/ dir and removes
# them from the jar, so ZKM never opens nested jar classes.
Add-Type -AssemblyName System.IO.Compression.FileSystem

$Jar = (Resolve-Path $Jar).Path
$nestedDir = Join-Path ([IO.Path]::GetDirectoryName($Jar)) (($([IO.Path]::GetFileName($Jar))) + '.nested')

$zip = [IO.Compression.ZipFile]::Open($Jar)
try {
    $toDelete = @($zip.Entries | Where-Object { $_.FullName -like 'META-INF/jars/*' -and $_.Length -gt 0 })
    foreach ($e in $toDelete) {
        $dest = Join-Path $nestedDir $e.FullName.Replace('/', [IO.Path]::DirectorySeparatorChar)
        New-Item -Force -ItemType Directory -Path ([IO.Path]::GetDirectoryName($dest)) | Out-Null
        [IO.Compression.ZipFileExtensions]::ExtractToFile($e, $dest, $true)
        Write-Host "  extracted: $($e.FullName)"
    }
    foreach ($e in $toDelete) {
        $e.Delete()
    }
    if ($toDelete.Count -eq 0) { Write-Host '  no nested jars found' }
} finally {
    $zip.Dispose()
}
