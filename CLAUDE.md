# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

"stuff": a personal inventory web app (items with photos, descriptions, QR/barcodes, and a "location" = containing item). Scala 2.13 / sbt multi-project: Play 3.0 (Pekko) server + Scala.js React client + a shared cross-project.

## Commands

sbt 1.11, needs JDK 17+ (tika-core 4 is Java 17 bytecode; Makefile uses `--java-home /usr/lib/jvm/java-21-openjdk`, runtime image is Temurin 21 JRE).

- Compile everything: `sbt compile` (server compile triggers the Scala.js/webpack pipeline for the client)
- Run dev server: `sbt "project server" run` (port 9000; HTTPS: `run -Dhttps.port=9443`). Uses `server/conf/application.conf`; DB root is `target/stuff-db`.
- Tests: `sbt test`; single suite: `sbt "sharedJVM/testOnly de.unruh.stuff.shared.UtilsTest"`. Only test is `shared/src/test/.../UtilsTest.scala` (ScalaTest).
- If webpack fails with `ERR_OSSL_EVP_UNSUPPORTED`: prefix sbt with `NODE_OPTIONS=--openssl-legacy-provider`.
- Docker image: `make docker_build` (runs `sbt dist`, unzips into `tmp/server`, builds image); `make docker_try` runs it locally; `make docker_deploy` pushes to registry.git.rwth-aachen.de. `DIST_NAME` in the Makefile must match the sbt version.
- Dependency updates: `sbt dependencyUpdates`. Jackson and Guava are forced via `dependencyOverrides` in `build.sbt` (Play 3.0 pins vulnerable versions); `ujson-play` and the scalajs-bundler plugin have exclusions to avoid pre-Play-3 artifacts. Check the packaged jars with `trivy rootfs <dist>/lib`. Client npm deps are pinned (no `latest`); webpack tool versions are set explicitly. Audit with `npm audit` in `client/target/scala-2.13/scalajs-bundler/main`.

## Architecture

**Projects** (`build.sbt`): `server` (Play, `server/app`), `client` (Scala.js + scalajs-react + MUI (`@mui/material`), bundled via scalajs-bundler/webpack; npm deps declared in `build.sbt`, not package.json), `shared` (pure crossProject; platform-specific bits in `shared/.js` and `shared/.jvm`).

**RPC**: Client/server talk via autowire + upickle. The interface is `shared/.../AjaxApi.scala`. Server impl `server/app/de/unruh/stuff/AjaxApiServer.scala` (`AjaxApiImpl`, one instance per user, holds the user's whole DB in memory as `Map[Id, Item]`, mutations `synchronized`). Route `POST /api/*method` in `controllers.Application.ajaxApi`. Client side `AjaxApiClient.scala` (sends `Csrf-Token` header) wrapped by `DbCache.scala` (client-side item cache, invalidated by `lastModified`). Adding an API method = add to `AjaxApi` trait + implement in `AjaxApiImpl` + (usually) a `DbCache` wrapper.

**Storage**: No database — flat files, one directory per user under `stuff.db_root`. Format spec in `doc/db_format.md` (read it before changing `Item` or YAML). `items/<id>.yaml` per item, `files/<id>/…` for attachments; `localstuff:` extended URLs reference them (`shared/.../ExtendedURL.scala`). YAML (de)serialization in `server/.../YamlRW.scala` (moultingyaml); file ops in `db/YamlDb.scala`. On save, `db/ProcessItems.scala` converts `data:` URL photos uploaded by the client into files in `files/<id>/` and bumps `lastModified`. Path construction/sanitizing in `Paths.scala`. Example DB in `example/`.

**Auth**: Google Sign-In ID token verified server-side (`controllers/AuthenticationController.scala`, client id in `stuff.google_client_id`); email stored in Play session cookie (`session.maxAge=7d`). New users go through a register/consent page which creates their DB directory. `Authenticated` trait provides `isAuthenticated`/`username` for controllers.

**Client app**: Single page, entry `AppMain.appMain()` (exported to JS, called from `views/app.scala.html`, which also sets JS globals `stuff_csrf_token`, `stuff_username`, `stuff_url_path_prefix` read via `JSVariables.scala`). Hash-based routing (`#search`, `#item/<id>`, `#create/<n>`, `#createcode/<code>`) with a custom `StatePreservingRouter` that keeps up to 20 previously rendered pages mounted (hidden) to preserve state and scroll position. JS library facades live in subpackages (`mui`, `zxing`, `notistack`, `reactwebcam`, `reactsimplewysiwyg`), mostly built on kinoplan's `ReactBridgeComponent` (`scalajs-react-bridge`). There is no full MUI facade library: add MUI components to `mui/Mui.scala` as needed.

**Deployment config**: `server/conf/container.conf` (Docker/Kubernetes: DB at `/opt/stuff-db`, secret from `APPLICATION_SECRET`, served under path prefix `application.context="/people/unruh/stuff/"`, allowed hosts). Any server-generated URL used by the client must respect `config.applicationContext` / `url_path_prefix`. Kubernetes manifests in `kubernetes.txt`. `heroku.conf`/`Procfile` are legacy.
