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
