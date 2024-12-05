public class FileEntry {
    private String nome;
    private String conteudo;

    public FileEntry(String nome) {
        this.nome = nome;
        this.conteudo = "";
    }

    // Métodos getters e setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}
