$file = "app\src\main\AndroidManifest.xml"
$content = Get-Content $file -Raw

# Add enableOnBackInvokedCallback to application tag
$old = '    <application
        android:allowBackup="true"
        android:icon="@android:drawable/sym_def_app_icon"'

$new = '    <application
        android:allowBackup="true"
        android:enableOnBackInvokedCallback="true"
        android:icon="@android:drawable/sym_def_app_icon"'

$content = $content -replace [regex]::Escape($old), $new
Set-Content $file $content -NoNewline
Write-Host "Added predictive back gesture support!"
