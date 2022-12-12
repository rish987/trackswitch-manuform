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

(def usb-holder-stl (import "../things/usb_holder_w_reset.stl"))
    
(def usb-holder (usb-holder-place usb-holder-stl))

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
