param(
	[ValidateSet("debug", "release")]
	[string] $AndroidVariant = "debug",

	[switch] $SkipDesktop,
	[switch] $SkipAndroid,
	[switch] $NoCleanIntermediates
)

$ErrorActionPreference = "Stop"

$root = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$gradle = Join-Path $root "gradlew.bat"
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outRoot = Join-Path $root "build\ready-to-install\$stamp"
$rootBuildGradle = Get-Content -Path (Join-Path $root "build.gradle") -Raw
$appName = if ($rootBuildGradle -match "appName\s*=\s*'([^']+)'") { $Matches[1] } else { "Reclaimed Pixel Dungeon" }
$appPackageName = if ($rootBuildGradle -match "appPackageName\s*=\s*'([^']+)'") { $Matches[1] } else { "com.erebus.reclaimedpixeldungeon" }
$appVersionName = if ($rootBuildGradle -match "appVersionName\s*=\s*'([^']+)'") { $Matches[1] } else { "1.0.0" }

$gradleArgs = @(
	"--no-daemon",
	"--max-workers=1",
	"--console=plain",
	"-Preclaimed.cleanPackageLabel=true",
	"-Dorg.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=192m -XX:CICompilerCount=1 -XX:TieredStopAtLevel=1 -XX:ActiveProcessorCount=1 -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
)

$desktopJvmArgs = @(
	"-XX:+IgnoreUnrecognizedVMOptions",
	"-Xms64m",
	"-Xmx512m",
	"-XX:MaxMetaspaceSize=160m",
	"-XX:CICompilerCount=2",
	"-XX:TieredStopAtLevel=1",
	"-XX:ActiveProcessorCount=4"
)
$desktopJvmArgString = $desktopJvmArgs -join " "

function Resolve-ProjectPath {
	param([string] $RelativePath)
	return (Join-Path $root $RelativePath)
}

function Remove-TransientPath {
	param([string] $RelativePath)

	$target = Resolve-ProjectPath $RelativePath
	if (-not (Test-Path -LiteralPath $target)) {
		return
	}

	$resolvedRoot = (Resolve-Path -LiteralPath $root).Path.TrimEnd('\')
	$resolvedTarget = (Resolve-Path -LiteralPath $target).Path
	if (-not $resolvedTarget.StartsWith($resolvedRoot + "\", [System.StringComparison]::OrdinalIgnoreCase)) {
		throw "Refusing to remove path outside project: $resolvedTarget"
	}

	try {
		Get-ChildItem -LiteralPath $resolvedTarget -Recurse -Force -ErrorAction SilentlyContinue | ForEach-Object {
			if (($_.Attributes -band [System.IO.FileAttributes]::ReadOnly) -ne 0) {
				$_.Attributes = $_.Attributes -band (-bnot [System.IO.FileAttributes]::ReadOnly)
			}
		}
		$targetItem = Get-Item -LiteralPath $resolvedTarget -Force
		if (($targetItem.Attributes -band [System.IO.FileAttributes]::ReadOnly) -ne 0) {
			$targetItem.Attributes = $targetItem.Attributes -band (-bnot [System.IO.FileAttributes]::ReadOnly)
		}
		Remove-Item -LiteralPath $resolvedTarget -Recurse -Force
	} catch {
		Write-Warning "Could not clean $resolvedTarget. Close running game builds, pause OneDrive sync, or close Java language-server tasks if this repeats."
		Write-Warning $_.Exception.Message
	}
}

function Clear-DesktopIntermediates {
	if ($NoCleanIntermediates) {
		return
	}
	Remove-TransientPath "desktop\build\libs"
	Remove-TransientPath "desktop\build\scripts"
	Remove-TransientPath "desktop\build\resources\main"
	Remove-TransientPath "desktop\build\jpackage"
	Remove-TransientPath "desktop\build\install"
}

function Clear-AndroidIntermediates {
	if ($NoCleanIntermediates) {
		return
	}
	Remove-TransientPath "android\build\intermediates"
	Remove-TransientPath "android\build\outputs\apk\$AndroidVariant"
}

function Invoke-Gradle {
	param([string[]] $Tasks)

	$oldErrorActionPreference = $ErrorActionPreference
	try {
		$ErrorActionPreference = "Continue"
		& $gradle @gradleArgs @Tasks 2>&1 | ForEach-Object { Write-Host $_ }
		return $LASTEXITCODE
	} finally {
		$ErrorActionPreference = $oldErrorActionPreference
	}
}

function Invoke-GradleTasks {
	param(
		[string[]] $Tasks,
		[string] $Name,
		[scriptblock] $Clean
	)

	Write-Host ""
	Write-Host "Building $Name..."
	& $Clean
	$exitCode = Invoke-Gradle $Tasks
	if ($exitCode -eq 0) {
		return $true
	}

	Write-Warning "$Name build failed. Cleaning transient outputs and retrying once."
	& $Clean
	$exitCode = Invoke-Gradle $Tasks
	return $exitCode -eq 0
}

function Resolve-JlinkPath {
	if ($env:JAVA_HOME) {
		$javaHomeJlink = Join-Path $env:JAVA_HOME "bin\jlink.exe"
		if (Test-Path -LiteralPath $javaHomeJlink) {
			return $javaHomeJlink
		}
	}

	$pathJlink = Get-Command "jlink.exe" -ErrorAction SilentlyContinue
	if ($pathJlink -ne $null) {
		return $pathJlink.Source
	}

	$localJlink = Join-Path $HOME "dev-tools\jdk-17\bin\jlink.exe"
	if (Test-Path -LiteralPath $localJlink) {
		return $localJlink
	}

	return $null
}

function Resolve-JpackagePath {
	if ($env:JAVA_HOME) {
		$javaHomeJpackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
		if (Test-Path -LiteralPath $javaHomeJpackage) {
			return $javaHomeJpackage
		}
	}

	$pathJpackage = Get-Command "jpackage.exe" -ErrorAction SilentlyContinue
	if ($pathJpackage -ne $null) {
		return $pathJpackage.Source
	}

	$localJpackage = Join-Path $HOME "dev-tools\jdk-17\bin\jpackage.exe"
	if (Test-Path -LiteralPath $localJpackage) {
		return $localJpackage
	}

	return $null
}

function Resolve-CSharpCompiler {
	$framework64 = Join-Path $env:WINDIR "Microsoft.NET\Framework64\v4.0.30319\csc.exe"
	if (Test-Path -LiteralPath $framework64) {
		return $framework64
	}

	$framework = Join-Path $env:WINDIR "Microsoft.NET\Framework\v4.0.30319\csc.exe"
	if (Test-Path -LiteralPath $framework) {
		return $framework
	}

	$pathCsc = Get-Command "csc.exe" -ErrorAction SilentlyContinue
	if ($pathCsc -ne $null) {
		return $pathCsc.Source
	}

	return $null
}

function New-CompatibleLauncherIcon {
	param([string] $OutputPath)

	$pngPath = Resolve-ProjectPath "desktop\src\main\assets\icons\icon_256.png"
	if (-not (Test-Path -LiteralPath $pngPath)) {
		return $false
	}

	try {
		Add-Type -AssemblyName System.Drawing -ErrorAction Stop
		$bitmap = [System.Drawing.Bitmap]::FromFile($pngPath)
		try {
			$hicon = $bitmap.GetHicon()
			$icon = [System.Drawing.Icon]::FromHandle($hicon)
			$stream = [System.IO.File]::Create($OutputPath)
			try {
				$icon.Save($stream)
			} finally {
				$stream.Close()
				$icon.Dispose()
			}
		} finally {
			$bitmap.Dispose()
		}
		return (Test-Path -LiteralPath $OutputPath)
	} catch {
		Write-Warning "Could not generate a compiler-compatible desktop launcher icon."
		Write-Warning $_.Exception.Message
		return $false
	}
}

function Copy-DesktopRuntime {
	param([string] $RuntimeOut)

	if (Test-Path -LiteralPath $RuntimeOut) {
		Remove-Item -LiteralPath $RuntimeOut -Recurse -Force
	}

	$jlink = Resolve-JlinkPath
	if ($jlink -eq $null) {
		Write-Warning "jlink.exe was not found. Desktop package will require Java on this PC."
		return
	}

	Write-Host "Creating bundled desktop runtime..."
	$modules = "java.base,java.desktop,java.logging,java.management,java.naming,java.prefs,jdk.crypto.ec,jdk.unsupported"
	& $jlink `
		"--add-modules" $modules `
		"--strip-debug" `
		"--compress=2" `
		"--no-header-files" `
		"--no-man-pages" `
		"--output" $RuntimeOut

	if ($LASTEXITCODE -ne 0) {
		Write-Warning "Could not create bundled desktop runtime. Desktop package will require Java on this PC."
		if (Test-Path -LiteralPath $RuntimeOut) {
			Remove-Item -LiteralPath $RuntimeOut -Recurse -Force
		}
	}
}

function New-ExeLauncher {
	param(
		[string] $AppOut,
		[string] $JarName
	)

	$exe = Join-Path $AppOut "$appName.exe"
	$escapedJarName = $JarName.Replace('\', '\\').Replace('"', '\"')
	$escapedJvmArgs = $desktopJvmArgString.Replace('\', '\\').Replace('"', '\"')
	$source = @"
using System;
using System.Diagnostics;
using System.IO;

public static class ReclaimedPixelDungeonLauncher {
	private const string JarName = "$escapedJarName";
	private const string JvmArgs = "$escapedJvmArgs";

	public static int Main(string[] args) {
		string appHome = AppDomain.CurrentDomain.BaseDirectory;
		string java = Path.Combine(appHome, "runtime", "bin", "javaw.exe");
		if (!File.Exists(java)) {
			string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
			if (!String.IsNullOrEmpty(javaHome)) {
				java = Path.Combine(javaHome, "bin", "javaw.exe");
			}
		}
		if (!File.Exists(java)) {
			java = "javaw.exe";
		}

		string jar = Path.Combine(appHome, "app", JarName);
		string arguments = JvmArgs + " -jar " + Quote(jar);
		foreach (string arg in args) {
			arguments += " " + Quote(arg);
		}

		ProcessStartInfo info = new ProcessStartInfo(java, arguments);
		info.WorkingDirectory = appHome;
		info.UseShellExecute = false;
		Process.Start(info);
		return 0;
	}

	private static string Quote(string value) {
		return "\"" + value.Replace("\"", "\\\"") + "\"";
	}
}
"@

	$csc = Resolve-CSharpCompiler
	if ($csc -eq $null) {
		Write-Warning "Could not find csc.exe to create the desktop .exe launcher. The .cmd fallback will still be created."
		return $null
	}

	$tempSource = Join-Path ([System.IO.Path]::GetTempPath()) ("reclaimed-pixel-dungeon-launcher-{0}.cs" -f [guid]::NewGuid())
	$tempIcon = Join-Path ([System.IO.Path]::GetTempPath()) ("reclaimed-pixel-dungeon-launcher-{0}.ico" -f [guid]::NewGuid())
	try {
		Set-Content -LiteralPath $tempSource -Value $source -Encoding UTF8
		$baseCompileArgs = @(
			"/nologo",
			"/target:winexe",
			"/out:$exe"
		)

		$compileExitCode = 1
		$compileOutput = $null
		$compileArgs = @($baseCompileArgs)
		if (New-CompatibleLauncherIcon $tempIcon) {
			$compileArgs += "/win32icon:$tempIcon"
		}
		$compileArgs += $tempSource

		$compileOutput = & $csc @compileArgs 2>&1
		$compileExitCode = $LASTEXITCODE

		if ($compileExitCode -ne 0 -or -not (Test-Path -LiteralPath $exe)) {
			Write-Warning "Could not embed a launcher icon; retrying without an embedded icon."
			Remove-Item -LiteralPath $exe -Force -ErrorAction SilentlyContinue
			$compileOutput = & $csc @baseCompileArgs $tempSource 2>&1
			$compileExitCode = $LASTEXITCODE
		}

		if ($compileExitCode -ne 0 -or -not (Test-Path -LiteralPath $exe)) {
			if ($compileOutput -ne $null) {
				Write-Warning (($compileOutput | Out-String).Trim())
			}
			throw "csc.exe failed to create $exe"
		}
		return $exe
	} catch {
		Write-Warning "Could not create desktop .exe launcher. The .cmd fallback will still be created."
		Write-Warning $_.Exception.Message
		return $null
	} finally {
		if (Test-Path -LiteralPath $tempSource) {
			Remove-Item -LiteralPath $tempSource -Force
		}
		if (Test-Path -LiteralPath $tempIcon) {
			Remove-Item -LiteralPath $tempIcon -Force
		}
	}
}

function Assert-NoForbiddenPackageFiles {
	param([string] $PackageRoot)

	$forbidden = Get-ChildItem -LiteralPath $PackageRoot -Recurse -Force -File -ErrorAction SilentlyContinue |
			Where-Object {
				$_.Name -like "hs_err_pid*.log" -or
				$_.Name -like "*.hprof" -or
				$_.Name -like "*.java" -or
				$_.Name -like "*.gradle" -or
				$_.Name -eq "gradlew" -or
				$_.Name -eq "gradlew.bat" -or
				$_.Name -eq "local.properties"
			}

	if ($forbidden -ne $null) {
		$sample = ($forbidden | Select-Object -First 8 | ForEach-Object { $_.FullName }) -join "`n"
		throw "Package contains files that should not be shipped:`n$sample"
	}
}

function Assert-DesktopVersionClean {
	param([string] $JarPath)

	$manifest = & jar xf $JarPath META-INF/MANIFEST.MF 2>$null
	$manifestPath = Join-Path (Get-Location) "META-INF\MANIFEST.MF"
	try {
		if (-not (Test-Path -LiteralPath $manifestPath)) {
			throw "Could not read manifest from $JarPath"
		}
		$text = Get-Content -LiteralPath $manifestPath -Raw
		if ($text -match "INDEV|IN-DEV") {
			throw "Desktop package still contains an INDEV version label."
		}
		if ($text -notmatch [regex]::Escape("Specification-Version: $appVersionName")) {
			throw "Desktop package manifest version was not $appVersionName."
		}
	} finally {
		if (Test-Path -LiteralPath (Join-Path (Get-Location) "META-INF")) {
			Remove-Item -LiteralPath (Join-Path (Get-Location) "META-INF") -Recurse -Force
		}
	}
}

function New-CmdLauncher {
	param(
		[string] $AppOut,
		[string] $JarName
	)

	$launcher = Join-Path $AppOut "$appName.cmd"
	$launcherContent = @(
		"@echo off",
		"setlocal",
		"cd /d ""%~dp0""",
		"set ""APP_HOME=%~dp0""",
		"if exist ""%APP_HOME%runtime\bin\javaw.exe"" (",
		"  set ""JAVA_EXE=%APP_HOME%runtime\bin\javaw.exe""",
		") else if defined JAVA_HOME (",
		"  set ""JAVA_EXE=%JAVA_HOME%\bin\javaw.exe""",
		") else (",
		"  set ""JAVA_EXE=javaw.exe""",
		")",
		"set ""JAVA_ARGS=$desktopJvmArgString""",
		"start """" ""%JAVA_EXE%"" %JAVA_ARGS% -jar ""%APP_HOME%app\$JarName"" %*"
	)
	Set-Content -Path $launcher -Value $launcherContent -Encoding ASCII
	return $launcher
}

function Copy-DesktopOutput {
	$releaseJar = Get-ChildItem -Path (Resolve-ProjectPath "desktop\build\libs") -Filter "desktop-*.jar" -File -ErrorAction SilentlyContinue |
			Sort-Object LastWriteTime -Descending |
			Select-Object -First 1

	if ($releaseJar -eq $null) {
		Write-Warning "Desktop build completed, but no release jar was found under desktop\build\libs."
		return
	}

	Assert-DesktopVersionClean $releaseJar.FullName

	$desktopOut = Join-Path $outRoot "desktop"
	$appOut = Join-Path $desktopOut $appName
	$staging = Join-Path $desktopOut "_staging"
	$runtimeDir = Join-Path $staging "runtime"
	$jarName = "$($appName -replace '[^A-Za-z0-9._-]', '')-$appVersionName.jar"

	New-Item -ItemType Directory -Path $desktopOut -Force | Out-Null
	if (Test-Path -LiteralPath $appOut) {
		Remove-Item -LiteralPath $appOut -Recurse -Force
	}
	if (Test-Path -LiteralPath $staging) {
		Remove-Item -LiteralPath $staging -Recurse -Force
	}
	New-Item -ItemType Directory -Path (Join-Path $appOut "app") -Force | Out-Null
	Copy-Item -Path $releaseJar.FullName -Destination (Join-Path $appOut "app\$jarName") -Force
	Copy-DesktopRuntime $runtimeDir
	if (Test-Path -LiteralPath $runtimeDir) {
		Copy-Item -Path $runtimeDir -Destination (Join-Path $appOut "runtime") -Recurse -Force
	}

	$exe = New-ExeLauncher $appOut $jarName
	New-CmdLauncher $appOut $jarName | Out-Null
	if ($exe -ne $null) {
		Write-Host "Desktop EXE ready:"
		Write-Host "  $exe"
	} else {
		Write-Host "Desktop app ready:"
		Write-Host "  $(Join-Path $appOut "$appName.cmd")"
	}

	if (Test-Path -LiteralPath $staging) {
		Remove-Item -LiteralPath $staging -Recurse -Force
	}
	Assert-NoForbiddenPackageFiles $appOut

}

function Copy-AndroidOutput {
	$apkDir = Resolve-ProjectPath "android\build\outputs\apk\$AndroidVariant"
	$metadata = Join-Path $apkDir "output-metadata.json"
	if (Test-Path -LiteralPath $metadata) {
		$metadataText = Get-Content -LiteralPath $metadata -Raw
		if ($metadataText -match "INDEV|IN-DEV") {
			throw "Android package metadata still contains an INDEV version label."
		}
		$metadataJson = $metadataText | ConvertFrom-Json
		$actualApplicationId = $metadataJson.applicationId
		if (-not $actualApplicationId) {
			$element = @($metadataJson.elements | Select-Object -First 1)
			if ($element.Count -gt 0) {
				$actualApplicationId = $element[0].applicationId
			}
		}
		if ($actualApplicationId -ne $appPackageName) {
			throw "Android package id was $actualApplicationId, expected $appPackageName."
		}
		if ($actualApplicationId -eq "com.shatteredpixel.shatteredpixeldungeon") {
			throw "Android package still uses Shattered Pixel Dungeon's package id."
		}
	}

	$apk = Get-ChildItem -Path $apkDir -Filter "*.apk" -File -ErrorAction SilentlyContinue |
			Sort-Object LastWriteTime -Descending |
			Select-Object -First 1

	if ($apk -eq $null) {
		Write-Warning "Android build completed, but no APK was found under $apkDir."
		return
	}

	$androidOut = Join-Path $outRoot "android"
	New-Item -ItemType Directory -Path $androidOut -Force | Out-Null
	$copiedApk = Join-Path $androidOut $apk.Name
	Copy-Item -Path $apk.FullName -Destination $copiedApk -Force
	Assert-NoForbiddenPackageFiles $androidOut

	Write-Host "Android APK ready:"
	Write-Host "  $copiedApk"

	if ($AndroidVariant -eq "release" -and $apk.Name -match "unsigned") {
		Write-Warning "This release APK appears to be unsigned. For direct phone installs, use the debug APK or add release signing."
	}
}

if ($SkipDesktop -and $SkipAndroid) {
	Write-Host "No build tasks selected."
	exit 0
}

New-Item -ItemType Directory -Path $outRoot -Force | Out-Null

$failed = $false

if (-not $SkipDesktop) {
	$desktopOk = Invoke-GradleTasks -Name "desktop app" -Tasks @("desktop:release") -Clean { Clear-DesktopIntermediates }
	if ($desktopOk) {
		Copy-DesktopOutput
	} else {
		$failed = $true
	}
}

if (-not $SkipAndroid) {
	$variantTask = $AndroidVariant.Substring(0, 1).ToUpperInvariant() + $AndroidVariant.Substring(1)
	$androidOk = Invoke-GradleTasks -Name "Android $AndroidVariant APK" -Tasks @("android:assemble$variantTask") -Clean { Clear-AndroidIntermediates }
	if ($androidOk) {
		Copy-AndroidOutput
	} else {
		$failed = $true
	}
}

Write-Host ""
Write-Host "Packaged outputs:"
Write-Host "  $outRoot"

if ($failed) {
	Write-Warning "One or more package builds failed. Any successful outputs above were still copied."
	exit 1
}
