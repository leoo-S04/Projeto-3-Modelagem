package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.Sistema;
import br.pucpr.ib2b.biblioteca.Livro;
import br.pucpr.ib2b.biblioteca.RenovacaoNegadaException;
import br.pucpr.ib2b.biblioteca.Reserva;
import br.pucpr.ib2b.biblioteca.Situacao;
import br.pucpr.ib2b.comum.RelogioAjustavel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Req. 8 — Abstract Factory: cenários de verificação (a família JSON usa pasta temporária, nunca o arquivo real). */
class R08_FamiliasDePersistenciaTest {

    @TempDir
    Path pasta;

    @Test
    void R08_configuracaoDeTesteFornecesSoObjetosEmMemoria() throws Exception {
        FabricaDePersistencia fabrica = ConfiguradorDePersistencia.fabricaPara(Perfil.TESTE, pasta.resolve("x.json"));
        UnidadeDeTrabalho uow = fabrica.criarUnidadeDeTrabalho();

        assertInstanceOf(FabricaEmMemoria.class, fabrica);
        assertInstanceOf(UnidadeDeTrabalhoEmMemoria.class, uow);
        assertInstanceOf(RepositorioLivroEmMemoria.class, fabrica.criarRepositorioLivro(uow));
        assertInstanceOf(RepositorioMovimentacaoEmMemoria.class, fabrica.criarRepositorioMovimentacao(uow));
        assertInstanceOf(RepositorioRelacionamentoEmMemoria.class, fabrica.criarRepositorioRelacionamento(uow));
        uow.executar(() -> fabrica.criarRepositorioLivro(uow).salvar(new Livro(1, "A")));
        try (var arquivos = Files.list(pasta)) {
            assertEquals(0, arquivos.count());                     // nada foi gravado em disco
        }
    }

    @Test
    void R08_configuracaoDeProducaoFornecesRepositoriosJsonQueCompartilhamAMesmaUnidade() throws Exception {
        Path arquivo = pasta.resolve("biblioteca.json");
        FabricaDePersistencia fabrica = ConfiguradorDePersistencia.fabricaPara(Perfil.PRODUCAO, arquivo);
        UnidadeDeTrabalho uow = fabrica.criarUnidadeDeTrabalho();
        RepositorioLivro livros = fabrica.criarRepositorioLivro(uow);
        RepositorioMovimentacao movimentacao = fabrica.criarRepositorioMovimentacao(uow);
        RepositorioRelacionamento relacionamento = fabrica.criarRepositorioRelacionamento(uow);

        assertInstanceOf(UnidadeDeTrabalhoJson.class, uow);
        assertInstanceOf(RepositorioLivroJson.class, livros);
        assertInstanceOf(RepositorioMovimentacaoJson.class, movimentacao);
        assertInstanceOf(RepositorioRelacionamentoJson.class, relacionamento);
        assertSame(uow, livros.unidade());
        assertSame(uow, movimentacao.unidade());
        assertSame(uow, relacionamento.unidade());

        // Um livro e uma reserva gravados pela MESMA unidade de trabalho, em uma única confirmação
        Sistema s = Sistema.montar(fabrica, RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO));
        s.tratadorDePessoa.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        s.tratadorDeEstudante.inserirEstudante("111", "M-100");
        s.tratadorDeAcervo.inserirLivro(1, "Dom Casmurro");
        s.tratadorDeEmprestimo.reservarLivro(1, "M-100", null);

        String json = Files.readString(arquivo);
        assertTrue(json.contains("\"situacao\" : \"RESERVADO\""));  // situação gravada como texto
        assertTrue(json.contains("\"reservas\""));

        // Outro sistema lendo o mesmo arquivo reconstitui livro, estado e reserva
        Sistema relido = Sistema.montar(new FabricaJson(arquivo), RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO));
        assertEquals(Situacao.RESERVADO, relido.tratadorDeAcervo.consultarLivro(1).getEstado());
        Reserva reserva = relido.tratadorDeEmprestimo.consultarReserva(1, "M-100");
        assertEquals(LocalDate.of(2026, 11, 13), reserva.getData());
    }

    @Test
    void R08_desfazerAposFalhaDeixaOArquivoInalterado() throws Exception {
        Path arquivo = pasta.resolve("biblioteca.json");
        Sistema s = Sistema.montar(new FabricaJson(arquivo), RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO));
        s.tratadorDePessoa.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        s.tratadorDeEstudante.inserirEstudante("111", "M-100");
        s.tratadorDeAcervo.inserirLivro(1, "Dom Casmurro");
        s.tratadorDeEmprestimo.reservarLivro(1, "M-100", null);
        s.tratadorDeEmprestimo.emprestarLivro(1, "M-100", null);
        s.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100");
        s.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100");
        String antes = Files.readString(arquivo);

        // Falha no meio de uma operação: o livro já foi alterado quando a exceção acontece
        assertThrows(IllegalStateException.class, () -> s.unidadeDeTrabalho().executar(() -> {
            s.tratadorDeAcervo.consultarLivro(1).setTitulo("ALTERADO");
            throw new IllegalStateException("falha simulada no meio da operação");
        }));
        // Recusa de domínio também desfaz (3ª renovação)
        assertThrows(RenovacaoNegadaException.class, () -> s.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100"));

        assertEquals(antes, Files.readString(arquivo));                       // arquivo inalterado
        assertEquals("Dom Casmurro", s.tratadorDeAcervo.consultarLivro(1).getTitulo()); // cópia recarregada
        assertFalse(Files.exists(pasta.resolve("biblioteca.json.tmp")));
        // Estado reconstituído do arquivo continua respeitando a guarda (2 renovações já feitas)
        Sistema relido = Sistema.montar(new FabricaJson(arquivo), RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO));
        assertEquals(Situacao.RENOVADO, relido.tratadorDeAcervo.consultarLivro(1).getEstado());
        assertThrows(RenovacaoNegadaException.class, () -> relido.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100"));
    }

    @Test
    void R08_naoEPermitidoMisturarFamilias() {
        UnidadeDeTrabalho daMemoria = new FabricaEmMemoria().criarUnidadeDeTrabalho();
        FabricaJson fabricaJson = new FabricaJson(pasta.resolve("biblioteca.json"));

        assertThrows(FamiliaIncompativelException.class, () -> fabricaJson.criarRepositorioLivro(daMemoria));
        assertThrows(FamiliaIncompativelException.class,
                () -> new FabricaEmMemoria().criarRepositorioMovimentacao(fabricaJson.criarUnidadeDeTrabalho()));
    }

    @Test
    void perfilAusenteInterrompeAInicializacao() {
        assertThrows(IllegalArgumentException.class, () -> ConfiguradorDePersistencia.fabricaPara(null, null));
    }
}
