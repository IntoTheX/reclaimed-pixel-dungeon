param(
	[Parameter(ValueFromRemainingArguments = $true)]
	[string[]] $GradleArgs
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$problemReportDir = Join-Path $root "build\reports\problems"

if (Test-Path -LiteralPath $problemReportDir) {
	Remove-Item -LiteralPath $problemReportDir -Recurse -Force
}

& (Join-Path $root "gradlew.bat") @GradleArgs
exit $LASTEXITCODE
