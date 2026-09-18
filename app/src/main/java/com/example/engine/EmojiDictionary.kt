package com.example.engine

import java.text.Normalizer

data class EmojiItem(
    val emoji: String,
    val keywords: List<String>
)

object EmojiDictionary {

    private val emojiList = listOf(
        // Rosto & Emoção
        EmojiItem("😀", listOf("sorriso", "feliz", "alegre", "contente", "dentes", "smile")),
        EmojiItem("😃", listOf("sorriso", "feliz", "olhos", "alegria", "happy")),
        EmojiItem("😄", listOf("feliz", "olho", "sorriso", "gargalhada", "riso")),
        EmojiItem("😁", listOf("sorriso", "dentes", "radiante", "orgulho")),
        EmojiItem("😆", listOf("risada", "fechado", "kkk", "haha", "rir")),
        EmojiItem("😅", listOf("suor", "alívio", "vergonha", "ufa", "nervoso")),
        EmojiItem("😂", listOf("chorando de rir", "risada", "kkk", "haha", "engraçado", "humor", "gargalhada")),
        EmojiItem("🤣", listOf("rolando de rir", "gargalhada", "kkk", "demais", "muito bom")),
        EmojiItem("🥲", listOf("sorriso com lágrima", "orgulho", "emocionado", "superação")),
        EmojiItem("🥹", listOf("olhos brilhando", "emocionado", "por favor", "comovido", "fofo")),
        EmojiItem("☺️", listOf("tímido", "calmo", "sorriso", "paz")),
        EmojiItem("😊", listOf("vergonha", "fofo", "sorriso", "meigo", "gentil")),
        EmojiItem("😇", listOf("anjo", "inocente", "santo", "aureola", "bom")),
        EmojiItem("🙂", listOf("sorriso leve", "simpático", "ok", "normal")),
        EmojiItem("🙃", listOf("de ponta cabeça", "ironia", "sarcasmo", "loucura")),
        EmojiItem("😉", listOf("piscadinha", "cumplicidade", "flerte", "segredo")),
        EmojiItem("😌", listOf("alívio", "calmo", "tranquilo", "relaxado")),
        EmojiItem("😍", listOf("olhos de coração", "apaixonado", "amor", "lindo", "amei", "perfeito")),
        EmojiItem("🥰", listOf("corações", "apaixonado", "carinho", "muito amor", "grato")),
        EmojiItem("😘", listOf("beijo", "coração", "beijinho", "carinho", "te amo")),
        EmojiItem("😗", listOf("beijo", "assobio", "biquinho")),
        EmojiItem("😙", listOf("beijo sorriso", "carinho", "gentil")),
        EmojiItem("😚", listOf("beijo olhos fechados", "afeto", "docilidade")),
        EmojiItem("😋", listOf("delícia", "gostoso", "comida", "língua", "apetite")),
        EmojiItem("😛", listOf("língua de fora", "brincadeira", "zombaria")),
        EmojiItem("😜", listOf("língua e piscada", "zoeira", "brincando", "festa")),
        EmojiItem("🤪", listOf("maluco", "doido", "pirado", "engraçado", "louco")),
        EmojiItem("🤨", listOf("desconfiado", "sobrancelha", "dúvida", "estranho")),
        EmojiItem("🧐", listOf("monóculo", "analisando", "investigando", "curioso")),
        EmojiItem("🤓", listOf("nerd", "estudioso", "óculos", "geek", "inteligente")),
        EmojiItem("😎", listOf("óculos escuros", "legal", "estiloso", "chefe", "tranquilo", "suave")),
        EmojiItem("🤩", listOf("olhos de estrela", "maravilhado", "fã", "incrível", "famoso")),
        EmojiItem("🥳", listOf("festa", "comemoração", "aniversário", "parabéns", "balada")),
        EmojiItem("😏", listOf("malicioso", "safado", "flerte", "convencido")),
        EmojiItem("😒", listOf("desanimado", "chateado", "tedio", "invejoso")),
        EmojiItem("😞", listOf("decepcionado", "triste", "desapontado", "baixo")),
        EmojiItem("😔", listOf("pensativo", "pesar", "tristeza", "lamento")),
        EmojiItem("😟", listOf("preocupado", "angústia", "medo", "tenso")),
        EmojiItem("😕", listOf("confuso", "perdido", "ué", "como assim")),
        EmojiItem("🙁", listOf("triste", "descontente", "baixo astral")),
        EmojiItem("☹️", listOf("muito triste", "chateado", "frown")),
        EmojiItem("😣", listOf("lutando", "difícil", "esforço", "dor")),
        EmojiItem("😖", listOf("angustiado", "frustrado", "irritado")),
        EmojiItem("😫", listOf("cansado", "exausto", "socorro", "não aguento")),
        EmojiItem("😩", listOf("desesperado", "gemendo", "triste", "cansado")),
        EmojiItem("🥺", listOf("pidão", "por favorzinho", "olhinhos", "careta", "dó")),
        EmojiItem("😢", listOf("chorando", "lágrima", "triste", "choro", "magoado")),
        EmojiItem("😭", listOf("chorando muito", "desespero", "berreiro", "lágrimas", "choro", "triste")),
        EmojiItem("😮‍💨", listOf("suspiro", "alívio", "cansaço", "respiro")),
        EmojiItem("😤", listOf("arrogante", "fúria", "orgulho", "bufando")),
        EmojiItem("😠", listOf("bravo", "irritado", "zangado", "raiva")),
        EmojiItem("😡", listOf("furioso", "muita raiva", "vermelho", "ódio", "puto")),
        EmojiItem("🤬", listOf("xingamento", "palavrão", "revoltado", "furioso")),
        EmojiItem("🤯", listOf("cabeça explodindo", "chocado", "surpreso", "mente")),
        EmojiItem("😳", listOf("envergonhado", "chocado", "rubor", "olhão")),
        EmojiItem("🥵", listOf("muito quente", "calor", "suando", "abafado", "febre")),
        EmojiItem("🥶", listOf("muito frio", "congelando", "gelo", "tremer")),
        EmojiItem("😱", listOf("grito", "pânico", "terror", "medo", "chocado")),
        EmojiItem("😨", listOf("assustado", "medo", "receio")),
        EmojiItem("😰", listOf("ansioso", "suor frio", "nervosismo")),
        EmojiItem("😥", listOf("aliviado", "lágrima", "preocupação")),
        EmojiItem("😓", listOf("suor frio", "cansaço", "desconforto")),
        EmojiItem("🤔", listOf("pensando", "dúvida", "reflexão", "hmm", "pensativo")),
        EmojiItem("🫢", listOf("mão na boca", "falei demais", "ops", "surpresa")),
        EmojiItem("🤫", listOf("silêncio", "psiu", "segredo", "quieto")),
        EmojiItem("🤥", listOf("mentiroso", "nariz comprido", "mentira", "pinóquio")),
        EmojiItem("😶", listOf("sem boca", "sem palavras", "calado", "mudo")),
        EmojiItem("😐", listOf("neutro", "sem graça", "sério", "indiferente")),
        EmojiItem("😑", listOf("inexpressivo", "saco cheio", "indiferença")),
        EmojiItem("😬", listOf("eita", "constrangido", "tensão", "queimação")),
        EmojiItem("🫠", listOf("derretendo", "vergonha", "desaparecendo", "calor")),
        EmojiItem("🙄", listOf("revirando olhos", "aff", "chatice", "desdém")),
        EmojiItem("😴", listOf("dormindo", "sono", "zzz", "ronco")),
        EmojiItem("🤤", listOf("babando", "delícia", "sono", "fome")),
        EmojiItem("😷", listOf("máscara", "doente", "vírus", "hospital")),
        EmojiItem("🤒", listOf("termômetro", "doente", "febre", "gripado")),
        EmojiItem("🤕", listOf("machucado", "curativo", "dor de cabeça")),
        EmojiItem("🤢", listOf("enjoado", "nojo", "verde", "mal")),
        EmojiItem("🤮", listOf("vomitando", "ânsia", "nojo", "podre")),
        EmojiItem("🤧", listOf("espirro", "lenço", "alergia", "resfriado")),
        EmojiItem("🤡", listOf("palhaço", "trouxa", "zoeira", "circo")),
        EmojiItem("💩", listOf("cocô", "merda", "bosta", "engraçado")),
        EmojiItem("👻", listOf("fantasma", "boo", "assombração", "halloween")),
        EmojiItem("💀", listOf("caveira", "morte", "morri", "morto de rir")),
        EmojiItem("☠️", listOf("perigo", "veneno", "pirata")),
        EmojiItem("👽", listOf("alien", "extraterrestre", "óvni")),
        EmojiItem("🤖", listOf("robô", "bot", "inteligência", "ia")),
        EmojiItem("🎃", listOf("abóbora", "halloween", "dia das bruxas")),

        // Gestos & Mãos
        EmojiItem("👍", listOf("joinha", "positivo", "ok", "curtir", "beleza", "concordo", "sim")),
        EmojiItem("👎", listOf("negativo", "descurtir", "ruim", "não gostei", "péssimo")),
        EmojiItem("👌", listOf("perfeito", "ok", "ótimo", "tudo certo", "show")),
        EmojiItem("🤌", listOf("italiano", "o que você quer", "gesto", "ma che")),
        EmojiItem("🤏", listOf("pouquinho", "pequeno", "quase", "pouco")),
        EmojiItem("✌️", listOf("paz e amor", "dois", "vitória", "v")),
        EmojiItem("🤞", listOf("figas", "sorte", "torcendo", "tomara")),
        EmojiItem("🫰", listOf("coração com dedos", "kpop", "dorama", "amor")),
        EmojiItem("🤟", listOf("te amo", "rock", "rock and roll", "sinal")),
        EmojiItem("🤘", listOf("rock", "metal", "chifre")),
        EmojiItem("🤙", listOf("hang loose", "tranquilo", "me liga", "surf")),
        EmojiItem("👈", listOf("esquerda", "olha ali", "apontar")),
        EmojiItem("👉", listOf("direita", "olha lá", "apontar")),
        EmojiItem("👆", listOf("cima", "olha em cima", "este")),
        EmojiItem("👇", listOf("baixo", "olha aqui", "abaixo")),
        EmojiItem("☝️", listOf("um minuto", "atenção", "primeiro")),
        EmojiItem("👏", listOf("palmas", "aplausos", "parabéns", "bravos")),
        EmojiItem("🙌", listOf("mãos para cima", "glória", "comemoração", "viva")),
        EmojiItem("🫶", listOf("mãos de coração", "amor", "carinho", "gratidão")),
        EmojiItem("👐", listOf("mãos abertas", "abraço")),
        EmojiItem("🤲", listOf("mãos juntas", "rezando", "pedindo")),
        EmojiItem("🤝", listOf("aperto de mão", "acordo", "fechado", "parceria", "negócio")),
        EmojiItem("🙏", listOf("por favor", "obrigado", "gratidão", "oração", "amém", "rezando", "valeu")),
        EmojiItem("✍️", listOf("escrevendo", "caneta", "anotando", "estudando")),
        EmojiItem("💅", listOf("unha", "deboche", "maravilhosa", "esmaltar")),
        EmojiItem("🤳", listOf("selfie", "foto", "celular")),
        EmojiItem("💪", listOf("força", "músculo", "treino", "academia", "forte", "foco")),

        // Corações & Sentimentos
        EmojiItem("❤️", listOf("coração vermelho", "amor", "te amo", "paixão", "vida")),
        EmojiItem("🧡", listOf("coração laranja", "energia", "amizade")),
        EmojiItem("💛", listOf("coração amarelo", "amizade", "alegria", "sol")),
        EmojiItem("💚", listOf("coração verde", "esperança", "natureza", "saúde")),
        EmojiItem("💙", listOf("coração azul", "confiança", "lealdade", "calma")),
        EmojiItem("💜", listOf("coração roxo", "violeta", "glamour")),
        EmojiItem("🖤", listOf("coração preto", "luto", "trevas", "gótico")),
        EmojiItem("🤍", listOf("coração branco", "paz", "pureza")),
        EmojiItem("🤎", listOf("coração marrom", "chocolate", "terra")),
        EmojiItem("💔", listOf("coração quebrado", "desilusão", "dor", "fim de namoro", "triste")),
        EmojiItem("❤️‍🔥", listOf("coração pegando fogo", "ardente", "paixão intensa")),
        EmojiItem("💖", listOf("coração brilhante", "carinho", "especial")),
        EmojiItem("💗", listOf("coração crescendo", "batimentos", "emocionado")),
        EmojiItem("💓", listOf("coração batendo", "palpitação", "amor")),
        EmojiItem("💞", listOf("corações girando", "romance")),
        EmojiItem("💕", listOf("dois corações", "fofura", "namoro")),
        EmojiItem("🔥", listOf("fogo", "chama", "quente", "top", "hype", "arrasou", "brabo")),
        EmojiItem("✨", listOf("brilho", "estrelas", "mágico", "especial", "novo", "limpo")),
        EmojiItem("💯", listOf("cem por cento", "nota dez", "perfeito", "certeza", "exato")),
        EmojiItem("💥", listOf("explosão", "boom", "impacto", "choque")),
        EmojiItem("🎉", listOf("festa", "confete", "comemoração", "parabéns", "celebração")),

        // Comidas & Bebidas
        EmojiItem("🍕", listOf("pizza", "fatia", "comida", "jantar", "queijo")),
        EmojiItem("🍔", listOf("hambúrguer", "lanche", "fast food", "carne")),
        EmojiItem("🍟", listOf("batata frita", "batata", "lanche")),
        EmojiItem("🌭", listOf("cachorro quente", "hot dog", "salsicha")),
        EmojiItem("🥪", listOf("sanduíche", "misto", "lanche natural")),
        EmojiItem("🌮", listOf("taco", "mexicano", "comida")),
        EmojiItem("🌯", listOf("burrito", "wrap")),
        EmojiItem("☕", listOf("café", "quente", "manhã", "espresso", "despertar")),
        EmojiItem("🍺", listOf("cerveja", "chopp", "happy hour", "álcool", "bar")),
        EmojiItem("🍻", listOf("brinde", "cervejas", "comemoração", "saúde")),
        EmojiItem("🍷", listOf("vinho", "taça", "jantar", "romântico")),
        EmojiItem("🥤", listOf("refrigerante", "suco", "copo", "bebida")),
        EmojiItem("🍿", listOf("pipoca", "cinema", "filme", "série")),
        EmojiItem("🎂", listOf("bolo", "aniversário", "parabéns", "festa")),
        EmojiItem("🍫", listOf("chocolate", "doce", "sobremesa")),
        EmojiItem("🍦", listOf("sorvete", "casquinha", "doce", "refrescante")),

        // Animais & Natureza
        EmojiItem("🐶", listOf("cachorro", "cão", "filhote", "auau", "pet")),
        EmojiItem("🐱", listOf("gato", "gatinho", "miau", "felino", "pet")),
        EmojiItem("🐵", listOf("macaco", "macaquinho", "zoeira")),
        EmojiItem("🦁", listOf("leão", "fera", "rei", "selva")),
        EmojiItem("🐯", listOf("tigre", "felino")),
        EmojiItem("🐻", listOf("urso", "ursinho", "abraço")),
        EmojiItem("🦊", listOf("raposa", "esperta")),
        EmojiItem("🐼", listOf("panda", "fofo")),
        EmojiItem("🐨", listOf("coala")),
        EmojiItem("🐮", listOf("vaca", "muu")),
        EmojiItem("🐷", listOf("porco", "porquinho")),
        EmojiItem("🐸", listOf("sapo", "pepe")),
        EmojiItem("☀️", listOf("sol", "dia", "calor", "ensolarado", "luz")),
        EmojiItem("🌙", listOf("lua", "noite", "lua crescente", "dormir")),
        EmojiItem("⭐", listOf("estrela", "brilho", "favorito")),
        EmojiItem("🌧️", listOf("chuva", "chovendo", "tempo feio", "água")),
        EmojiItem("🌈", listOf("arco-íris", "cores", "colorido", "orgulho")),

        // Objetos & Tecnologia
        EmojiItem("📱", listOf("celular", "smartphone", "telefone", "tela")),
        EmojiItem("💻", listOf("notebook", "computador", "laptop", "trabalho", "estudo")),
        EmojiItem("🎮", listOf("videogame", "controle", "game", "jogar", "playstation", "xbox")),
        EmojiItem("🚗", listOf("carro", "automóvel", "viagem", "trânsito")),
        EmojiItem("✈️", listOf("avião", "voar", "viagem", "férias", "aeroporto")),
        EmojiItem("💰", listOf("saco de dinheiro", "dinheiro", "grana", "riqueza", "rico")),
        EmojiItem("💵", listOf("dinheiro", "nota", "dólar", "reais", "pagamento")),
        EmojiItem("💡", listOf("ideia", "lâmpada", "pensamento", "insight", "solução")),
        EmojiItem("🔒", listOf("cadeado", "trancado", "segurança", "senha", "privado"))
    )

    fun search(query: String): List<String> {
        val normalizedQuery = removeAccents(query.trim().lowercase())
        if (normalizedQuery.isEmpty()) return emptyList()

        val results = mutableListOf<String>()
        for (item in emojiList) {
            val matches = item.keywords.any { keyword ->
                val normKeyword = removeAccents(keyword.lowercase())
                normKeyword.contains(normalizedQuery) || normalizedQuery.contains(normKeyword)
            }
            if (matches) {
                results.add(item.emoji)
            }
        }
        return results.distinct()
    }

    private fun removeAccents(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}
