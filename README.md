# Metodologia

O simulador foi desenvolvido em linguagem de programação Java. Ele recebe chamadas de métodos com os devidos parâmetros e implementa os métodos correspondentes aos comandos de um sistema operacional. O programa executa cada funcionalidade e exibe o resultado na tela quando necessário.

# Parte 1: Introdução ao Sistema de Arquivos com Journaling

## Descrição do Sistema de Arquivos

Um sistema de arquivos é uma estrutura lógica que o sistema operacional utiliza para controlar como os dados são armazenados e recuperados. Sem um sistema de arquivos, as informações colocadas em um meio de armazenamento seriam um grande corpo de dados sem forma, sem maneira de saber onde uma informação termina e a próxima começa.

Os sistemas de arquivos são essenciais para a organização, armazenamento e recuperação eficiente de dados. Eles permitem que os usuários criem, modifiquem e excluam arquivos e diretórios, fornecendo uma maneira estruturada de gerenciar informações em dispositivos de armazenamento.

## Journaling

O journaling é uma técnica usada em sistemas de arquivos para manter a integridade dos dados. Ele registra um log (journal) de alterações que serão feitas no sistema de arquivos antes que as alterações reais sejam aplicadas. Isso é crucial para a recuperação em caso de falhas, como interrupções de energia ou travamentos do sistema.

Existem diferentes tipos de journaling:

- **Write-Ahead Logging**: Registra todas as alterações planejadas antes de executá-las. Se ocorrer uma falha, o sistema pode ler o journal e aplicar ou reverter as alterações pendentes.
- **Log-Structured File Systems**: Escreve todas as alterações sequencialmente em um log contínuo, o que pode melhorar o desempenho em certos cenários.

O uso de journaling ajuda a prevenir corrupção de dados e garante que o sistema de arquivos possa ser recuperado para um estado consistente após uma falha.

# Parte 2: Arquitetura do Simulador

## Estrutura de Dados

Para representar o sistema de arquivos, utilizamos as seguintes classes em Java:

- **`FileEntry`**: Representa um arquivo com nome e conteúdo.
- **`DirectoryEntry`**: Representa um diretório que pode conter arquivos e subdiretórios.
- **`JournalEntry`**: Registra as operações realizadas no sistema de arquivos para o journaling.
- **`FileSystemSimulator`**: Gerencia as operações no sistema de arquivos e mantém o journal.

### Descrição das Classes

1. **FileEntry**

```java
public class FileEntry {
    private String nome;
    private String conteudo;

    public FileEntry(String nome) {
        this.nome = nome;
        this.conteudo = "";
    }

    // Métodos getters e setters
}
```

- Representa um arquivo simples com um nome e conteúdo.
- Os métodos getters e setters permitem o acesso e modificação dos atributos.

2. **DirectoryEntry**

```java
import java.util.HashMap;
import java.util.Map;

public class DirectoryEntry {
    private String nome;
    private Map<String, FileEntry> arquivos;
    private Map<String, DirectoryEntry> subDiretorios;

    public DirectoryEntry(String nome) {
        this.nome = nome;
        this.arquivos = new HashMap<>();
        this.subDiretorios = new HashMap<>();
    }

    // Métodos para adicionar, remover e obter arquivos e diretórios
}
```

- Representa um diretório que contém arquivos e outros diretórios.
- Utiliza mapas (`HashMap`) para armazenar arquivos e subdiretórios, permitindo acesso rápido.

3. **JournalEntry**

```java
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

    // Métodos getters
}
```

- Registra uma operação realizada no sistema de arquivos.
- Implementa `Serializable` para permitir a serialização e armazenamento em disco.

## Journaling

O journaling é implementado através da classe `JournalEntry` e de uma lista de journals no `FileSystemSimulator`. Antes de cada operação, uma entrada é adicionada ao journal. Em caso de falha, o sistema pode ler o journal e reproduzir as operações para restaurar o estado consistente do sistema de arquivos.

### Estrutura do Log

- O log é uma lista de objetos `JournalEntry`.
- Cada `JournalEntry` contém:
  - **Operação**: Tipo de operação realizada (e.g., criar, apagar, renomear).
  - **Caminho Alvo**: O caminho do arquivo ou diretório alvo da operação.
  - **Novo Nome**: O novo nome em casos de renomeação.

# Parte 3: Implementação em Java

## Classe "FileSystemSimulator"

A classe `FileSystemSimulator` é responsável por gerenciar o sistema de arquivos e as operações realizadas nele.

### Principais Componentes

1. **Atributos**

```java
public class FileSystemSimulator {
    private DirectoryEntry raiz;
    private List<JournalEntry> journal;
    private boolean isReplaying = false;

    public FileSystemSimulator() {
        this.raiz = new DirectoryEntry("raiz");
        this.journal = new ArrayList<>();
    }

    // Métodos de operação
}
```

- **`raiz`**: O diretório raiz do sistema de arquivos.
- **`journal`**: Lista de entradas de journal para o journaling.
- **`isReplaying`**: Indica se o sistema está reproduzindo o journal durante a inicialização.

2. **Método `executarOperacao`**

```java
public void executarOperacao(String operacao, String caminho, String novoNome) {
    if (!isReplaying) {
        journal.add(new JournalEntry(operacao, caminho, novoNome));
    }

    switch (operacao) {
        case "criarArquivo":
            criarArquivo(caminho);
            break;
        // Outras operações
    }
}
```

- Registra a operação no journal se não estiver em modo de reprodução.
- Executa a operação correspondente.

3. **Métodos para Operações**

Implementa métodos como `criarArquivo`, `apagarArquivo`, `renomearArquivo`, `criarDiretorio`, etc.

Exemplo do método `criarArquivo`:

```java
public void criarArquivo(String caminhoArquivo) {
    // Lógica para criar um arquivo
    if (!isReplaying) {
        System.out.println("Arquivo criado: " + caminhoArquivo);
    }
}
```

- Inclui verificações para evitar imprimir mensagens durante a reprodução do journal.

## Classes File e Directory

### Classe `FileEntry`

- Representa arquivos individuais no sistema de arquivos.
- Armazena o nome e o conteúdo do arquivo.

### Classe `DirectoryEntry`

- Representa diretórios que podem conter arquivos e subdiretórios.
- Utiliza mapas para armazenar referências a arquivos (`FileEntry`) e diretórios (`DirectoryEntry`).

## Classe Journal

O gerenciamento do journal é incorporado na classe `FileSystemSimulator`. O journal é salvo e carregado do disco, permitindo a persistência do estado do sistema de arquivos.

### Métodos Principais

1. **Salvar Journal**

```java
public void salvarJournal() {
    // Código para criptografar e salvar o journal
    ocultarArquivo("journal.dat");
}
```

- Salva o journal em um arquivo criptografado.
- Torna o arquivo oculto no sistema operacional.

2. **Carregar Journal**

```java
public void carregarJournal() {
    // Código para carregar e descriptografar o journal
    isReplaying = true;
    // Reproduzir as operações do journal
    isReplaying = false;
}
```

- Carrega o journal do disco e reproduz as operações para restaurar o estado do sistema de arquivos.

# Considerações Finais

O simulador de sistema de arquivos com journaling implementado em Java fornece uma compreensão prática de como sistemas operacionais gerenciam arquivos e diretórios, além de ilustrar a importância do journaling para a integridade dos dados.

A estrutura modular do código, com classes separadas para arquivos, diretórios e gerenciamento do sistema de arquivos, facilita a manutenção e a expansão do simulador. A implementação do journaling demonstra como operações podem ser registradas e reproduzidas para recuperar o estado após falhas.

Este projeto serve como uma ferramenta educacional para aprofundar o entendimento sobre sistemas de arquivos e mecanismos de proteção de dados em sistemas operacionais.