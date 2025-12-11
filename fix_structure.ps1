$file = "app\src\main\java\com\example\fadesleeptimer\MainActivity.kt"
$lines = Get-Content $file

# 1. Remove empty Box closing brace
# Look for the Box definition and the immediate closing brace
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Trim() -eq '.offset { burnInOffset }') {
        if ($lines[$i+2].Trim() -eq '}') {
            $lines[$i+2] = "" # Remove the line
            Write-Host "Removed empty Box brace at line $($i+3)"
        }
    }
}

# 2. Remove FadeSleepTimerApp closing brace before Monochrome block
# Look for the Monochrome block start
$monoStart = 0
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Trim() -eq '// Monochrome Overlay') {
        $monoStart = $i
        break
    }
}

if ($monoStart -gt 0) {
    # The closing brace for FadeSleepTimerApp should be a few lines before this
    # We expect:
    #     }
    # }
    # 
    # // Monochrome Overlay
    
    # We want to remove the last '}' before monoStart
    for ($j = $monoStart - 1; $j -gt $monoStart - 10; $j--) {
        if ($lines[$j].Trim() -eq '}') {
            $lines[$j] = "" # Remove it
            Write-Host "Removed FadeSleepTimerApp closing brace at line $($j+1)"
            break
        }
    }
}

# 3. Add FadeSleepTimerApp closing brace after Monochrome block
# Look for the End of Root Box comment
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Trim() -eq '} // End of Root Box') {
        $lines[$i] = "} // End of Root Box`n}" # Add closing brace for function
        Write-Host "Added FadeSleepTimerApp closing brace at line $($i+1)"
    }
}

$lines | Set-Content $file
Write-Host "Fixed structure!"
