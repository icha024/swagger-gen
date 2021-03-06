(ns swagger-gen.core-test
  (:require [clojure.test :refer :all]
            [swagger-gen.fixtures :refer :all]
            [swagger-gen.core :refer :all]))

(def petstore-yaml "resources/swagger/petstore.yaml")
(def uber-yaml     "resources/swagger/uber.yaml")

(def petstore (parse-swagger petstore-yaml))
(def uber     (parse-swagger uber-yaml))

(deftest load-swagger-file-test []
  (testing "should load a swagger yaml file"
    (is (map? (load-swagger-file petstore-yaml)))))

(deftest parse-swagger-test []
  (testing "it should parse a swagger spec"
    (is (= (keys petstore)
           '(:swagger :info :host :basePath :schemes :consumes :produces :paths :definitions)))))

(deftest parse-swagger-string-test []
  (testing "it should parse a swagger spec string"
    (let [from-string (parse-swagger-string (slurp petstore-yaml))]
      (is (= (keys from-string)
           '(:swagger :info :host :basePath :schemes :consumes :produces :paths :definitions))))))  

(deftest check-paths-test []
  (testing "paths should be a vector of paths containing method and path"
    (let [path (last (:paths petstore))]
      (is (= (:path path) "/pets/{id}"))
      (is (= (:method path) "delete"))
      (is (= (:operationId path) "deletePet")))))

(deftest check-definitions-test []
  (testing "should add name to definition and flatten into vector"
    (let [definitions (:definitions petstore)]
      )))

(deftest render-swagger-string-test []
  (testing "should render swagger and spec strings"
    (let [spec-string (slurp petstore-yaml)
          template-string "Title: {{info.title}}"
          actual (render-swagger-template spec-string template-string identity)]
      (is (= "Title: Swagger Petstore" actual)))))
