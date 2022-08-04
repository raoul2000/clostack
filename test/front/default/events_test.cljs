(ns default.events-test
  (:require [cljs.test :refer (deftest is)]
            [default.events :as e]))

(deftest saying-hi-handler-test
  (is (= {:saying-hi true} (e/saying-hi-handler {} [:ev true])))
  (is (= {:k1 10
          :saying-hi true} (e/saying-hi-handler {:k1 10} [:ev true]))))

(deftest said-hi-success-handler-test
  (is (= {:db {:said-hi-success "result"
               :greet-from-server nil}
          :fx [[:dispatch-later {:ms 1000, :dispatch [:saying-hi false]}]]}
         (e/said-hi-success-handler {:db {}
                                     :fx []} [:event "result"]))))

(deftest said-hi-failure-handler-test
  (is (= {:db {:said-hi-error "result"}
          :fx [[:dispatch [:saying-hi false]]]}
         (e/said-hi-failure-handler {:db {}
                                     :fx []} [:event "result"]))))

(deftest say-hi-handler-test

  (let [result (e/say-hi-handler {:db {}
                                  :fx [[:dispatch [:some-event]]]} [:event "bob"])]
    (is (= "bob" (get-in result [:db :username])))
    (is (= "/greet" (get-in result [:http-xhrio :uri])))
    (is (= "bob" (:say-hi-to-console result)))
    (is (=  [[:dispatch [:some-event]]
             [:dispatch [:saying-hi true]]] (:fx result))
        "dispatch event is appended to the existing :fx vector")))