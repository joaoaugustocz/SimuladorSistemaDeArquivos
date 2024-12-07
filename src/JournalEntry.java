import java.io.Serializable;

public class JournalEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String operacao;
    private String caminhoAlvo;
    private String novoNome;

    public JournalEntry(String operacao, String caminhoAlvo) {
        this.operacao = operacao;
        this.caminhoAlvo = caminhoAlvo;
    }

    public JournalEntry(String operacao, String caminhoAlvo, String novoNome) {
        this.operacao = operacao;
        this.caminhoAlvo = caminhoAlvo;
        this.novoNome = novoNome;
    }

    public String getOperacao() {
        return operacao;
    }

    public String getCaminhoAlvo() {
        return caminhoAlvo;
    }

    public String getNovoNome() {
        return novoNome;
    }
}
