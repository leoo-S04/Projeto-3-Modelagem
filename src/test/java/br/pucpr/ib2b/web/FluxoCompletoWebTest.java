package br.pucpr.ib2b.web;

import br.pucpr.ib2b.biblioteca.TratadorDeEmprestimo;
import br.pucpr.ib2b.financeiro.CobrancaDTO;
import br.pucpr.ib2b.financeiro.TratadorDeMulta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Interface web, perfil TESTE (memória): o fluxo completo pelas áreas de cada ator —
 * administrador cadastra → estudante reserva → atendente empresta → estudante renova → devolução atrasada →
 * multa → estudante paga no banco simulado.
 */
@SpringBootTest(properties = "biblioteca.perfil=TESTE")
@AutoConfigureMockMvc
class FluxoCompletoWebTest {

    @Autowired MockMvc web;
    @Autowired TratadorDeMulta multas;
    @Autowired TratadorDeEmprestimo emprestimos;

    @Test
    void fluxoReservaEmprestimoRenovacaoDevolucaoAtrasadaMultaEPagamento() throws Exception {
        MockHttpSession admin = entrar("ADMINISTRADOR", "admin", "admin");
        enviar(admin, "/admin/pessoa", "cpf", "123.456.789-09", "nome", "Ana", "sobrenome", "Souza", "email", "ana@pucpr.br")
                .andExpect(flash().attribute("sucesso", containsString("cadastrado")));
        enviar(admin, "/admin/estudante", "cpf", "12345678909", "matricula", "M-100");
        enviar(admin, "/admin/estudante", "cpf", "00000000000", "matricula", "M-999")       // pessoa inexistente
                .andExpect(flash().attribute("erro", containsString("pessoa não cadastrada")));
        enviar(admin, "/admin/pessoa", "cpf", "98765432100", "nome", "Bruno", "sobrenome", "Alves", "email", "bruno@pucpr.br");
        enviar(admin, "/admin/atendente", "cpf", "98765432100", "matriculaFuncional", "F-01");
        enviar(admin, "/admin/livro", "idLivro", "1", "titulo", "Dom Casmurro");

        MockHttpSession ana = entrar("ESTUDANTE", "M-100", "123.456.789-09");
        MockHttpSession bruno = entrar("ATENDENTE", "F-01", "98765432100");

        enviar(ana, "/estudante/reservar", "idSolicitacao", UUID.randomUUID().toString(), "idLivro", "1")
                .andExpect(flash().attribute("sucesso", containsString("reservado")));
        enviar(bruno, "/atendente/emprestar", "idLivro", "1", "idEstudante", "M-100")
                .andExpect(flash().attribute("sucesso", containsString("Devolver até")));
        String idRenovacao = UUID.randomUUID().toString();
        enviar(ana, "/estudante/renovar", "idSolicitacao", idRenovacao, "idLivro", "1")
                .andExpect(flash().attribute("sucesso", containsString("renovado")));
        enviar(ana, "/estudante/renovar", "idSolicitacao", idRenovacao, "idLivro", "1");   // duplo clique: não renova de novo

        enviar(admin, "/admin/relogio/avancar", "dias", "65");                                // vencimento = hoje + 60
        enviar(bruno, "/atendente/devolver", "idLivro", "1", "idEstudante", "M-100")
                .andExpect(flash().attribute("sucesso", containsString("5 dia(s) de atraso")))
                .andExpect(flash().attribute("sucesso", containsString("boleto nº")));

        CobrancaDTO boleto = multas.relatorioDeCobrancas().get(0);
        assertEquals("5.00", boleto.valor().toPlainString());                                // ORIGINAL: 5 × 1,00
        web.perform(get("/estudante").session(ana)).andExpect(content().string(containsString("Em aberto")));
        enviar(ana, "/simulador/banco-a/pagar/" + boleto.idExterno())
                .andExpect(flash().attribute("sucesso", containsString("Pagamento confirmado")));
        web.perform(get("/estudante").session(ana)).andExpect(content().string(containsString("Paga")));

        // consulta do empréstimo (atendente) e ficha do estudante (administrador) mostram o histórico
        int idEmprestimo = emprestimos.consultarEmprestimos("M-100").todos().get(0).idEmprestimo();
        web.perform(get("/atendente/emprestimo/" + idEmprestimo).session(bruno))
                .andExpect(content().string(containsString("5 dia(s) de atraso")));
        web.perform(get("/admin/estudante/M-100").session(admin)).andExpect(content().string(containsString("Dom Casmurro")));

        // o atendente também reserva para o estudante no balcão
        enviar(bruno, "/atendente/reservar", "idSolicitacao", UUID.randomUUID().toString(), "idLivro", "1", "idEstudante", "M-100")
                .andExpect(flash().attribute("erro", containsString("DEVOLVIDO")));          // ainda não inspecionado (State)
        enviar(bruno, "/atendente/inspecionar", "idLivro", "1", "emBomEstado", "true");
        enviar(bruno, "/atendente/reservar", "idSolicitacao", UUID.randomUUID().toString(), "idLivro", "1", "idEstudante", "M-100")
                .andExpect(flash().attribute("sucesso", containsString("Reserva registrada")));

        // todas as páginas renderizam
        web.perform(get("/estudante?busca=dom").session(ana)).andExpect(content().string(containsString("Dom Casmurro")));
        web.perform(get("/atendente").session(bruno)).andExpect(status().isOk());
        for (String aba : new String[]{"estudantes", "emprestimos", "pessoas", "livros", "multas&dias=5", "sistema"}) {
            web.perform(get("/admin?aba=" + aba).session(admin)).andExpect(status().isOk());
        }
    }

    @Test
    void cadaPerfilSoAbreAsSuasPaginas() throws Exception {
        web.perform(get("/estudante")).andExpect(redirectedUrl("/login"));                // sem login
        web.perform(get("/login")).andExpect(status().isOk());
        MockHttpSession admin = entrar("ADMINISTRADOR", "admin", "admin");
        web.perform(get("/estudante").session(admin)).andExpect(redirectedUrl("/admin"));  // outro perfil
        web.perform(post("/login").param("perfil", "ADMINISTRADOR").param("usuario", "admin").param("senha", "errada"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("erro", containsString("incorretos")));
    }

    private MockHttpSession entrar(String perfil, String usuario, String senha) throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        web.perform(post("/login").session(sessao).param("perfil", perfil).param("usuario", usuario).param("senha", senha))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeCount(0));
        return sessao;
    }

    private ResultActions enviar(MockHttpSession sessao, String url, String... campos) throws Exception {
        var requisicao = post(url).session(sessao);
        for (int i = 0; i < campos.length; i += 2) requisicao = requisicao.param(campos[i], campos[i + 1]);
        return web.perform(requisicao).andExpect(status().is3xxRedirection());
    }
}
