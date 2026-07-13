# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais (v0.0.3)

* 🎨 **Temas Dinâmicos e Glassmorphism:** Suporte a temas Light e Dark com forte ênfase em Glassmorphism, transparências e blur de fundo. Transparência consistente estendida para o painel de emojis.
* ⌨️ **Layout em Grade Perfeita:** Design elegante em 7 colunas perfeitamente alinhadas simulando um visual clean, com a tecla "space" substituída pelo nome "Tessera" para identidade da marca.
* 👻 **Ghost Text (Autocompletar Inline):** Sugestão avançada no próprio campo de texto onde a palavra em previsão aparece em tom cinza antes de ser confirmada. Funciona offline sem IA!
* ⚙️ **Barra de Tarefas Superior (Minimalista):** Acesso instantâneo com novos ícones "lines" minimalistas para Emojis, Configurações Diretas (abre as opções do app), Animação elegante na tecla Shift (Aa -> AA) e maior destaque para o Ditado por Voz.
* 🤖 **Previsão Inteligente e IA:** Motor de sugestão de palavras nativo (`PredictionEngine`) com sugestões rápidas e integração ao Gemini (Sparkles) acessível na base do teclado.
* 👆 **Feedback Tátil e Visual:** Animações instantâneas (ripples reais ajustados ao formato pílula), popups visuais ao pressionar, vibração e sons ajustados ao sistema.

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
