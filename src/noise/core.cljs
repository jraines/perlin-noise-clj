(ns ^:figwheel-always noise.core
    (:require))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(def gradients
  "test"
  [[14 21]
   [14 -21]
   [21 14]
   [-21 14]
   [-21 -21]
   [21 -21]
   [21 21]
   [-21 21]])

(defn get-gradient [[a b]]
  (let [ndx (int (mod (+ a b) 7))]
    (get gradients ndx)))

(defn xPlusGrad [x y]
  (+ x (first (get-gradient [x y]))))

(defn yPlusGrad [x y]
  (+ y (second  (get-gradient [x y]))))

(defn drawGrid []
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "blue")
    (.translate ctx 0.5 0.5)
    (doseq [n (range 100 500 100)]
        (.beginPath ctx)
        (.moveTo ctx 100 n)
        (.lineTo ctx 400 n)
        (.stroke ctx)
        (.beginPath ctx)
        (.moveTo ctx n 100)
        (.lineTo ctx n 400)
        (.stroke ctx)
        )))

(defn drawGradients []
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "red")
    (doseq [x (range 100 500 100)]
      (doseq [y (range 100 500 100)]
        (.beginPath ctx)
        (.moveTo ctx x y)
        (.lineTo ctx (xPlusGrad x y) (yPlusGrad x y))
        (.stroke ctx)))))

(defn drawPoint []
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "green")
    (.fillRect ctx 221 139 3 3)))

(defn get-corners [x y]
  (let [nw [(- x (mod x 100)) (- y (mod y 100))]
        [x y] nw
        ne [(+ 100 x) y]
        sw [x (+ 100 y)]
        se [(+ 100 x) (+ 100 y)]]
    [nw se sw se])
  )

(defn drawPointVectors []
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")
        x 221
        y 139]
    (set! (.-strokeStyle ctx) "pink")
    (doseq [[cx cy] (get-corners x y)]
      (.beginPath ctx)
      (.moveTo ctx cx cy)
      (.lineTo ctx x y)
      (.stroke ctx)
      )))

(drawGrid)
(drawGradients)
(drawPoint)
(drawPointVectors)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

