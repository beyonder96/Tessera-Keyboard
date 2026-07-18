# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais (v0.008)

* 🔴 **Teclas Perfeitamente Redondas:** Ajuste de layout dinâmico que converte as teclas em círculos perfeitos, eliminando a forma oval.
* 😊 **Layout de Emojis Aprimorado:** Emojis organizados em categorias perfeitas. Adição do botão "Backspace" integrado na janela de emojis e correção dos ícones indicadores de abas na barra inferior.
* á **Acentos 100% Interativos:** Popups focados permitem selecionar letras acentuadas (como á, é, í) através de cliques reais com feedback, corrigindo o problema de interação travada.
* 🐛 **Correção de Sobreposição:** Correção definitiva do bug onde a tela de emojis continuava visível e sobrepunha o teclado normal após bloquear e desbloquear o celular.
* ✍️ **Sugestões Inline e Barra de Palavras:** O texto sugerido (Ghost Text) é finalizado ao pressionar Espaço. Uma nova barra inteligente de sugestões aparece quando há predições para a palavra atual.
* 🎨 **Temas Dinâmicos e Glassmorphism:** Suporte a temas Light e Dark com forte ênfase em Glassmorphism, transparências e blur de fundo. Transparência consistente estendida para o painel de emojis. "lines" minimalistas para Emojis, Configurações Diretas (abre as opções do app), Animação elegante na tecla Shift (Aa -> AA) e maior destaque para o Ditado por Voz.
* 🤖 **Previsão Inteligente e IA em Background:** Motor de sugestão de palavras nativo (`PredictionEngine`) com sugestões rápidas, executadas em background coroutines.
* 👆 **Feedback Tátil e Visual Aprimorados:** Animações instantâneas (ripples reais ajustados ao formato pílula), popups centrais precisos ao pressionar (para seleção de acentos) e vibração.
* 🎙️ **Animação Premium de Voz:** Novo visual de onda senoidal (Sine Wave) fluida para feedback do microfone.
* 🌎 **Suporte Amplo a Emojis e Bandeiras:** Catálogo completo de centenas de emojis atualizados, suporte a tons de pele e lista exaustiva de bandeiras integrados à rolagem nativa.

---

## 🚀 Como Instalar

Para usar o Tessera Keyboard no seu celular sem precisar compilar o código:

1. Acesse a pasta [`/apks/`](./apks/) do repositório (ou baixe diretamente da aba **Releases**).
2. Baixe o arquivo [`app-release-v0.008.apk`](./apks/app-release-v0.008.apk) (ou [`app-debug-v0.008.apk`](./apks/app-debug-v0.008.apk)).
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
