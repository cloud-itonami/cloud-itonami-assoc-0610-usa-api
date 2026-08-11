(ns association-facts-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler]
            [kotoba.kir :as ir]))

(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url"
             "url-provenance" "established-date" "retrieved-at"])
(def expected
  [{"id" "api.about-api" "title" "About API" "association" "api"
    "isic" "0610" "country" "USA" "kind" "governance-program"
    "url" "https://www.api.org/about" "url-provenance" "official-association-site"
    "established-date" "1919-03-20" "retrieved-at" "2026-07-16"}
   {"id" "api.first-standard-oil-country-tubular-goods"
    "title" "Specifications for Steel and Iron Pipe for Oil Country Tubular Goods (API's first standard)"
    "association" "api" "isic" "0610" "country" "USA"
    "kind" "technical-standard" "url" "https://100yearsofstandards.api.org/"
    "url-provenance" "official-association-site"
    "established-date" "1924-10-20" "retrieved-at" "2026-07-16"}])

(deftest reference-preserves-complete-catalog-and-multiple-topics
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [index]
                         (into {} (map (fn [field]
                                         [field (present (call kir 'entry-field "api" index field))])
                                       fields)))
                       (range (call kir 'entry-count "api")))]
    (is (= expected observed))
    (is (= [1 2] (mapv #(call kir 'topic-count "api" %) [0 1])))
    (is (= ["governance"]
           (mapv #(present (call kir 'topic "api" 0 %)) [0])))
    (is (= ["technical-standard" "safety"]
           (mapv #(present (call kir 'topic "api" 1 %)) [0 1])))
    (is (= "api.about-api" (present (call kir 'by-topic-id "api" "governance" 0))))
    (is (= "api.first-standard-oil-country-tubular-goods"
           (present (call kir 'by-topic-id "api" "technical-standard" 0))))
    (is (= "api.first-standard-oil-country-tubular-goods"
           (present (call kir 'by-topic-id "api" "safety" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "unknown values and invalid indexes fail closed"
      (is (zero? (call kir 'entry-count "software-api")))
      (is (nil? (present (call kir 'entry-field "software-api" 0 "id"))))
      (is (nil? (present (call kir 'entry-field "api" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "api" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "api" 0 "unknown"))))
      (is (nil? (present (call kir 'topic "api" 0 1))))
      (is (nil? (present (call kir 'topic "api" 1 2))))
      (is (zero? (call kir 'by-topic-count "api" "labor")))
      (is (nil? (present (call kir 'by-topic-id "api" "safety" 1)))))))

(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))

(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8"))
        wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh
                "node" "--input-type=module" "-e"
                (str "import(process.argv[1]).then(async host=>{"
                     "const j=await import('data:text/javascript;base64," js64 "');"
                     "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));"
                     "const run=x=>{if(x['entry-count']('api')!==2n||x['entry-count']('software-api')!==0n)throw Error('count');"
                     "if(x['entry-field']('api',1n,'kind')[2]!=='technical-standard')throw Error('kind');"
                     "if(x['topic-count']('api',1n)!==2n||x['topic']('api',1n,0n)[2]!=='technical-standard'||x['topic']('api',1n,1n)[2]!=='safety')throw Error('topics');"
                     "if(x['by-topic-id']('api','safety',0n)[2]!=='api.first-standard-oil-country-tubular-goods')throw Error('filter');"
                     "if(x['topic']('api',1n,2n)[1]!==false||x['by-topic-id']('api','safety',1n)[1]!==false)throw Error('reject');};"
                     "run(j.instantiateKotoba({}));run(w.instance.exports);"
                     "}).catch(e=>{console.error(e);process.exit(99)})")
                (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))

(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
