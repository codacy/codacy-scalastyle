[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6ecd219db0924e07abe4aa687ddadd56)](https://www.codacy.com/gh/codacy/codacy-scalastyle?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codacy/codacy-scalastyle&amp;utm_campaign=Badge_Grade)
[![Build Status](https://circleci.com/gh/codacy/codacy-scalastyle.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/codacy/codacy-scalastyle)

# Codacy Scalastyle

This is the docker engine we use at Codacy to have [Scalastyle](http://www.scalastyle.org/) support.
You can also create a docker to integrate the tool and language of your choice!
See the [codacy-engine-scala-seed](https://github.com/codacy/codacy-engine-scala-seed) repository for more information.

## Usage

You can create the docker by doing:

```
sbt stage
docker build -t codacy-scalastyle .
```

The docker is ran with the following command:

```
docker run -it -v $srcDir:/src  <DOCKER_NAME>:<DOCKER_VERSION>
```

## Generate the docs

You can generate the docs by running:

```
sbt doc-generator/run
```

## Test

We use the [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test) to test our external tools integration.
You can follow the instructions there to make sure your tool is working as expected.

## Agent Playbook: Updating This Repository End-to-End

This section is written for an AI coding agent (or a human) tasked with updating this repo — most commonly bumping the wrapped Scalastyle version, but also base image / orb / dependency bumps. Follow it top to bottom; it tells you what to change, how to regenerate derived files, how to test locally, and how to interpret CI so you can iterate on failures without guessing.

### 1. What this repository is

This is a **Codacy engine**: a thin Scala wrapper (`src/main/scala/codacy/Engine.scala` and `src/main/scala/codacy/scalastyle/ScalaStyle.scala`, built on `codacy-engine-scala-seed`) that packages [Scalastyle](http://www.scalastyle.org/) as a Docker image Codacy's platform can run against a customer's Scala source code. Build tool is **sbt** (`build.sbt`, sbt 1.9.6 per `project/build.properties`).

The `docs/` directory is machine-consumed configuration, not just documentation:

- `docs/patterns.json` — the full list of Scalastyle rules ("patterns") Codacy knows about, their parameters/defaults, and which are enabled out of the box. Generated file, do not hand-edit.
- `docs/description/description.json` + `docs/description/*.md` — human-readable titles/descriptions per pattern, used in the Codacy UI. Generated file, do not hand-edit.
- `docs/tests/*` and `docs/multiple-tests/{with-config,without-config}/*` — fixtures used by `codacy-plugins-test` to validate the engine actually produces the results it claims to for real Scala code samples.
- `docs/tool-description.md` — short blurb about the tool, hand-maintained.
- `docs/scalastyle_config.xml` — a Scalastyle config fixture, hand-maintained.

Both generated artifacts above come from **`DocGenerator`** (`doc-generator/src/main/scala/DocGenerator.scala`), a separate sbt subproject (`doc-generator`, sharing `commonDeps` with the main build). Unlike some other Codacy engines, this generator does **not** clone anything over the network — it loads `scalastyle_definition.xml` and `scalastyle_documentation.xml` as classpath resources bundled inside the `com.beautiful-scala::scalastyle` jar itself (the same dependency pinned by `scalastyleVersion` in `build.sbt`), then walks those XML docs to build the pattern list and descriptions. This means regenerating docs after a version bump only requires sbt to resolve the new Scalastyle jar — no git/pandoc/network scraping step is involved.

There is also a generated `Versions.scala` (produced by a `sourceGenerators` task in `build.sbt`) exposing `Versions.scalastyle`, which `DocGenerator` uses to stamp `"version"` in `docs/patterns.json` — this is why `build.sbt`'s `scalastyleVersion` and `docs/patterns.json`'s `"version"` field must always move together.

### 2. Files that encode versions — check all of these on every update

| File | What it controls | What to check |
|---|---|---|
| `build.sbt` → `scalastyleVersion` | Which Scalastyle release is bundled, compiled against, and stamped into `docs/patterns.json` | Bump to the target version. Confirm `"com.beautiful-scala" %% "scalastyle" % <version>` actually exists on Maven Central for the pinned Scala version (2.13.5). |
| `build.sbt` → `codacy-engine-scala-seed` dependency | Codacy's engine SDK/base library | Check [Maven Central](https://mvnrepository.com/artifact/com.codacy/codacy-engine-scala-seed) for newer versions if asked to update it; not tied to Scalastyle bumps. |
| `build.sbt` → `scala-xml` dependency | XML parsing lib used by the engine and DocGenerator | Only touch if explicitly asked or if a Scalastyle bump forces a compatible version. |
| `docs/patterns.json` → top-level `"version"` field | Must mirror `scalastyleVersion` | Regenerated automatically by `DocGenerator` — do not hand-edit, just verify it matches after regeneration. |
| `project/plugins.sbt` → `codacy-sbt-plugin` | Codacy's shared sbt plugin (formatting/packaging conventions) | Bump only if asked, or if the build fails to load with the current pin. |
| `project/build.properties` → sbt version | sbt itself | Rarely needs touching; check only if the build itself fails to load. |
| `.circleci/config.yml` → `codacy/base` orb | Shared CircleCI steps (checkout, versioning, sbt build, docker publish, tagging) | Check the latest published version; `git log -p .circleci/config.yml` shows the real bump history as a fallback reference. |
| `.circleci/config.yml` → `codacy/plugins-test` orb | Runs `codacy-plugins-test` in CI after the image is built | Same as above. |
| `Dockerfile` → base image (currently `amazoncorretto:8u382-alpine3.18-jre`) | JRE the packaged app runs on | Only bump if asked explicitly, or if the new Scalastyle version raises its minimum JDK requirement — don't bump opportunistically. |

### 3. Step-by-step update procedure

1. **Bump `scalastyleVersion` in `build.sbt`** (and `.circleci/config.yml` orbs / `Dockerfile` base image / `project/plugins.sbt`, if scoped by the task).
2. **Regenerate the docs**: `sbt doc-generator/run`. This rewrites `docs/patterns.json` and `docs/description/*`. Review the diff carefully for new/removed/renamed checkers and stale fixture references in `docs/tests/` and `docs/multiple-tests/`.
3. **Compile and format-check**: `sbt scalafmtCheckAll scalafmtSbtCheck` (fix with `sbt scalafmt scalafmtSbt` if it fails), then `sbt stage`.
4. **Build the Docker image**: `docker build -t codacy-scalastyle .`
5. **Run `codacy-plugins-test` locally** before pushing — clone https://github.com/codacy/codacy-plugins-test and run its DockerTest commands (this repo's CI runs the "multiple" test mode via `run_multiple_tests: true`) against your local image tag.
6. **Iterate on failures**, re-running only the relevant test command after each fix.
7. **Commit** the version bump(s) together with the regenerated `docs/` files in one change.
8. **Push and open a PR.** CI (`.circleci/config.yml`) runs `codacy/checkout_and_version` -> `publish_docker_local` (which itself runs `scalafmtCheckAll`, `scalafmtSbtCheck`, `doc-generator/run`, `stage`, then builds and saves the Docker image) -> `plugins_test` -> `codacy/publish_docker` (master only) -> `codacy/tag_version`.
9. **Poll the PR's real CI checks until they all pass — local validation is NOT the finish line.** After every push, run `gh pr checks <pr-url>` and keep re-polling (short sleep while any check is `pending`) until all checks finish. If a check fails, fetch its actual log (CircleCI API/UI for the failing job — don't guess), find the true root cause, fix it, push again (never `--no-verify`, never force-push), and re-poll. Repeat until every check is green. The CI environment's toolchain can differ from your local one, so a clean local run does not guarantee CI passes. Only stop iterating when every check passes, or you hit a genuine product/infra decision that needs a human — in which case explain it in the PR rather than guessing.

### 4. Common failure modes and fixes

| Symptom | Likely cause | Fix |
|---|---|---|
| `scalafmtCheckAll`/`scalafmtSbtCheck` fails in CI/locally | Generated or hand-edited Scala file not formatted | Run `sbt scalafmt scalafmtSbt` then re-run the check command |
| `sbt doc-generator/run` fails to resolve dependencies | The pinned `scalastyleVersion` doesn't exist for the pinned Scala version on Maven Central | Verify the artifact coordinates upstream before bumping |
| `docs/patterns.json` `"version"` doesn't match `build.sbt`'s `scalastyleVersion` after regeneration | Forgot to re-run `sbt doc-generator/run` after bumping, or committed a stale copy | Re-run the generator and recommit |
| `plugins_test` (multiple-tests) fails on a specific fixture folder | Checker renamed/removed/added, or behavior changed, upstream between Scalastyle versions | Re-run `DocGenerator`; update `docs/tests/`/`docs/multiple-tests/` expectations to match the new (verified correct) output |

### 5. Definition of done

- Version bump(s) reflected in all files that encode them (`build.sbt`, and `.circleci/config.yml`/`Dockerfile`/`project/plugins.sbt` if in scope).
- `docs/patterns.json` and `docs/description/*` regenerated via `sbt doc-generator/run` and committed, with fixture inconsistencies in `docs/tests/`/`docs/multiple-tests/` resolved.
- `sbt scalafmtCheckAll scalafmtSbtCheck` and `sbt stage` pass locally.
- Docker image builds successfully.
- `codacy-plugins-test` passes locally against the freshly built image.
- **After pushing and opening/updating the PR, every CI check on it is green.** Poll `gh pr checks <pr-url>` and iterate on any failure (fetch the real CI log, fix, push, re-poll) until all pass — a passing local build is not sufficient, because the CI toolchain can differ from your local one (see step 9).

## What is Codacy?

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt, helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features:

- Identify new Static Analysis issues
- Commit and Pull Request Analysis with GitHub, BitBucket/Stash, GitLab (and also direct git repositories)
- Auto-comments on Commits and Pull Requests
- Integrations with Slack, HipChat, Jira, YouTrack
- Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
