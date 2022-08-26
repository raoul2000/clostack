# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [0.0.4]
### Added
- Front
  - Drawer (left menu bar)
  - Todo Widget : illustrate Re-frame features and ajax
- Back
  - Todo REST API: read and write todo list from file

## [0.0.3]
### Added
- Front:
  - add dependency to [Re-frame](https://day8.github.io/re-frame/)
  - add dependency to [re-frame-10x](https://github.com/day8/re-frame-10x)
  - add dependency to [cljs-devtools](https://github.com/binaryage/cljs-devtools)
  - add dependency to [Bulma](https://bulma.io) (pure CSS Framework)
  - redesign pages: 
    - home: the home page for presentation
    - widgets: demostrate various feature : ajax, modal, notification

## [0.0.2]
- ready to use dev environment and tooling (see [README](README.md) for details)
- Front: basic `index.html` page with React component and simple CSS styles
- Back: 
  - REST API Routes:
    - `GET /` : responds with the content of `resources/public/index.html`
    - `GET  /greet`: query param *name*
    - `GET /about` : returns version informations
    - `GET /echo` : returns the request data structure
    - `POST /upload` : multipart upload files
    - `GET /download`: download a file
  - Command Line :
    - `-n` or `--no-browser` : when set, default browser is *not* opened on startup

## [0.0.1]
- initial release


<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

