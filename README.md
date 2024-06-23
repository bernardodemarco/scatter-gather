# Scatter and Gather Design Pattern Implementation

##### Bernardo De Marco Gonçalves - 22102557

## Tecnologias utilizadas

A aplicação foi desenvolvida na linguagem de programação Java, e para a manipulação de _sockets_ foi utilizada suas implementações nativas (`java.net.Socket`, `java.net.ServerSocket`).

Para estruturação da aplicação, foi utilizado o gerenciador [Apache Maven](https://maven.apache.org/index.html). Além disso, para manipulação de JSONs foi utilizada a bibliteca [Gson](https://github.com/google/gson), e para _logs_ foi utilizado o [Apache Log4j](https://logging.apache.org/log4j/2.x/index.html).

## _Setup_ da aplicação

Para execução da aplicação, é necessário ter o Java 17 e o Apache Maven 3.6.3 instalados e configurados.

### Compilação

Para compilação, deve-se executar:

```bash
mvn compile
```

### Execução

Para execução, devem ser abertos quatro terminais, um para cada processo em execução. Em seguida, devem ser executados em ordem os _workers_, _root_ e o _client_.

1. Em um terminal, execute um _worker_:
    ```bash
    mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.worker.instances.Worker1"
    ```
2. Em outro, execute o outro _worker_:
    ```bash
    mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.worker.instances.Worker2"
    ```
3. Em outro, execute o _root_:
    ```bash
    mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.root.Root"
    ```
4. Por fim, abra mais um terminal e execute o _client_:
    ```bash
    mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.client.Client"
    ```

Após executar o _client_, ele se conectará ao _root_ e começará a enviar requisições. Um vídeo exemplificando a execução do programa pode ser encontrado nesse [_link_](https://www.youtube.com/watch?v=iwf3g23__EU).

## _Design da aplicação_

### Estrutura dos diretórios/pacotes

```bash
├── pom.xml # Apache Maven pom.xml file
├── README.md # docs in markdown
├── docs.pdf # docs in pdf
├── src
│   └── main # source code container
│       ├── java
│       │   └── com
│       │       └── github
│       │           └── bernardodemarco
│       │               └── textretrieval
│       │                   ├── client # client node
│       │                   │   ├── Client.java
│       │                   │   └── dto
│       │                   │       └── QueryDTO.java
│       │                   ├── communication # reusable and encapsulated components, such as Client, Server and ScatterGather implementations.
│       │                   │   ├── client
│       │                   │   │   ├── ClientConnection.java
│       │                   │   │   └── TCPClientConnection.java
│       │                   │   ├── scattergather
│       │                   │   │   ├── ScatterGather.java
│       │                   │   │   └── ScatterGatherService.java
│       │                   │   └── server
│       │                   │       ├── Server.java
│       │                   │       └── TCPServer.java
│       │                   ├── root # root node
│       │                   │   ├── dto
│       │                   │   │   ├── KeywordDTO.java
│       │                   │   │   └── QueryOccurrencesDTO.java
│       │                   │   └── Root.java
│       │                   ├── utils # utilities
│       │                   │   └── FileUtils.java
│       │                   └── worker # worker node
│       │                       ├── dto
│       │                       │   └── KeywordOccurrencesDTO.java
│       │                       ├── instances
│       │                       │   ├── Worker1.java
│       │                       │   └── Worker2.java
│       │                       └── Worker.java
│       └── resources # resources file containing configurations files
│           ├── client
│           │   ├── client.properties
│           │   └── queries.json # queries to be performed by the client
│           ├── log4j2.xml # log4j2 config
│           ├── root
│           │   └── root.properties
│           ├── textfiles # text files
│           │   ├── text1.txt
│           │   ├── text2.txt
│           │   ├── text3.txt
│           │   ├── text4.txt
│           │   └── text5.txt
│           └── workers
│               ├── worker1.properties
│               └── worker2.properties

```