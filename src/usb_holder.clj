(ns usb_holder
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [clojure.string :as str]
            [scad-clj.scad :refer :all]
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
(def ardumicro-length' 48.4)
(def ardumicro-width' 17.75)
(def ardumicro-height 10.28)
(def ardumicro-mountholes-vdist 45.35)
(def ardumicro-mountholes-hdist 15.00)
(def ardumicro-mountholes-rad 0.5)
(def ardumicro-mountholes-height 4)
(def ardumicro-extra-height 5.9)
(defn fb-wall-adj [length] (+ length wall-thickness))
(defn lr-wall-adj[length] (+ length (* 2 wall-thickness)))

(def ardumicro-length (+ ardumicro-length' clearance))
(def ardumicro-width (+ ardumicro-width' clearance))

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
(def mount-width (+ ardumicro-width trrs-width (* wall-thickness 2) (* mount-insert-depth 2)))

(def reset-height (+ 12.8 clearance))
(def reset-ardumicro-tare (+ (* 2 mount-insert-thickness) reset-height ardumicro-height))
(def reset-protrude 1)
(def reset-width (+ 12.8 clearance))
(def reset-rad (+ (/ 6.75 2) clearance))

(def total-height (+ reset-ardumicro-tare ardumicro-extra-height))
(def cutout-height (+ total-height (* 2 usb-holder-clearance)))

(def ardumicro-total-height (+ ardumicro-height ardumicro-extra-height))

(def usb-holder-stl (import "../things/usb_holder_w_reset.stl"))

(def ardumicro-mounthole (translate [0 0 0] (union 
                           (translate [0 0 0] (cube ardumicro-mountholes-width ardumicro-mountholes-length ardumicro-extra-height :center false))
                           (translate [(/ ardumicro-mountholes-width 2) (/ ardumicro-mountholes-length 2) (+ (/ ardumicro-mountholes-height 2) ardumicro-extra-height)] (with-fn 64 (cylinder ardumicro-mountholes-rad ardumicro-mountholes-height)))
                         )))

(defn mount-base [cutout] (let [total-height (if cutout cutout-height total-height)] (translate [(if cutout 0 (- (+ trrs-width (* mount-insert-depth 2)))) (fb-wall-adj ardumicro-length) (- total-height)] (difference 
                           (translate [0 0 0] (cube mount-width mount-thickness total-height :center false))
                           (translate [0 (/ (- mount-thickness mount-insert-thickness) 2) 0] (cube mount-insert-depth mount-insert-thickness total-height :center false))
                           (translate [(- mount-width mount-insert-depth) (/ (- mount-thickness mount-insert-thickness) 2) 0] (cube mount-insert-depth mount-insert-thickness total-height :center false))
                         ))))

(defn ardumicro-place [shape] (translate [0 0 (- total-height)] shape))
(def ardumicro-mountholes (ardumicro-place (union
                             (translate [wall-thickness wall-thickness 0] ardumicro-mounthole)
                             (translate [(+ wall-thickness ardumicro-mountholes-hdist) wall-thickness 0] ardumicro-mounthole)
                             (translate [wall-thickness (+ wall-thickness ardumicro-mountholes-vdist) 0] ardumicro-mounthole)
                             (translate [(+ wall-thickness ardumicro-mountholes-hdist) (+ wall-thickness ardumicro-mountholes-vdist) 0] ardumicro-mounthole)
                            )))
    
(def ardumicro-holder (ardumicro-place 
                        (union (difference 
                            (cube (lr-wall-adj ardumicro-width) (fb-wall-adj ardumicro-length) ardumicro-total-height :center false)
                            (translate [wall-thickness wall-thickness 0] (cube ardumicro-width ardumicro-length ardumicro-total-height :center false))
                          )
                        )
                      )
  )

(defn trrs-place [shape] (translate [(- trrs-width) (- (+ (fb-wall-adj ardumicro-length) (- mount-thickness trrs-protrude)) (+ trrs-length wall-thickness)) (- total-height)] shape))

(def trrs-cutout (trrs-place (translate [0 wall-thickness ardumicro-extra-height] (cube trrs-width trrs-length ardumicro-height :center false))))

; angled cutout to allow you to slide the TRRS into the mount
(def trrs-angle-cutout (trrs-place (->> (cube trrs-width trrs-length ardumicro-height :center false)
                                        (rotate (deg2rad trrs-angle-cutout-angle) [1 0 0])
                                        (translate [0 wall-thickness (+ ardumicro-extra-height trrs-angle-cutout-offset)])
                                        )))

(def trrs-holder (difference 
                   (trrs-place (cube trrs-width (fb-wall-adj trrs-length) ardumicro-total-height :center false))
                   trrs-cutout
                   trrs-angle-cutout
                 )
)

(def ardumicro-usb-cutout (translate [(/ (lr-wall-adj ardumicro-width) 2) (+ (/ ardumicro-usb-protrude 2) (fb-wall-adj ardumicro-length)) (- (/ ardumicro-height 2) reset-ardumicro-tare)] (cube ardumicro-usb-width ardumicro-usb-protrude ardumicro-height)))

(defn ardumicro-usb-place [shape] (translate [(/ (lr-wall-adj ardumicro-width) 2) (+ (fb-wall-adj ardumicro-length)) (- (+ ardumicro-usb-z-offset (/ ardumicro-usb-height 2)) reset-ardumicro-tare)] shape))

(defn trrs-hole-place [shape] (translate [(- (/ trrs-width 2)) (+ (fb-wall-adj ardumicro-length)) (- trrs-rad reset-ardumicro-tare)] shape))

(defn reset-place [shape] (translate [(/ (lr-wall-adj ardumicro-width) 2) (+ (fb-wall-adj ardumicro-length)) (- (+ mount-insert-depth (/ reset-height 2)))] shape))

(def reset-cutout (let [insert-thickness (- mount-thickness reset-protrude)] (reset-place (union 
  (translate [0 (/ insert-thickness 2) 0] (cube reset-width insert-thickness reset-height))
  (translate [0 (+ insert-thickness (/ reset-protrude 2) ) 0] (rotate (deg2rad 90) [1 0 0] (with-fn 64 (cylinder reset-rad reset-protrude))))
))))

(def trrs-hole-cutout (trrs-hole-place
                             (translate [0 (/ (+ mount-thickness clearance) 2) 0]
                                (rotate (deg2rad 90) [1 0 0] (with-fn 64 (cylinder trrs-rad (+ mount-thickness clearance))))
                             )
                            ))

(def ardumicro-hole-cutout (ardumicro-usb-place
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

(def angle-cutout (->> (cube (+ (lr-wall-adj ardumicro-width) trrs-width) (fb-wall-adj ardumicro-length) ardumicro-height :center false)
                    (rotate (deg2rad angle-cutout-angle) [1 0 0])
                    (translate [(- trrs-width) 0 (- angle-cutout-offset reset-ardumicro-tare)])
                  ))


(def mount (difference (mount-base false) trrs-cutout trrs-hole-cutout ardumicro-hole-cutout ardumicro-usb-cutout reset-cutout))

(defn usb-holder-shift [shape] (translate [(+ trrs-width (* mount-insert-depth 2)) (- (+ (fb-wall-adj ardumicro-length) mount-thickness)) (+ total-height usb-holder-bottom-offset)] shape))
(defn usb-holder-space-shift [shape] (translate [0 (- (+ (fb-wall-adj ardumicro-length) mount-thickness)) 0] shape))
(def usb-holder (usb-holder-shift (union (difference (union mount trrs-holder ardumicro-holder) angle-cutout) ardumicro-mountholes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; USB Controller Holder Cutout ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def usb-holder-cutout-height cutout-height)
(def body-cutout (translate [0 0 (- cutout-height)] (cube mount-width (fb-wall-adj ardumicro-length) cutout-height :center false)))

(def usb-holder-cutout' (usb-holder-space-shift (union (union (mount-base true) body-cutout))))

(def usb-holder-cutout 
      (extrude-linear {:height usb-holder-cutout-height :twist 0 :convexity 0}
        (offset usb-holder-clearance (project
            (scale [1.001 1 1] usb-holder-cutout')
          )
        )
      ))

(def usb-holder-cutout-bottom-offset (/ usb-holder-cutout-height 2))

(def usb-holder-space
  (union
    (translate [0 0 usb-holder-cutout-bottom-offset]
      usb-holder-cutout
    )
  )
)
