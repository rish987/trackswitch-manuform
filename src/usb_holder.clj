(ns usb_holder
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [clojure.string :as str]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))
(def usb-holder-z-rotate 1.5)
(def usb-holder-clearance 0.3)
(def usb-holder-bottom-offset 
    (/ usb-holder-clearance 2)
)
(def usb-holder-offset-coordinates
    [-16 49.75 usb-holder-bottom-offset])

(defn usb-holder-place [shape]
  (->> shape
       (translate usb-holder-offset-coordinates)
       (rotate (deg2rad usb-holder-z-rotate) [0 0 1])
  ))

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
(def ardumicro-total-height (+ ardumicro-height ardumicro-extra-height))

(def ardumicro-mountholes-length (- ardumicro-length ardumicro-mountholes-vdist))
(def ardumicro-mountholes-width (- ardumicro-width ardumicro-mountholes-hdist))

(def trrs-width (+ 6.06 clearance))
(def trrs-length (+ 12.08 clearance))
(def trrs-rad (+ 2.50 clearance))
(def trrs-protrude 2.20)

(def mount-thickness 4.2)
(def mount-insert-thickness 1.7)
(def mount-insert-depth 1.7)
(def mount-width (+ ardumicro-width trrs-width (* wall-thickness 2) (* mount-insert-depth 2)))

(def usb-holder-stl (import "../things/usb_holder_w_reset.stl"))

(def ardumicro-mounthole (translate [0 0 0] (union 
                           (translate [0 0 0] (cube ardumicro-mountholes-width ardumicro-mountholes-length ardumicro-extra-height :center false))
                           (translate [(/ ardumicro-mountholes-width 2) (/ ardumicro-mountholes-length 2) (+ (/ ardumicro-mountholes-height 2) ardumicro-extra-height)] (cylinder ardumicro-mountholes-rad ardumicro-mountholes-height))
                         )))

(def mount (translate [(- (+ trrs-width (* mount-insert-depth 2))) (fb-wall-adj ardumicro-length) (- ardumicro-total-height)] (difference 
                           (translate [0 0 0] (cube mount-width mount-thickness ardumicro-total-height :center false))
                           (translate [0 (/ (- mount-thickness mount-insert-thickness) 2) 0] (cube mount-insert-depth mount-insert-thickness ardumicro-total-height :center false))
                           (translate [(- mount-width mount-insert-depth) (/ (- mount-thickness mount-insert-thickness) 2) 0] (cube mount-insert-depth mount-insert-thickness ardumicro-total-height :center false))
                         )))
    
(def trrs-holder (usb-holder-place usb-holder-stl))
(def ardumicro-holder (->> 
                        (union (difference 
                            (cube (lr-wall-adj ardumicro-width) (fb-wall-adj ardumicro-length) ardumicro-total-height :center false)
                            (translate [wall-thickness wall-thickness 0] (cube ardumicro-width ardumicro-length ardumicro-total-height :center false))
                          )

                          (translate [wall-thickness wall-thickness 0] ardumicro-mounthole)
                          (translate [(+ wall-thickness ardumicro-mountholes-hdist) wall-thickness 0] ardumicro-mounthole)
                          (translate [wall-thickness (+ wall-thickness ardumicro-mountholes-vdist) 0] ardumicro-mounthole)
                          (translate [(+ wall-thickness ardumicro-mountholes-hdist) (+ wall-thickness ardumicro-mountholes-vdist) 0] ardumicro-mounthole)
                        )
                        (translate [0 0 (- ardumicro-total-height)])
                      )
  )
(def usb-holder (union trrs-holder ardumicro-holder mount))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; USB Controller Holder Cutout ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def usb-holder-cutout-stl (import "../things/usb_holder_vertical_cutout.stl"))

(def usb-holder-cutout-height (+ 15 usb-holder-clearance))

(def usb-holder-cutout (usb-holder-place
      (extrude-linear {:height usb-holder-cutout-height :twist 0 :convexity 0}
        (offset usb-holder-clearance (project
            (scale [1.001 1 1] usb-holder-cutout-stl)
          )
        )
      )))

(def usb-holder-cutout-bottom-offset (/ usb-holder-cutout-height 2))

(def usb-holder-space
  (union
    (translate [0 0 usb-holder-cutout-bottom-offset]
      usb-holder-cutout
    )
  )
)
