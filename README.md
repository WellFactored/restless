[![Build Status](https://travis-ci.org/WellFactored/restless.svg?branch=master)](https://travis-ci.org/WellFactored/restless)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/b8b7e2b4e1054aca93e5bec881f0183b)](https://www.codacy.com/app/doug/restless?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=WellFactored/restless&amp;utm_campaign=Badge_Grade)

# restless
Get less from your REST! A small scala/play library for filtering and projecting json results from REST calls

# Getting started

In your build.sbt include:

`libraryDependencies += "com.wellfactored" %% "restless" % "0.1.4"`

## Separate components
restless is implemented as `restless-core` and `restless-play-json` components. The `core` package implements the 
query parser that will generate an AST and `play-json` provides an implementation of an execution engine for the AST 
that applies it to documents made up of PlayFramework JsObjects. `core` depends only on 
Rob Norris (tpolecat)'s `att-core` library. `restless-play-json` will pull in `com.typesafe.play-json` and associated
dependencies.

## Query your REST results

`restless` implements a simple query language to filter lists of results. For example, of you had the following
json output from your api call:

```
[
  {
    "foo" : "bar",
    "x" : 14
    "y" : 13
  },
  {
    "foo" : "baz",
    "x" : 14,
    "y" : 15
  }
]
```

The you could use a query such as `foo = baz` to filter this down to

```
[
  {
    "foo" : "baz",
    "x" : 14,
    "y" : 15
  }
]
```

or something a bit more complex like `x > y`. It also supports compound expressions such as:

* `a = b and c > 3`
* `(a = b or c < 3) and d != "foo"`

Numeric queries can include all the usual comparison operators, namely `=`, `!=`, `<`, `>`, `<=` and `>=`.

String comparisons are case-insensitive and can be one of:
* `=`
* `!=`
* `starts-with` or `starts with`
* `ends-with` or `ends with`
* `contains`

## Todo
There are still some arbitrary restrictions in the query syntax. For example, you cannot put a constant on the left-hand-side
of a comparison at the moment.

Paths to string values can only be compared against string literals - it should be possible to compare them against other paths. 

## Thanks
Many thanks to Rob Norris (tpolecat) for the lovely `atto` parser and his help when I ran into some recursion issues.
If you want a nice small, easy-to-use parser then check it out - https://github.com/tpolecat/atto 