package br.pucpr.ib2b.web;

import br.pucpr.ib2b.financeiro.CobrancaDTO;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Interface web (Etapa 6), perfil TESTE (família em memória): o fluxo completo da demonstração —
 * cadastros → reserva → empréstimo → renovação → devolução atrasada → multa → pagamento no banco simulado.
 */
@SpringBootTest(properties = "biblioteca.perfil=TESTE")
@AutoConfigureMockMvc
class FluxoCompletoWebTest {

    @Autowired MockMvc web;
    @Autowired TratadorDeMulta multas;

    @Test
    void fluxoReservaEmprestimoRenovacaoDevolucaoAtrasadaMultaEPagamento() throws Exception {
        enviar("/cadastros/pessoa", "cpf", "12345678909", "nome", "Ana", "sobrenome", "Souza", "email", "ana@pucpr.br")
                .andExpect(flash().attribute("sucesso", containsString("cadastrado")));
        enviar("/cadastros/estudante", "cpf", "12345678909", "matricula", "M-100");
        enviar("/cadastros/estudante", "cpf", "00000000000", "matricula", "M-999")      // pessoa inexistente
                .andExpect(flash().attribute("erro", containsString("pessoa não cadastrada")));
        enviar("/cadastros/pessoa", "cpf", "98765432100", "nome", "Bruno", "sobrenome", "Alves", "email", "bruno@pucpr.br");
        enviar("/cadastros/atendente", "cpf", "98765432100", "matriculaFuncional", "F-01");
        enviar("/cadastros/livro", "idLivro", "1", "titulo", "Dom Casmurro");

        enviar("/circulacao/reservar", "idSolicitacao", UUID.randomUUID().toString(), "idLivro", "1",
                "idEstudante", "M-100", "idAtendente", "F-01")
                .andExpect(flash().attribute("sucesso", containsString("Reserva")));
        enviar("/circulacao/emprestar", "idLivro", "1", "idEstudante", "M-100", "idAtendente", "F-01")
                .andExpect(flash().attribute("sucesso", containsString("vencimento")));
        String idRenovacao = UUID.randomUUID().toString();
        enviar("/circulacao/renovar", "idSolicitacao", idRenovacao, "idLivro", "1", "idEstudante", "M-100")
                .andExpect(flash().attribute("sucesso", containsString("renovado")));
        enviar("/circulacao/renovar", "idSolicitacao", idRenovacao, "idLivro", "1", "idEstudante", "M-100"); // duplo clique

        enviar("/admin/relogio/avancar", "dias", "65");                                 // vencimento = hoje + 60
        enviar("/circulacao/devolver", "idLivro", "1", "idEstudante", "M-100", "idAtendente", "F-01")
                .andExpect(flash().attribute("sucesso", containsString("5 dia(s) de atraso")));

        CobrancaDTO boleto = multas.relatorioDeCobrancas().get(0);
        assertEquals("5.00", boleto.valor().toPlainString());                           // ORIGINAL: 5 × 1,00
        web.perform(get("/financeiro")).andExpect(status().isOk()).andExpect(content().string(containsString("EMITIDO")));

        enviar("/simulador/banco-a/pagar/" + boleto.idExterno());
        enviar("/simulador/banco-a/reenviar/" + boleto.idExterno());
        web.perform(get("/financeiro")).andExpect(content().string(containsString("QUITADO")));

        // todas as páginas renderizam
        for (String pagina : new String[]{"/", "/cadastros", "/circulacao", "/acervo?termo=dom", "/admin", "/financeiro?dias=5"}) {
            web.perform(get(pagina)).andExpect(status().isOk());
        }
        web.perform(get("/acervo?termo=dom")).andExpect(content().string(containsString("Dom Casmurro")));
    }

    @Test
    void apiJsonRecusaComMensagemDoDominio() throws Exception {
        web.perform(post("/api/emprestimos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLivro\":\"77\",\"idEstudante\":\"X-1\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro", containsString("Empréstimo recusado")));
    }

    private org.springframework.test.web.servlet.ResultActions enviar(String url, String... campos) throws Exception {
        var requisicao = post(url);
        for (int i = 0; i < campos.length; i += 2) requisicao = requisicao.param(campos[i], campos[i + 1]);
        return web.perform(requisicao).andExpect(status().is3xxRedirection());
    }
}
