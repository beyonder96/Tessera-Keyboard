# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais (v0.0.20)

* 🔤 **Abertura Padrão Sempre em Modo Texto:** Garantia estrita de inicialização no modo alfabético QWERTY, com reset automático de modos de símbolos e numéricos ao abrir novos campos ou reabrir o teclado.
* 🖼️ **Colar Fotos Direto do Clipboard (Estilo Gboard):** Suporte nativo à API de Rich Content (`commitContent`) do Android. Ao copiar uma imagem, o teclado apresenta um chip minimalista "Colar foto" e a insere diretamente em apps compatíveis (WhatsApp, Telegram, Discord, etc.).
* 💎 **Redesign Premium do Colar (Minimalismo Rigoroso):** Eliminação completa de emojis decorativos no clipboard. Layout maduro em pílulas translúcidas com borda sutil de 1px, ícones vetoriais lineares finos (1.5dp) e tipografia neutra do sistema.
* ✍️ **Restauração Canônica de Acentos:** Mapeamento em memória de centenas de formas frequentes sem acento para suas formas canônicas acentuadas no português brasileiro (`voce` $\rightarrow$ `você`, `nao` $\rightarrow$ `não`, `agua` $\rightarrow$ `água`, `opcao` $\rightarrow$ `opção`, `facil` $\rightarrow$ `fácil`), com preservação de maiúsculas/minúsculas.
* 🧠 **Aprendizado Dinâmico de Bigramas & Próxima Palavra:** Além dos bigramas estatísticos nativos, o motor memoriza automaticamente as sequências de palavras digitadas pelo próprio usuário, adaptando a previsão de próxima palavra ao seu vocabulário pessoal.
* ⚡ **Velocidade de Digitação & Latência Otimizada:** Cache em memória de preferências de digitação (`SharedPreferences`), pré-aquecimento de predições para todas as 26 letras, eliminação de contenção de locks e feedback tátil/sonoro imediato.
* 🎯 **Acentuação Correta & Sem Falsos Acentos:** Fim definitivo de acentos indevidos em monosílabos e palavras comuns (*que*, *de*, *do*, *no*, *para*, *passe*, *demonstra*, *na*). Mais de 3.500 entradas espúrias eliminadas do dicionário léxico PT-BR.
* 🧠 **Busca por Proximidade QWERTY Multierro:** Algoritmo guiado na árvore Trie capaz de corrigir palavras com múltiplos deslizes de digitação simultâneos em teclas vizinhas (ex: `dkgitsndk` $\rightarrow$ `digitando`) em tempo sub-milissegundo (~0.1ms).
* 🚀 **Fluidez Mecânica Absoluta & Fim do Lag:** Digitação fluida com multi-touch (`splitMotionEvents`), eliminação de chamadas IPC síncronas bloqueantes (`localEditCount`) e desacoplamento da transição maiúscula/minúscula (Shift).
* 🔄 **Autocorreção no Espaço com Undo no Backspace:** Espaço corrige erros automaticamente; 1 toque no Backspace desfaz imediatamente a troca e respeita a escolha original do usuário.
* ⚡ **Automações de Digitação:** Auto-capitalização no início de sentenças, duplo toque no espaço para inserir `". "` e anexação automática de pontuação colada à palavra precedente.
* 🖐️ **Gestos de Produtividade:** Deslizar para a esquerda no Backspace apaga palavras inteiras; deslizar horizontalmente na barra de espaço movimenta o cursor suavemente.
* 🔒 **Proteção de Privacidade:** Detecção automática de campos de senha e privados (`isPrivateOrPassword`), impedindo o vazamento ou gravação de termos confidenciais.
* 🎨 **Temas Dinâmicos e Glassmorphism:** Suporte a temas Light e Dark com transparência e blur de fundo nativo.
* 🎙️ **Ditado por Voz com Animação Senoidal:** Reconhecimento de fala integrado com feedback tátil e visual de alta fidelidade.

---

## 🚀 Como Instalar

Para usar o Tessera Keyboard no seu celular sem precisar compilar o código:

1. Baixe a versão mais recente clicando diretamente nos botões abaixo:

   [![Baixar APK Release](https://img.shields.io/badge/Baixar-APK_Release_v0.0.20-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/releases/download/v0.0.20/app-release-v0.0.20.apk)
   [![Baixar APK Debug](https://img.shields.io/badge/Baixar-APK_Debug_v0.0.20-0095D5?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/releases/download/v0.0.20/app-debug-v0.0.20.apk)
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
