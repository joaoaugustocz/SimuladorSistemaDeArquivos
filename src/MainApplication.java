import java.util.Scanner;

public class MainApplication {
    public static void main(String[] args) {
        FileSystemSimulator fs = new FileSystemSimulator();
        fs.carregarJournal(); // Carrega o journal na inicialização
        Scanner scanner = new Scanner(System.in);
        String input;

        System.out.println("Bem-vindo ao Simulador de Sistema de Arquivos!");
        while (true) {
            System.out.print("> ");
            input = scanner.nextLine();
            String[] partesComando = input.split(" ");

            if (partesComando[0].equalsIgnoreCase("sair")) {
                break;
            }

            String operacao = partesComando[0];
            String caminho = partesComando.length > 1 ? partesComando[1] : null;
            String novoNome = partesComando.length > 2 ? partesComando[2] : null;

            fs.executarOperacao(operacao, caminho, novoNome, true);
        }
        fs.salvarJournal(); // Salva o journal ao sair
        scanner.close();
    }
}
