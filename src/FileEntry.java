import java.io.Serializable;

public class FileEntry implements Serializable {
    private static final long serialVersionUID = 1L; // Adicione um serialVersionUID
    private String nome;
    private String conteudo;

    public FileEntry(String nome) {
        this.nome = nome;
        this.conteudo = "";
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
