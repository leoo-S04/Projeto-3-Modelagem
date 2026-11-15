import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.nio.file.*;
import java.util.*;

/**
 * Junta as imagens PNG exportadas pelo Astah em um único PDF (uma página por diagrama, com o nome no topo).
 * Usa o iText que já vem na instalação do Astah (lib/itext-1.3.3.jar).
 *
 * Uso: java -cp <itext> MontarPdf <saida.pdf> <titulo> <pasta-ou-imagem.png> [...]
 * Ordem: máquina de estados; depois, para cada requisito, o diagrama de classes (DC) e o de sequência (SD).
 */
public class MontarPdf {
    public static void main(String[] a) throws Exception {
        Path saida = Path.of(a[0]);
        String titulo = a[1];
        List<Path> imagens = new ArrayList<>();
        for (int i = 2; i < a.length; i++) {
            Path p = Path.of(a[i]);
            if (Files.isDirectory(p)) {
                try (var s = Files.list(p)) { s.filter(f -> f.toString().endsWith(".png")).forEach(imagens::add); }
            } else imagens.add(p);
        }
        imagens.sort(Comparator.comparing(MontarPdf::ordem));

        Document doc = new Document(new Rectangle(842, 595), 36, 36, 36, 36);   // capa em A4 paisagem
        PdfWriter.getInstance(doc, new FileOutputStream(saida.toFile()));
        doc.addTitle(titulo);
        doc.open();
        Font grande = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Paragraph capa = new Paragraph(titulo, grande);
        capa.setSpacingBefore(160);
        capa.setAlignment(Element.ALIGN_CENTER);
        doc.add(capa);
        Paragraph lista = new Paragraph("\n", normal);
        int n = 1;
        for (Path p : imagens) lista.add(n++ + ". " + nome(p) + "\n");
        doc.add(lista);

        for (Path p : imagens) {
            Image img = Image.getInstance(p.toString());
            float margem = 30, topo = 40;
            doc.setPageSize(new Rectangle(img.plainWidth() + 2 * margem, img.plainHeight() + margem + topo));
            doc.setMargins(margem, margem, 12, margem);
            doc.newPage();
            doc.add(new Paragraph(nome(p), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            img.setAbsolutePosition(margem, margem);
            doc.add(img);
        }
        doc.close();
        System.out.println("PDF: " + saida + " (" + imagens.size() + " diagramas)");
    }

    /** "DC Req 03 ..." e "SD R03 ..." viram "03-1" e "03-2"; a máquina de estados vem antes de tudo. */
    static String ordem(Path p) {
        String n = nome(p);
        var m = java.util.regex.Pattern.compile("^(DC|SD) R(?:eq )?(\\d\\d)").matcher(n);
        if (m.find()) return m.group(2) + "-" + (m.group(1).equals("DC") ? "1" : "2");
        return "00-" + n;
    }

    static String nome(Path p) {
        String f = p.getFileName().toString();
        return f.endsWith(".png") ? f.substring(0, f.length() - 4) : f;
    }
}
