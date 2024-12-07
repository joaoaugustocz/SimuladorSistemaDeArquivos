### Alunos:
### João Augusto Carvalho Araújo - 2214590
### João Pedro Monteiro

Este projeto implementa um simulador de sistema de arquivos em Java com suporte a journaling. O objetivo é compreender a organização e o gerenciamento de arquivos, diretórios e o uso do journaling para manter a integridade do sistema após falhas. Além disso, o estado completo do sistema (incluindo o journal) é criptografado e armazenado em um único arquivo (filesystem.dat), que é ocultado no sistema operacional Windows.


# Metodologia

O simulador é desenvolvido em Java. Ele executa comandos de criação, remoção, renomeação e listagem de arquivos e diretórios, bem como comandos auxiliares de navegação e ajuda. Cada operação é registrada em um journal interno (.journal.dat), que faz parte do próprio sistema de arquivos simulado.

Ao finalizar o simulador com o comando sair, o estado completo do sistema (incluindo a raiz, diretórios, arquivos e o .journal.dat) é serializado, criptografado e gravado em um arquivo chamado filesystem.dat. Esse arquivo é então marcado como oculto no Windows.

Ao iniciar o simulador novamente, o filesystem.dat é carregado, descriptografado, restaurando o estado exato do sistema de arquivos e o journal. Assim, a estrutura e o histórico de operações permanecem consistentes entre execuções.

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
```

# Tutorial de Utilização do Sistema de Arquivos

Este tutorial fornece instruções detalhadas sobre como utilizar o simulador de sistema de arquivos. São apresentadas todas as operações disponíveis, acompanhadas de explicações e exemplos práticos para facilitar o entendimento do funcionamento do sistema.

## Comandos Disponíveis

1. **criarArquivo \<caminho\>**
   - **Descrição:** Cria um novo arquivo no caminho especificado.
   - **Exemplo:**
     ```
     criarArquivo /documentos/relatorio.txt
     ```
     Este comando cria um arquivo chamado `relatorio.txt` dentro do diretório `/documentos`.

2. **apagarArquivo \<caminho\>**
   - **Descrição:** Remove o arquivo especificado do sistema de arquivos.
   - **Exemplo:**
     ```
     apagarArquivo /documentos/relatorio.txt
     ```
     Este comando apaga o arquivo `relatorio.txt` do diretório `/documentos`.

3. **renomearArquivo \<caminho\> \<novoNome\>**
   - **Descrição:** Renomeia o arquivo especificado para um novo nome.
   - **Exemplo:**
     ```
     renomearArquivo /documentos/relatorio.txt relatorio_final.txt
     ```
     Este comando renomeia o arquivo `relatorio.txt` para `relatorio_final.txt` dentro do diretório `/documentos`.

4. **criarDiretorio \<caminho\>**
   - **Descrição:** Cria um novo diretório no caminho especificado.
   - **Exemplo:**
     ```
     criarDiretorio /fotos/viagem
     ```
     Este comando cria o diretório `viagem` dentro de `/fotos`.

5. **apagarDiretorio \<caminho\>**
   - **Descrição:** Remove o diretório especificado do sistema de arquivos.
   - **Exemplo:**
     ```
     apagarDiretorio /fotos/viagem
     ```
     Este comando apaga o diretório `viagem` dentro de `/fotos`.

6. **renomearDiretorio \<caminho\> \<novoNome\>**
   - **Descrição:** Renomeia o diretório especificado para um novo nome.
   - **Exemplo:**
     ```
     renomearDiretorio /fotos/viagem viagem2022
     ```
     Este comando renomeia o diretório `viagem` para `viagem2022` dentro de `/fotos`.

7. **listarDiretorio \<caminho\>**
   - **Descrição:** Lista todos os arquivos e subdiretórios presentes no diretório especificado.
   - **Exemplo:**
     ```
     listarDiretorio /documentos
     ```
     Este comando exibe todos os conteúdos dentro do diretório `/documentos`.

8. **sair**
   - **Descrição:** Encerra o simulador e salva o estado atual do sistema de arquivos.
   - **Exemplo:**
     ```
     sair
     ```

## Exemplos Práticos

### 1. Criando Estruturas de Diretórios e Arquivos

**Passo a passo:**

1. **Criar um diretório chamado `projetos` na raiz:**

   ```
   criarDiretorio /projetos
   ```

2. **Dentro de `projetos`, criar um arquivo chamado `projeto1.txt`:**

   ```
   criarArquivo /projetos/projeto1.txt
   ```

3. **Criar um subdiretório chamado `codigo` dentro de `projetos`:**

   ```
   criarDiretorio /projetos/codigo
   ```

4. **Adicionar um arquivo `main.java` dentro de `/projetos/codigo`:**

   ```
   criarArquivo /projetos/codigo/main.java
   ```

5. **Listar o conteúdo do diretório `/projetos`:**

   ```
   listarDiretorio /projetos
   ```

   **Saída esperada:**

   ```
   Listando diretório: /projetos
   Arquivo: projeto1.txt
   Diretório: codigo
   ```

6. **Listar o conteúdo do diretório `/projetos/codigo`:**

   ```
   listarDiretorio /projetos/codigo
   ```

   **Saída esperada:**

   ```
   Listando diretório: /projetos/codigo
   Arquivo: main.java
   ```

### 2. Renomeando Arquivos e Diretórios

1. **Renomear o arquivo `projeto1.txt` para `projeto_final.txt`:**

   ```
   renomearArquivo /projetos/projeto1.txt projeto_final.txt
   ```

2. **Renomear o diretório `codigo` para `fonte`:**

   ```
   renomearDiretorio /projetos/codigo fonte
   ```

3. **Listar o conteúdo atualizado do diretório `/projetos`:**

   ```
   listarDiretorio /projetos
   ```

   **Saída esperada:**

   ```
   Listando diretório: /projetos
   Arquivo: projeto_final.txt
   Diretório: fonte
   ```

### 3. Apagando Arquivos e Diretórios

1. **Apagar o arquivo `main.java` dentro de `/projetos/fonte`:**

   ```
   apagarArquivo /projetos/fonte/main.java
   ```

2. **Apagar o diretório `fonte`:**

   ```
   apagarDiretorio /projetos/fonte
   ```

3. **Listar o conteúdo atualizado do diretório `/projetos`:**

   ```
   listarDiretorio /projetos
   ```

   **Saída esperada:**

   ```
   Listando diretório: /projetos
   Arquivo: projeto_final.txt
   ```

### 4. Exemplo Completo de Uso

**Cenário:** Organizando documentos pessoais.

1. **Criar diretório para documentos pessoais:**

   ```
   criarDiretorio /usuarios/joao/documentos
   ```

2. **Adicionar arquivos de texto:**

   ```
   criarArquivo /usuarios/joao/documentos/curriculo.docx
   criarArquivo /usuarios/joao/documentos/carta_de_apresentacao.docx
   ```

3. **Criar subdiretório para fotos:**

   ```
   criarDiretorio /usuarios/joao/documentos/fotos
   ```

4. **Adicionar arquivos de imagem:**

   ```
   criarArquivo /usuarios/joao/documentos/fotos/foto1.jpg
   criarArquivo /usuarios/joao/documentos/fotos/foto2.jpg
   ```

5. **Listar o conteúdo do diretório de documentos:**

   ```
   listarDiretorio /usuarios/joao/documentos
   ```

   **Saída esperada:**

   ```
   Listando diretório: /usuarios/joao/documentos
   Arquivo: curriculo.docx
   Arquivo: carta_de_apresentacao.docx
   Diretório: fotos
   ```

6. **Renomear o diretório `fotos` para `imagens`:**

   ```
   renomearDiretorio /usuarios/joao/documentos/fotos imagens
   ```

7. **Listar o conteúdo atualizado:**

   ```
   listarDiretorio /usuarios/joao/documentos
   ```

   **Saída esperada:**

   ```
   Listando diretório: /usuarios/joao/documentos
   Arquivo: curriculo.docx
   Arquivo: carta_de_apresentacao.docx
   Diretório: imagens
   ```

8. **Apagar o arquivo `carta_de_apresentacao.docx`:**

   ```
   apagarArquivo /usuarios/joao/documentos/carta_de_apresentacao.docx
   ```

9. **Listar o conteúdo final:**

   ```
   listarDiretorio /usuarios/joao/documentos
   ```

   **Saída esperada:**

   ```
   Listando diretório: /usuarios/joao/documentos
   Arquivo: curriculo.docx
   Diretório: imagens
   ```

### 5. Salvando e Carregando o Estado do Sistema de Arquivos

O simulador utiliza um mecanismo de journaling para salvar o estado do sistema de arquivos. Ao encerrar o simulador com o comando `sair`, o estado atual é salvo, e ao iniciar o simulador novamente, o estado é restaurado automaticamente.

**Passos:**

1. **Encerrar o simulador:**

   ```
   sair
   ```

2. **Iniciar o simulador novamente. O estado anterior será restaurado automaticamente.**

3. **Verificar o estado restaurado:**

   ```
   listarDiretorio /usuarios/joao/documentos
   ```

   **Saída esperada:** Mesmo conteúdo de antes do encerramento.

## Observações Importantes

- **Caminhos Absolutos:** Todos os comandos utilizam caminhos absolutos a partir da raiz (`/`).
- **Nomes de Arquivos e Diretórios:** Evite caracteres especiais ou espaços.
- **Sensível a Maiúsculas e Minúsculas:** O simulador diferencia maiúsculas de minúsculas.
- **Integridade dos Dados:** O journaling garante recuperação em caso de falhas.

## Dicas de Uso

- **Planejamento da Estrutura:** Planeje diretórios e arquivos antes de começar.
- **Verificação Frequente:** Use `listarDiretorio` para monitorar o estado do sistema.
- **Renomeações Cuidadosas:** Tenha atenção ao renomear.
- **Encerramento Seguro:** Sempre use `sair` para garantir que o estado seja salvo.

## Tratamento de Erros Comuns

- **Diretório/Arquivo Não Encontrado:** Verifique o caminho e o nome exato.
- **Operação Inválida:** Confira a lista de comandos com `help`.
- **Criar Arquivo em Diretório Inexistente:** Crie primeiro todos os diretórios necessários.

## Conclusão

Este tutorial detalha o uso do simulador, cobrindo todos os comandos e fornecendo exemplos práticos. Agora você está preparado para explorar e compreender melhor o funcionamento de sistemas de arquivos e o impacto do journaling na integridade dos dados.
