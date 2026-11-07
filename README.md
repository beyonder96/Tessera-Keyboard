# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais (v1.2.3)

* 💎 **Redesign Visual Inspirado no Tessera Core:** Interface do app e dashboard completamente remodelados no estilo minimalista Obsidian Void e Liquid Glass: cartões de agrupamento com cantos suaves de 26dp, cabeçalho serifado "Tessera." com indicador "MONOLITH IME", badges circulares táteis (40dp), controles segmentados para formato de tecla, cartões de temas com swatch visual dinâmico (incluindo Cyberpunk) e seletor cromático para rastro de deslize.
* 🤖 **Barra de Recomendações da IA Otimizada:** Solução definitiva para cortes de texto no copiloto de IA do teclado. O container agora conta com altura adaptativa (mínimo de 40dp), exibição expansível em até 2 linhas com elipsagem elegante, espaçamento respirável e botão de dispensa (`✕`) compacto de toque único.
* ⌨️ **Live Preview Interativo com Switcher IA:** Painel interativo de demonstração no app com suporte a todos os temas, renderização de dicas de caracteres em tempo real, animação tátil de clique e botão "Ver Barra IA ✨" para testes instantâneos.
* 🎯 **Refinamento de Dicas de Teclas & Linha Numérica:** Ocultação inteligente e dinâmica dos números secundários (`1..0`) nas teclas alfabéticas `Q..P` sempre que a Linha Numérica superior estiver habilitada, evitando redundâncias visuais e mantendo as teclas limpas.
* 🤖 **IA Participativa Resiliente (Qwen 3.8 & GPT-OSS na Groq):** Modelos de ponta da Groq (`qwen/qwen3.8-27b`, `openai/gpt-oss-20b` e `openai/gpt-oss-120b`) com descoberta dinâmica automática de modelos via API (`/models`) e cascata resiliente de fallbacks.
* 🎨 **Nova Identidade Visual & Ícones Adaptativos:** Logotipo com keycap mecânica tátil e monograma caligráfico "T" em alto contraste minimalista, suporte a ícones adaptativos do Android (API 26+) e tema dinâmico monocromático (Android 13+).
* 🔤 **Abertura Padrão Sempre em Modo Texto:** Inicialização no modo alfabético QWERTY, com reset automático de modos de símbolos e numéricos ao abrir novos campos.
* 🖼️ **Colar Fotos Direto do Clipboard (Estilo Gboard):** Suporte nativo à API de Rich Content (`commitContent`) do Android.
* ✍️ **Restauração Canônica de Acentos:** Mapeamento em memória de centenas de formas frequentes sem acento para suas formas canônicas acentuadas no português brasileiro (`voce` $\rightarrow$ `você`, `nao` $\rightarrow$ `não`, `agua` $\rightarrow$ `água`).
* ⚡ **Velocidade de Digitação & Latência Ultrabaixa:** Cache em memória de preferências de digitação, pré-aquecimento de predições para todas as 26 letras, eliminação de contenção de locks e feedback tátil/sonoro imediato.
* 🔄 **Autocorreção no Espaço com Undo no Backspace:** Espaço corrige erros automaticamente; 1 toque no Backspace desfaz imediatamente a troca e respeita a escolha original do usuário.
* 🔒 **Proteção de Privacidade Estrita:** Detecção automática de campos de senha e privados (`isPrivateOrPassword`), impedindo o vazamento ou gravação de termos confidenciais.

---

## 🚀 Como Instalar

Para usar o Tessera Keyboard no seu celular sem precisar compilar o código:

1. Baixe a versão mais recente clicando diretamente nos botões abaixo:

   [![Baixar APK Release](https://img.shields.io/badge/Baixar-APK_Release_v1.2.3-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/releases/download/v1.2.3/app-release-v1.2.3.apk)
   [![Baixar APK Debug](https://img.shields.io/badge/Baixar-APK_Debug_v1.2.3-0095D5?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/releases/download/v1.2.3/app-debug-v1.2.3.apk)
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
