# Tessera Keyboard — Antigravity Agent Guidelines

Este repositório é o **Tessera Keyboard**, um teclado customizado para Android desenvolvido em Kotlin, focado em alta performance, minimalismo e baixa latência através da API nativa `InputMethodService`.

---

## 🛠️ Ambiente de Desenvolvimento & Compilação

Para garantir builds consistentes no ambiente local do Windows:
- **Java Home (JDK 17 Portable):** `C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7`
- **Android SDK:** `C:\Users\kenne\.gemini\antigravity\scratch\android-sdk` (ou `$env:LOCALAPPDATA\Android\Sdk`)
- **Target SDK:** 36 (Android 16)
- **Compile SDK:** 36 (minorApiLevel = 1)
- **Min SDK:** 24 (Android 7.0)
- **Build Tool:** Gradle (`.\gradlew.bat`)
- **Signing Keystore:** `tessera-release.jks` na raiz do projeto (configurado em `signingConfigs.release`)

---

## ⚡ Comandos e Actions Disponíveis

### 1. Slash Command / Skill de Release (`/release-apk`)
Sempre que solicitado para gerar release, gerar APKs ou publicar versão:
- Execute a skill `release-apk` em `.agents/skills/release-apk/SKILL.md`
- Ou rode diretamente o script de automação no PowerShell:
  ```powershell
  & ".\.agents\skills\release-apk\scripts\release.ps1" -Version "0.0.20"
  ```

### 2. Slash Command / Skill de Validação Rápida (`/check`)
Sempre que fizer alterações em código Kotlin ou quiser validar sintaxe rapidamente:
- Execute a skill `check` em `.agents/skills/check/SKILL.md`
- Ou rode diretamente o script:
  ```powershell
  & ".\.agents\skills\check\scripts\check.ps1"
  ```
- Comando direto:
  ```powershell
  .\gradlew.bat compileDebugKotlin -Dorg.gradle.daemon=true
  ```

---

## 📐 Diretrizes de Arquitetura e Performance do Teclado

1. **Latência Ultrabaixa:** O teclado é executado em tempo real em primeiro plano. Evite processamentos pesados síncronos na Main Thread durante eventos de digitação ou toque.
2. **Gestão de Toques & Multi-Touch:** Manter `splitMotionEvents` e tratamentos não-bloqueantes para digitação rápida sem engasgos.
3. **Privacidade Estrita:** Nunca logar nem persistir dados se `isPrivateOrPassword` estiver ativo.
