(defproject google-play-music-sleep-timer "0.1.0-SNAPSHOT"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [org.clojure/core.async "0.3.443"]
                 [khroma "0.3.0"]
                 [prismatic/dommy "1.1.0"]
                 [reagent "0.8.0-alpha1"]
                 [reagent-material-ui "0.2.5"]
                 [cljsjs/moment "2.17.1-1"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]]
  :source-paths ["src"]
  :profiles {:dev
             {:plugins [[com.cemerick/austin "0.1.6"]
                        [lein-cljsbuild "1.1.7"]
                        [lein-chromebuild "0.3.0"]]
              :cljsbuild
              {:builds
               {:main
                {:source-paths ["src"]
                 :compiler {:output-to "target/unpacked/google_play_music_sleep_timer.js"
                            :output-dir "target/js"
                            :optimizations :whitespace
                            :pretty-print true}}}}}})
