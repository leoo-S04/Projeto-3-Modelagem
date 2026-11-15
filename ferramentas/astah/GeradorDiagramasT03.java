import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.*;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.presentation.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

import java.awt.geom.Point2D;
import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Gera, no arquivo .asta da equipe, os diagramas exigidos pelo Trabalho 03:
 *  - um diagrama de classes por padrão (Req. 1 a 10), com as classes lidas do código compilado (reflexão);
 *  - um diagrama de sequência por cenário de verificação (testes R01 a R10);
 *  - a máquina de estados do Livro atualizada (efeitos das transições conforme o código).
 *
 * Uso (ver gerar.cmd):
 *   java -cp <api do Astah> GeradorDiagramasT03 <entrada.asta> <saida.asta> <target/classes> <src/main/java> <cp.txt>
 *
 * As classes do Trabalho 03 ficam no pacote "Trabalho 03 - Implementacao" (espelho dos pacotes Java);
 * os diagramas ficam em um pacote por requisito ("Req 01 - State" ... "Req 10 - Composite").
 * Os diagramas do Trabalho 02 não são apagados.
 */
public class GeradorDiagramasT03 {

    static final String RAIZ_JAVA = "br.pucpr.ib2b";
    static ProjectAccessor pa;
    static BasicModelEditor bme;
    static ClassDiagramEditor cde;
    static SequenceDiagramEditor sde;
    static StateMachineDiagramEditor sme;
    static ClassLoader cl;
    static Path fontes;

    static final Map<String, String> indice = new HashMap<>();          // nome simples -> nome binário Java
    static final Map<String, IClass> modelo = new LinkedHashMap<>();    // nome simples -> classe no Astah
    static final Map<String, Class<?>> java = new LinkedHashMap<>();
    static final Map<String, IPackage> pacotes = new HashMap<>();
    static IPackage pacoteT03;

    record Rel(IElement elem, String origem, String destino) { }      // origem = cliente/sub/A; destino = fornecedor/super/B
    static final List<Rel> associacoes = new ArrayList<>();
    static final List<Rel> generalizacoes = new ArrayList<>();
    static final List<Rel> realizacoes = new ArrayList<>();
    static final List<Rel> dependencias = new ArrayList<>();

    /** Campos que podem ser nulos: multiplicidade 0..1. */
    static final Set<String> OPCIONAIS = Set.of("Livro.reservante", "ComponenteDoAcervo.pai");
    /** Agregação por composição (o todo é a classe dona do campo). */
    static final Set<String> COMPOSICOES = Set.of("GrupoDoAcervo.filhos", "PublicadorDeDevolucao.ouvintes");

    public static void main(String[] a) throws Exception {
        Path entrada = Path.of(a[0]), saida = Path.of(a[1]), classes = Path.of(a[2]);
        fontes = Path.of(a[3]);
        List<URL> urls = new ArrayList<>();
        urls.add(classes.toUri().toURL());
        for (String s : Files.readString(Path.of(a[4])).trim().split(File.pathSeparator)) {
            if (!s.isBlank()) urls.add(Path.of(s).toUri().toURL());
        }
        cl = new URLClassLoader(urls.toArray(URL[]::new), GeradorDiagramasT03.class.getClassLoader());
        indexar(classes);

        pa = AstahAPI.getAstahAPI().getProjectAccessor();
        pa.open(entrada.toString(), true, false, true);
        bme = ModelEditorFactory.getBasicModelEditor();
        cde = pa.getDiagramEditorFactory().getClassDiagramEditor();
        sde = pa.getDiagramEditorFactory().getSequenceDiagramEditor();
        sme = pa.getDiagramEditorFactory().getStateMachineDiagramEditor();

        TransactionManager.beginTransaction();
        try {
            IModel projeto = pa.getProject();
            apagarGeracaoAnterior(projeto);
            pacoteT03 = bme.createPackage(projeto, "Trabalho 03 - Implementacao");
            pacoteT03.setDefinition("Espelho dos pacotes Java de br.pucpr.ib2b (gerado do código compilado).");

            for (String n : classesNecessarias()) criarClasse(n);
            for (String n : new ArrayList<>(modelo.keySet())) criarMembros(n);
            for (String n : new ArrayList<>(modelo.keySet())) criarHeranca(n);
            criarDependencias();

            diagramasDeClasses(projeto);
            diagramasDeSequencia();
            atualizarMaquinaDeEstados(projeto);
            avisoNoDiagramaDoT02(projeto);
            TransactionManager.endTransaction();
        } catch (Throwable t) {
            TransactionManager.abortTransaction();
            throw t;
        }
        pa.saveAs(saida.toString());
        pa.close();
        System.out.println("Classes no modelo: " + modelo.size());
        System.out.println("OK -> " + saida);
    }

    /** Permite rodar de novo sobre o .asta já atualizado: remove o que esta ferramenta criou antes. */
    static void apagarGeracaoAnterior(IModel projeto) throws Exception {
        for (INamedElement e : projeto.getOwnedElements()) {
            if (e instanceof IPackage p && (p.getName().equals("Trabalho 03 - Implementacao") || p.getName().matches("Req \\d\\d - .*"))) {
                bme.delete(p);
            }
        }
    }

    // =====================================================================================
    // Diagramas de classes (um por padrão)
    // =====================================================================================

    record DC(String pacote, String nome, String[][] linhas, String nota) { }

    static final List<DC> DIAGRAMAS = List.of(
        new DC("Req 01 - State", "DC Req 01 - State (ciclo de vida do Livro)", new String[][]{
            {"ServicoDeCirculacao", "Livro", "Situacao"},
            {"EstadoLivro"},
            {"Livre", "Reservado", "Emprestado", "Renovado"},
            {"Devolvido", "Reparando", "Incinerado"},
            {"ExcecaoDeDominio", "OperacaoInvalidaException", "RenovacaoNegadaException"}},
            "State (Req. 1)\nContext: Livro (delega cada evento ao objeto em -estado)\nState: EstadoLivro (implementação padrão de cada evento = recusa())\n"
            + "ConcreteState: Livre, Reservado, Emprestado, Renovado, Devolvido, Reparando, Incinerado\n"
            + "Cada estado sobrescreve só os eventos das transições que saem dele.\n"
            + "Recusa: OperacaoInvalidaException (3ª renovação: RenovacaoNegadaException), lançada antes de alterar o Livro.\n"
            + "Cliente: ServicoDeCirculacao (fachada) - não testa a situação com if."),
        new DC("Req 02 - Strategy", "DC Req 02 - Strategy (politicas de multa)", new String[][]{
            {"TratadorDeMulta", "ServicoDeMulta", "ServicoDeSimulacao"},
            {"SeletorDePolitica", "ConfiguracaoAdministrativa", "Multa"},
            {"PoliticaDeMulta"},
            {"MultaDiaria", "MultaComTeto", "MultaComTolerancia", "PoliticaInexistenteException"}},
            "Strategy (Req. 2)\nStrategy: PoliticaDeMulta.calcular(dias)\nConcreteStrategy: MultaDiaria (ORIGINAL), MultaComTeto (TETO), MultaComTolerancia (TOLERANCIA)\n"
            + "Context: ServicoDeMulta (pede a política vigente ao SeletorDePolitica)\n"
            + "A Multa grava valor e politicaAplicada: trocar a política não recalcula multas já emitidas.\n"
            + "Nova política = nova classe registrada no Sistema; ServicoDeMulta não muda."),
        new DC("Req 03 - Adapter", "DC Req 03 - Adapter (bancos A e B)", new String[][]{
            {"ServicoDeMulta", "iCobranca", "iQuitacao", "ServicoDeQuitacao"},
            {"AdaptadorBancoA", "AdaptadorBancoB", "ConfirmacaoPagamento", "ValorInvalidoException"},
            {"ApiBancoA", "TituloA", "StatusA", "ApiBancoB", "CobrancaB"}},
            "Adapter (Req. 3)\nTarget: iCobranca (contrato institucional; valor em BigDecimal)\nAdapter: AdaptadorBancoA, AdaptadorBancoB\n"
            + "Adaptee: ApiBancoA (centavos, long) e ApiBancoB (decimal, CPF formatado)\nClient: ServicoDeMulta (sem condicionais por banco)\n"
            + "Notificações de pagamento (webhook JSON / XML) também são traduzidas pelo adaptador\n"
            + "e entregues a iQuitacao como ConfirmacaoPagamento; repetição não quita duas vezes."),
        new DC("Req 04 - Facade", "DC Req 04 - Facade (circulacao)", new String[][]{
            {"TratadorDeEmprestimo", "TratadorDeDevolucao"},
            {"iCirculacao"},
            {"ServicoDeCirculacao", "ReservaInvalidaException"},
            {"Acervo", "Movimentacao", "Relacionamento", "UnidadeDeTrabalho", "Livro"},
            {"ComprovanteReserva", "ComprovanteEmprestimo", "ComprovanteRenovacao", "ComprovanteDevolucao", "SituacaoLivroDTO"},
            {"HistoricoDeEmprestimos", "EmprestimoDTO"}},
            "Facade (Req. 4)\nFacade: iCirculacao / ServicoDeCirculacao\nSubsistema: Acervo, Movimentacao, Relacionamento, Livro (State), UnidadeDeTrabalho\n"
            + "Clientes: TratadorDeEmprestimo e TratadorDeDevolucao\n"
            + "efetivarEmprestimo: valida a reserva, remove a reserva ativa, cria o empréstimo e atualiza o Livro\n"
            + "numa única transação; devolve comprovantes (records), nunca coleções internas."),
        new DC("Req 05 - Observer", "DC Req 05 - Observer (reacoes a devolucao)", new String[][]{
            {"TratadorDeDevolucao", "PublicadorDeDevolucao", "EventoDevolucao"},
            {"OuvinteDeDevolucao"},
            {"OuvinteInspecao", "OuvinteMulta", "OuvinteIndicadores", "OuvinteEmail"},
            {"AgendaDeInspecao", "iMulta", "PainelDeIndicadores", "ServicoDeEmail", "ServicoDeEmailSimulado"}},
            "Observer (Req. 5)\nSubject: PublicadorDeDevolucao (registrar, remover, publicar)\nObserver: OuvinteDeDevolucao.aoDevolver(ev)\n"
            + "ConcreteObserver: OuvinteInspecao, OuvinteMulta, OuvinteIndicadores, OuvinteEmail\n"
            + "TratadorDeDevolucao publica o EventoDevolucao depois da confirmação da transação.\n"
            + "Falha de um ouvinte vai para a fila de reenvio e não afeta os demais."),
        new DC("Req 06 - Command", "DC Req 06 - Command (solicitacoes de circulacao)", new String[][]{
            {"TratadorDeEmprestimo", "ExecutorDeSolicitacoes", "RegistroDeSolicitacoes", "RepositorioDeSolicitacoes"},
            {"SolicitacaoDeCirculacao", "ResultadoSolicitacao", "StatusSolicitacao"},
            {"SolicitacaoDeReserva", "SolicitacaoDeRenovacao", "iCirculacao"}},
            "Command (Req. 6)\nCommand: SolicitacaoDeCirculacao (id, autor, criadaEm; executar())\nConcreteCommand: SolicitacaoDeReserva, SolicitacaoDeRenovacao\n"
            + "Invoker: ExecutorDeSolicitacoes.submeter(s)\nReceiver: iCirculacao (fachada)\nClient: TratadorDeEmprestimo (cria a solicitação)\n"
            + "RegistroDeSolicitacoes: o mesmo id executa uma única vez (resultado gravado e reaproveitado)."),
        new DC("Req 07 - Template Method", "DC Req 07 - Template Method (cadastros)", new String[][]{
            {"TratadorDeCadastro", "ResultadoCadastro", "StatusCadastro", "CadastroRecusadoException"},
            {"TratadorDeAcervo", "TratadorDePessoa", "TratadorDeEstudante", "TratadorDeAtendente"},
            {"DadosLivro", "DadosPessoa", "DadosEstudante", "DadosAtendente", "UnidadeDeTrabalho", "iPessoa"}},
            "Template Method (Req. 7)\nAbstractClass: TratadorDeCadastro<T, D>; método-modelo inserir(dados) {leaf} (final):\n"
            + "buscarExistente -> validar -> verificarPreRequisitos (gancho) -> criar -> armazenar (na transação) -> apresentar\n"
            + "ConcreteClass: TratadorDeAcervo, TratadorDePessoa, TratadorDeEstudante, TratadorDeAtendente\n"
            + "Recusa (CadastroRecusadoException) acontece antes de qualquer gravação."),
        new DC("Req 08 - Abstract Factory", "DC Req 08 - Abstract Factory (familias de persistencia)", new String[][]{
            {"ConfiguradorDePersistencia", "Perfil", "FabricaDePersistencia", "FamiliaIncompativelException"},
            {"FabricaEmMemoria", "FabricaJson"},
            {"UnidadeDeTrabalho", "RepositorioLivro", "RepositorioMovimentacao", "RepositorioRelacionamento"},
            {"UnidadeDeTrabalhoEmMemoria", "RepositorioLivroEmMemoria", "RepositorioMovimentacaoEmMemoria", "RepositorioRelacionamentoEmMemoria"},
            {"UnidadeDeTrabalhoJson", "RepositorioLivroJson", "RepositorioMovimentacaoJson", "RepositorioRelacionamentoJson"}},
            "Abstract Factory (Req. 8)\nAbstractFactory: FabricaDePersistencia\nConcreteFactory: FabricaEmMemoria (perfil TESTE), FabricaJson (perfil PRODUCAO, biblioteca.json)\n"
            + "AbstractProduct: UnidadeDeTrabalho e repositórios; ConcreteProduct: ...EmMemoria / ...Json\n"
            + "Os repositórios recebem a mesma UnidadeDeTrabalho (confirmar/desfazer juntos); misturar famílias -> FamiliaIncompativelException.\n"
            + "Outros produtos da mesma fábrica (mesma estrutura): RepositorioDeSolicitacoes, RepositorioFinanceiro, RepositorioDeOrganizacao.\n"
            + "Nos repositórios, a realização passa por uma classe ...Base (código comum), omitida aqui."),
        new DC("Req 09 - Decorator", "DC Req 09 - Decorator (consulta ao acervo)", new String[][]{
            {"MontadorDeConsulta", "MontadorDeConsulta.ConfigInstalacao", "ConsultaAcervo"},
            {"ConsultaAcervoBasica", "DecoradorDeConsulta"},
            {"DecoradorDeMedicao", "DecoradorDeCache"},
            {"Acervo", "RegistroDeMetricas", "ArmazenamentoDeCache", "MetadadosLivro", "Criterio"}},
            "Decorator (Req. 9)\nComponent: ConsultaAcervo.buscarMetadados(criterio)\nConcreteComponent: ConsultaAcervoBasica\n"
            + "Decorator: DecoradorDeConsulta (#interno)\nConcreteDecorator: DecoradorDeMedicao, DecoradorDeCache\n"
            + "MontadorDeConsulta monta a cadeia conforme a configuração: Medição -> Cache -> Básica.\n"
            + "MetadadosLivro tem só idLivro e titulo: o cache nunca informa a situação do livro.\nCliente: TratadorDeAcervo.buscarMetadados(termo)."),
        new DC("Req 10 - Composite", "DC Req 10 - Composite (organizacao do acervo)", new String[][]{
            {"OrganizadorDoAcervo", "RepositorioDeOrganizacao", "Acervo"},
            {"ComponenteDoAcervo"},
            {"ItemDoAcervo", "GrupoDoAcervo", "TipoDeGrupo"},
            {"NoDoAcervoDTO", "HierarquiaInvalidaException"}},
            "Composite (Req. 10)\nComponent: ComponenteDoAcervo (contarLivros, listarIds)\nLeaf: ItemDoAcervo (referencia o Livro pelo idLivro)\n"
            + "Composite: GrupoDoAcervo (UNIDADE, SECAO, SUBSECAO) - soma recursiva dos filhos\n"
            + "Client: OrganizadorDoAcervo (chamado pelo TratadorDeAcervo)\n"
            + "Mover um livro move só o ItemDoAcervo: id, estado e empréstimo do Livro não mudam.")
    );

    /** Dependências que não aparecem como campos («create» das fábricas, «use» de parâmetros). */
    static void criarDependencias() throws Exception {
        String[][] deps = {
            {"ConfiguradorDePersistencia", "FabricaEmMemoria", "create"},
            {"ConfiguradorDePersistencia", "FabricaJson", "create"},
            {"FabricaEmMemoria", "UnidadeDeTrabalhoEmMemoria", "create"},
            {"FabricaEmMemoria", "RepositorioLivroEmMemoria", "create"},
            {"FabricaEmMemoria", "RepositorioMovimentacaoEmMemoria", "create"},
            {"FabricaEmMemoria", "RepositorioRelacionamentoEmMemoria", "create"},
            {"FabricaJson", "UnidadeDeTrabalhoJson", "create"},
            {"FabricaJson", "RepositorioLivroJson", "create"},
            {"FabricaJson", "RepositorioMovimentacaoJson", "create"},
            {"FabricaJson", "RepositorioRelacionamentoJson", "create"},
            {"EstadoLivro", "OperacaoInvalidaException", "create"},
            {"Renovado", "RenovacaoNegadaException", "create"},
            {"Emprestado", "RenovacaoNegadaException", "create"},
            {"ServicoDeCirculacao", "ComprovanteEmprestimo", "create"},
            {"ServicoDeCirculacao", "ReservaInvalidaException", "create"},
            {"ServicoDeCirculacao", "Livro", "use"},
            {"TratadorDeDevolucao", "EventoDevolucao", "create"},
            {"OuvinteDeDevolucao", "EventoDevolucao", "use"},
            {"TratadorDeEmprestimo", "SolicitacaoDeReserva", "create"},
            {"TratadorDeEmprestimo", "SolicitacaoDeRenovacao", "create"},
            {"SolicitacaoDeCirculacao", "ResultadoSolicitacao", "create"},
            {"ServicoDeMulta", "Multa", "create"},
            {"ServicoDeMulta", "PoliticaDeMulta", "use"},
            {"AdaptadorBancoA", "ConfirmacaoPagamento", "create"},
            {"AdaptadorBancoB", "ConfirmacaoPagamento", "create"},
            {"AdaptadorBancoA", "ValorInvalidoException", "create"},
            {"TratadorDeCadastro", "ResultadoCadastro", "create"},
            {"TratadorDeCadastro", "CadastroRecusadoException", "use"},
            {"TratadorDeAcervo", "DadosLivro", "use"},
            {"TratadorDePessoa", "DadosPessoa", "use"},
            {"TratadorDeEstudante", "DadosEstudante", "use"},
            {"TratadorDeAtendente", "DadosAtendente", "use"},
            {"MontadorDeConsulta", "DecoradorDeCache", "create"},
            {"MontadorDeConsulta", "DecoradorDeMedicao", "create"},
            {"ConsultaAcervoBasica", "MetadadosLivro", "create"},
            {"ConsultaAcervo", "Criterio", "use"},
            {"OrganizadorDoAcervo", "GrupoDoAcervo", "create"},
            {"OrganizadorDoAcervo", "ItemDoAcervo", "create"},
            {"OrganizadorDoAcervo", "NoDoAcervoDTO", "create"},
            {"GrupoDoAcervo", "HierarquiaInvalidaException", "create"},
            {"SeletorDePolitica", "PoliticaInexistenteException", "create"},
            {"ItemDoAcervo", "Livro", "use"},
        };
        for (String[] d : deps) {
            IClass c = modelo.get(d[0]), f = modelo.get(d[1]);
            if (c == null || f == null) continue;
            IDependency dep = bme.createDependency(f, c, "");
            dep.addStereotype(d[2]);
            dependencias.add(new Rel(dep, d[0], d[1]));
        }
    }

    static Set<String> classesNecessarias() {
        Set<String> s = new LinkedHashSet<>();
        for (DC d : DIAGRAMAS) for (String[] l : d.linhas()) s.addAll(List.of(l));
        for (Seq q : sequencias()) for (Lin l : q.linhas) if (l.base != null) s.add(l.base);
        return s;
    }

    static void diagramasDeClasses(IModel projeto) throws Exception {
        for (DC d : DIAGRAMAS) {
            IPackage dono = pacoteDoRequisito(projeto, d.pacote());
            IClassDiagram dg = cde.createClassDiagram(dono, d.nome());
            Map<String, INodePresentation> nos = new HashMap<>();
            List<List<INodePresentation>> linhas = new ArrayList<>();
            double larguraMax = 0;
            for (String[] linha : d.linhas()) {
                List<INodePresentation> l = new ArrayList<>();
                double w = 0;
                for (String n : linha) {
                    INodePresentation p = cde.createNodePresentation(modelo.get(n), new Point2D.Double(0, 0));
                    try { p.setProperty("notation_type", "normal"); } catch (Exception ignorada) { }   // interface como retângulo
                    nos.put(n, p);
                    l.add(p);
                    w += p.getWidth() + 60;
                }
                linhas.add(l);
                larguraMax = Math.max(larguraMax, w - 60);
            }
            double y = 40;
            for (List<INodePresentation> l : linhas) {
                double w = -60, h = 0;
                for (INodePresentation p : l) { w += p.getWidth() + 60; h = Math.max(h, p.getHeight()); }
                double x = 40 + (larguraMax - w) / 2;
                for (INodePresentation p : l) {
                    p.setLocation(new Point2D.Double(x, y));
                    x += p.getWidth() + 60;
                }
                y += h + 90;
            }
            ligar(generalizacoes, nos, true);
            ligar(realizacoes, nos, true);
            ligar(associacoes, nos, false);
            ligar(dependencias, nos, true);
            if (d.nota() != null) cde.createNote(d.nota(), new Point2D.Double(40, y));
        }
    }

    static void ligar(List<Rel> rels, Map<String, INodePresentation> nos, boolean fornecedorPrimeiro) {
        for (Rel r : rels) {
            INodePresentation o = nos.get(r.origem()), d = nos.get(r.destino());
            if (o == null || d == null) continue;
            try {
                if (fornecedorPrimeiro) cde.createLinkPresentation(r.elem(), d, o);
                else cde.createLinkPresentation(r.elem(), o, d);
            } catch (Exception e1) {
                try {
                    if (fornecedorPrimeiro) cde.createLinkPresentation(r.elem(), o, d);
                    else cde.createLinkPresentation(r.elem(), d, o);
                } catch (Exception e2) {
                    System.out.println("  aviso: ligação " + r.origem() + " -> " + r.destino() + ": " + e2.getMessage());
                }
            }
        }
    }

    static final Map<String, IPackage> pacotesReq = new HashMap<>();

    static IPackage pacoteDoRequisito(IModel projeto, String nome) throws Exception {
        IPackage p = pacotesReq.get(nome);
        if (p == null) {
            p = bme.createPackage(projeto, nome);
            pacotesReq.put(nome, p);
        }
        return p;
    }

    // =====================================================================================
    // Classes a partir do código compilado
    // =====================================================================================

    static void indexar(Path classes) throws Exception {
        try (var s = Files.walk(classes)) {
            for (Path p : s.filter(f -> f.toString().endsWith(".class")).toList()) {
                String rel = classes.relativize(p).toString().replace(File.separatorChar, '/');
                String bin = rel.substring(0, rel.length() - 6).replace('/', '.');
                String simples = bin.substring(bin.lastIndexOf('.') + 1);
                if (simples.matches(".*\\$\\d.*") || simples.equals("package-info")) continue;
                String chave = simples.replace('$', '.');
                if (indice.containsKey(chave)) System.out.println("  aviso: nome repetido " + chave);
                indice.put(chave, bin);
            }
        }
    }

    static String chave(Class<?> k) {
        return k.getEnclosingClass() != null ? chave(k.getEnclosingClass()) + "." + k.getSimpleName() : k.getSimpleName();
    }

    static boolean modelada(Class<?> k) { return k != null && modelo.containsKey(chave(k)); }

    static IPackage pacote(String pacoteJava) throws Exception {
        String rel = pacoteJava.equals(RAIZ_JAVA) ? "" : pacoteJava.substring(RAIZ_JAVA.length() + 1);
        if (rel.isEmpty()) return pacoteT03;
        IPackage p = pacotes.get(rel);
        if (p != null) return p;
        int i = rel.lastIndexOf('.');
        IPackage pai = i < 0 ? pacoteT03 : pacote(RAIZ_JAVA + "." + rel.substring(0, i));
        p = bme.createPackage(pai, rel.substring(i + 1));
        pacotes.put(rel, p);
        return p;
    }

    static IClass criarClasse(String nome) throws Exception {
        IClass c = modelo.get(nome);
        if (c != null) return c;
        String bin = indice.get(nome);
        if (bin == null) throw new IllegalArgumentException("Classe não encontrada no código: " + nome);
        Class<?> k = Class.forName(bin, false, cl);
        if (k.getEnclosingClass() != null) {
            IClass externa = criarClasse(chave(k.getEnclosingClass()));
            c = k.isInterface() ? bme.createInterface(externa, k.getSimpleName()) : bme.createClass(externa, k.getSimpleName());
        } else {
            IPackage pk = pacote(k.getPackageName());
            if (k.isInterface()) c = bme.createInterface(pk, k.getSimpleName());
            else if (k.isEnum()) c = bme.createEnumeration(pk, k.getSimpleName());
            else c = bme.createClass(pk, k.getSimpleName());
        }
        if (k.isEnum() && k.getEnclosingClass() == null) {
            for (Object lit : k.getEnumConstants()) bme.createEnumerationLiteral((IEnumeration) c, ((Enum<?>) lit).name());
        }
        if (!k.isInterface() && Modifier.isAbstract(k.getModifiers()) && !k.isEnum()) c.setAbstract(true);
        if (k.isRecord()) c.addStereotype("record");
        for (TypeVariable<?> tv : k.getTypeParameters()) {
            try { bme.createTemplateParameter(c, tv.getName(), (String) null, null); } catch (Exception ignorada) { }
        }
        modelo.put(nome, c);
        java.put(nome, k);
        return c;
    }

    /** Posição do membro no arquivo-fonte, para manter a ordem do código. */
    static String fonte(Class<?> k) {
        Class<?> topo = k;
        while (topo.getEnclosingClass() != null) topo = topo.getEnclosingClass();
        Path p = fontes.resolve(topo.getName().replace('.', '/') + ".java");
        try { return Files.readString(p, StandardCharsets.UTF_8); } catch (Exception e) { return ""; }
    }

    static int posicao(String src, String nome) {
        int i = src.indexOf(" " + nome + "(");
        if (i < 0) i = src.indexOf(" " + nome + " ");
        if (i < 0) i = src.indexOf(" " + nome + ";");
        if (i < 0) i = src.indexOf(" " + nome + ",");
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    static void criarMembros(String nome) throws Exception {
        IClass c = modelo.get(nome);
        Class<?> k = java.get(nome);
        String src = fonte(k);
        Set<String> componentes = new HashSet<>();
        if (k.isRecord()) for (RecordComponent rc : k.getRecordComponents()) componentes.add(rc.getName());

        List<Field> campos = new ArrayList<>(List.of(k.getDeclaredFields()));
        campos.sort(Comparator.comparingInt(f -> posicao(src, f.getName())));
        for (Field f : campos) {
            if (f.isSynthetic() || f.isEnumConstant() || f.getName().startsWith("$") || f.getName().equals("serialVersionUID")) continue;
            boolean estatico = Modifier.isStatic(f.getModifiers());
            String alvo = estatico ? null : alvoModelado(f.getGenericType());
            if (alvo != null) {
                IClass destino = modelo.get(alvo);
                IAssociation as = bme.createAssociation(c, destino, "", "", f.getName());
                IAttribute fim = null, outro = null;
                for (IAttribute e : as.getMemberEnds()) {
                    if (f.getName().equals(e.getName())) fim = e; else outro = e;
                }
                if (fim != null) {
                    fim.setVisibility(visibilidade(f.getModifiers()));
                    String mult = multiplo(f.getType()) ? "0..*" : OPCIONAIS.contains(nome + "." + f.getName()) ? "0..1" : "1";
                    try { fim.setMultiplicityString(mult); } catch (Exception ignorada) { }
                    fim.setNavigability("Navigable");
                }
                if (outro != null) {
                    outro.setNavigability("Non_Navigable");
                    if (COMPOSICOES.contains(nome + "." + f.getName())) outro.setComposite();
                }
                associacoes.add(new Rel(as, nome, alvo));
                continue;
            }
            IAttribute at = bme.createAttribute(c, f.getName(), tipo(f.getGenericType()));
            at.setVisibility(visibilidade(f.getModifiers()));
            if (estatico) at.setStatic(true);
            if (estatico && Modifier.isFinal(f.getModifiers())) {
                at.setChangeable(false);
                try {
                    f.setAccessible(true);
                    Object v = f.get(null);
                    if (v instanceof Number || v instanceof String || v instanceof Boolean) {
                        at.setInitialValue(v instanceof String ? "\"" + v + "\"" : String.valueOf(v));
                    }
                } catch (Throwable ignorada) { }
            } else if (k.isRecord() && componentes.contains(f.getName())) {
                at.setChangeable(false);
            }
        }

        List<Method> metodos = new ArrayList<>(List.of(k.getDeclaredMethods()));
        metodos.sort(Comparator.comparingInt(m -> posicao(src, m.getName())));
        for (Method m : metodos) {
            String n = m.getName();
            if (m.isSynthetic() || m.isBridge() || n.startsWith("lambda$") || n.startsWith("$")) continue;
            if (k.isEnum() && (n.equals("values") || n.equals("valueOf"))) continue;
            if (k.isRecord() && (componentes.contains(n) && m.getParameterCount() == 0
                    || n.equals("equals") || n.equals("hashCode") || n.equals("toString"))) continue;
            IOperation op = bme.createOperation(c, n, tipo(m.getGenericReturnType()));
            for (Parameter p : m.getParameters()) bme.createParameter(op, p.getName(), tipo(p.getParameterizedType()));
            op.setVisibility(k.isInterface() ? "public" : visibilidade(m.getModifiers()));
            if (Modifier.isStatic(m.getModifiers())) op.setStatic(true);
            if (Modifier.isAbstract(m.getModifiers())) op.setAbstract(true);
            if (Modifier.isFinal(m.getModifiers()) && !Modifier.isFinal(k.getModifiers())) op.setLeaf(true);
        }
    }

    static void criarHeranca(String nome) throws Exception {
        Class<?> k = java.get(nome);
        IClass c = modelo.get(nome);
        if (k.isInterface()) {
            for (Class<?> i : k.getInterfaces()) generalizarInterface(nome, c, i);
            return;
        }
        List<Class<?>> interfaces = new ArrayList<>(List.of(k.getInterfaces()));
        Class<?> s = k.getSuperclass();
        while (s != null && s != Object.class && !modelada(s)) {
            interfaces.addAll(List.of(s.getInterfaces()));
            s = s.getSuperclass();
        }
        if (modelada(s)) {
            IGeneralization g = bme.createGeneralization(c, modelo.get(chave(s)), "");
            generalizacoes.add(new Rel(g, nome, chave(s)));
        }
        Set<String> feitas = new HashSet<>();
        for (Class<?> i : interfaces) realizar(nome, c, i, feitas);
    }

    static void realizar(String nome, IClass c, Class<?> i, Set<String> feitas) throws Exception {
        if (modelada(i)) {
            if (feitas.add(chave(i))) {
                IRealization r = bme.createRealization(c, modelo.get(chave(i)), "");
                realizacoes.add(new Rel(r, nome, chave(i)));
            }
        } else {
            for (Class<?> sup : i.getInterfaces()) realizar(nome, c, sup, feitas);
        }
    }

    static void generalizarInterface(String nome, IClass c, Class<?> i) throws Exception {
        if (modelada(i)) {
            IGeneralization g = bme.createGeneralization(c, modelo.get(chave(i)), "");
            generalizacoes.add(new Rel(g, nome, chave(i)));
        } else {
            for (Class<?> sup : i.getInterfaces()) generalizarInterface(nome, c, sup);
        }
    }

    static String alvoModelado(Type t) {
        if (t instanceof Class<?> k) {
            if (k.isArray()) return alvoModelado(k.getComponentType());
            return modelada(k) ? chave(k) : null;
        }
        if (t instanceof ParameterizedType p) {
            Class<?> raw = (Class<?>) p.getRawType();
            if (Collection.class.isAssignableFrom(raw) || Map.class.isAssignableFrom(raw) || raw == Optional.class) {
                Type[] args = p.getActualTypeArguments();
                return alvoModelado(args[args.length - 1]);
            }
            return alvoModelado(raw);
        }
        return null;
    }

    static boolean multiplo(Class<?> k) {
        return k.isArray() || Collection.class.isAssignableFrom(k) || Map.class.isAssignableFrom(k);
    }

    static String visibilidade(int mod) {
        if (Modifier.isPublic(mod)) return "public";
        if (Modifier.isProtected(mod)) return "protected";
        if (Modifier.isPrivate(mod)) return "private";
        return "package";
    }

    static String tipo(Type t) {
        if (t instanceof Class<?> k) {
            if (k.isArray()) return tipo(k.getComponentType()) + "[]";
            return k.getSimpleName();
        }
        if (t instanceof ParameterizedType p) {
            StringBuilder sb = new StringBuilder(tipo(p.getRawType())).append('<');
            Type[] args = p.getActualTypeArguments();
            for (int i = 0; i < args.length; i++) sb.append(i > 0 ? ", " : "").append(tipo(args[i]));
            return sb.append('>').toString();
        }
        if (t instanceof TypeVariable<?> v) return v.getName();
        if (t instanceof GenericArrayType g) return tipo(g.getGenericComponentType()) + "[]";
        if (t instanceof WildcardType) return "?";
        return t.getTypeName();
    }

    // =====================================================================================
    // Diagramas de sequência (um por cenário de verificação)
    // =====================================================================================

    static final double PASSO = 32;

    static class Lin { String chave, nome, base; Lin(String c, String n, String b) { chave = c; nome = n; base = b; } }
    static abstract class Passo { }
    static class Msg extends Passo {
        String de, para, op, args, ret; boolean criar; List<Passo> filhos;
        Msg(String de, String para, String op, String args, String ret, boolean criar, Passo... f) {
            this.de = de; this.para = para; this.op = op; this.args = args; this.ret = ret; this.criar = criar; filhos = List.of(f);
        }
    }
    static class Frag extends Passo {
        String tipo; String[] guardas; List<List<Passo>> operandos;
        Frag(String tipo, String[] guardas, List<List<Passo>> ops) { this.tipo = tipo; this.guardas = guardas; operandos = ops; }
    }
    static class Seq {
        String pacote, nome, nota; List<Lin> linhas = new ArrayList<>(); List<Passo> passos = new ArrayList<>();
        Seq(String pacote, String nome) { this.pacote = pacote; this.nome = nome; }
        Seq l(String chave, String nome, String base) { linhas.add(new Lin(chave, nome, base)); return this; }
        Seq p(Passo... ps) { passos.addAll(List.of(ps)); return this; }
        Seq nota(String n) { nota = n; return this; }
    }

    static Msg m(String de, String para, String op, String args, String ret, Passo... f) { return new Msg(de, para, op, args, ret, false, f); }
    static Msg criar(String de, String para, String op, String args) { return new Msg(de, para, op, args, null, true); }
    static Frag frag(String tipo, String guarda, Passo... f) { return new Frag(tipo, new String[]{guarda}, List.of(List.of(f))); }
    static Frag alt(String g1, List<Passo> o1, String g2, List<Passo> o2) { return new Frag("alt", new String[]{g1, g2}, List.of(o1, o2)); }

    static List<Seq> sequenciasCache;

    static List<Seq> sequencias() {
        if (sequenciasCache != null) return sequenciasCache;
        List<Seq> s = new ArrayList<>();

        s.add(new Seq("Req 01 - State", "SD R01 - Terceira renovacao recusada")
            .l("t", "R01_CicloDeVidaDoLivroTest", null)
            .l("te", "", "TratadorDeEmprestimo").l("s", "s", "SolicitacaoDeRenovacao").l("ex", "", "ExecutorDeSolicitacoes")
            .l("reg", "", "RegistroDeSolicitacoes").l("circ", "", "ServicoDeCirculacao").l("uow", "", "UnidadeDeTrabalho")
            .l("mov", "", "Movimentacao").l("ac", "", "Acervo").l("livro", "livro", "Livro").l("est", "", "Renovado")
            .p(m("t", "te", "renovarEmprestimo", "1, \"M-100\"", "resultado RECUSADA",
                criar("te", "s", "SolicitacaoDeRenovacao", "id, 1, \"M-100\""),
                m("te", "ex", "submeter", "s", "resultado",
                    m("ex", "reg", "reservarExecucao", "s.id", "true"),
                    m("ex", "s", "executar", "", null,
                        m("s", "circ", "renovar", "1, \"M-100\"", null,
                            m("circ", "uow", "executar", "bloco", "RenovacaoNegadaException",
                                m("circ", "mov", "buscarEmprestimo", "1, \"M-100\"", "emprestimo"),
                                m("circ", "ac", "buscarPorId", "1", "livro"),
                                m("circ", "livro", "renovarEmprestimo", "", null,
                                    m("livro", "est", "renovarEmprestimo", "livro", null,
                                        frag("alt", "numRenovacoes >= 2 (já renovou 2 vezes)",
                                            m("est", "est", "throw RenovacaoNegadaException", "1", null)))),
                                m("uow", "uow", "desfazer", "", null)))),
                    m("ex", "ex", "ResultadoSolicitacao.recusada", "s, \"limite de 2 renovações\"", null),
                    m("ex", "reg", "concluir", "resultado", null))))
            .nota("Cenário R01 (State)\nAntes: livro 1 RENOVADO, numRenovacoes = 2, vencimento 11/02/2027.\n"
                + "Renovado.renovarEmprestimo testa a guarda ANTES de prorrogar():\n3ª renovação -> RenovacaoNegadaException; a transação é desfeita.\n"
                + "Depois: vencimento continua 11/02/2027, numRenovacoes = 2, estado RENOVADO.\n\n"
                + "2ª parte do cenário: livro DEVOLVIDO (não inspecionado) + reservarLivro:\n"
                + "Devolvido não sobrescreve reservarLivro -> EstadoLivro.recusa() ->\nOperacaoInvalidaException (\"DEVOLVIDO\"); nenhuma reserva é gravada."));

        s.add(new Seq("Req 02 - Strategy", "SD R02 - Politicas de multa para 5 dias e sem atraso")
            .l("t", "R02_PoliticasDeMultaTest", null)
            .l("md", "", "MultaDiaria").l("mt", "", "MultaComTolerancia").l("tm", "", "TratadorDeMulta")
            .l("sel", "", "SeletorDePolitica").l("pol", "p", "PoliticaDeMulta")
            .p(m("t", "md", "calcular", "5", "R$ 5,00  (5 x 1,00)"),
               m("t", "mt", "calcular", "5", "R$ 3,00  ((5 - 2) x 1,00)"),
               m("t", "tm", "politicasDisponiveis", "", "[ORIGINAL, TETO, TOLERANCIA]",
                   m("tm", "sel", "todas", "", "lista")),
               frag("loop", "para cada p em politicasDisponiveis()",
                   m("t", "pol", "calcular", "0", "R$ 0,00")))
            .nota("Cenário R02 (Strategy)\n5 dias de atraso: ORIGINAL (MultaDiaria) = R$ 5,00; TOLERANCIA = R$ 3,00.\n"
                + "Sem atraso: todas as políticas devolvem zero.\n\n"
                + "No sistema, quem escolhe a estratégia é o ServicoDeMulta:\n"
                + "gerarMulta(cpf, idDevolucao, dias) -> SeletorDePolitica.vigente() -> politica.calcular(dias)\n"
                + "-> new Multa(..., valor, politica.id()) (ver SD R05)."));

        s.add(new Seq("Req 03 - Adapter", "SD R03 - R$ 7,00 nos bancos A e B")
            .l("t", "R03_BancosAdapterTest", null).l("sm", "", "ServicoDeMulta")
            .l("ada", "", "AdaptadorBancoA").l("apia", "", "ApiBancoA").l("adb", "", "AdaptadorBancoB").l("apib", "", "ApiBancoB")
            .p(m("t", "sm", "cobrar", "multa (R$ 7,00)  [servico montado com o banco A]", "boleto banco A",
                   m("sm", "ada", "registrarBoleto", "1, cpf, 7.00, email", "\"A-...\" (nosso número)",
                       m("ada", "ada", "paraCentavos", "7.00", null),
                       m("ada", "apia", "gerarTitulo", "\"12345678909\", 700, \"1\"", "TituloA")),
                   m("sm", "ada", "codigoDoBanco", "", "\"A\"")),
               m("t", "sm", "cobrar", "multa (R$ 7,00)  [servico montado com o banco B]", "boleto banco B",
                   m("sm", "adb", "registrarBoleto", "2, cpf, 7.00, email", "\"TXB...\" (txid)",
                       m("adb", "adb", "paraDocumento", "\"12345678909\"", null),
                       m("adb", "apib", "abrirCobranca", "\"123.456.789-09\", 7.00, \"MULTA-2\"", "CobrancaB")),
                   m("sm", "adb", "codigoDoBanco", "", "\"B\"")))
            .nota("Cenário R03 (Adapter)\nO mesmo código do ServicoDeMulta chama iCobranca.registrarBoleto(..., 7.00, ...).\n"
                + "Banco A recebe 700 (centavos, long); banco B recebe 7.00 (decimal) e o CPF formatado.\n"
                + "Não há if por banco: o adaptador é escolhido na montagem (Sistema).\n\n"
                + "2ª parte: webhook repetido do banco A -> receberWebhook traduz para ConfirmacaoPagamento\n"
                + "-> iQuitacao.registrarQuitacao; na repetição o boleto já está QUITADO e não quita de novo."));

        s.add(new Seq("Req 04 - Facade", "SD R04 - Efetivar emprestimo pela fachada")
            .l("t", "R04_FachadaDeCirculacaoTest", null).l("te", "", "TratadorDeEmprestimo").l("circ", "", "ServicoDeCirculacao")
            .l("uow", "", "UnidadeDeTrabalho").l("rel", "", "Relacionamento").l("mov", "", "Movimentacao").l("ac", "", "Acervo")
            .l("livro", "livro", "Livro").l("est", "", "Reservado").l("emp", "e", "Emprestimo")
            .p(m("t", "te", "emprestarLivro", "1, \"M-100\", \"F-01\"", "ComprovanteEmprestimo (vence 13/12/2026)",
                m("te", "circ", "efetivarEmprestimo", "1, \"M-100\", \"F-01\"", "comprovante",
                    m("circ", "uow", "executar", "bloco", "comprovante",
                        m("circ", "rel", "buscarEstudante", "\"M-100\"", "estudante"),
                        m("circ", "mov", "buscarReservaAtiva", "1", "reserva"),
                        m("circ", "circ", "exigirReservaDo", "estudante, reserva, \"M-100\"", null),
                        m("circ", "ac", "buscarPorId", "1", "livro"),
                        m("circ", "livro", "emprestarLivro", "estudante, 13/11/2026", null,
                            m("livro", "est", "emprestarLivro", "livro, estudante, 13/11/2026", null,
                                m("est", "livro", "definirVencimento", "13/12/2026", null),
                                m("est", "livro", "mudarPara", "Emprestado.INSTANCIA", null))),
                        m("circ", "mov", "removerReserva", "reserva", "true"),
                        m("circ", "mov", "gerarIdTransacao", "", "id"),
                        criar("circ", "emp", "Emprestimo", "id, hoje, livro, estudante, atendente"),
                        m("circ", "mov", "registrarEmprestimo", "e", null),
                        m("circ", "ac", "atualizar", "livro", null),
                        m("uow", "uow", "confirmar", "", null)))))
            .nota("Cenário R04 (Facade)\nO tratador faz uma única chamada: efetivarEmprestimo.\n"
                + "A fachada valida a reserva, remove a reserva ativa, cria o empréstimo e atualiza o Livro\n"
                + "dentro de uma transação (UnidadeDeTrabalho.executar: confirmar no fim; desfazer em exceção).\n"
                + "Sem reserva ou reserva de outro estudante: exigirReservaDo lança ReservaInvalidaException\n"
                + "antes de alterar qualquer coisa. O retorno é um comprovante (record), nunca coleções."));

        s.add(new Seq("Req 05 - Observer", "SD R05 - Devolucao atrasada notifica os ouvintes")
            .l("t", "R05_ReacoesADevolucaoTest", null).l("td", "", "TratadorDeDevolucao").l("circ", "", "iCirculacao")
            .l("pub", "", "PublicadorDeDevolucao").l("oi", "", "OuvinteInspecao").l("ag", "", "AgendaDeInspecao")
            .l("om", "", "OuvinteMulta").l("fin", "", "iMulta").l("oind", "", "OuvinteIndicadores").l("pi", "", "PainelDeIndicadores")
            .l("oe", "", "OuvinteEmail").l("em", "", "ServicoDeEmail")
            .p(m("t", "td", "registrarDevolucao", "1, \"M-100\", \"F-01\"  [16/12/2026]", "comprovante (diasAtraso = 3)",
                m("td", "circ", "devolver", "1, \"M-100\", \"F-01\"", "comprovante (livro DEVOLVIDO)"),
                m("td", "td", "EventoDevolucao.de", "comprovante, agora", null),
                m("td", "pub", "publicar", "ev", null,
                    frag("loop", "para cada ouvinte registrado (falha -> registrarFalha, segue para o próximo)",
                        m("pub", "oi", "aoDevolver", "ev", null, m("oi", "ag", "agendar", "item (livro 1)", null)),
                        m("pub", "om", "aoDevolver", "ev", null,
                            frag("opt", "ev.diasAtraso() > 0",
                                m("om", "fin", "gerarMulta", "cpf, idDevolucao, 3", "boleto R$ 3,00"))),
                        m("pub", "oind", "aoDevolver", "ev", null, m("oind", "pi", "registrar", "3", null)),
                        m("pub", "oe", "aoDevolver", "ev", null, m("oe", "em", "enviar", "email, assunto, texto", null))))))
            .nota("Cenário R05 (Observer)\nA devolução é confirmada (commit) e só depois o evento é publicado.\n"
                + "3 dias de atraso: inspeção agendada, multa de R$ 3,00 (política ORIGINAL), indicadores e e-mail.\n"
                + "No prazo (diasAtraso = 0): OuvinteMulta não chama iMulta -> nenhuma multa.\n"
                + "O livro fica DEVOLVIDO até TratadorDeDevolucao.inspecionar(1, true) -> LIVRE.\n"
                + "Novo ouvinte = nova classe registrada no publicador; o tratador não muda."));

        s.add(new Seq("Req 06 - Command", "SD R06 - Mesma renovacao enviada duas vezes")
            .l("t", "R06_SolicitacoesCommandTest", null).l("te", "", "TratadorDeEmprestimo")
            .l("s1", "s", "SolicitacaoDeRenovacao").l("s2", "s'", "SolicitacaoDeRenovacao")
            .l("ex", "", "ExecutorDeSolicitacoes").l("reg", "", "RegistroDeSolicitacoes").l("circ", "", "iCirculacao").l("livro", "livro", "Livro")
            .p(m("t", "te", "renovarEmprestimo", "s42, 1, \"M-100\"", "EXECUTADA",
                   criar("te", "s1", "SolicitacaoDeRenovacao", "s42, 1, \"M-100\""),
                   m("te", "ex", "submeter", "s", "resultado",
                       m("ex", "reg", "reservarExecucao", "s42", "true"),
                       m("ex", "s1", "executar", "", "EXECUTADA",
                           m("s1", "circ", "renovar", "1, \"M-100\"", "vence 12/01/2027",
                               m("circ", "livro", "renovarEmprestimo", "", null))),
                       m("ex", "reg", "concluir", "resultado", null))),
               m("t", "te", "renovarEmprestimo", "s42, 1, \"M-100\"  (reenvio)", "o mesmo resultado",
                   criar("te", "s2", "SolicitacaoDeRenovacao", "s42, 1, \"M-100\""),
                   m("te", "ex", "submeter", "s'", "resultado gravado",
                       m("ex", "reg", "reservarExecucao", "s42", "false (já executado)"),
                       m("ex", "reg", "resultadoDe", "s42", "resultado da 1ª execução"))))
            .nota("Cenário R06 (Command)\nO mesmo id (s42) executa uma única vez: o vencimento vai só de 13/12/2026\n"
                + "para 12/01/2027 (+30 dias) e numRenovacoes = 1.\n"
                + "Solicitações com ids diferentes executam, mas a 3ª é recusada pelo Livro (State):\n"
                + "RenovacaoNegadaException -> ResultadoSolicitacao RECUSADA."));

        s.add(new Seq("Req 07 - Template Method", "SD R07 - Cadastro de estudante (metodo-modelo)")
            .l("t", "R07_CadastroTemplateMethodTest", null).l("tes", "", "TratadorDeEstudante")
            .l("ga", "", "GrupoDeAcademicos").l("pes", "", "iPessoa")
            .p(m("t", "tes", "inserirEstudante", "\"111\", \"M-100\"  (já cadastrado)", "EXISTENTE (o mesmo objeto)",
                   m("tes", "tes", "inserir", "DadosEstudante(\"111\", \"M-100\")", null,
                       m("tes", "tes", "buscarExistente", "dados", null,
                           m("tes", "ga", "buscarPorMatricula", "\"M-100\"", "estudante")),
                       m("tes", "tes", "apresentar", "estudante, false", null))),
               m("t", "tes", "inserirEstudante", "\"999\", \"M-999\"  (CPF fora do RH)", "RECUSADO \"pessoa não cadastrada\"",
                   m("tes", "tes", "inserir", "DadosEstudante(\"999\", \"M-999\")", null,
                       m("tes", "tes", "buscarExistente", "dados", null,
                           m("tes", "ga", "buscarPorMatricula", "\"M-999\"", "null")),
                       m("tes", "tes", "validar", "dados", null),
                       m("tes", "tes", "verificarPreRequisitos", "dados", null,
                           m("tes", "pes", "consultarPessoa", "\"999\"", "null")),
                       m("tes", "tes", "ResultadoCadastro.recusado", "\"pessoa não cadastrada (CPF 999)\"", null))))
            .nota("Cenário R07 (Template Method)\ninserir(dados) é final em TratadorDeCadastro: a ordem dos passos não muda.\n"
                + "Duplicado: buscarExistente encontra M-100 -> apresentar(existente) -> EXISTENTE.\n"
                + "CPF não cadastrado: verificarPreRequisitos lança CadastroRecusadoException ->\n"
                + "RECUSADO antes de criar() e de transacao.executar(armazenar): zero gravações.\n"
                + "Caminho normal (novo): ... -> criar -> uow.executar(armazenar) -> GrupoDeAcademicos.adicionar -> NOVO."));

        s.add(new Seq("Req 08 - Abstract Factory", "SD R08 - Familias de teste e de producao")
            .l("t", "R08_FamiliasDePersistenciaTest", null).l("conf", "", "ConfiguradorDePersistencia")
            .l("fm", "", "FabricaEmMemoria").l("um", "", "UnidadeDeTrabalhoEmMemoria").l("rlm", "", "RepositorioLivroEmMemoria")
            .l("fj", "", "FabricaJson").l("uj", "uow", "UnidadeDeTrabalhoJson").l("rlj", "livros", "RepositorioLivroJson")
            .l("rmj", "", "RepositorioMovimentacaoJson").l("rrj", "", "RepositorioRelacionamentoJson")
            .p(m("t", "conf", "fabricaPara", "TESTE, x.json", "fabrica", criar("conf", "fm", "FabricaEmMemoria", "")),
               m("t", "fm", "criarUnidadeDeTrabalho", "", "uow", criar("fm", "um", "UnidadeDeTrabalhoEmMemoria", "")),
               m("t", "fm", "criarRepositorioLivro", "uow", "repositorio", criar("fm", "rlm", "RepositorioLivroEmMemoria", "uow")),
               m("t", "conf", "fabricaPara", "PRODUCAO, biblioteca.json", "fabrica", criar("conf", "fj", "FabricaJson", "biblioteca.json")),
               m("t", "fj", "criarUnidadeDeTrabalho", "", "uow", criar("fj", "uj", "UnidadeDeTrabalhoJson", "biblioteca.json")),
               m("t", "fj", "criarRepositorioLivro", "uow", "livros",
                   m("fj", "fj", "daFamilia", "uow", null), criar("fj", "rlj", "RepositorioLivroJson", "uow")),
               m("t", "fj", "criarRepositorioMovimentacao", "uow", "movimentacao", criar("fj", "rmj", "RepositorioMovimentacaoJson", "uow")),
               m("t", "fj", "criarRepositorioRelacionamento", "uow", "relacionamento", criar("fj", "rrj", "RepositorioRelacionamentoJson", "uow")),
               m("t", "rlj", "unidade", "", "uow (o mesmo objeto)"))
            .nota("Cenário R08 (Abstract Factory)\nPerfil TESTE: só objetos em memória; nenhum arquivo é criado.\n"
                + "Perfil PRODUCAO: repositórios JSON que compartilham a mesma UnidadeDeTrabalhoJson\n"
                + "(assertSame(uow, livros.unidade())...): um confirmar() grava o arquivo inteiro de uma vez.\n"
                + "daFamilia(uow) recusa uma unidade de outra família (FamiliaIncompativelException)."));

        s.add(new Seq("Req 09 - Decorator", "SD R09 - Segunda consulta vem do cache e e medida")
            .l("t", "R09_ConsultaComDecoradoresTest", null).l("mont", "", "MontadorDeConsulta")
            .l("dm", "", "DecoradorDeMedicao").l("dc", "", "DecoradorDeCache").l("arm", "", "ArmazenamentoDeCache")
            .l("cb", "", "ConsultaAcervoBasica").l("ac", "", "Acervo").l("met", "", "RegistroDeMetricas")
            .p(m("t", "mont", "montar", "completa, basica, armazenamento, metricas, relogio", "consulta (DecoradorDeMedicao)",
                   criar("mont", "dc", "DecoradorDeCache", "basica, armazenamento, 5 min"),
                   criar("mont", "dm", "DecoradorDeMedicao", "cache, metricas")),
               m("t", "dm", "buscarMetadados", "Criterio(\"Dom Casmurro\")", "[1 - Dom Casmurro]",
                   m("dm", "dc", "buscarMetadados", "criterio", "lista",
                       m("dc", "arm", "obter", "criterio", "null (ainda vazio)"),
                       m("dc", "cb", "buscarMetadados", "criterio", "lista", m("cb", "ac", "listar", "", "livros")),
                       m("dc", "arm", "guardar", "criterio, Entrada(lista, expira em 5 min)", null)),
                   m("dm", "met", "registrar", "medição 1", null)),
               m("t", "dm", "buscarMetadados", "Criterio(\"  dom casmurro \")", "[1 - Dom Casmurro]",
                   m("dm", "dc", "buscarMetadados", "criterio", "lista (do cache)",
                       m("dc", "arm", "obter", "criterio", "entrada válida")),
                   m("dm", "met", "registrar", "medição 2", null)))
            .nota("Cenário R09 (Decorator)\nCadeia: DecoradorDeMedicao -> DecoradorDeCache -> ConsultaAcervoBasica.\n"
                + "2ª consulta (mesma chave normalizada): atendida pelo cache, a básica é chamada 1 vez só,\n"
                + "e mesmo assim gera a 2ª medição (a medição está por fora do cache).\n\n"
                + "2ª parte: o cache guarda só MetadadosLivro(idLivro, titulo). reservarLivro sempre lê o\n"
                + "Livro atual no Acervo -> Emprestado recusa (OperacaoInvalidaException \"EMPRESTADO\")."));

        s.add(new Seq("Req 10 - Composite", "SD R10 - Contar livros e mover entre secoes")
            .l("t", "R10_OrganizacaoDoAcervoTest", null).l("org", "", "OrganizadorDoAcervo")
            .l("cen", "Central", "GrupoDoAcervo").l("lit", "Literatura", "GrupoDoAcervo").l("cie", "Ciencias", "GrupoDoAcervo")
            .l("item", "livro1", "ItemDoAcervo")
            .p(m("t", "org", "contarLivros", "\"Central\"", "5",
                   m("org", "cen", "contarLivros", "", "5",
                       frag("loop", "para cada filho",
                           m("cen", "lit", "contarLivros", "", "3  (1 + 1 + 1: itens 1, 2, 3)",
                               m("lit", "item", "contarLivros", "", "1")),
                           m("cen", "cie", "contarLivros", "", "2  (itens 4, 5)")))),
               m("t", "org", "mover", "1, \"Ciencias\"", null,
                   m("org", "org", "localizarItem", "1", null),
                   m("org", "lit", "remover", "item", null, m("lit", "item", "definirPai", "null", null)),
                   m("org", "cie", "adicionar", "item", null,
                       m("cie", "cie", "ehAncestralDe", "item", null),
                       m("cie", "item", "definirPai", "Ciencias", null))),
               m("t", "org", "contarLivros", "\"Central\"", "5  (Literatura 2, Ciencias 3)"))
            .nota("Cenário R10 (Composite)\nUnidade Central = Literatura (3 itens) + Ciencias (2 itens) -> contarLivros() = 5.\n"
                + "O cliente trata grupo e item do mesmo jeito (ComponenteDoAcervo.contarLivros).\n"
                + "mover(1, \"Ciencias\") só troca o pai do ItemDoAcervo: o Livro 1 continua com o mesmo id,\n"
                + "estado EMPRESTADO, vencimento 13/12/2026 e empréstimo ativo na Movimentacao."));

        sequenciasCache = s;
        return s;
    }

    static Map<String, INodePresentation> linhasDeVida;
    static Map<String, Double> xDe, larguraDe;
    static Map<String, String> baseDe;

    static void diagramasDeSequencia() throws Exception {
        for (Seq q : sequencias()) {
            IPackage dono = pacoteDoRequisito(pa.getProject(), q.pacote);
            sde.createSequenceDiagram(dono, q.nome);
            linhasDeVida = new HashMap<>(); xDe = new HashMap<>(); larguraDe = new HashMap<>(); baseDe = new HashMap<>();
            ativa = new HashMap<>();
            double x = 20;
            for (Lin l : q.linhas) {
                INodePresentation n = sde.createLifeline(l.nome, x);
                if (l.base != null) ((ILifeline) n.getModel()).setBase(modelo.get(l.base));
                try { n.setProperty("notation_type", "normal"); } catch (Exception ignorada) { }   // interface como retângulo
                double w = Math.max(n.getWidth(), 7.0 * ((l.nome.isEmpty() ? 2 : l.nome.length() + 3) + (l.base == null ? 0 : l.base.length())) + 20);
                try { n.setWidth(w); } catch (Exception ignorada) { }
                linhasDeVida.put(l.chave, n); xDe.put(l.chave, x); larguraDe.put(l.chave, w); baseDe.put(l.chave, l.base);
                x += w + 45;
            }
            double y = 110;
            for (Passo p : q.passos) y = desenhar(p, y) + 6;
            if (q.nota != null) sde.createNote(q.nota, new Point2D.Double(20, y + 30));
        }
    }

    static int linhasDe(Passo p) {
        if (p instanceof Msg m) {
            if (m.criar) return 2;
            int n = 1;
            for (Passo f : m.filhos) n += linhasDe(f);
            if (m.ret != null) n += 1;
            return n;
        }
        Frag f = (Frag) p;
        int n = 1;
        for (List<Passo> o : f.operandos) for (Passo x : o) n += linhasDe(x);
        return n + f.operandos.size();
    }

    /** Desenha o passo a partir de y e devolve o y seguinte livre. */
    static double desenhar(Passo p, double y) throws Exception {
        if (p instanceof Msg m) return desenharMsg(m, y);
        return desenharFrag((Frag) p, y);
    }

    /** Ativação corrente de cada linha de vida: a mensagem sai dela para o Astah aninhar (1, 1.1, 1.1.1...). */
    static Map<String, INodePresentation> ativa;

    static double fim(IPresentation p) {
        INodePresentation n = (INodePresentation) p;
        return n.getLocation().getY() + n.getHeight();
    }

    static double desenharMsg(Msg m, double y) throws Exception {
        INodePresentation origem = ativa.getOrDefault(m.de, linhasDeVida.get(m.de));
        INodePresentation para = linhasDeVida.get(m.para);
        if (m.criar) {
            ILinkPresentation lk = sde.createCreateMessage(m.op, origem, para, y);
            if (!m.args.isEmpty()) ((IMessage) lk.getModel()).setArgument(m.args);
            lembrarOrigem(m.de, lk);
            return y + PASSO * 2.2;
        }
        ILinkPresentation lk = sde.createMessage(m.op, origem, para, y);
        lembrarOrigem(m.de, lk);
        IMessage msg = (IMessage) lk.getModel();
        IOperation op = operacao(baseDe.get(m.para), m.op);
        if (op != null) { try { msg.setOperation(op); } catch (Exception ignorada) { } }
        if (!m.args.isEmpty()) msg.setArgument(m.args);
        INodePresentation alvo = (INodePresentation) lk.getTargetEnd();
        INodePresentation anterior = ativa.put(m.para, alvo);
        double interno = y + PASSO;
        for (Passo f : m.filhos) interno = desenhar(f, interno);
        if (anterior != null) ativa.put(m.para, anterior); else ativa.remove(m.para);
        if (m.ret != null && !m.de.equals(m.para)) {
            sde.createReturnMessage(m.ret, lk);
            return Math.max(interno, fim(alvo)) + PASSO * 0.8;
        }
        return Math.max(interno, fim(alvo) + 8);
    }

    /** A linha de vida que ainda não tinha ativação passa a usar a criada por esta mensagem. */
    static void lembrarOrigem(String chave, ILinkPresentation lk) {
        if (!ativa.containsKey(chave) && lk.getSourceEnd() instanceof INodePresentation n && "Activation".equals(n.getType())) {
            ativa.put(chave, n);
        }
    }

    static double desenharFrag(Frag f, double y) throws Exception {
        double y0 = y;
        double interno = y + PASSO * 1.6;
        List<List<IPresentation>> alvos = new ArrayList<>();
        Set<String> envolvidas = new HashSet<>();
        for (List<Passo> o : f.operandos) {
            List<IPresentation> lista = new ArrayList<>();
            for (Passo x : o) {
                coletar(x, envolvidas);
                interno = desenhar(x, interno);
            }
            alvos.add(lista);
        }
        interno += 6;
        double xMin = Double.MAX_VALUE, xMax = 0;
        for (String k : envolvidas) {
            xMin = Math.min(xMin, xDe.get(k));
            xMax = Math.max(xMax, xDe.get(k) + larguraDe.get(k));
        }
        double largura = Math.max(xMax - xMin + 40, 320);
        INodePresentation cf = sde.createCombinedFragment("", f.tipo, new Point2D.Double(xMin - 20, y0 - 8), largura, interno - y0);
        ICombinedFragment modeloCf = (ICombinedFragment) cf.getModel();
        modeloCf.getInteractionOperands()[0].setGuard(f.guardas[0]);
        for (int i = 1; i < f.guardas.length; i++) modeloCf.addInteractionOperand("", f.guardas[i]);
        return interno + PASSO * 0.5;
    }

    static void coletar(Passo p, Set<String> envolvidas) {
        if (p instanceof Msg m) {
            envolvidas.add(m.de); envolvidas.add(m.para);
            for (Passo f : m.filhos) coletar(f, envolvidas);
        } else for (List<Passo> o : ((Frag) p).operandos) for (Passo x : o) coletar(x, envolvidas);
    }

    /** Operação do modelo com esse nome na classe da linha de vida (ou em uma superclasse/interface modelada). */
    static IOperation operacao(String base, String nome) {
        if (base == null) return null;
        Deque<Class<?>> fila = new ArrayDeque<>();
        fila.add(java.get(base));
        while (!fila.isEmpty()) {
            Class<?> k = fila.poll();
            if (k == null) continue;
            IClass c = modelada(k) ? modelo.get(chave(k)) : null;
            if (c != null) for (IOperation o : c.getOperations()) if (o.getName().equals(nome)) return o;
            if (k.getSuperclass() != null) fila.add(k.getSuperclass());
            fila.addAll(List.of(k.getInterfaces()));
        }
        return null;
    }

    // =====================================================================================
    // Máquina de estados do Livro (diagrama existente do Trabalho 02, atualizado)
    // =====================================================================================

    static void atualizarMaquinaDeEstados(IModel projeto) throws Exception {
        IStateMachineDiagram dg = null;
        for (INamedElement e : projeto.getOwnedElements()) {
            if (e instanceof IClass c && c.getName().equals("Livro")) {
                for (IDiagram d : c.getDiagrams()) if (d instanceof IStateMachineDiagram s) dg = s;
            }
        }
        if (dg == null) throw new IllegalStateException("Máquina de estados do Livro não encontrada");
        sme.setDiagram(dg);
        Map<String, String> classeDoEstado = Map.of("LIVRE", "Livre", "RESERVADO", "Reservado", "EMPRESTADO", "Emprestado",
                "RENOVADO", "Renovado", "DEVOLVIDO", "Devolvido", "REPARANDO", "Reparando", "INCINERADO", "Incinerado");
        Map<String, INodePresentation> estados = new HashMap<>();
        INodePresentation escolha = null, nota = null;
        List<ILinkPresentation> transicoes = new ArrayList<>();
        for (IPresentation p : dg.getPresentations()) {
            IElement m = p.getModel();
            if (p instanceof INodePresentation n && m instanceof IState st && classeDoEstado.containsKey(st.getName())) {
                estados.put(st.getName(), n);
                st.setEntry("mudarPara(" + classeDoEstado.get(st.getName()) + ".INSTANCIA)");
            } else if (p instanceof INodePresentation n && m instanceof IPseudostate ps && ps.isChoicePseudostate()) {
                escolha = n;
            } else if (p instanceof INodePresentation n && "Note".equalsIgnoreCase(p.getType())) {
                nota = n;
            } else if (p instanceof ILinkPresentation l && m instanceof ITransition) {
                transicoes.add(l);
            }
        }
        for (ILinkPresentation l : transicoes) {
            ITransition t = (ITransition) l.getModel();
            String de = t.getSource().getName(), para = t.getTarget().getName(), ev = t.getEvent();
            String acao = null;
            if ("reservarLivro".equals(ev)) acao = "definirReservante(e)";
            else if ("emprestarLivro".equals(ev)) acao = "definirVencimento(data + 30)";
            else if ("renovarEmprestimo".equals(ev)) acao = "prorrogar(30)";
            else if ("inspecionar".equals(ev) && "LIVRE".equals(para)) acao = "limparCiclo()";
            else if ("inspecionar".equals(ev) && "REPARANDO".equals(para)) acao = "";
            else if ("retornarDoReparo".equals(ev)) acao = "limparCiclo()";
            if (acao != null && acao.isEmpty()) { try { t.setAction(null); } catch (Exception e) { t.setAction(""); } }
            else if (acao != null) t.setAction(acao);
            if ("renovarEmprestimo".equals(ev) && "EMPRESTADO".equals(de)) t.setGuard("numRenovacoes = 0");
        }
        // a multa deixou de ser efeito da transição (agora é reação ao evento, Observer): remove o nó de decisão
        if (escolha != null) {
            IElement modeloEscolha = escolha.getModel();
            sme.deletePresentation(escolha);
            try { bme.delete(modeloEscolha); } catch (Exception ignorada) { }
            for (String origem : List.of("EMPRESTADO", "RENOVADO")) {
                ILinkPresentation nova = sme.createTransition(estados.get(origem), estados.get("DEVOLVIDO"));
                ITransition t = (ITransition) nova.getModel();
                t.setEvent("registrarDevolucao");
                t.setGuard("emprestimo registrado");
                t.setAction("publicar(EventoDevolucao)");
                // dobra a linha para os dois rótulos não ficarem um sobre o outro
                INodePresentation o = estados.get(origem), d = estados.get("DEVOLVIDO");
                double yMeio = (o.getLocation().getY() + o.getHeight() + d.getLocation().getY()) / 2;
                double xDobra = origem.equals("EMPRESTADO") ? o.getLocation().getX() - 40
                        : o.getLocation().getX() + o.getWidth() + 30;
                Point2D ini = new Point2D.Double(o.getLocation().getX() + o.getWidth() / 2, o.getLocation().getY() + o.getHeight());
                Point2D fimP = new Point2D.Double(d.getLocation().getX() + (origem.equals("EMPRESTADO") ? 20 : d.getWidth() - 20),
                        d.getLocation().getY());
                try { nova.setAllPoints(new Point2D[]{ini, new Point2D.Double(xDobra, yMeio), fimP}); } catch (Exception ignorada) { }
            }
        }
        String texto = "Ciclo de vida da classe Livro - padrão State (Trabalho 03).\n"
            + "Livro (Context) delega cada evento ao objeto -estado : EstadoLivro; cada estado é uma classe\n"
            + "(Livre, Reservado, Emprestado, Renovado, Devolvido, Reparando, Incinerado) que sobrescreve só os eventos\n"
            + "das transições que saem dele. Evento não aceito -> OperacaoInvalidaException, sem alterar o livro.\n"
            + "entry / mudarPara(X.INSTANCIA) = troca do objeto de estado (visibilidade de pacote).\n"
            + "Limite de 2 renovações: guarda numRenovacoes < 2 em Renovado (senão RenovacaoNegadaException).\n"
            + "Devolução: o tratador publica o EventoDevolucao; o OuvinteMulta gera a multa quando diasAtraso() > 0 (Observer).\n"
            + "incinerar = laudo irrecuperável. Eventos chamados pelo ServicoDeCirculacao (fachada).";
        if (nota != null) {
            try { nota.setLabel(texto); } catch (Exception e) { sme.createNote(texto, new Point2D.Double(700, 900)); }
        } else {
            sme.createNote(texto, new Point2D.Double(700, 900));
        }
    }

    static void avisoNoDiagramaDoT02(IModel projeto) throws Exception {
        for (IDiagram d : projeto.getDiagrams()) {
            if (d instanceof IClassDiagram && d.getName().equals("Classes de Projeto")) {
                String texto = "Diagrama do Trabalho 02 (mantido como histórico).\n"
                    + "O modelo do Trabalho 03 (gerado do código) está no pacote \"Trabalho 03 - Implementacao\";\n"
                    + "os diagramas de cada padrão e dos cenários estão nos pacotes \"Req 01 - State\" ... \"Req 10 - Composite\".";
                IPresentation existente = null;
                for (IPresentation p : d.getPresentations()) {
                    String r = p.getLabel();
                    if (r != null && r.contains("mantido como histórico")) existente = p;
                }
                // lugar livre: à direita de todos os elementos, na altura do primeiro
                double direita = 0, topo = Double.MAX_VALUE;
                for (IPresentation p : d.getPresentations()) {
                    if (p == existente || !(p instanceof INodePresentation n)) continue;
                    direita = Math.max(direita, n.getLocation().getX() + n.getWidth());
                    topo = Math.min(topo, n.getLocation().getY());
                }
                Point2D lugar = new Point2D.Double(direita + 60, topo == Double.MAX_VALUE ? 20 : topo);
                if (existente instanceof INodePresentation nota) {
                    existente.setLabel(texto);   // atualiza o texto de uma geração anterior
                    nota.setLocation(lugar);
                    continue;
                }
                cde.setDiagram(d);
                cde.createNote(texto, lugar);
            }
        }
    }
}
