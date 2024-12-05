import java.io.Serializable;

public class JournalEntry implements Serializable {
    private static final long serialVersionUID = 1L; // ID de versão da classe

    private String operacao;
    private String caminhoAlvo;
    private String novoNome; // Para operações de renomear

    public JournalEntry(String operacao, String caminhoAlvo) {
        this.operacao = operacao;
        this.caminhoAlvo = caminhoAlvo;
    }

    public JournalEntry(String operacao, String caminhoAlvo, String novoNome) {
        this.operacao = operacao;
        this.caminhoAlvo = caminhoAlvo;
        this.novoNome = novoNome;
    }

    // Métodos getters
    public String getOperacao() { return operacao; }
    public String getCaminhoAlvo() { return caminhoAlvo; }
    public String getNovoNome() { return novoNome; }
}
