# Scatter and Gather Design Pattern Implementation

This project implements the **Scatter and Gather** distributed computing design pattern in Java. Each system component runs as a separate Java process. Communication between components is established using Java’s native socket classes: `java.net.Socket` and `java.net.ServerSocket`.

[Apache Maven](https://maven.apache.org/index.html) is used for dependency management. The [Gson](https://github.com/google/gson) library handles JSON manipulation, and [Apache Log4j](https://logging.apache.org/log4j/2.x/index.html) is used for logging.

## Implementation Design

The following image provides an overview of the implementation design:

![image](https://github.com/bernardodemarco/text-retrieval/assets/115510880/a7567055-047b-4834-97b4-1131e8dc1782)

### Communication Components

All nodes in the system rely on custom-built dependencies that encapsulate, abstract, and promote reuse of distributed programming concepts. As such, components were implemented to represent **TCP connections**, **TCP servers** and a **Scatter and Gather** service.

#### `TCPClientConnection`

The `TCPClientConnection` class abstracts the creation of sockets, connection to endpoints, message transmission and reception and connection teardown.

To use this component, simply instantiate it with a target IP address and port. The `Client` and `ScatterGatherService` classes internally rely on this component.

https://github.com/bernardodemarco/scatter-gather/blob/73f0aaade01351329fb28214b3dcda0fd2397074/src/main/java/com/github/bernardodemarco/textretrieval/communication/scattergather/ScatterGatherService.java#L29-L41

https://github.com/bernardodemarco/scatter-gather/blob/73f0aaade01351329fb28214b3dcda0fd2397074/src/main/java/com/github/bernardodemarco/textretrieval/client/Client.java#L33-L34

#### `TCPServer`

This class encapsulates the behavior and responsibilities of a TCP server. It provides operations for listening on a port, sending and receiving data, and closing the communication channel. The `Root` and `Worker` classes use this component.

https://github.com/bernardodemarco/scatter-gather/blob/73f0aaade01351329fb28214b3dcda0fd2397074/src/main/java/com/github/bernardodemarco/textretrieval/root/Root.java#L36

https://github.com/bernardodemarco/scatter-gather/blob/73f0aaade01351329fb28214b3dcda0fd2397074/src/main/java/com/github/bernardodemarco/textretrieval/root/Root.java#L43

#### `ScatterGatherService`

This class implements the **Scatter and Gather** design pattern. It exposes methods for scattering requests and gathering responses.

Internally, it maintains a list of active connections and a thread pool. When scattering requests, the service distributes them in a round-robin fashion. For each sent request, a task is submitted to the thread pool to listen for the corresponding response.

https://github.com/bernardodemarco/scatter-gather/blob/73f0aaade01351329fb28214b3dcda0fd2397074/src/main/java/com/github/bernardodemarco/textretrieval/communication/scattergather/ScatterGatherService.java#L47-L62

When gathering, the service waits until all thread pool tasks have completed. Once all responses have been collected, they are returned to the caller.

https://github.com/bernardodemarco/scatter-gather/blob/73f0aaade01351329fb28214b3dcda0fd2397074/src/main/java/com/github/bernardodemarco/textretrieval/communication/scattergather/ScatterGatherService.java#L64-L81

### Application Nodes

#### `Client`

The `Client` process is responsible for sending text search requests to the system's `Root` node. The communication protocol defines that requests from the Client to the Root must follow this structure:

```json
{
    "query": "parallel and distributed computing"
}
```

After sending a request, the Client waits for the response. Before submitting the next query, it pauses execution for a random interval between 1000 and 2000 milliseconds.

Each response returned for a query follows this format:

```json
[
   {
      "fileName": "text.txt",
      "fileContent": [
         "first line",
         "second line",
         "third and last line"
      ],
      "occurrences": 2
   },
]
```

#### `Root`

The `Root` node depends on the TCP server and the `ScatterGatherService`. Upon receiving a request, it parses the query into a set of keywords. These keywords are then distributed to the Worker nodes via the Scatter and Gather service. Each request sent to a Worker node follows this structure:

```json
{
    "keyword": "parallel and distributed computing"
}
```

The Root then gathers all responses, parses them, and builds the final response to return to the Client.

#### `Worker`

Each `Worker` node receives a keyword and searches for its occurrences across text files. After completing the search, it returns a response to the Root node in the following format:

```json
[
   {
      "fileName": "text.txt",
      "occurrences": 2
   },
]
```

## Application Setup

To run the application, Java 17 and Apache Maven 3.6.3 must be installed and properly configured.

To compile the application, run:

```bash
mvn compile
```

To execute it, follow these steps in order:

1. Run a Worker node:
  ```bash
  mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.worker.instances.Worker1"
    ```
2. Run another Worker node:
  ```bash
  mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.worker.instances.Worker2"
  ```
3. Run the Root node:
  ```bash
  mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.root.Root"
  ```
4. Run the Client node:
  ```bash
  mvn exec:java -Dexec.mainClass="com.github.bernardodemarco.textretrieval.client.Client"
  ```

After launching the Client node, it will connect to the Root node and begin sending search requests.

## Packages Structures

```bash
├── pom.xml # Apache Maven pom.xml file
├── README.md # docs in markdown
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

## Application Execution Examples

When running the application, logs are generated to provide insight into the system's behavior. Three log levels are used:

- `DEBUG`: Provides detailed information useful for debugging and understanding the system's internal state.
- `INFO`: Displays important runtime events, such as connection establishments and request responses.
- `ERROR`: Indicates unexpected errors that occur during execution and cannot be gracefully handled, such as a failure to read a text file.

### `Client` Execution

When executing the Client node, the following log messages are particularly relevant:

```bash
# client successfully connected to the Root node/server
2024-06-23 20:43:34 [INFO] (c.g.b.t.c.c.TCPClientConnection) - Successfully connected to server [127.0.0.1:8000].
```

```bash
# Request being sent to the Root node/server
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.c.Client) - Sending query [{"query":"concurrent and parallel and distributed programming"}].
```

```bash
# Response received from the Root node/server
2024-06-23 20:43:34 [INFO] (c.g.b.t.c.Client) - Received query response [[
  {
    "fileName": "text5.txt",
    "fileContent": [
      "Parallel programming is not without its challenges. One of the primary difficulties is ensuring that parallelized tasks do not interfere with each other, leading to bugs and unpredictable behavior. Debugging parallel programs can be significantly more complex than debugging sequential programs due to the interactions between concurrently executing tasks. Tools and techniques for debugging and profiling parallel applications are essential for developers in this field.",
      "",
      "Moreover, not all problems can be easily parallelized. The extent to which a problem can be parallelized depends on its nature and the dependencies between tasks. Amdahl\u0027s Law provides a theoretical limit to the speedup achievable through parallelization, highlighting the diminishing returns of adding more processors to a parallel system. Understanding these limitations is crucial for effectively designing and optimizing parallel programs."
    ],
    "occurrences": 11
  },
  {
    "fileName": "text4.txt",
    "fileContent": [
      "Distributed programming enables the development of applications that can scale horizontally by adding more machines to the network. This scalability is achieved through the decomposition of tasks into smaller, independent units that can be executed on different machines. Distributed systems can handle increased load by simply adding more nodes, making them ideal for handling large-scale, resource-intensive applications.",
      "",
      "A significant advantage of distributed programming is fault tolerance. Distributed systems can continue functioning even if some nodes fail, as the workload can be redistributed among the remaining nodes. This redundancy is critical for maintaining the availability and reliability of applications, especially in environments where downtime can have severe consequences, such as financial systems or healthcare applications."
    ],
    "occurrences": 7
  },
  {
    "fileName": "text3.txt",
    "fileContent": [
      "In parallel programming, synchronization and data sharing between threads are critical issues. Proper synchronization mechanisms like locks, semaphores, and barriers are essential to prevent race conditions and ensure data consistency. These mechanisms allow threads to coordinate their activities and access shared resources safely, which is vital for the correctness of parallel programs.",
      "",
      "Another important aspect of parallel programming is load balancing, which ensures that all processors are utilized efficiently. Dynamic load balancing techniques can redistribute tasks among processors to avoid scenarios where some processors are idle while others are overloaded. This helps in achieving optimal performance and efficient resource utilization in parallel computing environments."
    ],
    "occurrences": 11
  },
  {
    "fileName": "text1.txt",
    "fileContent": [
      "Parallel programming is a computing paradigm that enables the execution of multiple processes simultaneously. By dividing large problems into smaller tasks, parallel programming can leverage multiple processors to solve these tasks concurrently. This approach significantly reduces the time required for computation and enhances the performance of applications, especially in scientific and engineering domains.",
      "",
      "The primary advantage of parallel programming is its ability to handle large datasets and complex computations efficiently. Techniques such as multithreading, multiprocessing, and using parallel algorithms are common in this field. Multithreading allows multiple threads to run concurrently within a single program, while multiprocessing utilizes multiple CPUs to execute processes simultaneously. These techniques are essential in modern computing environments where performance and speed are critical."
    ],
    "occurrences": 12
  },
  {
    "fileName": "text2.txt",
    "fileContent": [
      "Distributed programming involves the creation of software systems that run on multiple computers, which communicate and coordinate their actions by passing messages. These systems are designed to share resources and data processing tasks across a network, enhancing the scalability and reliability of applications. Distributed programming is fundamental to the operation of large-scale web services, cloud computing, and enterprise-level applications.",
      "",
      "One of the key challenges in distributed programming is ensuring consistency and fault tolerance across multiple nodes. Techniques such as distributed databases, consensus algorithms, and replication are used to manage data consistency and availability. Distributed systems must also handle network latency and potential failures gracefully, making robust error handling and recovery mechanisms crucial components of these systems."
    ],
    "occurrences": 17
  }
]].
```

```bash
# Informing that it will wait to perform the next request query
2024-06-23 20:43:36 [DEBUG] (c.g.b.t.c.Client) - Sleeping for 1329 milliseconds.
```

### `Root` Execution

```bash
# Successfully connected to the Worker node listening on port 8001
2024-06-23 20:43:34 [INFO] (c.g.b.t.c.c.TCPClientConnection) - Successfully connected to server [127.0.0.1:8001].
```

```bash
# Successfully connected to the Worker node listening on port 8002
2024-06-23 20:43:34 [INFO] (c.g.b.t.c.c.TCPClientConnection) - Successfully connected to server [127.0.0.1:8002].
```

```bash
# Received request from the Client
2024-06-23 20:43:34 [INFO] (c.g.b.t.r.Root) - Received query [{"query":"concurrent and parallel and distributed programming"}] from client.
```

```bash
# Gathering requests for the Worker nodes
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.c.s.ScatterGatherService) - Scattering data [[{"keyword":"programming"}, {"keyword":"concurrent"}, {"keyword":"and"}, {"keyword":"parallel"}, {"keyword":"distributed"}]] to [2] connections using round-robin algorithm.
```

```bash
# Received responses from the Worker nodes
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.r.Root) - Received [[[{"fileName":"text1.txt","occurrences":3},{"fileName":"text2.txt","occurrences":3},{"fileName":"text3.txt","occurrences":2},{"fileName":"text4.txt","occurrences":2},{"fileName":"text5.txt","occurrences":1}], [], [{"fileName":"text1.txt","occurrences":5},{"fileName":"text2.txt","occurrences":9},{"fileName":"text3.txt","occurrences":5},{"fileName":"text4.txt","occurrences":1},{"fileName":"text5.txt","occurrences":5}], [{"fileName":"text1.txt","occurrences":4},{"fileName":"text3.txt","occurrences":4},{"fileName":"text5.txt","occurrences":5}], [{"fileName":"text2.txt","occurrences":5},{"fileName":"text4.txt","occurrences":4}]]] from workers.
```

### `Worker` Execution

```bash
# Worker server listening for requests
2024-06-23 20:43:34 [INFO] (c.g.b.t.c.s.TCPServer) - Listening on port 8001.
```

```bash
# Received a request with a keyword
2024-06-23 20:43:34 [INFO] (c.g.b.t.w.Worker) - Received keyword [{"keyword":"programming"}].
```

```bash
# Searches for keywords in the text files
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.w.Worker) - Keyword [programming] has appeared [3] times in [text1.txt]
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.w.Worker) - Keyword [programming] has appeared [3] times in [text2.txt]
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.w.Worker) - Keyword [programming] has appeared [2] times in [text3.txt]
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.w.Worker) - Keyword [programming] has appeared [2] times in [text4.txt]
2024-06-23 20:43:34 [DEBUG] (c.g.b.t.w.Worker) - Keyword [programming] has appeared [1] times in [text5.txt]
```
