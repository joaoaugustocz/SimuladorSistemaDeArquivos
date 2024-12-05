import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;




public class FileSystemSimulator {
    private DirectoryEntry raiz;
    private List<JournalEntry> journal;
    private boolean isReplaying = false;

    public FileSystemSimulator() {
        this.raiz = new DirectoryEntry("raiz");
        this.journal = new ArrayList<>();
    }

    // Método para executar uma operação
    public void executarOperacao(String operacao, String caminho, String novoNome, boolean registrarNoJournal) {
        // Registrar a operação se necessário
        if (registrarNoJournal) {
            journal.add(new JournalEntry(operacao, caminho, novoNome));
        }
        // Executar a operação
        switch (operacao) {
            case "criarArquivo":
                criarArquivo(caminho);
                break;
            case "apagarArquivo":
                apagarArquivo(caminho);
                break;
            case "renomearArquivo":
                renomearArquivo(caminho, novoNome);
                break;
            case "criarDiretorio":
                criarDiretorio(caminho);
                break;
            case "apagarDiretorio":
                apagarDiretorio(caminho);
                break;
            case "renomearDiretorio":
                renomearDiretorio(caminho, novoNome);
                break;
            case "listarDiretorio":
                listarDiretorio(caminho);
                break;
            default:
                System.out.println("Operação inválida.");
        }
    }

    // Implementações das operações de arquivos e diretórios

    private DirectoryEntry navegarParaDiretorio(String caminho) {
        String[] partes = caminho.split("/");
        DirectoryEntry atual = raiz;
        for (String parte : partes) {
            if (!parte.isEmpty()) {
                atual = atual.getDiretorio(parte);
                if (atual == null) {
                    if (!isReplaying) System.out.println("Diretório não encontrado: " + parte);
                    return null;
                }
            }
        }
        return atual;
    }
    
    public void criarArquivo(String caminhoArquivo) {
        int ultimaBarra = caminhoArquivo.lastIndexOf('/');
        String caminhoDir = caminhoArquivo.substring(0, ultimaBarra);
        String nomeArquivo = caminhoArquivo.substring(ultimaBarra + 1);
    
        DirectoryEntry dir = navegarParaDiretorio(caminhoDir);
        if (dir != null) {
            if (dir.getArquivo(nomeArquivo) == null) {
                dir.adicionarArquivo(new FileEntry(nomeArquivo));
                if (!isReplaying) System.out.println("Arquivo criado: " + caminhoArquivo);
            } else {
                if (!isReplaying) System.out.println("Arquivo já existe: " + nomeArquivo);
            }
        }
    }

    public void apagarArquivo(String caminhoArquivo) {
        int ultimaBarra = caminhoArquivo.lastIndexOf('/');
        String caminhoDir = caminhoArquivo.substring(0, ultimaBarra);
        String nomeArquivo = caminhoArquivo.substring(ultimaBarra + 1);
    
        DirectoryEntry dir = navegarParaDiretorio(caminhoDir);
        if (dir != null) {
            if (dir.getArquivo(nomeArquivo) != null) {
                dir.removerArquivo(nomeArquivo);
                if (!isReplaying) System.out.println("Arquivo apagado: " + caminhoArquivo);
            } else {
                if (!isReplaying) System.out.println("Arquivo não encontrado: " + nomeArquivo);
            }
        }
    }
    
    public void renomearArquivo(String caminhoArquivo, String novoNomeArquivo) {
        int ultimaBarra = caminhoArquivo.lastIndexOf('/');
        String caminhoDir = caminhoArquivo.substring(0, ultimaBarra);
        String nomeArquivo = caminhoArquivo.substring(ultimaBarra + 1);
    
        DirectoryEntry dir = navegarParaDiretorio(caminhoDir);
        if (dir != null) {
            FileEntry arquivo = dir.getArquivo(nomeArquivo);
            if (arquivo != null) {
                dir.removerArquivo(nomeArquivo);
                arquivo.setNome(novoNomeArquivo);
                dir.adicionarArquivo(arquivo);
                if (!isReplaying) System.out.println("Arquivo renomeado para: " + novoNomeArquivo);
            } else {
                if (!isReplaying) System.out.println("Arquivo não encontrado: " + nomeArquivo);
            }
        }
    }

    public void criarDiretorio(String caminhoDir) {
        int ultimaBarra = caminhoDir.lastIndexOf('/');
        String caminhoPai = caminhoDir.substring(0, ultimaBarra);
        String nomeDir = caminhoDir.substring(ultimaBarra + 1);
    
        DirectoryEntry dirPai = navegarParaDiretorio(caminhoPai);
        if (dirPai != null) {
            if (dirPai.getDiretorio(nomeDir) == null) {
                dirPai.adicionarDiretorio(new DirectoryEntry(nomeDir));
                if (!isReplaying) System.out.println("Diretório criado: " + caminhoDir);
            } else {
                if (!isReplaying) System.out.println("Diretório já existe: " + nomeDir);
            }
        }
    }

    public void apagarDiretorio(String caminhoDir) {
        int ultimaBarra = caminhoDir.lastIndexOf('/');
        String caminhoPai = caminhoDir.substring(0, ultimaBarra);
        String nomeDir = caminhoDir.substring(ultimaBarra + 1);
    
        DirectoryEntry dirPai = navegarParaDiretorio(caminhoPai);
        if (dirPai != null) {
            if (dirPai.getDiretorio(nomeDir) != null) {
                dirPai.removerDiretorio(nomeDir);
                if (!isReplaying) System.out.println("Diretório apagado: " + caminhoDir);
            } else {
                if (!isReplaying) System.out.println("Diretório não encontrado: " + nomeDir);
            }
        }
    }

    public void renomearDiretorio(String caminhoDir, String novoNomeDir) {
        int ultimaBarra = caminhoDir.lastIndexOf('/');
        String caminhoPai = caminhoDir.substring(0, ultimaBarra);
        String nomeDir = caminhoDir.substring(ultimaBarra + 1);
    
        DirectoryEntry dirPai = navegarParaDiretorio(caminhoPai);
        if (dirPai != null) {
            DirectoryEntry dir = dirPai.getDiretorio(nomeDir);
            if (dir != null) {
                dirPai.removerDiretorio(nomeDir);
                dir.setNome(novoNomeDir);
                dirPai.adicionarDiretorio(dir);
                if (!isReplaying) System.out.println("Diretório renomeado para: " + novoNomeDir);
            } else {
                if (!isReplaying) System.out.println("Diretório não encontrado: " + nomeDir);
            }
        }
    }

    public void listarDiretorio(String caminhoDir) {
        DirectoryEntry dir = navegarParaDiretorio(caminhoDir);
        if (dir != null) {
            if (!isReplaying) System.out.println("Listando diretório: " + caminhoDir);
            for (String nomeArquivo : dir.getArquivos().keySet()) {
                if (!isReplaying) System.out.println("Arquivo: " + nomeArquivo);
            }
            for (String nomeSubDir : dir.getSubDiretorios().keySet()) {
                if (!isReplaying) System.out.println("Diretório: " + nomeSubDir);
            }
        }
    }
    

    // Método para salvar o journal em um arquivo
    public void salvarJournal() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("journal.dat"))) {
            oos.writeObject(journal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para carregar o journal de um arquivo
    @SuppressWarnings("unchecked")
    public void carregarJournal() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("journal.dat"))) {
            journal = (List<JournalEntry>) ois.readObject();
            // Iniciar modo de reprodução
            isReplaying = true;
            // Reproduzir o journal
            for (JournalEntry entry : journal) {
                executarOperacao(entry.getOperacao(), entry.getCaminhoAlvo(), entry.getNovoNome(), false);
            }
            // Finalizar modo de reprodução
            isReplaying = false;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Nenhum journal anterior encontrado. Iniciando novo.");
        }
    }
}
