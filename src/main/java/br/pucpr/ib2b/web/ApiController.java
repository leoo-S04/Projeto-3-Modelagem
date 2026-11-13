package br.pucpr.ib2b.web;

import br.pucpr.ib2b.academico.TratadorDeEstudante;
import br.pucpr.ib2b.biblioteca.ComprovanteDevolucao;
import br.pucpr.ib2b.biblioteca.ComprovanteEmprestimo;
import br.pucpr.ib2b.biblioteca.ResultadoSolicitacao;
import br.pucpr.ib2b.biblioteca.SituacaoLivroDTO;
import br.pucpr.ib2b.biblioteca.TratadorDeAcervo;
import br.pucpr.ib2b.biblioteca.TratadorDeDevolucao;
import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.comum.ExcecaoDeDominio;
import br.pucpr.ib2b.comum.ResultadoCadastro;
import br.pucpr.ib2b.financeiro.CobrancaDTO;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import br.pucpr.ib2b.rh.TratadorDeAtendente;
import br.pucpr.ib2b.rh.TratadorDePessoa;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API JSON para cadastros e para o fluxo reserva → empréstimo → renovação → devolução → multa.
 * Só chama os tratadores; recusas de regra viram HTTP 422 com a mensagem do domínio.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final TratadorDePessoa pessoas;
    private final TratadorDeEstudante estudantes;
    private final TratadorDeAtendente atendentes;
    private final TratadorDeAcervo acervo;
    private final TratadorDeEmprestimo emprestimos;
    private final TratadorDeDevolucao devolucoes;
    private final TratadorDeMulta multas;

    public ApiController(TratadorDePessoa pessoas, TratadorDeEstudante estudantes, TratadorDeAtendente atendentes,
                         TratadorDeAcervo acervo, TratadorDeEmprestimo emprestimos, TratadorDeDevolucao devolucoes,
                         TratadorDeMulta multas) {
        this.pessoas = pessoas;
        this.estudantes = estudantes;
        this.atendentes = atendentes;
        this.acervo = acervo;
        this.emprestimos = emprestimos;
        this.devolucoes = devolucoes;
        this.multas = multas;
    }

    // ---- Cadastros ----
    @GetMapping("/livros")
    public List<SituacaoLivroDTO> livros() { return acervo.listarLivros(); }

    @PostMapping("/livros")
    public Map<String, Object> inserirLivro(@RequestBody Map<String, String> c) {
        return resumo(acervo.inserirLivro(Integer.parseInt(c.get("idLivro")), c.get("titulo")));
    }

    @PostMapping("/pessoas")
    public Map<String, Object> inserirPessoa(@RequestBody Map<String, String> c) {
        return resumo(pessoas.inserirPessoa(c.get("cpf"), c.get("nome"), c.get("sobrenome"), c.get("email")));
    }

    @PostMapping("/estudantes")
    public Map<String, Object> inserirEstudante(@RequestBody Map<String, String> c) {
        return resumo(estudantes.inserirEstudante(c.get("cpf"), c.get("matricula")));
    }

    @PostMapping("/atendentes")
    public Map<String, Object> inserirAtendente(@RequestBody Map<String, String> c) {
        return resumo(atendentes.inserirAtendente(c.get("cpf"), c.get("matriculaFuncional")));
    }

    // ---- Circulação ----
    @PostMapping("/reservas")
    public ResultadoSolicitacao reservar(@RequestBody Map<String, String> c) {
        return emprestimos.reservarLivro(idSolicitacao(c), Integer.parseInt(c.get("idLivro")), c.get("idEstudante"),
                c.get("idAtendente"));
    }

    @PostMapping("/emprestimos")
    public ComprovanteEmprestimo emprestar(@RequestBody Map<String, String> c) {
        return emprestimos.emprestarLivro(Integer.parseInt(c.get("idLivro")), c.get("idEstudante"), c.get("idAtendente"));
    }

    @PostMapping("/renovacoes")
    public ResultadoSolicitacao renovar(@RequestBody Map<String, String> c) {
        return emprestimos.renovarEmprestimo(idSolicitacao(c), Integer.parseInt(c.get("idLivro")), c.get("idEstudante"));
    }

    @PostMapping("/devolucoes")
    public ComprovanteDevolucao devolver(@RequestBody Map<String, String> c) {
        return devolucoes.registrarDevolucao(Integer.parseInt(c.get("idLivro")), c.get("idEstudante"), c.get("idAtendente"));
    }

    @GetMapping("/cobrancas")
    public List<CobrancaDTO> cobrancas() { return multas.relatorioDeCobrancas(); }

    @ExceptionHandler(ExcecaoDeDominio.class)
    public ResponseEntity<Map<String, String>> recusa(ExcecaoDeDominio e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("erro", e.getMessage()));
    }

    private static UUID idSolicitacao(Map<String, String> c) {
        String id = c.get("idSolicitacao");
        return id == null || id.isBlank() ? UUID.randomUUID() : UUID.fromString(id);
    }

    private static Map<String, Object> resumo(ResultadoCadastro<?> r) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", r.getStatus().name());
        if (r.getMotivo() != null) m.put("motivo", r.getMotivo());
        return m;
    }
}
