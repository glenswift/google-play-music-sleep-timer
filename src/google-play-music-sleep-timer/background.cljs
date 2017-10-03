(ns google-play-music-sleep-timer.background
  (:require [khroma.log :as console]
            [khroma.runtime :as runtime]
            [cljs.core.async :refer [>! <!]]
            [cljs-uuid-utils.core :as uuid]
            [google-play-music-sleep-timer.services.messaging :as messaging]
            [google-play-music-sleep-timer.utils.timer :refer [wrap unwrap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def schedule-instances (atom {}))
(def schedule (atom '()))

(defn- send-schedule
  [schedule]
  (messaging/send-message :popup!set-schedule (map wrap schedule)))

(defn- add-to-schedule
  [timer]
  (swap! schedule #(sort-by :time (conj % timer))))

(defn- remove-from-schedule
  [id]
  (swap! schedule #(filter (fn [task]
                             (not= (:id task) id)) %)))

(defn- run-task
  [task]
  (remove-from-schedule (:id task))
  (send-schedule @schedule)
  (.log js/console "foo"))

(defn- run-schedule
  []
  ;(console/log (vals @schedule-instances))
  (doall (for [[_ timeout] @schedule-instances] (js/clearTimeout timeout)))
  (reset! schedule-instances (reduce (fn [memo task]
                                       (assoc
                                         memo
                                         (keyword (uuid/uuid-string (:id task)))
                                         (js/setTimeout #(run-task task) 10000))) {} @schedule)))

(defn- attach-id
  [timer]
  (assoc timer :id (uuid/make-random-uuid)))

(defn- process-message
  [message]
  (let [signal (get message "signal")
        payload (get message "payload")]
    (case (get message "signal")
      "background!add-to-schedule" (->> payload
                                        (unwrap)
                                        (attach-id)
                                        (add-to-schedule)
                                        ;(map wrap)
                                        (send-schedule))
      "background!request-schedule" (send-schedule @schedule)
      (console/log (str "Unknown signal " signal)))
    (run-schedule)))


(defn init []
  (messaging/listen process-message))
