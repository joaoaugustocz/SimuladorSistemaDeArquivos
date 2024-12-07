import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.CipherOutputStream;
import javax.crypto.CipherInputStream;
import java.util.Base64;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileSystemSimulator implements Serializable {
    private static final long serialVersionUID = 1L;
    private DirectoryEntry raiz;
    private List<JournalEntry> journal;
    private boolean isReplaying = false;
    private static final String JOURNAL_FILE_NAME = ".journal.dat";
    private static final String ESTADO_ARQUIVO = "filesystem.dat";

    public FileSystemSimulator() {
        // Ao iniciar, tenta carregar o estado completo do arquivo
        if (!carregarEstadoCompleto()) {
            // Se não existe filesystem.dat, cria uma raiz nova e inicializa o journal
            this.raiz = new DirectoryEntry("raiz");
            this.journal = new ArrayList<>();
            inicializarJournal();
        } else {
            // Se carregou com sucesso, journal já deve estar no raiz como .journal.dat
            // Carregar o journal do .journal.dat
            FileEntry journalEntry = raiz.getArquivo(JOURNAL_FILE_NAME);
            if (journalEntry != null) {
                carregarJournalDoArquivo(journalEntry);
            } else {
                // Caso não exista por algum motivo, cria novamente
                this.journal = new ArrayList<>();
                inicializarJournal();
            }
        }
    }

    // Este método já existia, apenas garantindo que nada mude na lógica
    private void inicializarJournal() {
        FileEntry journalEntry = raiz.getArquivo(JOURNAL_FILE_NAME);
        if (journalEntry == null) {
            journalEntry = new FileEntry(JOURNAL_FILE_NAME);
            // Salvando um journal vazio criptografado
            salvarJournalNoArquivo(journalEntry);
            raiz.adicionarArquivo(journalEntry);
        } else {
            carregarJournalDoArquivo(journalEntry);
        }
    }

    // Método para salvar todo o estado (raiz, subdiretórios, arquivos, incluindo .journal.dat)
    public void salvarEstadoCompleto() {
        try {
            // Configurando a criptografia AES
            SecretKey chaveSecreta = CryptoUtils.getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, chaveSecreta);
    
            // Serializando o estado do sistema de arquivos
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(this);
            }
    
            // Criptografando os dados serializados
            byte[] dadosCriptografados = cipher.doFinal(baos.toByteArray());
    
            // Gravando os dados criptografados no arquivo
            try (FileOutputStream fos = new FileOutputStream(ESTADO_ARQUIVO)) {
                fos.write(dadosCriptografados);
            }
    
            // Tornar o arquivo oculto no Windows
            tornarArquivoOculto();
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    // Método para carregar todo o estado do arquivo filesystem.dat
    private boolean carregarEstadoCompleto() {
        File f = new File(ESTADO_ARQUIVO);
        if (!f.exists()) {
            return false;
        }
        try {
            // Configurando a descriptografia AES
            SecretKey chaveSecreta = CryptoUtils.getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, chaveSecreta);
    
            // Lendo os dados criptografados do arquivo
            byte[] dadosCriptografados = new byte[(int) f.length()];
            try (FileInputStream fis = new FileInputStream(f)) {
                fis.read(dadosCriptografados);
            }
    
            // Descriptografando os dados
            byte[] dadosSerializados = cipher.doFinal(dadosCriptografados);
    
            // Desserializando o estado do sistema de arquivos
            ByteArrayInputStream bais = new ByteArrayInputStream(dadosSerializados);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                FileSystemSimulator fsCarregado = (FileSystemSimulator) ois.readObject();
                this.raiz = fsCarregado.raiz;
                this.journal = fsCarregado.journal;
                this.isReplaying = false;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void tornarArquivoOculto() {
        try {
            File arquivo = new File(ESTADO_ARQUIVO);
            if (arquivo.exists()) {
                // Comando do Windows para marcar um arquivo como oculto
                Process process = Runtime.getRuntime().exec("attrib +H " + arquivo.getAbsolutePath());
                process.waitFor(); // Aguarda a execução do comando
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void salvarJournal() {
        try {
            // Criptografa e salva o journal no FileEntry .journal.dat
            SecretKey chaveSecreta = CryptoUtils.getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, chaveSecreta);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (CipherOutputStream cos = new CipherOutputStream(baos, cipher);
                 ObjectOutputStream oos = new ObjectOutputStream(cos)) {
                oos.writeObject(journal);
            }
            byte[] criptografado = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(criptografado);

            FileEntry journalEntry = raiz.getArquivo(JOURNAL_FILE_NAME);
            if (journalEntry == null) {
                journalEntry = new FileEntry(JOURNAL_FILE_NAME);
                raiz.adicionarArquivo(journalEntry);
            }
            journalEntry.setConteudo(base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarJournalDoArquivo(FileEntry journalEntry) {
        try {
            SecretKey chaveSecreta = CryptoUtils.getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, chaveSecreta);

            String base64 = journalEntry.getConteudo();
            byte[] criptografado = Base64.getDecoder().decode(base64);

            ByteArrayInputStream bais = new ByteArrayInputStream(criptografado);
            try (CipherInputStream cis = new CipherInputStream(bais, cipher);
                 ObjectInputStream ois = new ObjectInputStream(cis)) {
                journal = (List<JournalEntry>) ois.readObject();
            }

            // Reproduzir o journal
            isReplaying = true;
            for (JournalEntry entry : journal) {
                executarOperacao(entry.getOperacao(), entry.getCaminhoAlvo(), entry.getNovoNome());
            }
            isReplaying = false;

        } catch (Exception e) {
            e.printStackTrace();
            // Se der erro, inicia um journal vazio
            journal = new ArrayList<>();
        }
    }

    private void salvarJournalNoArquivo(FileEntry journalEntry) {
        // Semelhante ao salvarJournal(), mas usado ao inicializar para criar um journal vazio
        try {
            SecretKey chaveSecreta = CryptoUtils.getSecretKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, chaveSecreta);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (CipherOutputStream cos = new CipherOutputStream(baos, cipher);
                 ObjectOutputStream oos = new ObjectOutputStream(cos)) {
                oos.writeObject(journal);
            }
            byte[] criptografado = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(criptografado);

            journalEntry.setConteudo(base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Executar uma operação
    public void executarOperacao(String operacao, String caminho, String novoNome) {
        // Registrar a operação somente se não estiver reproduzindo o journal
        if (!isReplaying && !operacao.equals("help") && !operacao.equals("listar")) {
            journal.add(new JournalEntry(operacao, caminho, novoNome));
            salvarJournal();
        }

        // Executar a operação
        switch (operacao.toLowerCase()) {
            case "criararquivo":
                criarArquivo(caminho);
                break;
            case "apagararquivo":
                apagarArquivo(caminho);
                break;
            case "renomeararquivo":
                renomearArquivo(caminho, novoNome);
                break;
            case "criardiretorio":
                criarDiretorio(caminho);
                break;
            case "apagardiretorio":
                apagarDiretorio(caminho);
                break;
            case "renomeardiretorio":
                renomearDiretorio(caminho, novoNome);
                break;
            case "listardiretorio":
                listarDiretorio(caminho);
                break;
            case "listar":
                // Este comando será tratado no MainApplication
                break;
            case "help":
                // Este comando será tratado no MainApplication
                break;
            default:
                if (!isReplaying) {
                    System.out.println("Operação inválida. Digite 'help' para ver a lista de comandos.");
                }
        }
    }


    public DirectoryEntry getRaiz() {
        return raiz;
    }

    // Criar um arquivo
    public void criarArquivo(String caminhoArquivo) {
        DirectoryEntry dir = navegarParaDiretorio(caminhoArquivo, true, raiz);
        if (dir != null) {
            String nomeArquivo = extrairNome(caminhoArquivo);
            if (dir.getArquivo(nomeArquivo) == null) {
                dir.adicionarArquivo(new FileEntry(nomeArquivo));
                if (!isReplaying) {
                    System.out.println("Arquivo criado: " + caminhoArquivo);
                }
            } else {
                if (!isReplaying) {
                    System.out.println("Arquivo já existe: " + nomeArquivo);
                }
            }
        }
    }

    // Apagar um arquivo
    public void apagarArquivo(String caminhoArquivo) {
        DirectoryEntry dir = navegarParaDiretorio(caminhoArquivo, false, raiz);
        if (dir != null) {
            String nomeArquivo = extrairNome(caminhoArquivo);
            if (dir.getArquivo(nomeArquivo) != null) {
                dir.removerArquivo(nomeArquivo);
                if (!isReplaying) {
                    System.out.println("Arquivo apagado: " + caminhoArquivo);
                }
            } else {
                if (!isReplaying) {
                    System.out.println("Arquivo não encontrado: " + nomeArquivo);
                }
            }
        }
    }

    // Renomear um arquivo
    public void renomearArquivo(String caminhoArquivo, String novoNomeArquivo) {
        DirectoryEntry dir = navegarParaDiretorio(caminhoArquivo, false, raiz);
        if (dir != null) {
            String nomeArquivo = extrairNome(caminhoArquivo);
            FileEntry arquivo = dir.getArquivo(nomeArquivo);
            if (arquivo != null) {
                dir.removerArquivo(nomeArquivo);
                arquivo.setNome(novoNomeArquivo);
                dir.adicionarArquivo(arquivo);
                if (!isReplaying) {
                    System.out.println("Arquivo renomeado para: " + novoNomeArquivo);
                }
            } else {
                if (!isReplaying) {
                    System.out.println("Arquivo não encontrado: " + nomeArquivo);
                }
            }
        }
    }

    // Criar um diretório
    public void criarDiretorio(String caminhoDir) {
        DirectoryEntry dirPai = navegarParaDiretorio(caminhoDir, true, raiz);
        if (dirPai != null) {
            String nomeDir = extrairNome(caminhoDir);
            if (dirPai.getDiretorio(nomeDir) == null) {
                dirPai.adicionarDiretorio(new DirectoryEntry(nomeDir, dirPai));
                if (!isReplaying) {
                    System.out.println("Diretório criado: " + caminhoDir);
                }
            } else {
                if (!isReplaying) {
                    System.out.println("Diretório já existe: " + nomeDir);
                }
            }
        }
    }

    // Apagar um diretório
    public void apagarDiretorio(String caminhoDir) {
        DirectoryEntry dirPai = navegarParaDiretorio(caminhoDir, false, raiz);
        if (dirPai != null) {
            String nomeDir = extrairNome(caminhoDir);
            if (dirPai.getDiretorio(nomeDir) != null) {
                dirPai.removerDiretorio(nomeDir);
                if (!isReplaying) {
                    System.out.println("Diretório apagado: " + caminhoDir);
                }
            } else {
                if (!isReplaying) {
                    System.out.println("Diretório não encontrado: " + nomeDir);
                }
            }
        }
    }

    // Renomear um diretório
    public void renomearDiretorio(String caminhoDir, String novoNomeDir) {
        DirectoryEntry dirPai = navegarParaDiretorio(caminhoDir, false, raiz);
        if (dirPai != null) {
            String nomeDir = extrairNome(caminhoDir);
            DirectoryEntry dir = dirPai.getDiretorio(nomeDir);
            if (dir != null) {
                dirPai.removerDiretorio(nomeDir);
                dir.setNome(novoNomeDir);
                dirPai.adicionarDiretorio(dir);
                if (!isReplaying) {
                    System.out.println("Diretório renomeado para: " + novoNomeDir);
                }
            } else {
                if (!isReplaying) {
                    System.out.println("Diretório não encontrado: " + nomeDir);
                }
            }
        }
    }

    // Listar diretório específico
    public void listarDiretorio(String caminhoDir) {
        DirectoryEntry dir = navegarParaDiretorio(caminhoDir, false, raiz);
        if (dir != null) {
            System.out.println("Listando diretório: " + caminhoDir);
            for (String nomeArquivo : dir.getArquivos().keySet()) {
                if (!nomeArquivo.startsWith(".")) { // Ocultar arquivos que começam com ponto
                    System.out.println("Arquivo: " + nomeArquivo);
                }
            }
            for (String nomeSubDir : dir.getSubDiretorios().keySet()) {
                System.out.println("Diretório: " + nomeSubDir);
            }
        }
    }

    // // Listar diretório atual
    // public void listarDiretorioAtual(DirectoryEntry atualDir) {
    //     System.out.println("Listando diretório: " + obterCaminho(atualDir));
    //     for (String nomeArquivo : atualDir.getArquivos().keySet()) {
    //         if (!nomeArquivo.startsWith(".")) { // Ocultar arquivos que começam com ponto
    //             System.out.println("Arquivo: " + nomeArquivo);
    //         }
    //     }
    //     for (String nomeSubDir : atualDir.getSubDiretorios().keySet()) {
    //         System.out.println("Diretório: " + nomeSubDir);
    //     }
    // }

    // Exibir ajuda
    public void exibirAjuda() {
        System.out.println("Comandos disponíveis:");
        System.out.println("criarArquivo <caminho> - Cria um novo arquivo no caminho especificado.");
        System.out.println("apagarArquivo <caminho> - Remove o arquivo especificado.");
        System.out.println("renomearArquivo <caminho> <novoNome> - Renomeia o arquivo especificado.");
        System.out.println("criarDiretorio <caminho> - Cria um novo diretório no caminho especificado.");
        System.out.println("apagarDiretorio <caminho> - Remove o diretório especificado.");
        System.out.println("renomearDiretorio <caminho> <novoNome> - Renomeia o diretório especificado.");
        System.out.println("listar - Lista todos os arquivos e diretórios no diretório atual.");
        System.out.println("help - Exibe esta ajuda.");
        System.out.println("sair - Encerra o simulador e salva o estado.");
        System.out.println("cd <caminho> - Muda para o diretório especificado.");
    }

    // Navegar para diretório com base em um diretório base
    public DirectoryEntry navegarParaDiretorio(String caminho, boolean criarCasoNaoExistir, DirectoryEntry baseDirectory) {
        DirectoryEntry atual;
        if (caminho.startsWith("/")) {
            atual = raiz;
            caminho = caminho.substring(1); // Remove a barra inicial
        } else {
            atual = baseDirectory;
        }

        String[] partes = caminho.split("/");
        for (String parte : partes) {
            if (parte.isEmpty() || parte.equals(".")) {
                continue;
            } else if (parte.equals("..")) {
                if (atual.getPai() != null) {
                    atual = atual.getPai();
                }
                // Se já estiver na raiz, permanece lá
            } else {
                DirectoryEntry proximo = atual.getDiretorio(parte);
                if (proximo == null) {
                    if (criarCasoNaoExistir) {
                        proximo = new DirectoryEntry(parte, atual);
                        atual.adicionarDiretorio(proximo);
                    } else {
                        if (!isReplaying) {
                            System.out.println("Diretório não encontrado: " + parte);
                        }
                        return null;
                    }
                }
                atual = proximo;
            }
        }
        return atual;
    }

    // Extrair nome do arquivo ou diretório a partir do caminho
    private String extrairNome(String caminho) {
        int ultimaBarra = caminho.lastIndexOf('/');
        if (ultimaBarra == -1 || ultimaBarra == caminho.length() - 1) {
            return caminho;
        }
        return caminho.substring(ultimaBarra + 1);
    }

    // Obter o caminho completo de um diretório como string
    public String obterCaminho(DirectoryEntry dir) {
        if (dir == null) {
            return "/";
        }
        StringBuilder caminho = new StringBuilder();
        DirectoryEntry atual = dir;
        while (atual != null && atual.getPai() != null) { // root's parent is null
            caminho.insert(0, "/" + atual.getNome());
            atual = atual.getPai();
        }
        if (caminho.length() == 0) {
            return "/";
        }
        return caminho.toString();
    }

    // // Salvar o journal
    // public void salvarJournal() {
    //     try {
    //         SecretKey chaveSecreta = CryptoUtils.getSecretKey();
    //         Cipher cipher = Cipher.getInstance("AES");
    //         cipher.init(Cipher.ENCRYPT_MODE, chaveSecreta);

    //         ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //         try (CipherOutputStream cos = new CipherOutputStream(baos, cipher);
    //              ObjectOutputStream oos = new ObjectOutputStream(cos)) {

    //             oos.writeObject(journal);
    //         }

    //         byte[] dadosCriptografados = baos.toByteArray();
    //         String dadosCodificados = Base64.getEncoder().encodeToString(dadosCriptografados);

    //         FileEntry journalEntry = raiz.getArquivo(JOURNAL_FILE_NAME);
    //         if (journalEntry != null) {
    //             journalEntry.setConteudo(dadosCodificados);
    //         } else {
    //             journalEntry = new FileEntry(JOURNAL_FILE_NAME);
    //             journalEntry.setConteudo(dadosCodificados);
    //             raiz.adicionarArquivo(journalEntry);
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // // Salvar o journal no FileEntry (usado na inicialização)
    // private void salvarJournalNoArquivo(FileEntry journalEntry) {
    //     try {
    //         SecretKey chaveSecreta = CryptoUtils.getSecretKey();
    //         Cipher cipher = Cipher.getInstance("AES");
    //         cipher.init(Cipher.ENCRYPT_MODE, chaveSecreta);

    //         ByteArrayOutputStream baos = new ByteArrayOutputStream();
    //         try (CipherOutputStream cos = new CipherOutputStream(baos, cipher);
    //              ObjectOutputStream oos = new ObjectOutputStream(cos)) {

    //             oos.writeObject(journal);
    //         }

    //         byte[] dadosCriptografados = baos.toByteArray();
    //         String dadosCodificados = Base64.getEncoder().encodeToString(dadosCriptografados);

    //         journalEntry.setConteudo(dadosCodificados);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // // Carregar o journal do FileEntry
    // private void carregarJournalDoArquivo(FileEntry journalEntry) {
    //     try {
    //         SecretKey chaveSecreta = CryptoUtils.getSecretKey();
    //         Cipher cipher = Cipher.getInstance("AES");
    //         cipher.init(Cipher.DECRYPT_MODE, chaveSecreta);

    //         String dadosCodificados = journalEntry.getConteudo();
    //         byte[] dadosCriptografados = Base64.getDecoder().decode(dadosCodificados);

    //         ByteArrayInputStream bais = new ByteArrayInputStream(dadosCriptografados);
    //         try (CipherInputStream cis = new CipherInputStream(bais, cipher);
    //              ObjectInputStream ois = new ObjectInputStream(cis)) {

    //             journal = (List<JournalEntry>) ois.readObject();
    //         }

    //         // Reproduzir o journal
    //         isReplaying = true;
    //         for (JournalEntry entry : journal) {
    //             executarOperacao(entry.getOperacao(), entry.getCaminhoAlvo(), entry.getNovoNome());
    //         }
    //         isReplaying = false;
    //     } catch (FileNotFoundException e) {
    //         if (!isReplaying) {
    //             System.out.println("Nenhum journal anterior encontrado. Iniciando novo.");
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // Método para listar diretório atual
    public void listarDiretorioAtual(DirectoryEntry atualDir) {
        System.out.println("Listando diretório: " + obterCaminho(atualDir));
        for (String nomeArquivo : atualDir.getArquivos().keySet()) {
            if (!nomeArquivo.startsWith(".")) { // Ocultar arquivos que começam com ponto
                System.out.println("Arquivo: " + nomeArquivo);
            }
        }
        for (String nomeSubDir : atualDir.getSubDiretorios().keySet()) {
            System.out.println("Diretório: " + nomeSubDir);
        }
    }
}
