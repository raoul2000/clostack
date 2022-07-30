# Clostack :  Fullstack App Boilerplate

Clojure/script web application in a single *jar* file.

## Requirements

Note that the project may also run on previous versions, but these are the ones it has been developed on.

- node
```shell
$ node -v
v16.13.1
```
- Java 
```shell
$ java -version
java version "17.0.1" 2021-10-19 LTS
Java(TM) SE Runtime Environment (build 17.0.1+12-LTS-39)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.1+12-LTS-39, mixed mode, sharing)
```

- Clojure
```shell
$ clojure --version
Clojure CLI version 1.10.3.1029
```

Recommended:
- [Visual Studio Code](https://code.visualstudio.com/) + [Calva extension](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva)

## Frontend

Managed by [Shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html) (see [configuration file](shadow-cljs.edn) for details).

- React (with [reagent](https://cljdoc.org/d/reagent/reagent/1.1.1/doc/documentation-index))

## Backend

Powered by [Pedestal](http://pedestal.io/) (see [configuration file](deps.edn) for details).

- Command line arguments
- Content Negociation
- *dev* and *production* running mode

## Usage

- install project
```
$ git clone https://github.com/raoul2000/clostack.git
$ cd clostack
$ npm install
```

### Work on the Frontend

- start *shadow-cljs* server and *watch* changes on the main application
```shell
$ npx shadow-cljs watch app
or
$ npm run watch-app
```
- when `Build completed` and then:
  - go to http://localhost:9630 for the REPL
  - go to http://localhost:8000 for the main
- **tests** are configured to run in browser. To watch and run test
```shell
$ npx shadow-cljs watch test
or
$ npm run watch-test
```
- when started, test page is at http://localhost:8021/ 

### Work on the Backend

- If you're using *Calva* Extension with VSCode, just start a new REPL (aka *Jack-in*)
- evaluate `src\back\server.clj`
- from the REPL call 
  - `(start-dev)` : to START the server
  - `(stop-dev)` : to STOP the server
  - `(restart)` : to STOP+START the server

By default, server responds to `http://localhost:8890/index.html`.

- run the project directly
```shell
$ clojure -M:run-m
```
- run tests
```shell
$ clojure -T:build test
```

### Build for Production

- build final *jar* into the `./target` folder
```shell
$ clojure -T:build ci
```
- Run that uberjar (show usage) :
```shell
$ java -jar target/clostack-X.X.X.jar --help
```
