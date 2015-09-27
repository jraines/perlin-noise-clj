(ns ^:figwheel-always noise.core
    (:require))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn foo []
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "black")
    ;(.translate ctx 0.5 0.5)
    (doseq [n (range 50.5 500 50)]
        (.beginPath ctx)
        (.moveTo ctx 0.5 n)
        (.lineTo ctx 500.5 n)
        (.stroke ctx)
        (.beginPath ctx)
        (.moveTo ctx n 0.5)
        (.lineTo ctx n 500.5)
        (.stroke ctx)
        ))
  )

(foo)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

