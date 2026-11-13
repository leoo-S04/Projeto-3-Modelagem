package br.pucpr.ib2b.persistencia;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Papel GoF: ConcreteProduct da família JSON (perfil PRODUCAO) — artefato biblioteca.json do diagrama de implantação.
 * <ul>
 *   <li>Ao iniciar: carrega o arquivo em uma cópia de trabalho (se não existir, cria um arquivo vazio).</li>
 *   <li>confirmar(): grava o arquivo INTEIRO de forma atômica: escreve biblioteca.json.tmp e o move por cima do
 *       original. Uma falha no meio nunca deixa o arquivo pela metade.</li>
 *   <li>desfazer(): descarta a cópia de trabalho e recarrega do disco.</li>
 * </ul>
 */
public final class UnidadeDeTrabalhoJson extends UnidadeDeTrabalhoBase {

    private final Path arquivo;
    private final ObjectMapper json = ConversorJson.criar();

    public UnidadeDeTrabalhoJson(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath();
        if (Files.exists(this.arquivo)) {
            this.dados = ler();
        } else {
            this.dados = new BaseDeDados();
            gravar();
        }
    }

    @Override
    public synchronized void confirmar() {
        gravar();
    }

    @Override
    public synchronized void desfazer() {
        dados = ler();
    }

    public Path arquivo() { return arquivo; }

    private BaseDeDados ler() {
        try {
            return MapeadorDeDados.paraBase(json.readValue(arquivo.toFile(), DocumentoDados.class));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler " + arquivo, e);
        }
    }

    /** synchronized (via chamadores): só uma thread grava o arquivo por vez. */
    private void gravar() {
        Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        try {
            Files.createDirectories(arquivo.getParent());
            json.writeValue(temporario.toFile(), MapeadorDeDados.paraDocumento(dados));
            try {
                Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException semSuporte) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar " + arquivo, e);
        }
    }
}
