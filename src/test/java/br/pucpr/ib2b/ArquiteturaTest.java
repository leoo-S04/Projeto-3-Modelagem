package br.pucpr.ib2b;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Critérios de pronto verificados no código-fonte (CLAUDE.md / enunciado). */
class ArquiteturaTest {

    private static final Path FONTES = Path.of("src/main/java/br/pucpr/ib2b");

    private static List<Path> arquivos(Path pasta) throws IOException {
        try (Stream<Path> s = Files.walk(pasta)) {
            return s.filter(p -> p.toString().endsWith(".java")).toList();
        }
    }

    @Test
    void interfaceWebNaoReferenciaMovimentacaoAcervoRelacionamento() throws IOException {
        Pattern proibido = Pattern.compile("\\b(Movimentacao|Acervo|Relacionamento|RepositorioLivro|RepositorioMovimentacao)\\b");
        for (Path p : arquivos(FONTES.resolve("web"))) {
            assertFalse(proibido.matcher(semComentarios(Files.readString(p))).find(), p + " referencia um ponto interno do domínio");
        }
    }

    /** Remove comentários (a documentação pode citar as classes; o código não pode usá-las). */
    private static String semComentarios(String fonte) {
        return fonte.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
    }

    @Test
    void nenhumTratadorTestaASituacaoDoLivro() throws IOException {
        Pattern situacao = Pattern.compile("getEstado\\(\\)|Situacao\\.|instanceof\\s+(Livre|Reservado|Emprestado|Renovado|Devolvido|Reparando|Incinerado)");
        for (Path p : arquivos(FONTES)) {
            if (!p.getFileName().toString().startsWith("Tratador")) continue;
            assertFalse(situacao.matcher(Files.readString(p)).find(), p + " testa a situação do livro");
        }
    }

    @Test
    void servicoDeMultasNaoTemCondicionalPorBancoNemPorPolitica() throws IOException {
        String servico = Files.readString(FONTES.resolve("financeiro/ServicoDeMulta.java"));
        for (String marca : List.of("\"A\"", "\"B\"", "BancoA", "BancoB", "ORIGINAL", "TETO", "TOLERANCIA", "instanceof")) {
            assertFalse(servico.contains(marca), "ServicoDeMulta conhece " + marca);
        }
    }
}
