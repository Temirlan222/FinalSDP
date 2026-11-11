param(
    [switch]$Test,
    [switch]$Run,
    [switch]$Web,
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'

$jdkRoot = if ($env:JDK_HOME) { $env:JDK_HOME } else { $env:JAVA_HOME }
if ($jdkRoot) {
    $javacPath = Join-Path $jdkRoot 'bin\javac.exe'
} else {
    $javacPath = (Get-Command javac -ErrorAction Stop).Source
}
$javaPath = Join-Path (Split-Path $javacPath) 'java.exe'
if (-not (Test-Path -LiteralPath $javaPath)) {
    throw 'Set JDK_HOME or JAVA_HOME to a JDK 11+ installation.'
}

$outputDir = Join-Path $PSScriptRoot 'out'
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src\main\java') -Filter '*.java' -Recurse | ForEach-Object FullName)
if ($Test) {
    $sources += @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'test') -Filter '*.java' -Recurse | ForEach-Object FullName)
}
& $javacPath --release 11 -encoding UTF-8 -Xlint:all -d $outputDir @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Test) {
    & $javaPath -cp $outputDir giftbox.PartTests
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    & $javaPath -cp $outputDir giftbox.ProjectTests
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
if ($Web) {
    & $javaPath -cp $outputDir giftbox.web.WebServer $Port (Join-Path $PSScriptRoot 'web')
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
if ($Run) {
    & $javaPath -cp $outputDir giftbox.demo.Demo
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
