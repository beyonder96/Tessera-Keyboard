---
name: release-apk
description: >-
  Build, package, sign and release Android APKs for Tessera Keyboard, create Git tags, and publish GitHub Releases with attached APKs.
---

# Release APK & GitHub Publishing Workflow — Tessera Keyboard

Esta skill / slash command automatiza o ciclo completo de build, geração de APKs assinados (Release & Debug), tagueamento Git e publicação de Releases no GitHub com assets anexados para o **Tessera Keyboard**.

## Quando Usar
- Quando o usuário pedir para gerar release, gerar APK, subir nova versão do teclado, criar tag no GitHub ou executar `/release-apk`.
- Quando o usuário executar `/release-apk` no chat do Antigravity.

## Passos de Execução

### 1. Verificar Versão Atual e Mudanças Pendentes
- Inspecione `app/build.gradle.kts` para ler `versionCode` e `versionName`.
- Se o usuário especificou uma nova versão (ex: `0.0.20`), atualize `versionCode = versionCode + 1` e `versionName = "x.y.z"` no `app/build.gradle.kts`.
- Atualize os badges de download e notas de versão no `README.md` se aplicável.

### 2. Configurar Ambiente Local
Certifique-se de que o JDK 17 e Android SDK estão no PATH da sessão:
```powershell
$env:JAVA_HOME = "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7"
$env:ANDROID_HOME = "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk"
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
```

### 3. Compilar APKs (Release Assinado e Debug)
Execute:
```powershell
.\gradlew.bat assembleRelease assembleDebug --stacktrace
```

Copie os binários gerados para a pasta `apks/`:
```powershell
New-Item -ItemType Directory -Force -Path "apks"
Copy-Item "app/build/outputs/apk/release/app-release.apk" -Destination "apks/app-release.apk" -Force
Copy-Item "app/build/outputs/apk/release/app-release.apk" -Destination "apks/app-release-v$version.apk" -Force
Copy-Item "app/build/outputs/apk/debug/app-debug.apk" -Destination "apks/app-debug.apk" -Force
Copy-Item "app/build/outputs/apk/debug/app-debug.apk" -Destination "apks/app-debug-v$version.apk" -Force
```

### 4. Commit e Tag Git
```powershell
git add app/build.gradle.kts README.md apks/
git commit -m "chore(release): v$version"
git tag -a "v$version" -m "Release v$version"
git push origin main
git push origin "v$version"
```

### 5. Publicar GitHub Release com Assets
```powershell
gh release create "v$version" "apks/app-release-v$version.apk" "apks/app-debug-v$version.apk" --title "v$version" --generate-notes
```

---

### ⚡ Atalho Rápido via Script de Automação
Você também pode executar todas as etapas acima com um único comando:
```powershell
& ".\.agents\skills\release-apk\scripts\release.ps1" -Version "0.0.20"
```
*(Parâmetros opcionais: `-SkipBuild`, `-SkipPublish`, `-ReleaseNotes "notas"`)*
