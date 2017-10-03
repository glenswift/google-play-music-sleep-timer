(ns google-play-music-sleep-timer.utils.timer)

(defn wrap
  [timer]
  (assoc timer :time (.toString (:time timer))))

(defn unwrap
  [timer]
  (-> timer
      (assoc :time (js/Date. (get timer "time")))
      (assoc :type (get timer "type"))))
