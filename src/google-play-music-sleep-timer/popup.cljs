(ns google-play-music-sleep-timer.popup
  (:require [clojure.string :as string]
            [khroma.runtime :as runtime]
            [khroma.log :as console]
            [cljs.core.async :refer [>! <! chan]]
            [reagent.core :as r]
            [reagent-material-ui.core :as ui]
            [cljsjs.moment]
            [google-play-music-sleep-timer.services.messaging :as messaging]
            [google-play-music-sleep-timer.utils.timer :refer [wrap unwrap]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def empty-timer {:time nil
                  :type "turn-off"})
(defonce new-timer (r/atom empty-timer))
(defonce schedule (r/atom '()))

(defn request-schedule
  []
  (messaging/send-message :background!request-schedule))

(defn- set-schedule
  [new-schedule]
  (console/log new-schedule)
  (reset! schedule new-schedule))

(defn classnames
  [declaration]
  (reduce-kv (fn [memo k v]
               (if (true? v)
                 (str memo " " k)
                 memo)) "" declaration))

(defn create-schedule-item [timer]
  (messaging/send-message :background!add-to-schedule (wrap timer)))

(defn- remove-schedule-item
  [id]
  (.log js/console id))

(defn icon
  [props icon-name]
  (let [classname (classnames {"material-icons" true
                               "in-list-left"   (true? (:in-list-left props))
                               "in-list-right"  (true? (:in-list-right props))})]
    [ui/FontIcon {:class-name classname :color (:color props)} icon-name]))

(defn icon-button
  [props icon-name]
  (fn []
    [:div {:on-click #((:on-click props))}
     (icon props icon-name)]))

(defn new-timer-component [props]
  (let [timer (:timer props)
        schedule (:schedule props)
        on-timer-change (:on-timer-change props)
        on-timer-submit (:on-timer-submit props)
        styles {:radio-button {:margin "8px 0"}}]
    (fn []
      [ui/MuiThemeProvider {}
       [:div {:style {:width 300 :height 550}}
        [ui/AppBar {:title "Music Sleep Timer" :icon-element-left nil}]
        [ui/Card {:expanded true}
         [ui/CardHeader {:title "Stop playing music at"}]
         [ui/CardText
          [ui/TimePicker {:hint-text "Pick some time"
                          :value     (:time @timer)
                          :on-change #(on-timer-change (assoc @timer :time %2))}]
          [ui/RadioButtonGroup {:name           "type"
                                :value-selected (:type @timer)
                                :on-change      #(on-timer-change (assoc @timer :type %2))}
           [ui/RadioButton {:label "Turn music OFF"
                            :value "turn-off"
                            :style (:radio-button styles)
                            }]
           [ui/RadioButton {:label "Turn music ON"
                            :value "turn-on"
                            :style (:radio-button styles)}]]]
         [ui/CardActions
          [ui/RaisedButton {:on-click on-timer-submit
                            :disabled (nil? (:time @timer))
                            :primary  true} "SCHEDULE"]]]
        [ui/List
         (for [schedule-item @schedule
               :let [icon-name (if (= (:type schedule-item) "turn-off") "volume_off" "volume_up")
                     icon-color (if (= (:type schedule-item) "turn-off") ui/colors.red400 ui/colors.green400)]]
           ^{:key schedule-item}
           [ui/ListItem {:primary-text (.format (js/moment (:time schedule-item)) "HH:mm A")
                         :left-icon    (r/as-element [icon {:color        icon-color
                                                            :in-list-left true} icon-name])
                         :right-icon   (r/as-element [icon-button {:on-click #(remove-schedule-item (:id schedule-item))
                                                                   :in-list-right true} "delete"])}]
           )]
        ]
       ]
      )))

(defn schedule-manager []
  (new-timer-component {:timer           new-timer
                        :schedule        schedule
                        :on-timer-change #(reset! new-timer %)
                        :on-timer-submit (fn []
                                           (create-schedule-item @new-timer)
                                           (reset! new-timer empty-timer))}))

(defn- process-message
  [message]
  (let [signal (get message "signal")
        payload (get message "payload")]
    (case (get message "signal")
      "popup!set-schedule" (->> payload
                                (map unwrap)
                                (set-schedule))
      (console/log (str "Unknown signal " signal)))))

(defn mount []
  (r/render [schedule-manager]
            (.-body js/document)))

(defn init []
  ;(establish-connection)
  (messaging/listen process-message)
  (request-schedule)
  (mount))
