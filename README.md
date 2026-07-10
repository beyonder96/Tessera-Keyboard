# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais

* 🎨 **Temas Dinâmicos:** Suporte nativo para os temas **Claro (Light)** e **Escuro (Dark)**, adaptando-se às suas preferências.
* 🎙️ **Digitação por Voz:** Integração com reconhecimento de fala avançado, incluindo interface de gravação (`voice_ui_root`).
* 🤖 **Previsão Inteligente e IA:** Motor de sugestão de palavras nativo (`PredictionEngine`) com sugestões rápidas para facilitar a digitação.
* 😄 **Suporte a Emojis:** Painel integrado de Emojis para uso rápido em qualquer aplicativo.
* ⚙️ **Configurações Customizáveis:** Acesse facilmente opções adicionais direto da interface do teclado (`settings_ui_root`).
* 👆 **Feedback Tátil e Visual:** Animações, popups visuais instantâneos ao pressionar as teclas, acompanhados de vibração e sons ajustados ao sistema.

---

## 🚀 Como Instalar

Para usar o Tessera Keyboard no seu celular sem precisar compilar o código:

1. Acesse o repositório e vá até a pasta `/apks/` (ou baixe diretamente da aba **Releases**).
2. Baixe o arquivo `app-release.apk` (ou `app-debug.apk`).
3. Instale o APK no seu dispositivo (talvez seja necessário habilitar a "Instalação de Fontes Desconhecidas" nas configurações do seu aparelho).
4. Ao final da instalação, abra o aplicativo **Tessera** ou vá até as configurações do seu Android em `Sistema > Idiomas e entrada > Teclado na tela`.
5. Ative o Tessera Keyboard e o defina como o teclado padrão.

---

## 🛠️ Para Desenvolvedores

Se você deseja contribuir para o projeto ou modificá-lo localmente, o processo é bem simples. A arquitetura central é construída usando o `InputMethodService` nativo do Android (principalmente via `StitchKeyboardService`).

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
