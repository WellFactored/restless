[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b8b7e2b4e1054aca93e5bec881f0183b)](https://www.codacy.com/app/doug/restless?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=WellFactored/restless&amp;utm_campaign=Badge_Grade)


# restless
Get less from your REST! A small scala/play library for filtering and projecting json results from REST calls

# Getting started

In your build.sbt include:

`libraryDependencies += "com.wellfactored" %% "restless" % "0.1.2"`

## Separate components
restless is implemented as `restless-core` and `restless-play-json` components. The `core` package implements the 
query parser that will generate an AST and `play-json` provides an implementation of an execution engine for the AST 
that applies it to documents made up of PlayFramework JsObjects. `core` depends only on 
Rob Norris (tpolecat)'s `att-core` library. `restless-play-json` will pull in `com.typesafe.play-json` and associated
dependencies.
