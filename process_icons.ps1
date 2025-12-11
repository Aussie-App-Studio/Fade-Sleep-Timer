$icons = @("disco", "future", "sheep", "lamp", "christian", "bitcoin", "cat")
$dpis = @("hdpi", "mdpi", "xhdpi", "xxhdpi", "xxxhdpi")
$srcBase = "c:\AndroidProjects\FadeSleepTimer\temp_icons"
$destBase = "c:\AndroidProjects\FadeSleepTimer\app\src\main\res"

foreach ($icon in $icons) {
    Write-Host "Processing $icon..."
    
    # 1. Move PNGs
    foreach ($dpi in $dpis) {
        $srcDir = "$srcBase\icon_$icon\mipmap-$dpi"
        $destDir = "$destBase\mipmap-$dpi"
        
        if (Test-Path $srcDir) {
            Get-ChildItem -Path $srcDir -Filter "*.png" | ForEach-Object {
                $destFile = Join-Path $destDir $_.Name
                Copy-Item -Path $_.FullName -Destination $destFile -Force
                Write-Host "Copied $($_.Name) to $dpi"
            }
        }
    }
    
    # 2. Process XMLs
    $xmlDir = "$srcBase\icon_$icon\mipmap-anydpi-v26"
    $destXmlDir = "$destBase\mipmap-anydpi-v26"
    
    if (Test-Path $xmlDir) {
        # Regular Icon
        $xmlContent = Get-Content "$xmlDir\ic_launcher.xml" -Raw
        $newContent = $xmlContent -replace "@mipmap/ic_launcher_foreground", "@mipmap/icon_${icon}_foreground"
        $newContent | Set-Content "$destXmlDir\icon_$icon.xml"
        Write-Host "Created icon_$icon.xml"
        
        # Round Icon
        $xmlContentRound = Get-Content "$xmlDir\ic_launcher_round.xml" -Raw
        $newContentRound = $xmlContentRound -replace "@mipmap/ic_launcher_foreground", "@mipmap/icon_${icon}_foreground"
        $newContentRound | Set-Content "$destXmlDir\icon_${icon}_round.xml"
        Write-Host "Created icon_${icon}_round.xml"
    }
}
