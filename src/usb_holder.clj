(ns usb_holder
  (:refer-clojure :exclude [use import])
  (:require [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))
(def usb-holder-clearance 0.3)
(def usb-holder-bottom-offset 
    (/ usb-holder-clearance 2)
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; USB Controller Holder ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def wall-thickness 2)
(def clearance 0.3)
(def ardumicro-height 10.28)
(def ardumicro-mountholes-vdist 45.35)
(def ardumicro-mountholes-hdist 15.00)
(def ardumicro-mountholes-rad 0.5)
(def ardumicro-mountholes-height 4)
(def ardumicro-extra-height 5.9)
(defn fb-wall-adj [length] (+ length wall-thickness))
(defn lr-wall-adj[length] (+ length (* 2 wall-thickness)))

(def ardumicro-length (+ 48.4 clearance))
(def ardumicro-width (+ 17.75 clearance))

(def promicro-length (+ 32.85 clearance))
(def promicro-width (+ 17.75 clearance))

(def promicro-mountstrip-width 7.50)
(def promicro-mountstrip-thickness 1.5)

(defn mcwidth [promicro] (if promicro promicro-width ardumicro-width))
(defn mclength [promicro] (if promicro promicro-length ardumicro-length))

(def ardumicro-mountholes-length (- ardumicro-length ardumicro-mountholes-vdist))
(def ardumicro-mountholes-width (- ardumicro-width ardumicro-mountholes-hdist))

(def ardumicro-usb-protrude 1.4)
(def ardumicro-usb-width (+ 7.8 clearance))
(def ardumicro-usb-height (+ 2.8 clearance))
(def ardumicro-usb-z-offset (+ 0.95 0.65))
(def ardumicro-usb-interface-thickness 1)
(def ardumicro-usb-cable-width (+ 11.10 clearance))
(def ardumicro-usb-cable-height (+ 7.00 clearance))

(def trrs-width (+ 6.06 clearance))
(def trrs-length (+ 12.08 clearance))
(def trrs-height ardumicro-height)
(def trrs-rad (+ 2.50 clearance))
(def trrs-protrude 2.10)

(def angle-cutout-offset 1)
(def angle-cutout-angle 7)

(def trrs-angle-cutout-offset 1.9)
(def trrs-angle-cutout-angle 13)

(def mount-thickness 4.2)
(def mount-insert-thickness 1.7)
(def mount-insert-depth 1.7)
(defn mount-width [promicro] (+ (mcwidth promicro) trrs-width (* wall-thickness 2) (* mount-insert-depth 2)))

(def reset-height (+ 12.8 clearance))
(def reset-ardumicro-tare (+ (* 2 mount-insert-thickness) reset-height ardumicro-height))
(def reset-protrude 1)
(def reset-width (+ 12.8 clearance))
(def reset-rad (+ (/ 6.75 2) clearance))

(def total-height (+ reset-ardumicro-tare ardumicro-extra-height))
(def cutout-height (+ total-height (* 2 usb-holder-clearance)))

(def ardumicro-total-height (+ ardumicro-height ardumicro-extra-height))

(def ardumicro-mounthole (translate [0 0 0] (union 
                           (translate [0 0 0] (cube ardumicro-mountholes-width ardumicro-mountholes-length ardumicro-extra-height :center false))
                           (translate [(/ ardumicro-mountholes-width 2) (/ ardumicro-mountholes-length 2) (+ (/ ardumicro-mountholes-height 2) ardumicro-extra-height)] (with-fn 64 (cylinder ardumicro-mountholes-rad ardumicro-mountholes-height)))
                         )))

(defn mount-base [promicro cutout] (let [total-height (if cutout cutout-height total-height)] (translate [(if cutout 0 (- (+ trrs-width (* mount-insert-depth 2)))) (fb-wall-adj (mclength promicro)) (- total-height)] (difference 
                           (translate [0 0 0] (cube (mount-width promicro) mount-thickness total-height :center false))
                           (translate [0 (/ (- mount-thickness mount-insert-thickness) 2) 0] (cube mount-insert-depth mount-insert-thickness total-height :center false))
                           (translate [(- (mount-width promicro) mount-insert-depth) (/ (- mount-thickness mount-insert-thickness) 2) 0] (cube mount-insert-depth mount-insert-thickness total-height :center false))
                         ))))

(defn ardumicro-place [shape] (translate [0 0 (- total-height)] shape))

(def ardumicro-mountholes (ardumicro-place (union
                             (translate [wall-thickness wall-thickness 0] ardumicro-mounthole)
                             (translate [(+ wall-thickness ardumicro-mountholes-hdist) wall-thickness 0] ardumicro-mounthole)
                             (translate [wall-thickness (+ wall-thickness ardumicro-mountholes-vdist) 0] ardumicro-mounthole)
                             (translate [(+ wall-thickness ardumicro-mountholes-hdist) (+ wall-thickness ardumicro-mountholes-vdist) 0] ardumicro-mounthole)
                            )))

(def promicro-mountstrip (translate [(/ (lr-wall-adj ardumicro-width) 2) (+ wall-thickness (/ promicro-length 2)) (+ (- total-height) (- (/ promicro-mountstrip-thickness 2)) ardumicro-extra-height)] (cube promicro-mountstrip-width promicro-length promicro-mountstrip-thickness )))

(defn microcontroller-holder [promicro]
                      (ardumicro-place 
                        (union (difference 
                            (cube (lr-wall-adj (mcwidth promicro)) (fb-wall-adj (mclength promicro)) ardumicro-total-height :center false)
                            (translate [wall-thickness wall-thickness 0] (cube (mcwidth promicro) (mclength promicro) ardumicro-total-height :center false))
                          )
                        )
                      )
)
    
(defn trrs-place [promicro shape] (translate [(- trrs-width) (- (+ (fb-wall-adj (mclength promicro)) (- mount-thickness trrs-protrude)) (+ trrs-length wall-thickness)) (- total-height)] shape))

(defn trrs-cutout [promicro] (trrs-place promicro (translate [0 wall-thickness ardumicro-extra-height] (cube trrs-width trrs-length ardumicro-height :center false))))

; angled cutout to allow you to slide the TRRS into the mount
(defn trrs-angle-cutout [promicro] (trrs-place promicro (->> (cube trrs-width trrs-length ardumicro-height :center false)
                                        (rotate (deg2rad trrs-angle-cutout-angle) [1 0 0])
                                        (translate [0 wall-thickness (+ ardumicro-extra-height trrs-angle-cutout-offset)])
                                        )))

(defn trrs-holder [promicro] (difference 
                   (trrs-place promicro (cube trrs-width (fb-wall-adj trrs-length) ardumicro-total-height :center false))
                   (trrs-cutout promicro)
                   (trrs-angle-cutout promicro)
                 )
)

(defn mc-usb-cutout [promicro] (translate [(/ (lr-wall-adj (mcwidth promicro)) 2) (+ (/ ardumicro-usb-protrude 2) (fb-wall-adj (mclength promicro))) (- (/ ardumicro-height 2) reset-ardumicro-tare)] (cube ardumicro-usb-width ardumicro-usb-protrude ardumicro-height)))

(defn mc-usb-place [promicro shape] (translate [(/ (lr-wall-adj (mcwidth promicro)) 2) (+ (fb-wall-adj (mclength promicro))) (- (+ ardumicro-usb-z-offset (/ ardumicro-usb-height 2)) reset-ardumicro-tare)] shape))

(defn trrs-hole-place [promicro shape] (translate [(- (/ trrs-width 2)) (+ (fb-wall-adj (mclength promicro))) (- trrs-rad reset-ardumicro-tare)] shape))

(defn reset-place [promicro shape] (translate [(/ (lr-wall-adj (mcwidth promicro)) 2) (+ (fb-wall-adj (mclength promicro))) (- (+ mount-insert-depth (/ reset-height 2)))] shape))

(defn reset-cutout [promicro] (let [insert-thickness (- mount-thickness reset-protrude)] (reset-place promicro (union 
  (translate [0 (/ insert-thickness 2) 0] (cube reset-width insert-thickness reset-height))
  (translate [0 (+ insert-thickness (/ reset-protrude 2) ) 0] (rotate (deg2rad 90) [1 0 0] (with-fn 64 (cylinder reset-rad reset-protrude))))
))))

(defn trrs-hole-cutout [promicro] (trrs-hole-place promicro
                             (translate [0 (/ (+ mount-thickness clearance) 2) 0]
                                (rotate (deg2rad 90) [1 0 0] (with-fn 64 (cylinder trrs-rad (+ mount-thickness clearance))))
                             )
                            ))

(defn mc-hole-cutout [promicro] (mc-usb-place promicro
                             (union
                               (translate [0 (/ mount-thickness 2) 0] (union
                                          (cube (- ardumicro-usb-width ardumicro-usb-height) mount-thickness ardumicro-usb-height)
                                          (let [circ (rotate (deg2rad 90) [1 0 0] (with-fn 64 (cylinder (/ ardumicro-usb-height 2) mount-thickness)))]
                                            (union
                                              (translate [(- (/ (- ardumicro-usb-width ardumicro-usb-height) 2)) 0 0] circ)
                                              (translate [(/ (- ardumicro-usb-width ardumicro-usb-height) 2) 0 0] circ)
                                            )
                                          )
                               ))
                               (translate [0 (+ (/ mount-thickness 2) ardumicro-usb-protrude ardumicro-usb-interface-thickness) 0] (cube ardumicro-usb-cable-width mount-thickness ardumicro-usb-cable-height))
                             )
                            ))

(defn angle-cutout [promicro] (->> (cube (+ (lr-wall-adj (mcwidth promicro)) trrs-width) (fb-wall-adj (mclength promicro)) ardumicro-height :center false)
                    (rotate (deg2rad angle-cutout-angle) [1 0 0])
                    (translate [(- trrs-width) 0 (- angle-cutout-offset reset-ardumicro-tare)])
                  ))


(defn mount [promicro] (difference (mount-base promicro false) (trrs-cutout promicro) (trrs-hole-cutout promicro) (mc-hole-cutout promicro) (mc-usb-cutout promicro) (reset-cutout promicro)))

(defn usb-holder-shift [promicro shape] (translate [(+ trrs-width (* mount-insert-depth 2)) (- (+ (fb-wall-adj (mclength promicro)) mount-thickness)) (+ total-height usb-holder-bottom-offset)] shape))
(defn usb-holder-space-shift [promicro shape] (translate [0 (- (+ (fb-wall-adj (mclength promicro)) mount-thickness)) 0] shape))
(defn usb-holder [promicro] (usb-holder-shift promicro (union (difference (union (mount promicro) (trrs-holder promicro) (microcontroller-holder promicro)) (angle-cutout promicro)) (if promicro promicro-mountstrip ardumicro-mountholes))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; USB Controller Holder Cutout ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def usb-holder-cutout-height cutout-height)
(defn body-cutout [promicro] (translate [0 0 (- cutout-height)] (union
  (cube (mount-width promicro) (fb-wall-adj (mclength promicro)) cutout-height :center false)
  (translate [0 (+ (fb-wall-adj (mclength promicro)) mount-thickness) 0] (cube (mount-width promicro) mount-thickness cutout-height :center false)) ; cut out some space in the front too
)))

(defn usb-holder-cutout' [promicro] (usb-holder-space-shift promicro (union (union (mount-base promicro true) (body-cutout promicro)))))

(defn usb-holder-cutout [promicro]
      (extrude-linear {:height usb-holder-cutout-height :twist 0 :convexity 0}
        (offset usb-holder-clearance (project
            (scale [1.001 1 1] (usb-holder-cutout' promicro))
          )
        )
      ))

(def usb-holder-cutout-bottom-offset (/ usb-holder-cutout-height 2))

(defn usb-holder-space [promicro]
  (union
    (translate [0 0 usb-holder-cutout-bottom-offset]
      (usb-holder-cutout promicro)
    )
  )
)
