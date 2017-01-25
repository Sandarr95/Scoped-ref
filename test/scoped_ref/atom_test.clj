(ns scoped-ref.atom-test
  (:require [clojure.test :refer :all]
            [scoped-ref.atom :refer [scope]]))

(def test-atom (atom {:nest {:tree 0 :struct 0}}))

(def test-scope1 (scope test-atom [:nest :tree]))
(def test-scope2 (scope test-atom [:nest :struct]))

(deftest scoping
  (testing "Scope return value"
    (is (= 0 @test-scope1)))
  (testing "Scope record"
    (is (= (:root test-scope1) test-atom))
    (is (= (:path test-scope1) [:nest :tree]))))

(deftest swapping
  (testing "Scoped swapping"
    (is (= (inc (get-in @test-atom [:nest :tree])) (swap! test-scope1 inc) (get-in @test-atom [:nest :tree])))
    (is (= (swap! test-scope2 + 2) (get-in @test-atom [:nest :struct])))))

(deftest scopes
  (testing "Scopes still good"
    (is (= @test-atom (assoc-in @test-atom (:path test-scope1) @test-scope1)))))

(deftest resetting
  (testing "Reset scope"
    (is (= (reset! test-scope1 0) 0 @test-scope1 (get-in @test-atom (:path test-scope1))))))

(deftest watching
  (testing "Add watch to scope, only see changes to scope"
    (let [p (promise)]
      (add-watch test-scope1 :test-watcher (fn [_ _ _ n] (deliver p n)))
      (swap! test-scope2 inc)
      (is (not (realized? p)))
      (swap! test-scope1 inc)
      (is (realized? p))
      (is (= @p @test-scope1)))))
