# swagger-gen

[![Build Status](https://travis-ci.org/owainlewis/swagger-gen.svg)](https://travis-ci.org/owainlewis/swagger-gen)

Swagger-gen is a tool for generating code or documents from a swagger spec.

It can quickly generate boilerplate code in your application in any number of different languages.

![](http://www.davenewson.com/_media/tutorials/php/swagger-logo.gif)

## Install

[![Clojars Project](http://clojars.org/io.forward/swagger-gen/latest-version.svg)](http://clojars.org/io.forward/swagger-gen)

A library for easy swagger code generation using Mustache as the template engine.

## Goals

There is an existing Java generator for swagger but Clojure seems a much leaner and easier language for the task.

1. Lightweight and simple DSL for traversing Swagger Specs
2. Clean and simple code gen based on templates
3. More complex code generation through Clojure functions

## Usage

This example generates HTML docs from the canonical petstore example.

We start with a simple mustache template which contains placeholders for our final HTML file

```html
<html>
<head>
  <title>{{info.title}}</title>
  <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet"/>
</head>
<body>
  <div class="container">
    <h1>{{info.title}}</h1>
    <p>{{info.description}}</p>
    <table class="table table-striped">
      <tr>
        <th>Method</th>
        <th>Path</th>
        <th>Operation ID</th>
      </tr>

      {{#paths}}
        <tr>
          <td>{{method}}</td>
          <td>{{path}}</td>
          <td>{{operationId}}</td>
        </tr>
      {{/paths}}
    </table>
  </div>
</body>
</html>
```

For more complex code generation tasks we can write custom functions to assist with the generation.

The only real "logic" here is adding some additional data to the swagger structure to make rendering in the templates
easier. For example in the example below we have a custom function that renders a swagger path as a scala
case class string.

```clojure
(ns swagger-gen.examples.scala.generator
  (:require [swagger-gen.spray :as spray]
            [swagger-gen.core :refer [render-swagger]]))

(defn expand-model
  "Add some additional data here so we don't have to do any
   tricky logic in the template"
  [model]
  (assoc model :class   (spray/render-case-class model)
               :arglen  (count (:args model))))

(defn -main
  "An example using custom rendering logic to generate model
   code in Scala for a standard Spray application"
  []
  (let [spec "resources/swagger/petstore.yaml"
        template "src/swagger_gen/examples/scala/template.mustache"
        additional-params { :namespace "com.google.service.models" }]
    (print
      (render-swagger spec template
        (fn [spec]
          (merge additional-params
            (assoc spec :definitions
              (map expand-model (:definitions spec)))))))))
```

Swagger gen templates are just simple mustache files.

```
package {{namespace}}

import spray.json.DefaultJsonProtocol

{{#definitions}}
{{class}}

object {{name}} extends DefaultJsonProtocol {
  implicit val format = jsonFormat{{arglen}}({{name}}.apply)
}

{{/definitions}}
```

## Running the example

```
➜  swagger-gen git:(master) lein run -m swagger-gen.examples.scala.generator
```

Generates the following Scala code and dumps it in the terminal

```scala
package com.google.service.models

import spray.json.DefaultJsonProtocol

case object Pet

object Pet extends DefaultJsonProtocol {
  implicit val format = jsonFormat0(Pet.apply)
}

case class NewPet(name: String, tag: String)

object NewPet extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(NewPet.apply)
}

case class Error(code: Int, message: String)

object Error extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(Error.apply)
}

```

## Further examples

Check out the examples directory for inspiration. Here is an example of how you can generate a working golang rest
API from a swagger spec

```
lein run -m swagger-gen.examples.golang.generator && \
go run src/swagger_gen/examples/golang/main.go && \
curl -i http://localhost:8080/pets
```

## License

Copyright © 2015 Owain Lewis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
