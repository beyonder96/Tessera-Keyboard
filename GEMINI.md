# Diretrizes de Desenvolvimento — Tessera Keyboard

Diretrizes de arquitetura, compilação e qualidade para o Tessera Keyboard (IME Android).

---

## ☕ 1. Ambiente & Compilação no Windows

Antes de chamar `.\gradlew.bat`, configure o ambiente:
```powershell
$env:JAVA_HOME = if (Test-Path "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot") { "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot" } else { "C:\Users\kenne\.gemini\antigravity\scratch\jdk-17\jdk-17.0.12+7" }
$env:ANDROID_HOME = if (Test-Path "C:\Users\kenne\AppData\Local\Android\Sdk") { "C:\Users\kenne\AppData\Local\Android\Sdk" } else { "C:\Users\kenne\.gemini\antigravity\scratch\android-sdk" }
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"
```

### Comandos Rápidos
- Validação rápida de sintaxe:
  ```powershell
  .\gradlew.bat compileDebugKotlin --daemon
  ```
- Gerar APKs:
  ```powershell
  .\gradlew.bat assembleRelease assembleDebug --stacktrace
  ```

---

## 📱 2. Diretrizes de Código e Arquitetura IME (Teclado)

1. **Privacidade e Segurança Rígida:**
   - **NUNCA** logar, transmitir ou armazenar textos digitados em campos com `isPrivateOrPassword`.
   - Limite histórico/aprendizado estritamente ao armazenamento seguro local (`EncryptedSharedPreferences` ou DataStore isolado).

2. **Baixa Latência & IME Service:**
   - O teclado precisa de resposta tátil/renderização sub-milissegundo para evitar atrasos na digitação.
   - Cálculos pesados (sugestões de palavras, Trie, modelos) devem rodar de forma assíncrona.

3. **Jetpack Compose:**
   - Modelos de teclas `@Immutable`.
   - Evitar recomposições globais em cada tecla pressionada.
