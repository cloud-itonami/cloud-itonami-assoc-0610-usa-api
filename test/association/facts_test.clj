(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest api-has-spec-basis
  (let [sb (facts/spec-basis "api")]
    (is (= 2 (count sb)))
    (is (every? #(= "0610" (:association-rule/isic %)) sb))
    (is (every? #(= "USA" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "wef")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["api" "wef"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["wef"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["api.first-standard-oil-country-tubular-goods"]
         (mapv :association-rule/id (facts/by-topic "api" :technical-standard))))
  (is (empty? (facts/by-topic "api" :labor)))
  (is (empty? (facts/by-topic "wef" :governance))))
