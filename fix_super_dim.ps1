$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$content = Get-Content $file -Raw

# Remove padding from outer Box
$oldBox = ".background(Slate950)`r`n                .padding(16.dp)"
$newBox = ".background(Slate950)"
$content = $content.Replace($oldBox, $newBox)

# Add padding to inner Column
$oldCol = "Column(modifier = Modifier.fillMaxSize()) {"
$newCol = "Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {"
$content = $content.Replace($oldCol, $newCol)

Set-Content $file $content -NoNewline
Write-Host "Fixed Super Dim padding!"
