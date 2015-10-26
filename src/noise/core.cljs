(ns ^:figwheel-always noise.core
    (:require))

(enable-console-print!)

;#Perlin Noise (naive & slow implementation)
;
;See http://flafla2.github.io/2014/08/09/perlinnoise.html
;
;Perlin noise is "pseudo-random" noise, which gives it a more
;organic look than truly random noise, because the noise function
;will be similar for two nearby points. This is accomplished by
;placing a grid on a coordinate plane and associating random vectors with each intersection point on the grid.
;For each point in the plane, its value is a function of the dot product between a set of
;vectors from the points to each corner of its bounding box, and
;the pseudorandom gradients associated with those corners.

(def gradients
  "generate 8 random gradients"
  (vec (take 8 (repeatedly (fn []
                             (let [x (rand)
                                   y (rand)
                                   mult (if (> (rand) (rand)) 1.0 -1.0)
                                   mult2 (if (> (rand) (rand)) 1.0 -1.0)]
                               [(* x mult) (* y mult2)]))))))

(defn get-gradient [[a b]]
  "find the gradient associated with a corner of a grid cell"
  (let [ndx (int (mod (+ a b) 7))]
    (get gradients ndx)))

(defn get-corners [x y]
  "for a given point, find the corners of the bounding box (grid cell) it is contained within"
  (let [nw [(- x (mod x 100)) (- y (mod y 100))]
        [x y] nw
        ne [(+ 100 x) y]
        sw [x (+ 100 y)]
        se [(+ 100 x) (+ 100 y)]]
    [nw ne sw se]))

(defn dot [X Y]
  "vector dot product"
  (reduce + (map * X Y)))

(defn lerp [t a b]
  "linear interpolation function"
  (+ a (* t (- b a))))

(defn ease [t]
  "ease function. This particular one is chosen because it is diff"
  (- (* 3 (.pow js/Math t 2))
     (* 2 (.pow js/Math t 3))))

(defn corner-gradients [x y]
  "find the gradients of each corner of the bounding box"
  (map get-gradient (get-corners x y)))

(defn corner-to-point-vectors [x y]
  "find the vectors from the point to the bounding box corners"
  (map (fn [[cx cy]] [(- x cx) (- y cy)])
       (get-corners x y)))

(defn influences [x y]
  "compute the 'influences' of the corner gradients, by taking the dot product
   of the corner gradient and the vector from the point to that corner"
  (let [gs (corner-gradients x y)
        vs (corner-to-point-vectors x y)]
    (map dot gs vs)))

(defn noise [x y]
  "compute the noise function value for a given pixel"
  (let [
        ;situate the point within a unit square
        rel-x (/ x 100)
        rel-y (/ y 100)

        ;find the coordinates within the unit square
        frac-x (mod rel-x 1)
        frac-y (mod rel-y 1)

        ;exaggerate proximity to corner
        Sx (ease frac-x)
        Sy (ease frac-y)

        ;compute influences of corner gradients
        [nw ne sw se] ((fn []
                         (influences x y)))

        ;linearly interpolate between the exaggerated point the "influenced" point
        a (lerp Sx nw ne)
        b (lerp Sx sw se)
        z (lerp Sy a b)]

    ;I forgot why this was needed :(
    (.abs js/Math (/ z 10))))


;;#drawing related stuff

;multiplication by 40 is just so the gradients are visible on the drawn grid
(defn xPlusGrad [x y]
  (+ x (* 40 (first (get-gradient [x y])))))

(defn yPlusGrad [x y]
  (+ y (* 40 (second  (get-gradient [x y])))))

(defn stroke [ctx {:keys [startx starty endx endy]}]
  "helper function to draw a stroke on the canvas"
  (.beginPath ctx)
  (.moveTo ctx startx starty)
  (.lineTo ctx endx endy)
  (.stroke ctx))

(defn drawGrid []
  "draw the grid which shows the corner vectors and vectors to corners for an example point"
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "blue")
    ;crisper lines on the canvas
    (.translate ctx 0.5 0.5)
    (doseq [n (range 100 500 100)]
      (stroke ctx
              { :startx 100 :starty n
                :endx 400 :endy n })
      (stroke ctx
              { :startx n :starty 100 
                :endx n :endy 400 }))))

(defn drawGradients []
  "draw the corner gradients on the example grid"
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "red")
    (doseq [x (range 100 500 100)]
      (doseq [y (range 100 500 100)]
        (stroke ctx
                {:startx x :starty y
                 :endx (xPlusGrad x y) :endy (yPlusGrad x y) })))))

(defn drawPoint []
  "draw the example point on the grid"
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")]
    (set! (.-strokeStyle ctx) "green")
    (.fillRect ctx 221 139 3 3)))

(defn drawPointVectors []
  "draw the vectors from the point to the corners of its bounding box"
  (let [canvas (.getElementById js/document "surface")
        ctx (.getContext canvas "2d")
        x 221
        y 139]
    (set! (.-strokeStyle ctx) "pink")
    (doseq [[cx cy] (get-corners x y)]
      (stroke ctx
              {:startx cx :starty cy
               :endx x :endy y }))))

(defn drawNoiseCanvas []
  "draw a canvas with the noise function computed for each pixel"
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
