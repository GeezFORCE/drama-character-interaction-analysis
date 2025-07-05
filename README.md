# Drama Character Interaction Analysis

- Project work for the course Text Technology

> **_The project is still a work in progress with the processor part just finishing up._**

## What is the project about?

- Dramas from earlier centuries can be hard to follow.
- They’re filled with archaic language and references long-lost to time.
- This often breaks the flow of the story and makes it difficult to grasp how characters relate to one another.
- But what if we could visualize those relationships instead?
- What if we could visualize which characters interact with one another?
- That’s what this project is about, turning complex, classical drama into a clear visualization of character
  interaction.

## Project Structure

- The project is organized as a monorepo with currently 4 different modules
- The `common` module contains the common utilities used across the project
- The `scraper` module contains the code related to the scraper
- The `processor` module contains code related to the processor
- The `visualizer` module contains code for visualizing character interactions
- The `api/` directory is the root directory storing the collection files for a popular REST API client
  called [Bruno](https://www.usebruno.com) which we use for testing

```
.
├── README.md
├── docker-compose.yaml
├── pom.xml
├── api/
├── common/
├── processor/
├── visualizer/
├── samples
│   ├── anon-a-larum-for-london.xml
│   └── drama.xml
└── scraper/
```

## How to run the code

> As a pre-requisite, please have Java (preferably JDK v 21) and maven installed

> 💡 If you have IntelliJ IDEA installed, open the code as a project simplifying it

- Make sure you are at the root directory
- Run `mvn compile` or `mvn package` to compile the project
- For running each application, run their respective jars.
- Here is an example for the scraper module

```shell
java -jar scraper/target/scraper.jar
```

- This will bring up the scraper
- Similarly, one can bring up the processor module as well

> 💡 You can know if the services are up using the health endpoint
> `curl -s -X GET http://localhost:8080/health`
> Refer the port mappings in [How to test the code section](#how-to-test-the-code)

## Bringing up eXist-DB

> As a pre-requisite, please have Docker installed

- We have packaged eXist-DB into a docker-compose service
- To bring up the database, execute the following command

```shell
docker compose -f docker-compose.yaml up -d
```

- If you get an error
  `Cannot connect to the Docker daemon at unix:///Users/user1/.docker/run/docker.sock. Is the docker daemon running?`,
  please ensure docker is up and running

- Please note that it will take some time for the DB to be up and running, please run the following command and verify
  the mentioned log appears

```shell
docker logs exist-db
```

```shell
25 Jun 2025 19:03:53,165 [main] INFO  (JettyStart.java [run]:289) - Server has started, listening on: 
25 Jun 2025 19:03:53,165 [main] INFO  (JettyStart.java [run]:291) - http://172.18.0.2:8080/ 
25 Jun 2025 19:03:53,165 [main] INFO  (JettyStart.java [run]:291) - https://172.18.0.2:8443/ 
25 Jun 2025 19:03:53,165 [main] INFO  (JettyStart.java [run]:294) - Configured contexts: 
25 Jun 2025 19:03:53,165 [main] INFO  (JettyStart.java [run]:300) - /exist (eXist XML Database) 
25 Jun 2025 19:03:53,168 [main] INFO  (JettyStart.java [run]:316) - /exist/iprange (IPrange filter) 
25 Jun 2025 19:03:53,168 [main] INFO  (JettyStart.java [run]:300) - / (eXist-db portal) 
25 Jun 2025 19:03:53,169 [main] INFO  (JettyStart.java [run]:316) - /iprange (IPrange filter) 
```

## How to test the code

> It will be easier if you have [Bruno](https://www.usebruno.com) installed.
> You can also use other API clients like `Postman` or even `curl` from the command line

- Open the folder `api` in bruno to view the collection

- Following are the port mappings for the services

| Service   | Port |
|-----------|------|
| eXist-DB  | 8080 |
| Scraper   | 8081 |
| Processor | 8082 |
| Visualizer | 8083 |

- Once the services and the database are up and running, perform a REST API POST request to `/load/all` endpoint of the
  scraper to start the process.
- You can verify the data insertion using RESTful API provided by eXist-DB as
  documented [here](https://exist-db.org/exist/apps/doc/devguide_rest)

> 💡 Some handy sample data is provided in the directory `samples` if you want to explore the data

## Using the Visualizer

The visualizer module provides a web-based interface for visualizing character interactions in dramas:

1. Make sure the Neo4j database is running and contains processed drama data
2. Start the visualizer service:
   ```shell
   java -jar visualizer/target/visualizer.jar
   ```
3. Open a web browser and navigate to `http://localhost:8083`
4. Select a drama from the dropdown menu to visualize character interactions
5. Interact with the graph:
   - Hover over nodes to see character details
   - Click on a character to view detailed interaction information
   - Drag nodes to rearrange the graph

The visualizer provides a force-directed graph where:
- Nodes represent characters (color-coded by gender)
- Links represent interactions between characters
- Link thickness indicates the number of interactions

This visualization helps to quickly understand the relationships and interaction patterns between characters in complex dramas.
