import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DirectoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nome;
    private DirectoryEntry pai;
    private Map<String, FileEntry> arquivos;
    private Map<String, DirectoryEntry> subDiretorios;

    public DirectoryEntry(String nome) {
        this.nome = nome;
        this.arquivos = new HashMap<>();
        this.subDiretorios = new HashMap<>();
        this.pai = null;
    }

    public DirectoryEntry(String nome, DirectoryEntry pai) {
        this.nome = nome;
        this.arquivos = new HashMap<>();
        this.subDiretorios = new HashMap<>();
        this.pai = pai;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public DirectoryEntry getPai() {
        return pai;
    }

    public void setPai(DirectoryEntry pai) {
        this.pai = pai;
    }

    public void adicionarArquivo(FileEntry arquivo) {
        arquivos.put(arquivo.getNome(), arquivo);
    }

    public void removerArquivo(String nomeArquivo) {
        arquivos.remove(nomeArquivo);
    }

    public FileEntry getArquivo(String nomeArquivo) {
        return arquivos.get(nomeArquivo);
    }

    public void adicionarDiretorio(DirectoryEntry dir) {
        dir.setPai(this);
        subDiretorios.put(dir.getNome(), dir);
    }

    public void removerDiretorio(String nomeDir) {
        subDiretorios.remove(nomeDir);
    }

    public DirectoryEntry getDiretorio(String nomeDir) {
        return subDiretorios.get(nomeDir);
    }

    public Map<String, FileEntry> getArquivos() {
        return arquivos;
    }

    public Map<String, DirectoryEntry> getSubDiretorios() {
        return subDiretorios;
    }
}
