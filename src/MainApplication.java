import java.util.Scanner;

public class MainApplication {
    public static void main(String[] args) {
        FileSystemSimulator fs = new FileSystemSimulator();
        Scanner scanner = new Scanner(System.in);
        String input;

        DirectoryEntry currentDirectory = fs.getRaiz();

        System.out.println("Bem-vindo ao Simulador de Sistema de Arquivos!");
        while (true) {
            System.out.print(fs.obterCaminho(currentDirectory) + " > ");
            input = scanner.nextLine();
            String[] partesComando = input.trim().split("\\s+");

            if (partesComando.length == 0 || partesComando[0].isEmpty()) {
                continue;
            }

            String operacao = partesComando[0].toLowerCase();
            String caminho = partesComando.length > 1 ? partesComando[1] : null;
            String novoNome = partesComando.length > 2 ? partesComando[2] : null;

            if (operacao.equals("sair")) {
                fs.salvarJournal();         // Atualiza o .journal.dat com as operações mais recentes
                fs.salvarEstadoCompleto();  // Salva todo o estado (incluindo .journal.dat e todos os arquivos) em filesystem.dat
                System.out.println("Simulador encerrado.");
                break;
            } else if (operacao.equals("cd")) {
                if (caminho != null) {
                    DirectoryEntry novaDir = fs.navegarParaDiretorio(caminho, false, currentDirectory);
                    if (novaDir != null) {
                        currentDirectory = novaDir;
                    } else {
                        System.out.println("Diretório não encontrado: " + caminho);
                    }
                } else {
                    System.out.println("Uso: cd <caminho>");
                }
            } else {
                if (operacao.equals("listar")) {
                    fs.listarDiretorioAtual(currentDirectory);
                } else if (operacao.equals("help")) {
                    fs.exibirAjuda();
                } else {
                    // Para operações que requerem caminho, ajustar para caminho absoluto
                    if (caminho != null && !caminho.startsWith("/")) {
                        // Construir caminho absoluto com base no diretório atual
                        String caminhoAbsoluto = fs.obterCaminho(currentDirectory) + "/" + caminho;
                        fs.executarOperacao(operacao, caminhoAbsoluto, novoNome);
                    } else {
                        fs.executarOperacao(operacao, caminho, novoNome);
                    }
                }
            }
        }
        scanner.close();
    }
}
