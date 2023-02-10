# Clostack :  Fullstack App Boilerplate

TLDR: Clojure/script web application in a single *jar* file.

## Rationale

The first goal of this project is *self* educational. In other words, it's an attempt to explore some of the features available in the clojure(script) world of webapp, from the back to the front end. Next was the idea to provide a single *jar* file, easy to invoke, that would provide a set of reusable examples illustrating various aspects composing a webapp. From this *boilerplate* is should be quite simple to implement your/my next idea.

Last, I just wanted to have fun playing with clojure...

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
- [Re-Frame](https://day8.github.io/re-frame/)
- Dev tools
  - [re-frame-10x](https://github.com/day8/re-frame-10x): instrument, and then inspect, the inner workings of a running re-frame application
  - [cljs-devtools](https://github.com/binaryage/cljs-devtools): adds enhancements into Chrome DevTools for ClojureScript developers
- [Bulma](https://bulma.io) (pure CSS Framework)
  - [Material Design theme](https://jenil.github.io/bulmaswatch/materia)
  - [Material Design Icons](https://materialdesignicons.com/)

## Backend

Powered by [Pedestal](http://pedestal.io/) (see [configuration file](deps.edn) for details).

- Command line interface (use `--help` to get full options list)
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
- after `Build completed`:
  - go to http://localhost:9630 for the REPL
  - go to http://localhost:8000 for the main
- **tests** are configured to run in browser. To watch and run test
```shell
$ npx shadow-cljs watch test
or
$ npm run watch-test
```
- when started, test page is at http://localhost:8021/ (auto-releoad)

### Work on the Backend

- If you're using *Calva* Extension with VSCode, just start a new REPL (aka *Jack-in*)
- evaluate `src\back\server.clj`
- from the REPL call 
  - `(start-dev)` : to START the server
  - `(stop-dev)` : to STOP the server
  - `(restart)` : to STOP+START the server

By default, server responds to `http://localhost:8890/index.html`.

- run the project directly
```bash
$ clojure -M:run-m
# with options ...
$ clojure -M:run-m --help
```
- run tests
```shell
$ clojure -T:build test
```

### Build for Production

- build the frontend app
```shell
$ shadow-cljs release app
or
$ npm run release
```
- build final *jar* into the `./target` folder
```shell
$ clojure -T:build ci
```

### Run
- Run uberjar:
```shell
# display inline help
$ java -jar target/clostack-X.X.X.jar --help
# start server at port 8808
$ java -jar target/clostack-X.X.X.jar --port 8808
```

## More ...

### VSCode REST client

This project includes the [REST Client extension](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) for VSCode. It requires minimal configuration to add to the `.vscode/settings.json` configuration file.


```json
"rest-client.environmentVariables": {
	"local": {
		"version": "v2",
		"baseUrl": "http://localhost:8890"
	}
}
```
Update `baseUrl` property to match your settings customization.

REST scripts are stored in `test/bask/http`.


