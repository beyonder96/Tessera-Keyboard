# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais (v0.0.11)

* 🚀 **Fluidez Mecânica Absoluta & Fim do Lag:** Digitação em 120fps com multi-touch (`splitMotionEvents`), eliminação de chamadas IPC síncronas bloqueantes (`localEditCount`) e som assíncrono.
* 📖 **Dicionário Expandido PT-BR & Auto-Acentuação:** Léxico embutido de 7.500 termos frequentes. Palavras sem acento são corrigidas instantaneamente (`nao` $\rightarrow$ `não`, `voce` $\rightarrow$ `você`), preservando a caixa alta/baixa.
* 🎯 **Matriz de Proximidade QWERTY & Autocorreção Fuzzy:** Correção automática de toques em teclas adjacentes (`rudo` $\rightarrow$ `tudo`) e transposição de letras com árvore Trie $O(k)$.
* 🔄 **Autocorreção no Espaço com Undo no Backspace:** Espaço corrige erros automaticamente; 1 toque no Backspace desfaz imediatamente a troca e respeita a escolha original do usuário.
* ⚡ **Automações de Digitação:** Auto-capitalização no início de sentenças, duplo toque no espaço para inserir `". "` e anexação automática de pontuação colada à palavra precedente.
* 🖐️ **Gestos de Produtividade:** Deslizar para a esquerda no Backspace apaga palavras inteiras; deslizar horizontalmente na barra de espaço movimenta o cursor suavemente.
* 📋 **Adaptação ao Campo & Clipboard:** Tecla Enter dinâmica (Busca, Enviar, Avançar, Concluir), atalhos contextuais de `@` e `/` para e-mail/URL e pílula de 1 toque para colar a área de transferência.
* 🔒 **Proteção de Privacidade:** Detecção automática de campos de senha e privados (`isPrivateOrPassword`), impedindo o vazamento ou gravação de termos confidenciais.
* 🎨 **Temas Dinâmicos e Glassmorphism:** Suporte a temas Light e Dark com transparência e blur de fundo nativo.
* 🎙️ **Ditado por Voz com Animação Senoidal:** Reconhecimento de fala integrado com feedback tátil e visual de alta fidelidade.

---

## 🚀 Como Instalar

Para usar o Tessera Keyboard no seu celular sem precisar compilar o código:

1. Baixe a versão mais recente clicando diretamente nos botões abaixo:

   [![Baixar APK Release](https://img.shields.io/badge/Baixar-APK_Release_v0.0.11-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/raw/main/apks/app-release-v0.0.11.apk)
   [![Baixar APK Debug](https://img.shields.io/badge/Baixar-APK_Debug_v0.0.11-0095D5?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/raw/main/apks/app-debug-v0.0.11.apk)
3. Instale o APK no seu dispositivo (talvez seja necessário habilitar a "Instalação de Fontes Desconhecidas" nas configurações do seu aparelho).
4. Ao final da instalação, abra o aplicativo **Tessera** ou vá até as configurações do seu Android em `Sistema > Idiomas e entrada > Teclado na tela`.
5. Ative o Tessera Keyboard e o defina como o teclado padrão.

---

## 🛠️ Para Desenvolvedores

Se você deseja contribuir para o projeto ou modificá-lo localmente, o processo é bem simples. A arquitetura central é construída usando o `InputMethodService` nativo do Android (principalmente via `StitchKeyboardService`).
Os pacotes do projeto foram organizados em boas práticas, dividindo responsabilidades em: `activity`, `service`, `api`, `manager` e `engine`.

### Pré-requisitos
- [Android Studio](https://developer.android.com/studio) atualizado
- JDK 17+
- Android SDK (API Level 33+)

### Compilando o Projeto via Terminal

```bash
# Clone o repositório
git clone https://github.com/seu-usuario/Tessera-Keyboard.git
cd Tessera-Keyboard

# Para compilar a versão de Debug (APK será gerado em app/build/outputs/apk/debug/)
./gradlew assembleDebug

# Para compilar a versão de Release
./gradlew assembleRelease
```

---

## 🤝 Contribuições

Sinta-se à vontade para realizar um *fork*, abrir *Issues* ou enviar *Pull Requests* para melhorar o projeto. Feedbacks e melhorias na engine de previsão e novas sugestões de layout são muito bem-vindas!
