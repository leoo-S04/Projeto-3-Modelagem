package br.pucpr.ib2b.persistencia;

import br.pucpr.ib2b.Cenario;
import br.pucpr.ib2b.Sistema;
import br.pucpr.ib2b.biblioteca.Livro;
import br.pucpr.ib2b.biblioteca.RenovacaoNegadaException;
import br.pucpr.ib2b.biblioteca.ComprovanteReserva;
import br.pucpr.ib2b.biblioteca.StatusSolicitacao;
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
        assertInstanceOf(RepositorioDeSolicitacoesEmMemoria.class, fabrica.criarRepositorioDeSolicitacoes(uow));
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
        ComprovanteReserva reserva = relido.tratadorDeEmprestimo.consultarReserva(1, "M-100");
        assertEquals(LocalDate.of(2026, 11, 13), reserva.data());
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
        assertEquals(antes, Files.readString(arquivo));                       // arquivo inalterado
        assertEquals("Dom Casmurro", s.tratadorDeAcervo.consultarLivro(1).getTitulo()); // cópia recarregada
        assertFalse(Files.exists(pasta.resolve("biblioteca.json.tmp")));

        // Recusa de domínio dentro da fachada (3ª renovação) também desfaz: o livro no arquivo não muda
        assertThrows(RenovacaoNegadaException.class, () -> s.circulacao().renovar(1, "M-100"));
        assertEquals(antes, Files.readString(arquivo));

        // Estado reconstituído do arquivo continua respeitando a guarda (2 renovações já feitas)
        Sistema relido = Sistema.montar(new FabricaJson(arquivo), RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO));
        assertEquals(Situacao.RENOVADO, relido.tratadorDeAcervo.consultarLivro(1).getEstado());
        assertEquals(StatusSolicitacao.RECUSADA, relido.tratadorDeEmprestimo.renovarEmprestimo(1, "M-100").status());
    }

    @Test
    void multaBoletoSolicitacoesEConfiguracaoSobrevivemAoReinicio() throws Exception {
        Path arquivo = pasta.resolve("biblioteca.json");
        RelogioAjustavel relogio = RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO);
        Sistema s = Sistema.montar(new FabricaJson(arquivo), relogio);
        s.tratadorDePessoa.inserirPessoa("111", "Ana", "Souza", "ana@pucpr.br");
        s.tratadorDeEstudante.inserirEstudante("111", "M-100");
        s.tratadorDeAcervo.inserirLivro(1, "Dom Casmurro");
        s.tratadorDeEmprestimo.reservarLivro(1, "M-100", null);
        s.tratadorDeEmprestimo.emprestarLivro(1, "M-100", null);
        s.tratadorDeMulta.definirPoliticaVigente("TOLERANCIA");
        relogio.avancarDias(37);                                              // 7 dias de atraso → 5,00
        s.tratadorDeDevolucao.registrarDevolucao(1, "M-100", null);

        String json = Files.readString(arquivo);
        assertTrue(json.contains("\"valor\" : 5.00"));                        // BigDecimal sem perder casas
        assertTrue(json.contains("\"politicaVigente\" : \"TOLERANCIA\""));

        Sistema relido = Sistema.montar(new FabricaJson(arquivo), relogio);
        assertEquals("TOLERANCIA", relido.tratadorDeMulta.politicaVigente());
        var boleto = relido.tratadorDeMulta.listarBoletos().get(0);
        assertEquals(new java.math.BigDecimal("5.00"), boleto.getValorTotal());
        assertEquals(boleto.getIdBoleto(), relido.tratadorDeDevolucao.emitirBoletoDeMulta(1, "M-100").getIdBoleto());
    }

    @Test
    void organizacaoDoAcervoSobreviveAoReinicio() {
        Path arquivo = pasta.resolve("biblioteca.json");
        RelogioAjustavel relogio = RelogioAjustavel.fixoEm(Cenario.HOJE, Cenario.FUSO);
        Sistema s = Sistema.montar(new FabricaJson(arquivo), relogio);
        for (int id = 1; id <= 3; id++) s.tratadorDeAcervo.inserirLivro(id, "Livro " + id);
        s.tratadorDeAcervo.criarUnidade("Central");
        s.tratadorDeAcervo.criarGrupo("Central", "Literatura", br.pucpr.ib2b.biblioteca.TipoDeGrupo.SECAO);
        s.tratadorDeAcervo.criarGrupo("Literatura", "Poesia", br.pucpr.ib2b.biblioteca.TipoDeGrupo.SUBSECAO);
        s.tratadorDeAcervo.adicionarLivroAoGrupo("Literatura", 1);
        s.tratadorDeAcervo.adicionarLivroAoGrupo("Poesia", 2);
        s.tratadorDeAcervo.adicionarLivroAoGrupo("Central", 3);

        Sistema relido = Sistema.montar(new FabricaJson(arquivo), relogio);
        assertEquals(3, relido.tratadorDeAcervo.contarLivros("Central"));
        assertEquals(1, relido.tratadorDeAcervo.contarLivros("Poesia"));
        relido.tratadorDeAcervo.moverLivro(2, "Literatura");
        assertEquals(2, relido.tratadorDeAcervo.contarLivros("Literatura"));
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
