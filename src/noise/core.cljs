(ns ^:figwheel-always noise.core
    (:require))

(enable-console-print!)

(def gradients
  (vec (take 8 (repeatedly (fn []
                             (let [x (rand)
                                   y (rand)
                                   mult (if (> (rand) (rand)) 1.0 -1.0)
                                   mult2 (if (> (rand) (rand)) 1.0 -1.0)]
                               [(* x mult) (* y mult2)]))))))

(defn get-gradient [[a b]]
  (let [ndx (int (mod (+ a b) 7))]
    (get gradients ndx)))

(defn get-corners [x y]
  (let [nw [(- x (mod x 100)) (- y (mod y 100))]
        [x y] nw
        ne [(+ 100 x) y]
        sw [x (+ 100 y)]
        se [(+ 100 x) (+ 100 y)]]
    [nw ne sw se]))

;vector dot product
(defn dot [X Y]
  "take the dot product of two vectors"
  (reduce + (map * X Y)))

;linear interpolation
(defn lerp [t a b]
  (+ a (* t (- b a))))

(defn ease [t]
  (- (* 3 (.pow js/Math t 2))
     (* 2 (.pow js/Math t 3))))

(defn corner-gradients [x y]
  (map get-gradient (get-corners x y)))

(defn corner-to-point-vectors [x y]
  (map (fn [[cx cy]] [(- x cx) (- y cy)])
       (get-corners x y)))

(defn influences [x y]
  (let [gs (corner-gradients x y)
        vs (corner-to-point-vectors x y)]
    (map dot gs vs)))

(defn noise [x y]
  (let [
        rel-x (/ x 100)
        rel-y (/ y 100)

        frac-x (mod rel-x 1)
        frac-y (mod rel-y 1)

        Sx (ease frac-x)
        Sy (ease frac-y)

        [nw ne sw se] ((fn []
                         (influences x y)))

        a (lerp Sx nw ne)
        b (lerp Sx sw se)
        z (lerp Sy a b)]
    (.abs js/Math (/ z 10))))


;;drawing related stuff

(defn xPlusGrad [x y]
  (+ x (* 40 (first (get-gradient [x y])))))

(defn yPlusGrad [x y]
  (+ y (* 40 (second  (get-gradient [x y])))))

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
      (.stroke ctx))))

(defn drawNoiseCanvas []
  (let [canvas (.getElementById js/document "noise")
        ctx (.getContext canvas "2d")]
    (doseq [x (range 100 400)
            y (range 100 400)
            :let [n  (int (* 256 (noise x y)))
                  color (str "rgb(" n "," n "," n ")")]]
      (set! (.-fillStyle ctx) color)
      (.fillRect ctx x y 1 1))))


(drawGrid)
(drawGradients)
(drawPointVectors)
(drawPoint)

(drawNoiseCanvas)
