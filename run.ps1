$libPath = "./lib/*.jar"
$libFiles = Get-ChildItem -Path $libPath -Filter "*.jar" | Foreach-Object { Resolve-Path $_.FullName }

$classpath = ".\bin;" + ($libFiles -join ";")

java "-XX:+ShowCodeDetailsInExceptionMessages" -cp $classpath "LungoBrowser.App" $args