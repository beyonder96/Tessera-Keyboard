[CmdletBinding()]
param (
    [string]$Version,
    [switch]$SkipBuild,
    [switch]$SkipPublish,
    [string]$ReleaseNotes
)

$ErrorActionPreference = "Stop"

$ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..\..\..").Path
Set-Location $ProjectRoot

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host " Tessera Keyboard — Release Automation    " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Configuração de Ambiente
$DefaultJdk = "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7"
$DefaultSdk = "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk"
$UserSdk = "C:\Users\kenne\AppData\Local\Android\Sdk"

if (-not $env:JAVA_HOME -and (Test-Path $DefaultJdk)) {
    $env:JAVA_HOME = $DefaultJdk
}
if (-not $env:ANDROID_HOME) {
    if (Test-Path $UserSdk) {
        $env:ANDROID_HOME = $UserSdk
    } elseif (Test-Path $DefaultSdk) {
        $env:ANDROID_HOME = $DefaultSdk
    }
}

$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

Write-Host " Java Home:    $env:JAVA_HOME" -ForegroundColor Gray
Write-Host " Android Home: $env:ANDROID_HOME" -ForegroundColor Gray

# 2. Resolução da Versão
$GradleFile = Join-Path $ProjectRoot "app\build.gradle.kts"
$GradleContent = Get-Content $GradleFile -Raw

$CurrentVersion = ""
if ($GradleContent -match 'versionName\s*=\s*"([^"]+)"') {
    $CurrentVersion = $Matches[1]
}
$CurrentCode = 0
if ($GradleContent -match 'versionCode\s*=\s*(\d+)') {
    $CurrentCode = [int]$Matches[1]
}

Write-Host " Versao Atual: v$CurrentVersion (Code: $CurrentCode)" -ForegroundColor Yellow

if ($Version) {
    $TargetVersion = $Version.TrimStart("v")
    $NewCode = $CurrentCode + 1
    Write-Host " Atualizando para v$TargetVersion (Code: $NewCode)..." -ForegroundColor Green
    
    $NewGradleContent = $GradleContent -replace 'versionCode\s*=\s*\d+', "versionCode = $NewCode"
    $NewGradleContent = $NewGradleContent -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$TargetVersion`""
    Set-Content -Path $GradleFile -Value $NewGradleContent -NoNewline

    # Atualizar badges no README.md se existir
    $ReadmeFile = Join-Path $ProjectRoot "README.md"
    if (Test-Path $ReadmeFile) {
        $ReadmeContent = Get-Content $ReadmeFile -Raw
        $ReadmeContent = $ReadmeContent -replace 'v\d+\.\d+\.\d+', "v$TargetVersion"
        $ReadmeContent = $ReadmeContent -replace 'Release_v\d+\.\d+\.\d+', "Release_v$TargetVersion"
        $ReadmeContent = $ReadmeContent -replace 'Debug_v\d+\.\d+\.\d+', "Debug_v$TargetVersion"
        Set-Content -Path $ReadmeFile -Value $ReadmeContent -NoNewline
        Write-Host " README.md atualizado com os badges de v$TargetVersion" -ForegroundColor Gray
    }
} else {
    $TargetVersion = $CurrentVersion
}

# 3. Compilação dos APKs
if (-not $SkipBuild) {
    Write-Host "`n Compilando APKs Release e Debug..." -ForegroundColor Cyan
    & ".\gradlew.bat" assembleRelease assembleDebug --stacktrace
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Falha ao compilar APKs com Gradle."
        exit $LASTEXITCODE
    }

    $ApkDir = Join-Path $ProjectRoot "apks"
    if (-not (Test-Path $ApkDir)) {
        New-Item -ItemType Directory -Path $ApkDir | Out-Null
    }

    $ReleaseSrc = Join-Path $ProjectRoot "app\build\outputs\apk\release\app-release.apk"
    $DebugSrc = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"

    if (Test-Path $ReleaseSrc) {
        Copy-Item $ReleaseSrc -Destination "$ApkDir\app-release.apk" -Force
        Copy-Item $ReleaseSrc -Destination "$ApkDir\app-release-v$TargetVersion.apk" -Force
    }
    if (Test-Path $DebugSrc) {
        Copy-Item $DebugSrc -Destination "$ApkDir\app-debug.apk" -Force
        Copy-Item $DebugSrc -Destination "$ApkDir\app-debug-v$TargetVersion.apk" -Force
    }

    Write-Host " APKs gerados com sucesso na pasta /apks:" -ForegroundColor Green
    Get-ChildItem $ApkDir -Filter "*v$TargetVersion.apk" | ForEach-Object {
        Write-Host "   - $($_.Name) ($([math]::Round($_.Length / 1MB, 2)) MB)" -ForegroundColor Gray
    }
}

# 4. Commit, Tag e Push
if ($Version) {
    Write-Host "`n Registrando Git commit e tag v$TargetVersion..." -ForegroundColor Cyan
    & git add app/build.gradle.kts README.md apks/ .agents/ AGENTS.md
    & git commit -m "chore(release): v$TargetVersion"
    & git tag -a "v$TargetVersion" -m "Release v$TargetVersion"
    & git push origin main
    & git push origin "v$TargetVersion"
}

# 5. Publicação no GitHub
if (-not $SkipPublish) {
    $TagName = "v$TargetVersion"
    Write-Host "`n Publicando Release $TagName no GitHub..." -ForegroundColor Cyan
    
    $ReleaseArgs = @(
        "release", "create", $TagName,
        "apks\app-release-v$TargetVersion.apk",
        "apks\app-debug-v$TargetVersion.apk",
        "--title", $TagName,
        "--generate-notes"
    )
    
    if ($ReleaseNotes) {
        $ReleaseArgs += @("--notes", $ReleaseNotes)
    }

    & gh @ReleaseArgs
    if ($LASTEXITCODE -eq 0) {
        Write-Host " Release publicada com sucesso no GitHub!" -ForegroundColor Green
    }
}

Write-Host "`n Processo concluido para o Tessera Keyboard!" -ForegroundColor Cyan
