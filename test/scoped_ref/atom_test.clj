(ns scoped-ref.atom-test
  (:require [clojure.test :refer :all]
            [scoped-ref.atom :refer [scope]]))

(defn init-test-atom [] (atom {:nest {:tree 0 :struct 0}}))

(defn init-test-scopes [a]
  [(scope a [:nest :tree])
   (scope a [:nest :struct])])

(deftest scoping
  (let [rat (init-test-atom)
        [s1 s2] (init-test-scopes rat)]
    (testing "Scope return value"
      (is (= @s1 @s2 0)))
    (testing "Scope record"
      (is (= (:root s1) (:root s2) rat))
      (is (= (:path s1) [:nest :tree]))
      (is (= (:path s2) [:nest :struct])))))

(deftest swapping
  (let [rat (init-test-atom)
        [s1 s2] (init-test-scopes rat)]
    (testing "Scoped swapping"
      (is (= (inc (get-in @rat [:nest :tree])) (swap! s1 inc) (get-in @rat [:nest :tree])))
      (is (= (swap! s2 + 2) (get-in @rat [:nest :struct]))))))

(deftest resetting
  (let [rat (init-test-atom)
        [s1 s2] (init-test-scopes rat)]
    (testing "Reset scope"
      (is (= (reset! s1 0) 0 @s1 (get-in @rat (:path s1)))))))

(deftest watching
  (let [rat (init-test-atom)
        [s1 s2] (init-test-scopes rat)
        p (promise)]
    (testing "Add watch to scope, only see changes to scope"
      (add-watch s1 :test-watcher (fn [_ _ _ n] (deliver p n)))
      (swap! s2 inc)
      (is (not (realized? p)))
      (swap! s1 inc)
      (is (realized? p))
      (is (= @p @s1)))))
