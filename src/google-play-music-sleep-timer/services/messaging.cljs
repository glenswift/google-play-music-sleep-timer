(ns google-play-music-sleep-timer.services.messaging
  (:require [khroma.log :as console]
            [khroma.runtime :as runtime]
            [cljs.core.async :refer [>! <!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn listen
  [func]
  (go-loop
    []
    (when-let [message (<! (runtime/on-message))]
      (func (:message message))
      (recur))))

(defn send-message
  ([signal]
   (send-message signal {}))
  ([signal payload]
   (runtime/send-message {:signal  signal
                          :payload payload})))
