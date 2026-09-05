# Tessera Keyboard ⌨️

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)

**Tessera Keyboard** é um teclado customizado para Android desenvolvido em Kotlin. Ele foca em uma interface minimalista e fluida, oferecendo usabilidade avançada, performance otimizada e um conjunto de recursos modernos para melhorar a sua experiência de digitação.

---

## ✨ Funcionalidades Principais (v0.0.10)

* 🚀 **Desacoplamento Completo da UI Thread:** Digitação instantânea com buffer local mutável em memória, eliminando chamadas síncronas bloqueantes de IPC Binder (`getTextBeforeCursor`) da thread principal.
* 🌳 **Árvore de Prefixos Trie ($O(k)$) com Frequência:** Autocomplete em tempo $O(k)$ com normalização rápida de diacríticos sem alocações de Regex, ranqueando palavras aprendidas e termos mais frequentes.
* 🧹 **Eliminação de GC Thrashing:** Reutilização de arrays primitivos para coordenadas de popups (`keyLocation` e `rootLocation`), validação inline de caracteres e erradicação de alocações temporárias na JVM durante a digitação.
* ⚡ **Otimização da Camada Visual & Shimmer:** Acesso $O(1)$ ao mapa de teclas (`keyViewMap`), eliminação de passagens redundantes de layout e pausa automática da animação shimmer na barra de espaço durante digitação rápida contínua.
* 🎯 **Autocompletar Não-Invasivo:** O Espaço agora insere exclusivamente `' '`, deixando a aceitação de sugestões para o clique intencional do usuário na barra.
* 🔒 **Proteção de Privacidade:** Detecção automática de campos de senha e privados (`isPrivateOrPassword`), impedindo o aprendizado de termos confidenciais no dicionário.
* 🔴 **Teclas Perfeitamente Redondas:** Ajuste de layout dinâmico que converte as teclas em círculos perfeitos, eliminando a forma oval.
* 😊 **Layout de Emojis Aprimorado:** Emojis organizados em categorias, botão "Backspace" integrado e correção dos indicadores de abas.
* á **Acentos 100% Interativos:** Popups focados permitem selecionar letras acentuadas (como á, é, í) através de cliques reais com feedback.
* 🎨 **Temas Dinâmicos e Glassmorphism:** Suporte a temas Light e Dark com forte ênfase em Glassmorphism, transparências e blur de fundo.
* 🎙️ **Ditado por Voz com Animação Senoidal:** Feedback tátil e visual de alta fidelidade para reconhecimento de fala.

---

## 🚀 Como Instalar

Para usar o Tessera Keyboard no seu celular sem precisar compilar o código:

1. Baixe a versão mais recente clicando diretamente nos botões abaixo:

   [![Baixar APK Release](https://img.shields.io/badge/Baixar-APK_Release_v0.0.10-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/raw/main/apks/app-release-v0.0.10.apk)
   [![Baixar APK Debug](https://img.shields.io/badge/Baixar-APK_Debug_v0.0.10-0095D5?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beyonder96/Tessera-Keyboard/raw/main/apks/app-debug-v0.0.10.apk)
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
