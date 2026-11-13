package br.pucpr.ib2b.biblioteca;

import br.pucpr.ib2b.academico.Estudante;

import java.time.LocalDate;

/**
 * Papel GoF: Context do State. Guarda o estado corrente e os dados do ciclo atual
 * (reservante, vencimento, numRenovacoes) e DELEGA cada evento ao objeto de estado.
 * Só as classes de estado trocam o estado (mudarPara é de pacote).
 */
public class Livro {

    /** Prazo do primeiro empréstimo (os enunciados não definem; premissa da equipe). */
    static final int PRAZO_EMPRESTIMO_DIAS = 30;
    /** Cada renovação acrescenta 30 dias ao vencimento atual (regras 8.2 e 8.3). */
    static final int DIAS_POR_RENOVACAO = 30;

    private final int idLivro;
    private String titulo;
    private EstadoLivro estado = Livre.INSTANCIA;   // transição inicial: inserirLivro → LIVRE

    // Dados do ciclo atual (zerados por limparCiclo ao voltar a LIVRE)
    private Estudante reservante;
    private LocalDate vencimento;
    private int numRenovacoes = 0;

    public Livro(int idLivro, String titulo) {
        this.idLivro = idLivro;
        this.titulo = titulo;
    }

    // ---- Eventos da máquina de estados (nomes do Astah): só delegam ao estado ----
    public void reservarLivro(Estudante e)                  { estado.reservarLivro(this, e); }
    public void emprestarLivro(Estudante e, LocalDate data) { estado.emprestarLivro(this, e, data); }
    public void renovarEmprestimo()                         { estado.renovarEmprestimo(this); }
    public void registrarDevolucao()                        { estado.registrarDevolucao(this); }
    public void inspecionar(boolean emBomEstado)            { estado.inspecionar(this, emBomEstado); }
    public void enviarParaReparo()                          { estado.enviarParaReparo(this); }
    public void retornarDoReparo()                          { estado.retornarDoReparo(this); }
    public void incinerar()                                 { estado.incinerar(this); }
    public void excluirLivro()                              { estado.excluirLivro(this); }

    // ---- Usados apenas pelas classes de estado (visibilidade de pacote) ----
    void mudarPara(EstadoLivro novo)        { this.estado = novo; }   // entry / setEstado(X) da máquina
    void definirReservante(Estudante e)     { this.reservante = e; }
    void definirVencimento(LocalDate data)  { this.vencimento = data; }

    /** Ação da renovação: vencimento += dias; numRenovacoes++. */
    void prorrogar(int dias) {
        this.vencimento = vencimento.plusDays(dias);
        this.numRenovacoes++;
    }

    /** Prepara o exemplar para um novo ciclo: sem reservante, sem vencimento, sem renovações. */
    void limparCiclo() {
        this.reservante = null;
        this.vencimento = null;
        this.numRenovacoes = 0;
    }

    // ---- Consultas ----
    public int getIdLivro() { return idLivro; }
    public String getTitulo() { return titulo; }
    public Situacao getEstado() { return estado.situacao(); }
    public Estudante getReservante() { return reservante; }
    public LocalDate getVencimento() { return vencimento; }
    public int getNumRenovacoes() { return numRenovacoes; }

    /** Regra 1.2 (atualizar livro): muda o título, nunca o id. */
    public void setTitulo(String titulo) { this.titulo = titulo; }
}
