(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [clojure.string :as str]
            [scad-clj.scad :refer :all]
            [usb_holder :refer [usb-holder usb-holder-cutout usb-holder-space]]
            [scad-clj.model :refer :all]))

;(def testing false)
(def testing true)

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

(defn rotate-x [angle shape]
  (rotate angle [1 0 0] shape))

(defn rotate-y [angle shape]
  (rotate angle [0 1 0] shape))

(defn rotate-z [angle shape]
  (rotate angle [0 0 1] shape))

(def usb-holder-z-rotate 7)
(def usb-holder-offset [-11.5 -0.4 0])

(defn rotate-around-x [angle position]
  (mmul
    [[1 0 0]
     [0 (Math/cos angle) (- (Math/sin angle))]
     [0 (Math/sin angle) (Math/cos angle)]]
    position))

(defn rotate-around-y [angle position]
  (mmul
    [[(Math/cos angle) 0 (Math/sin angle)]
     [0 1 0]
     [(- (Math/sin angle)) 0 (Math/cos angle)]]
    position))

(defn rotate-around-z [angle position]
  (mmul
    [[(Math/cos angle) (- (Math/sin angle)) 0]
     [(Math/sin angle) (Math/cos angle) 0]
     [0 0 1]]
    position))

(defn debug [shape]
  (color [0.5 0.5 0.5 0.5] shape))

(def WHI [255/255 255/255 255/255 1])
(def RED [255/255 0/255 0/255 1])
(def ORA [220/255 128/255 0/255 1])
(def YEL [220/255 255/255 0/255 1])
(def GRE [0/255 255/255 0/255 1])
(def DGR [21/255 71/255 52/255 1])
(def CYA [0/255 255/255 255/255 1])
(def BLU [0/255 128/255 255/255 1])
(def NBL [0/255 0/255 255/255 1])
(def PUR [127/255 0/255 255/255 1])
(def PIN [255/255 0/255 255/255 1])
(def MAG [255/255 0/255 127/255 1])
(def BRO [102/255 51/255 0/255 1])
(def BLA [0/255 0/255 0/255 1])
(def GRY [128/255 128/255 128/255 1])
(def TRNS [128/255 128/255 128/255 0.5])
(def SLT [112/255 128/255 144/255 1])

; (def KEYCAP [220/255 163/255 163/255 1])
(def KEYCAP [239/255 222/255 205/255 0.95])

(def TRIANGLE-RES 3)
(def SQUARE-RES 4)
(def ROUND-RES 30)

;;;;;;;;;;;;;;;;;;;;;;
;; Shape parameters ;;
;;;;;;;;;;;;;;;;;;;;;;

(def nrows 5)
(def ncols 6)

(def bottom-row false)

(def track-ball true)

;select only one of the following
(def use_flex_pcb_holder false) ; optional for flexible PCB, ameobas don't really benefit from this
(def use_hotswap_holder true)   ; kailh hotswap holder
(def use_solderless false)      ; solderless switch plate, RESIN PRINTER RECOMMENDED!
(def wire-diameter 1.75)        ; outer diameter of silicone covered 22awg ~1.75mm 26awg ~1.47mm)

(def controller-holder 1) ; 1=printed usb-holder; 2=pcb-holder
(def north_facing true)
(def extra-height-top-row true) ; raise numrow for mt3 and oem keycap profiles to match SA R1 num key height
(def extra-zheight-top-row 1.5)
(def extra-curve-bottom-row true) ; enable magic number curve of bottom two keys
(def tilt-outer-columns 7)        ; angle to tilt outer columns in degrees, adjust spacing where this is used if increased
(def recess-bottom-plate false)
(def adjustable-wrist-rest-holder-plate true)

; ** for two part designs only **
(def top-screw-insert-top-plate-bumps true) ; add additional threaded insert holder to top plate
(def hide-top-screws -1.5) ; 0 or 0.25 for resin prints, -1.5 for non-resin prints to not have holes cut through top plate

(def rendered-caps true) ; slows down model viewing but much nicer looking for more accurate clearances

(defn column-curvature [column] 
              (cond  (= column 0)  (deg2rad 22) ;;index outer
                     (= column 1)  (deg2rad 20) ;;index
                     (= column 2)  (deg2rad 17) ;;middle
                     (= column 3)  (deg2rad 17) ;;ring
                     (= column 4)  (deg2rad 24) ;;pinky
                     (>= column 5) (deg2rad 26) ;;pinky outer
                     :else 0 ))
(def row-curvature (deg2rad 1))  ; curvature of the rows
(defn centerrow [column] 
       (cond  (= column 0)  2.0 ;;index outer
              (= column 1)  2.0 ;;index
              (= column 2)  2.1 ;;middle
              (= column 3)  2.1 ;;ring
              (= column 4)  1.8 ;;pinky
              (>= column 5) 1.8 ;;pinky outer
              :else 0 ))

(def tenting-angle (deg2rad 20)) ; controls left-right tilt / tenting (higher number is more tenting) 
(def centercol 3)                ; Zero indexed, TODO: this should be 2.5 for a 6 column board, but it will break all the things now

(defn column-offset [column] (cond
                  (= column 0)  [0  -5  2  ] ;;index outer
                  (= column 1)  [0  -5  2  ] ;;index
                  (= column 2)  [0   -1.95 -2.1  ] ;;middle
                  (= column 3)  [0   -5.65 2.4] ;;ring
                  (= column 4)  [0 -12.5  6  ] ;;pinky
                  (>= column 5) [0 -12.5  6  ] ;;pinky outer
                  :else [0 0 0]))

(defn below-rot [column] (cond
                  (= column 1)  180 ;;index
                  (= column 2)  180 ;;middle
                  (= column 3)  180 ;;ring
                  (= column 4)  180 ;;pinky
                  :else 0))

(defn below-extra-dist [column] (cond
                  (= column 1)  2.5 ;;index
                  (= column 2)  2.5 ;;middle
                  (= column 3)  2.5 ;;ring
                  (= column 4)  1.5 ;;pinky
                  :else 0))

(defn below-x-rot [column] (cond
                  (= column 1)  (- 23) ;;index
                  (= column 2)  (- 23) ;;middle
                  (= column 3)  (- 23) ;;ring
                  (= column 4)  (- 47) ;;pinky -- keep this lower or else it will get in the way when actuating "s"
                  :else 0))

(defn below-z-rot [column] (cond
                  (= column 1)  0 ;;index
                  (= column 2)  0 ;;middle
                  (= column 3)  0 ;;ring
                  (= column 4)  0 ;;pinky
                  :else 0))

(defn above-rot [column] (cond
                  (= column 0)  32 ;;index outer
                  (= column 1)  0 ;;index
                  (= column 2)  0 ;;middle
                  (= column 3)  0 ;;ring
                  (= column 4)  0 ;;pinky
                  :else 0))

(defn above-extra-dist [column] (cond
                  (= column 0)  6.6 ;;index outer
                  (= column 1)  2 ;;index
                  (= column 2)  2.7 ;;middle
                  (= column 3)  2 ;;ring
                  (= column 4)  1.5 ;;pinky
                  :else 0))

(defn above-z-rot [column] (cond
                  (= column 0)  -21.9 ;;index outer
                  (= column 1)  0 ;;index
                  (= column 2)  0 ;;middle
                  (= column 3)  0 ;;ring
                  (= column 4)  0 ;;pinky
                  :else 0))

(defn above-x-off [column] (cond
                  (= column 0)  -7.6 ;;index outer
                  (= column 1)  0 ;;index
                  (= column 2)  0 ;;middle
                  (= column 3)  0 ;;ring
                  (= column 4)  0 ;;pinky
                  :else 0))

(defn above-z-off [column] (cond
                  (= column 0)  2.7 ;;index outer
                  (= column 1)  0 ;;index
                  (= column 2)  0 ;;middle
                  (= column 3)  0 ;;ring
                  (= column 4)  0 ;;pinky
                  :else 0))

(defn above-x-rot [column] (cond
                  (= column 0)  (- 37) ;;index outer
                  (= column 1)  (- 46.5) ;;index
                  (= column 2)  (- 46.5) ;;middle
                  (= column 3)  (- 46.5) ;;ring
                  (= column 4)  (- 46.5) ;;pinky
                  :else 0))

(def g-rot 100)
(def g-extra-dist 4.35)
(def g-x-off -0.35)
(def g-x-rot (- 43))
(def g-z-off -2.5)
(def g-z-rot (+ 0 (+ 8.5)))

(def p-rot (- 100))
(def p-extra-dist 3.9)
(def p-x-off 0.7)
(def p-x-rot (- 20))
(def p-z-off 6.26)
(def p-z-rot (+ 0 (- 18)))

(def v-key-case-extend 1.5)
(def v-key-case-wall-thickness 0.3)

(def thumb-u-rot 50)
(def thumb-u-z-rot -12)
(def thumb-u-z-off 3.5)
(def thumb-u-x-off (- 3))
(def thumb-u-x-rot (- 40.00))
(def thumb-u-extra-dist 4.5)

(def thumb-ub-rot 125)
(def thumb-ub-z-off 2.8)
(def thumb-ub-x-off (- 16.0))
(def thumb-ub-x-rot (- 27.00))
(def thumb-ub-extra-dist 6.5)

(def thumb-urr-rot (- 75))
(def thumb-urr-x-rot (- 40))
(def thumb-urr-extra-dist 5.2)
(def thumb-urr-x-off 6)
(def thumb-urr-z-off 0)

(def thumb-ur-rot 0)
(def thumb-ur-x-rot (- 20.00))
(def thumb-ur-z-off 0)
(def thumb-ur-x-off 0)
(def thumb-ur-extra-dist 4.2)

(def thumb-m-rot [ 8 -21.5 20])
(def thumb-m-move [-23 -15.2 -6])
(def thumb-r-rot [14 -35   10])
(def thumb-r-move [-4.5 -10 5])

(def keyboard-z-offset 25.5)  ; controls overall height

(def  extra-x 2)         ; extra horizontal space between the base of keys
(defn extra-y [column]   ; extra vertical space between the base of keys
          (cond  (= column 0)  2.1 ;;index outer
                 (= column 1)  1.9 ;;index
                 (= column 2)  1.7 ;;middle
                 (= column 3)  1.7 ;;ring
                 (= column 4)  2.0 ;;pinky
                 (>= column 5) 2.0 ;;pinky outer
                 :else 0 ))

(def wall-z-offset -7)  ; length of the first downward-sloping part of the wall (negative)
(def wall-xy-offset 1)
(def wall-thickness 1)  ; wall thickness parameter

(def thumb-pos [-10 -9 -19.8])
(def thumb-rot [0 -5 0] )
  
;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def firstrow 1)
(def firstcol 0)

(def lastrow (dec nrows))
(def real-lastrow (- nrows 2))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Trackball variables ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def dowel-depth-in-shell 0.9)
(def bearing-protrude (- 3 dowel-depth-in-shell)) ; Radius of the baring minus how deep it's going into the shell
(def trackball-width 35)
(def trackball-width-plus-bearing (+ bearing-protrude trackball-width 1)) ; Add one just to give some wiggle
(def holder-thickness' 4.2)
(def outer-width (+ (* 2 holder-thickness') trackball-width-plus-bearing))

(def axel-angle 15)
(def dowell-width 3)
(def dowel-top-change 0)
(def dowel-top-height 1.5)
(def dowell-height 6) ; Dowel height is actually 6mm. But attempting to get it to "snap" in place
(def dowell (union (with-fn 50 (cylinder (- (/ dowell-width 2) dowel-top-change) (+ dowell-height dowel-top-height))) (with-fn 50 (cylinder (/ dowell-width 2) dowell-height))))
(def bearing (with-fn 50 (cylinder (/ 8.5 2) 3))) ; Bearing is actually 6mm x 2.5mm, model it as 8.5mm x 3 to give it room to spin
(def dowell-bearing (rotate (deg2rad 90) [1 0 0] (union dowell bearing)))
(defn rotated_dowell [angle]
  (rotate (deg2rad angle) [0, 0, 1] (rotate (deg2rad axel-angle) [0, 1, 0] (
                                                                             translate [(+ (/ trackball-width-plus-bearing 2) dowel-depth-in-shell) 0 0] (union
                                                                                                                                                          ; Add a cube on the side of the dowell so there's an insertion point when we diff with the shell
                                                                                                                                                          (translate [(- (/ dowell-width 2)) 0 0] (cube (+ dowell-width 1) (- dowell-height dowel-top-change) dowell-width))
                                                                                                                                                          dowell-bearing
                                                                                                                                                          )
                                                                                       )))
  )
; We know the ball will sit approx bearing-protrude over the sensor holder. Eliminate the bottom and make it square
; up to that point with trim
(def trim (- (+ holder-thickness' bearing-protrude) 0.5))
(def bottom-trim-origin [0 0 (- (/ outer-width 2))])

(def mount-x-rotate -56)
(def mount-y-rotate -20)
(def mount-z-rotate 10)

(def sensor-x-rotate 0)
(def sensor-y-rotate 0)
(def sensor-z-rotate 10)

(def tb-mount-length 18)
(def tb-mount-width 23)
(def tb-mount-offset 3)

(def sensor-length 28)
(def sensor-width 21.5)
(def sensor-holder-width (/ sensor-width 2))
(def sensor-height 7.3)
(def sensor-screw-length (- 12 7.3))

;;;;;;;;;;;;;;;;;
;; Switch Hole ;;
;;;;;;;;;;;;;;;;;

(def keyswitch-height 13.8)
(def keyswitch-width 13.9)
(def plate-thickness 5)

(def retention-tab-thickness 1.5)
(def retention-tab-hole-thickness (- plate-thickness retention-tab-thickness))
(def mount-width (+ keyswitch-width 3))
(def mount-height (+ keyswitch-height 3))

(def holder-x mount-width)
(def holder-thickness    (/ (- holder-x keyswitch-width) 2))
(def holder-y            (+ keyswitch-height (* holder-thickness 2)))
(def swap-z              3)
(def web-thickness (if use_hotswap_holder (+ plate-thickness swap-z) plate-thickness))
(def keyswitch-below-plate (- 8 web-thickness)) ; approx space needed below keyswitch, ameoba is 6mm
(def square-led-size     6)

(def switch-teeth-cutout
  (let [
        ; cherry, gateron, kailh switches all have a pair of tiny "teeth" that stick out
        ; on the top and bottom, this gives those teeth somewhere to press into
        teeth-x        4.5
        teeth-y        0.75
        teeth-z-down   1.65 ;FIXME(rish) too low?
        teeth-z        (- plate-thickness teeth-z-down)
        teeth-x-offset 0
        teeth-y-offset (+ (/ keyswitch-height 2) (/ teeth-y 2.01))
        teeth-z-offset (- plate-thickness (/ teeth-z 1.99) teeth-z-down)
       ]
      (->> (cube teeth-x teeth-y teeth-z)
           (translate [teeth-x-offset teeth-y-offset teeth-z-offset])
      )
  )
)

(def hotswap-x2          (* (/ holder-x 3) 1.85))
(def hotswap-y1          4.3) ;first y-size of kailh hotswap holder
(def hotswap-y2          6.2) ;second y-size of kailh hotswap holder
(def hotswap-z           (+ swap-z 0.5));thickness of kailn hotswap holder + some margin of printing error (0.5mm)
(def hotswap-cutout-z-offset -2.6)
(def hotswap-cutout-2-x-offset (- (- (/ holder-x 4) 0.70)))
(def hotswap-cutout-1-y-offset 4.95)
(def hotswap-cutout-2-y-offset 4)
(def hotswap-case-cutout-x-extra 2.75)
(defn hotswap-case-cutout [mirror-internals]
  (let [shape (union
                (translate [0 
                            hotswap-cutout-1-y-offset 
                            hotswap-cutout-z-offset] 
                           (cube (+ keyswitch-width hotswap-case-cutout-x-extra) 
                                 hotswap-y1 
                                 hotswap-z))
                (translate [hotswap-cutout-2-x-offset 
                            hotswap-cutout-2-y-offset 
                            hotswap-cutout-z-offset]
                           (cube hotswap-x2 hotswap-y2 hotswap-z))
              )
        rotated
             (if north_facing
                 (->> shape
                      (mirror [1 0 0])
                      (mirror [0 1 0])
                 )
                 shape
             )
        mirrored 
          (->> (if mirror-internals
                   (->> rotated (mirror [1 0 0]))
                   rotated))
        ]
    mirrored
  )
)
(defn hotswap-holder [trackswitch-mount]
  ;irregularly shaped hot swap holder
  ;    ____________
  ;  |  _|_______|    |  hotswap offset from out edge of holder with room to solder
  ; y1 |_|_O__  \ _  y2  hotswap pin
  ;    |      \O_|_|  |  hotswap pin
  ;    |  o  O  o  |     fully supported friction holes
  ;    |    ___    |  
  ;    |    |_|    |  space for LED under SMD or transparent switches
  ;
  ; can be described as having two sizes in the y dimension depending on the x coordinate
  (let [
        swap-x              holder-x
        swap-y              holder-y
        
        swap-offset-x       0
        swap-offset-y       (/ (- holder-y swap-y) 2)
        swap-offset-z       (- (/ swap-z 2)) ; the bottom of the hole.
        swap-holder         (->> (cube swap-x swap-y swap-z)
                                 (translate [swap-offset-x 
                                             swap-offset-y
                                             swap-offset-z]))
        hotswap-x           holder-x ;cutout full width of holder instead of only 14.5mm
        hotswap-x3          (/ holder-x 4)
        hotswap-y3          (/ hotswap-y1 2)

        hotswap-cutout-1-x-offset 0.01
        hotswap-cutout-2-x-offset (- (/ holder-x 4.5))
        hotswap-cutout-3-x-offset (- (/ holder-x 2) (/ hotswap-x3 2.01))
        hotswap-cutout-4-x-offset (- (/ hotswap-x3 2.01) (/ holder-x 2))

        hotswap-cutout-3-y-offset 7.4 

        hotswap-cutout-led-x-offset 0
        hotswap-cutout-led-y-offset -6
        
        hotswap-cutout-1    (->> (cube hotswap-x hotswap-y1 hotswap-z)
                                 (translate [hotswap-cutout-1-x-offset 
                                             hotswap-cutout-1-y-offset 
                                             hotswap-cutout-z-offset]))
        hotswap-cutout-2    (->> (cube hotswap-x2 hotswap-y2 hotswap-z)
                                 (translate [hotswap-cutout-2-x-offset 
                                             hotswap-cutout-2-y-offset 
                                             hotswap-cutout-z-offset]))
        hotswap-cutout-3    (->> (cube hotswap-x3 hotswap-y3 hotswap-z)
                                 (translate [ hotswap-cutout-3-x-offset
                                              hotswap-cutout-3-y-offset
                                              hotswap-cutout-z-offset]))
        hotswap-cutout-4    (->> (cube hotswap-x3 hotswap-y3 hotswap-z)
                                 (translate [ hotswap-cutout-4-x-offset
                                              hotswap-cutout-3-y-offset
                                              hotswap-cutout-z-offset]))
        hotswap-led-cutout  (->> (cube square-led-size square-led-size 10)
                                 (translate [ hotswap-cutout-led-x-offset
                                              hotswap-cutout-led-y-offset
                                              hotswap-cutout-z-offset]))
        hotswap-cutout      (union hotswap-cutout-1
                                   hotswap-cutout-2
                                   hotswap-cutout-3
                                   hotswap-cutout-4)

        diode-wire-dia 0.75
        diode-wire-channel-depth (* 1.5 diode-wire-dia)
        diode-body-width 1.95
        diode-body-length 4
        diode-corner-hole (->> (cylinder diode-wire-dia (* 2 hotswap-z))
                              (with-fn ROUND-RES)
                              (translate [-6.55 -6.75 0]))
        diode-view-hole   (->> (cube (/ diode-body-width 2) (/ diode-body-length 1.25) (* 2 hotswap-z))
                              (translate [-6.25 -3 0]))
        diode-socket-hole-left (->> (cylinder diode-wire-dia hotswap-z)
                                    (with-fn ROUND-RES)
                                    (translate [-6.85 1.5 0]))
        diode-channel-pin-left (->> (cube diode-wire-dia 2.5 diode-wire-channel-depth)
                                    (rotate (deg2rad 10) [0 0 1])
                                    (translate [-6.55  0 (* -0.49 diode-wire-channel-depth)])
                               )
        diode-socket-hole-right (->> (cylinder diode-wire-dia hotswap-z)
                                    (with-fn ROUND-RES)
                                    (translate [6.85 3.5 0]))
        diode-channel-pin-right (->> (cube diode-wire-dia 6.5 diode-wire-channel-depth)
                                    (rotate (deg2rad -5) [0 0 1])
                                    (translate [6.55  0 (* -0.49 diode-wire-channel-depth)])
                               )
        diode-channel-wire (translate [-6.25 -5.75 (* -0.49 diode-wire-channel-depth)]
                               (cube diode-wire-dia 2 diode-wire-channel-depth))
        diode-body (translate [-6.25 -3.0 (* -0.49 diode-body-width)]
                       (cube diode-body-width diode-body-length diode-body-width))
        diode-cutout (union diode-corner-hole
                            diode-view-hole
                            diode-channel-wire
                            diode-body)

        ; for the main axis
        main-axis-hole      (->> (cylinder (/ 4.1 2) 10)
                                 (with-fn ROUND-RES))
        pin-hole            (->> (if trackswitch-mount (cube 3.0 3.0 10) (cylinder (/ 3.3 2) 10))
                                 (with-fn ROUND-RES))
        plus-hole           (translate [-3.81 2.54 0] pin-hole)
        minus-hole          (translate [ 2.54 5.08 0] pin-hole)
        friction-hole       (->> (cylinder (/ 1.95 2) 10)
                                 (with-fn ROUND-RES))
        friction-hole-right (translate [ 5 0 0] friction-hole)
        friction-hole-left  (translate [-5 0 0] friction-hole)
        hotswap-shape
            (difference 
                       ; (union 
                               swap-holder
                               ; (debug diode-channel-wire))
                        main-axis-hole
                        plus-hole
                        minus-hole
                        friction-hole-left
                        friction-hole-right
                        diode-cutout
                        diode-socket-hole-left
                        diode-channel-pin-left
                        (mirror [1 0 0] diode-cutout)
                        diode-socket-hole-right
                        diode-channel-pin-right
                        hotswap-cutout
                        hotswap-led-cutout)
       ]
       (if north_facing
           (->> hotswap-shape
                (mirror [1 0 0])
                (mirror [0 1 0])
           )
           hotswap-shape
       )
  )
)

(def solderless-plate
  (let [
        solderless-x        holder-x
        solderless-y        holder-y ; should be less than or equal to holder-y
        solderless-z        4;
        solderless-cutout-z (* 1.01 solderless-z)
        solderless-offset-x 0
        solderless-offset-y (/ (- holder-y solderless-y) 2)
        solderless-offset-z (- (/ solderless-z 2)) ; the bottom of the hole. 
        switch_socket_base  (cube solderless-x 
                                  solderless-y 
                                  solderless-z)
        wire-channel-diameter (+ 0.3 wire-diameter); elegoo saturn prints 1.75mm tubes ~1.62mm
        wire-channel-offset  (- (/ solderless-z 2) (/ wire-channel-diameter 3))
        led-cutout-x-offset  0
        led-cutout-y-offset -6
        led-cutout          (translate [0 -6 0] 
                                 (cube square-led-size 
                                       square-led-size 
                                       solderless-cutout-z))
        main-axis-hole      (->> (cylinder (/ 4.1 2) solderless-cutout-z)
                                 (with-fn ROUND-RES))
        plus-hole           (->> (cylinder (/ 1.55 2) solderless-cutout-z)
                                 (with-fn ROUND-RES)
                                 (scale [1 0.85 1])
                                 (translate [-3.81 2.54 0]))
        minus-hole          (->> (cylinder (/ 1.55 2) solderless-cutout-z)
                                 (with-fn ROUND-RES)
                                 (scale [1 0.85 1])
                                 (translate [2.54 5.08 0]))
        friction-hole       (->> (cylinder (/ 1.95 2) solderless-cutout-z)
                                 (with-fn ROUND-RES))
        friction-hole-right (translate [ 5 0 0] friction-hole)
        friction-hole-left  (translate [-5 0 0] friction-hole)

        diode-wire-dia 0.75
        diode-row-hole   (->> (cylinder (/ diode-wire-dia 2) solderless-cutout-z)
                              (with-fn ROUND-RES)
                              (translate [3.65 3.0 0]))
        diode-pin  (translate [-3.15 3.0 (/ solderless-z 2)]
                       (cube 2 diode-wire-dia 2))
        diode-wire (translate [2.75 3.0 (/ solderless-z 2)]
                       (cube 2 diode-wire-dia 2))
        diode-body (translate [-0.2 3.0 (/ solderless-z 2)]
                       (cube 4 1.95 3))

        row-wire-radius             (/ wire-channel-diameter 2)
        row-wire-channel-end-radius 3.25
        row-wire-channel-end (->> (circle row-wire-radius)
                                  (with-fn 50)
                                  (translate [row-wire-channel-end-radius 0 0])
                                  (extrude-rotate {:angle 90})
                                  (rotate (deg2rad 90) [1 0 0])
                                  (translate [(+ 7 (- row-wire-channel-end-radius)) 
                                              5.08 
                                              (+ wire-channel-offset (- row-wire-channel-end-radius))])
                             )
        row-wire-channel-ends (translate [8 5.08 -1.15] 
                                  (union (cube 3 wire-channel-diameter solderless-z)
                                         (translate [(/ 3 -2) 0 0] 
                                             (->> (cylinder (/ wire-channel-diameter 2) solderless-z)
                                                  (with-fn 50)))))
        row-wire-channel-cube-end (union (->> (cube wire-channel-diameter
                                                    wire-channel-diameter 
                                                    wire-channel-diameter)
                                              (translate [6 5.08 (+ 0 wire-channel-offset)])
                                         )
                                         (->> (cylinder (/ wire-channel-diameter 2)
                                                        wire-channel-diameter)
                                              (with-fn 50)
                                              (translate [5 5.08 (+ (/ wire-channel-diameter 2) wire-channel-offset)])
                                         )
                                  )
        row-wire-channel-curve-radius 45
        row-wire-channel (union
                             (->> (circle row-wire-radius)
                                  (with-fn 50)
                                  (translate [row-wire-channel-curve-radius 0 0])
                                  (extrude-rotate {:angle 90})
                                  (rotate (deg2rad 90) [1 0 0])
                                  (rotate (deg2rad -45) [0 1 0])
                                  (translate [0 
                                              5.08 
                                              (+ 0.25 wire-channel-offset (- row-wire-channel-curve-radius))])
                             )
                             row-wire-channel-end
                             row-wire-channel-ends
                             row-wire-channel-cube-end
                             (->> (union row-wire-channel-end
                                         row-wire-channel-ends
                                         row-wire-channel-cube-end
                                  )
                                  (mirror [1 0 0])
                             )
                         )
        col-wire-radius       (+ 0.025 (/ wire-channel-diameter 2))
        col-wire-ends-radius  (+ 0.1   (/ wire-channel-diameter 2))
        col-wire-ends-zoffset    0.0725 ; should be diff of two magic numbers above
        col-wire-channel-curve-radius 15
        col-wire-channel (->> (circle col-wire-radius)
                              (with-fn 50)
                              (translate [col-wire-channel-curve-radius 0 0])
                              (extrude-rotate {:angle 90})
                              (rotate (deg2rad 135) [0 0 1])
                              (translate [(+ 3.10 col-wire-channel-curve-radius) 
                                          0 
                                          (- 0.1 wire-channel-offset)])
                         )

        solderless-shape 
            (translate [solderless-offset-x 
                        solderless-offset-y
                        solderless-offset-z]
                (difference (union switch_socket_base
                                   ;(debug row-wire-channel-cube-end) ; may have to disable below to appear
                            )
                            main-axis-hole
                            plus-hole
                            minus-hole
                            friction-hole-left
                            friction-hole-right
                            diode-row-hole
                            row-wire-channel
                            col-wire-channel
                            diode-pin
                            diode-body
                            diode-wire
                            led-cutout
            ))
       ]
       (if north_facing
           (->> solderless-shape
                (mirror [1 0 0])
                (mirror [0 1 0])
           )
           solderless-shape
       )
  )
)

(def switch-dogbone-cutout
  (let [ cutout-radius 0.75
         cutout (->> (cylinder cutout-radius 99)
                     (with-fn 15))
         cutout-x (- (/ keyswitch-width  2) (/ cutout-radius 2))
         cutout-y (- (/ keyswitch-height 2) (/ cutout-radius 2))
       ]
    (union
      (translate [   cutout-x    cutout-y  0] cutout)
      (translate [(- cutout-x)   cutout-y  0] cutout)
      (translate [   cutout-x (- cutout-y) 0] cutout)
    )
  )
)

(def amoeba-x 1) ; mm width TODO wtf?
(def amoeba-y 16) ; mm high
(def keyswitch-below-clearance (/ keyswitch-below-plate -2))

(def switch-bottom
  (translate [0 0 keyswitch-below-clearance] 
             (cube amoeba-y 
                   amoeba-y 
                   keyswitch-below-plate)))

(def flex-pcb-holder
  (let [pcb-holder-x (* 0.99 amoeba-y); keyswitch-width
        pcb-holder-y 5
        pcb-holder-z 3 ;keyswitch-below-plate
        pcb-holder-z-offset (- (* 2 keyswitch-below-clearance) (/ pcb-holder-z 2))
        minus-hole          (->> (cylinder (/ 4 2) 99)
                                 (with-fn 15)
                                 (translate [2.54 5.08 0]))
       ]
  (union
        (difference
           (translate [0 
                   (/ keyswitch-height 2)
                   pcb-holder-z-offset]
              (difference (cube pcb-holder-x pcb-holder-y pcb-holder-z)
                          ;cut triangle out of pcb clip
                          (->> (cube (* 1.01 pcb-holder-x) pcb-holder-y pcb-holder-z)
                              (translate [0 0 (/ pcb-holder-z -1.25)])
                              (rotate (deg2rad -45) [1 0 0])
                          )
              )
           )
           minus-hole 
        )
        (translate [0 
                    (+ (/ keyswitch-height 2) (/ pcb-holder-y 3) )
                    keyswitch-below-clearance]
            (color YEL (cube pcb-holder-x 
                             (/ pcb-holder-y 3) 
                             (* 3 keyswitch-below-plate)))
        )
   )))

(def single-plate-wall-thickness 1.5)

(defn single-plate' [mirror-internals trackswitch-mount]
 ; (render ;tell scad to try and cache this repetitive code, kinda screws up previews
  (let [top-wall (->> (cube mount-height single-plate-wall-thickness plate-thickness)
                      (translate [0
                                  (+ (/ single-plate-wall-thickness 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube single-plate-wall-thickness mount-width plate-thickness)
                       (translate [(+ (/ single-plate-wall-thickness 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        plate-half (difference (union top-wall left-wall) 
                               switch-teeth-cutout
                               switch-dogbone-cutout)
        plate (union plate-half
                  (->> plate-half
                       (mirror [1 0 0])
                       (mirror [0 1 0]))
                  (when use_hotswap_holder (hotswap-holder trackswitch-mount))
                  (when use_solderless solderless-plate)
              )
       ]
    (->> (if mirror-internals
           (->> plate (mirror [1 0 0]))
           plate
         )
    )
  )
 ; )
)

(defn single-plate [mirror-internals] (single-plate' mirror-internals false))

(defn single-plate-cutout' [thickness] (translate [0 0 (/ thickness 2)] (cube (+ keyswitch-width single-plate-wall-thickness) (+ keyswitch-height single-plate-wall-thickness) thickness)))

(def single-plate-cutout (single-plate-cutout' plate-thickness))

(defn single-plate-extra-cutout' [thickness] (translate [0 0 (- plate-thickness (/ thickness 2))] (cube (+ keyswitch-width single-plate-wall-thickness) (+ keyswitch-height single-plate-wall-thickness) thickness)))

(def single-plate-extra-cutout (single-plate-extra-cutout' (/ web-thickness 2)))

(def single-plate-blank
    (union 
        (translate [0 0  (/ plate-thickness 2)]
            (cube mount-width
                  mount-height
                  (+ plate-thickness 0.001)
            )
        )
        (if use_hotswap_holder (translate [0 0 (- (/ hotswap-z 2))] 
                            (cube mount-width 
                                  mount-height 
                                  hotswap-z)))
        (if use_solderless (hull solderless-plate))
    )
)

(defn single-plate-cut [mirror-internals]
  (difference 
    single-plate-blank
    (single-plate mirror-internals)
  )
)

;;;;;;;;;;;;;;;;
;; SA Keycaps ;;
;;;;;;;;;;;;;;;;

(def sa-length 18.52)
(def sa-length1 18.52)
(def sa-length2 12.05)
(def sa-height 12.5)
(def sa-height1 7.15)
(def sa-height2 14.67)

(def sa-key-height-from-plate 7.39)
(def sa-cap-bottom-height (+ sa-key-height-from-plate plate-thickness))
(def sa-cap-bottom-height-pressed 0)
(def sa-cap-bottom-height-pressed' (+ sa-cap-bottom-height-pressed plate-thickness))

(def sa-double-length 37.5)
(defn sa-cap [keysize col row]
    (let [ bl2 (case keysize 1   (/ sa-length 2)
                             1.5 (/ sa-length 2)
                             2      sa-length   )
           bw2 (case keysize 1   (/ sa-length 2)
                             1.5 (/ 27.94 2)
                             2   (/ sa-length 2))
           m 8.25
           keycap-xy (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
           keycap-top (case keysize 1    (polygon [[6  6] [  6  -6] [ -6  -6] [-6  6]])
                                    1.52 (polygon [[11 6] [-11   6] [-11  -6] [11 -6]])
                                    2    (polygon [[6 16] [  6 -16] [ -6 -16] [-6 16]]))
           key-cap (hull (->> keycap-xy
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 0.05]))
                                (->> (polygon [[m m] [m (- m)] [(- m) (- m)] [(- m) m]])
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 (/ sa-height 2)]))
                                (->> keycap-top
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 sa-height])))
           rendered-cap-filename (case row 0 "../things/caps/matty3-deep-R1.stl"
                                           1 "../things/caps/matty3-deep-R2.stl"
                                           2 "../things/caps/matty3-deep-R3.stl"
                                           3 "../things/caps/matty3-deep-R4.stl"
                                           4 "../things/caps/matty3-deep-R5.stl"
                                           5 "../things/caps/matty3-deep-R5.stl")
           rendered-cap-filename-full ; (cond (= col 0)       (str/replace (str/replace rendered-cap-filename #"DD.stl" ".stl") #".stl" "L.stl")
                                            ; (= col lastcol) (str/replace (str/replace rendered-cap-filename #"DD.stl" ".stl") #".stl" "R.stl")
                                            ; :default         
                                            rendered-cap-filename
                                      ; )
           key-cap-display (if rendered-caps (import rendered-cap-filename-full)
                               key-cap)
         ]
         (union
           (->> key-cap-display
                (translate [0 0 sa-cap-bottom-height])
                (color KEYCAP))
           ; (debug (->> key-cap
           ;      (translate [0 0 sa-cap-bottom-height-pressed])))
         )
    )
)

(defn sa-cap-cutout [keysize]
    (let [ cutout-x 0.40
           cutout-y 0.30
           bl0 (/ 16.15 2)
           bw0 (/ 16.15 2)
           bl1 (case keysize 
                     1   (+ (/ sa-length1 2) cutout-y)
                     1.5 (+ (/ sa-length1 2) cutout-y)
                     2   (+ sa-length cutout-y))
           bw1 (case keysize
                     1   (+ (/ sa-length1 2) cutout-x)
                     1.5 (+ (/ 27.94 2) cutout-x)
                     2   (+ (/ sa-length1 2) cutout-x))
           bl2 (case keysize 
                     1   (+ (/ sa-length2 2) cutout-y)
                     1.5 (+ (/ sa-length2 2) cutout-y)
                     2   (+ sa-length cutout-y))
           bw2 (case keysize
                     1   (+ (/ sa-length2 2) cutout-x)
                     1.5 (+ (/ 27.94 2) cutout-x)
                     2   (+ (/ sa-length2 2) cutout-x))
           keycap-cutout-xy0 (polygon [[bw0 bl0] [bw0 (- bl0)] [(- bw0) (- bl0)] [(- bw0) bl0]])
           keycap-cutout-xy1 (polygon [[bw1 bl1] [bw1 (- bl1)] [(- bw1) (- bl1)] [(- bw1) bl1]])
           keycap-cutout-xy2 (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
           key-cap-cutout (hull (->> keycap-cutout-xy1
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 0.05]))
                                (->> keycap-cutout-xy1
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 (- sa-height1 sa-cap-bottom-height-pressed)]))
                                (->> keycap-cutout-xy2
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 (- sa-height2 sa-cap-bottom-height-pressed)]))
                          )
           bottom-height-cutout (hull (->> keycap-cutout-xy0
                                           (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                           (translate [0 0 0.05]))
                                      (->> keycap-cutout-xy0
                                           (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                           (translate [0 0 sa-cap-bottom-height-pressed]))
                                )
         ]
         (union
           (->> key-cap-cutout
              (translate [0 0 sa-cap-bottom-height-pressed']))
           (->> bottom-height-cutout
              (translate [0 0 plate-thickness]))
         )
    )
)

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def columns (range firstcol (inc lastcol)))
(def columns' (range (inc firstcol) lastcol))
(def rows (range firstrow (inc lastrow)))

(defn apply-key-geometry' [translate-fn rotate-x-fn rotate-y-fn column row shape]
  (let [; begin wonky code to handle tilted outer columns
        extra-row-tilt (if (> tilt-outer-columns 0)
                                (case column
                                    0  tilt-outer-columns
                                    5 (- tilt-outer-columns)
                                       0
                                )
                                0
                            )
        extra-x-for-tilt (if (> tilt-outer-columns 0)
                                 (case column
                                     0 0.55
                                     5 0.8
                                       0
                                 )
                                 0
                             )
        extra-y-for-tilt (if (> tilt-outer-columns 0)
                                 (case column
                                     0 0.5
                                     5 0.5
                                       0
                                 )
                                 0
                             )
        extra-z-for-tilt (if (> tilt-outer-columns 0)
                      (case column
                          0 2.0
                          5 0.75
                            0
                      )
                      0
                  )
        extra-width (+ extra-x extra-x-for-tilt)
        ; end wonky code to handle tilted outer columns

        extra-y-for-toprow (if (and extra-height-top-row (= row 0))
                               -0.5
                               0
                           )
        extra-z-for-toprow (if (and extra-height-top-row (= row 0))
                               extra-zheight-top-row
                               0
                           )

        ; being wonky bottom row extra rotation code
        extra-rotation (if (and extra-curve-bottom-row
                               (.contains [2 3] column)
                               (= row lastrow))
                           -0.4
                           (if (and extra-height-top-row (= row 0))
                               -0.1
                               0
                           )
                       )
        extra-rotation-offset (if (and extra-curve-bottom-row
                                       (= row lastrow))
                                  (case column
                                      3 0.095
                                      2 0.09
                                      0
                                  )
                                  0
                              )
        extra-rotation-zheight (if (and extra-curve-bottom-row 
                                        (= row lastrow))
                                   (case column
                                       3 7.5
                                       2 6.75
                                       0
                                   )
                                   0
                               )
        ; end wonky bottom row extra rotation code

        column-radius (+ (/ (/ (+ mount-width extra-width) 2)
                            (Math/sin (/ row-curvature 2)))
                         sa-cap-bottom-height)
        height-space (+ (extra-y column) extra-y-for-tilt extra-y-for-toprow)
        row-radius (+ (/ (/ (+ mount-height height-space) 2)
                         (Math/sin (/ (column-curvature column) 2)))
                      sa-cap-bottom-height)
        column-angle (* row-curvature (- centercol column))
        placed-shape (->> shape
                          (translate-fn [0 0 extra-z-for-tilt])
                          (rotate-y-fn (deg2rad extra-row-tilt))
                          (rotate-x-fn extra-rotation)
                          (translate-fn [0 0 extra-z-for-toprow])
                          (translate-fn [0 0 extra-rotation-zheight])
                          (translate-fn [0 0 (- row-radius)])
                          (rotate-x-fn (* (+ extra-rotation-offset (column-curvature column)) 
                                          (- (centerrow column) row)))
                          (translate-fn [0 0 row-radius])
                          (translate-fn [0 0 (- column-radius)])
                          (rotate-y-fn column-angle)
                          (translate-fn [0 0 column-radius])
                          (translate-fn (column-offset column))
                          )]

    (->> placed-shape
         ;(rotate-y-fn tenting-angle)
         ;(translate-fn [0 0 keyboard-z-offset])
         )))

(def homerow 2)

; distance from top of plate to top of keycap
(def key-top-dist sa-height2)

; distance from center of keycap to top edge of keycap
(def key-edge-dist (/ sa-length2 2))

(defn key-upper-place' [translate-fn rotate-x-fn rotate-y-fn rotate-z-fn extra-dist x-off z-off init-z-rot x-rot z-rot shape] (->> shape
  (rotate-z-fn (deg2rad init-z-rot))
  (rotate-x-fn (deg2rad 90))
  (translate-fn [0 (+ plate-thickness key-top-dist) key-edge-dist])
  (translate-fn [x-off 0 0])
  (rotate-x-fn (deg2rad x-rot))
  (translate-fn [0 (+ key-edge-dist extra-dist) (+ plate-thickness key-top-dist)])
  (translate-fn [0 0 z-off])
  (rotate-z-fn (deg2rad z-rot))
))

(defn key-upper-place-zrot [extra-dist x-off z-off z-init-rot x-rot z-rot shape] 
  (key-upper-place' translate rotate-x rotate-y rotate-z extra-dist x-off z-off z-init-rot x-rot z-rot shape)
)

(defn key-upper-place [extra-dist x-off z-off x-rot z-rot shape] 
  (key-upper-place' translate rotate-x rotate-y rotate-z extra-dist x-off z-off 0 x-rot z-rot shape)
)

(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn rotate-z-fn column row shape]
  (if (and (or (= row homerow)) (not (and (= row homerow) (or (= column 0) (= column 5)))))
    (apply-key-geometry' translate-fn rotate-x-fn rotate-y-fn column row shape)
    (if (= row homerow)
      (if (= column 0)
        (->> 
             (key-upper-place' translate-fn rotate-x-fn rotate-y-fn rotate-z-fn g-extra-dist g-x-off g-z-off g-z-rot g-x-rot g-rot shape)
             (apply-key-geometry' translate-fn rotate-x-fn rotate-y-fn 1 homerow)
        )
        ; column 5
        (->> 
             (key-upper-place' translate-fn rotate-x-fn rotate-y-fn rotate-z-fn p-extra-dist p-x-off p-z-off p-z-rot p-x-rot p-rot shape)
             (apply-key-geometry' translate-fn rotate-x-fn rotate-y-fn 4 homerow)
        )
      )
      (if (= row 1) 
        (if (= column 0) 
          (->> 
               (key-upper-place' translate-fn rotate-x-fn rotate-y-fn rotate-z-fn (above-extra-dist column) (above-x-off column) (above-z-off column) (above-z-rot column) (above-x-rot column) (above-rot column) shape)
               (apply-key-geometry' translate-fn rotate-x-fn rotate-y-fn 1 homerow)
          )
          (->> 
               (key-upper-place' translate-fn rotate-x-fn rotate-y-fn rotate-z-fn (above-extra-dist column) (above-x-off column) (above-z-off column) (above-z-rot column) (above-x-rot column) (above-rot column) shape)
               (apply-key-geometry' translate-fn rotate-x-fn rotate-y-fn column homerow)
          )
        )
        (when (= row 3)
          (->> 
              (key-upper-place' translate-fn rotate-x-fn rotate-y-fn rotate-z-fn (below-extra-dist column) 0 0 (below-z-rot column) (below-x-rot column) (below-rot column) shape)
              (apply-key-geometry' translate-fn rotate-x-fn rotate-y-fn column homerow)
          )
        )
      )
    )
  )
)


(defn key-place [column row shape]
  (apply-key-geometry translate
                      rotate-x
                      rotate-y
                      rotate-z
                      column row shape))

(defn shift-model [model] (union 
  (->> model
     (rotate tenting-angle [0 1 0])
     (translate [0 0 keyboard-z-offset])
  )
))

(defn shift-model-position [position]
  (map + (rotate-around-y tenting-angle position) [0 0 keyboard-z-offset]))

(defn key-place-shifted [column row shape] (shift-model (key-place column row shape)))

(defn key-place' [border column row shape] ((if border key-place key-place-shifted) column row shape))

(defn key-position-orig [column row position]
  (apply-key-geometry' (partial map +) rotate-around-x rotate-around-y column row position))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y rotate-around-z column row position))

(def caps
    (apply union
         (for [column columns
               row rows
               :when (or (and bottom-row (.contains [2 3] column))
                         (not= row lastrow))]
             (->> (sa-cap 1 column row)
                (key-place column row)))))
(defn key-places [shape]
  (apply union
         (for [column columns
               row rows
               :when (and (not (and (= row real-lastrow) (= column firstcol)))
                       (or (and bottom-row (.contains [2 3] column))
                         (not= row lastrow))
                       (not (= column lastcol))
                       )]
             (->> shape
                (key-place column row)))))

(def key-space-below
  (key-places switch-bottom))
(def caps-cutout
  (key-places (sa-cap-cutout 1)))

(defn flex-pcb-holder-places [shape]
  (apply union
         (for [column columns] (->> shape (key-place column 0)))
         (for [column columns]
               (->> (->> shape
                         (mirror [1 0 0])
                         (mirror [0 1 0])) 
                    (key-place column 
                               (if (.contains [2 3] column) 
                                   lastrow 
                                   cornerrow)))
         )))
(def flex-pcb-holders
  (flex-pcb-holder-places flex-pcb-holder))

;;;;;;;;;;;;;;;;;;;;
;; Web Connectors ;;
;;;;;;;;;;;;;;;;;;;;

; posts are located at the inside corners of the key plates.
; the 'web' is the fill between key plates.
;

(def post-size 0.1)
(def web-post (->> (cube post-size post-size web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      plate-thickness)])))

(def wall-post (->> (cube wall-thickness wall-thickness web-thickness)
                   (translate [0 0 (+ (/ web-thickness -2)
                                      plate-thickness)])))

(def post-adj (/ post-size 2))
(def wall-adj (/ wall-thickness 2))
(def web-post-tr (translate [(- (/ mount-width  2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
(def web-post-tm (translate [                             0  (- (/ mount-height  2) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-bm (translate [                             0  (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ mount-width  2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))

(def wall-post-bl (translate [(- (/ mount-width -2) (- wall-adj post-size)) (+ (/ mount-height -2) wall-adj) 0] wall-post))


; plate posts for connecting columns together without wasting material
; or blocking sides of hotswap sockets
(def plate-post-size 1.2)
(def plate-post-thickness (- web-thickness 2))
(def plate-post (->> (cube plate-post-size plate-post-size plate-post-thickness)
                   (translate [0 0 (+ plate-post-thickness (/ plate-post-thickness -1.5)
                                      )])))

(def plate-top-offset (+ (/ plate-post-thickness 2) plate-post-thickness (/ plate-post-thickness -1.5)))

(def short-post-thickness 1)
(def short-post-size 1.2)
(def short-post (->> (cube short-post-size short-post-size short-post-thickness)
                   (translate [0 0 (+ (- (/ short-post-thickness 2)) plate-top-offset)])))
(def short-post-back (translate [0 0 (- short-post-thickness web-thickness)] short-post))

(def upper-post-offset 9) ; amount of clearance to insert hotswap behind vertical keys

(def short-post-adj (/ short-post-size 2))

(def short-post-back-tr (translate [(- (/ mount-width  2) short-post-adj) (- (/ mount-height  2) short-post-adj) 0] short-post-back))
(def short-post-back-tl (translate [(+ (/ mount-width -2) short-post-adj) (- (/ mount-height  2) short-post-adj) 0] short-post-back))
(def short-post-back-bl (translate [(+ (/ mount-width -2) short-post-adj) (+ (/ mount-height -2) short-post-adj) 0] short-post-back))
(def short-post-back-br (translate [(- (/ mount-width  2) short-post-adj) (+ (/ mount-height -2) short-post-adj) 0] short-post-back))

(def short-post-tr (translate [(- (/ mount-width  2) short-post-adj) (- (/ mount-height  2) short-post-adj) 0] short-post))
(def short-post-tl (translate [(+ (/ mount-width -2) short-post-adj) (- (/ mount-height  2) short-post-adj) 0] short-post))
(def short-post-bl (translate [(+ (/ mount-width -2) short-post-adj) (+ (/ mount-height -2) short-post-adj) 0] short-post))
(def short-post-br (translate [(- (/ mount-width  2) short-post-adj) (+ (/ mount-height -2) short-post-adj) 0] short-post))

(def plate-post-adj (/ plate-post-size 2))

(def plate-post-tm (translate [                                   0  (- (/ mount-height  2) plate-post-adj) 0] plate-post))
(def plate-post-bm (translate [                                   0  (+ (/ mount-height -2) plate-post-adj) 0] plate-post))

(def plate-post-tr (translate [(- (/ mount-width  2) plate-post-adj) (- (/ mount-height  2) plate-post-adj) 0] plate-post))
(def plate-post-tl (translate [(+ (/ mount-width -2) plate-post-adj) (- (/ mount-height  2) plate-post-adj) 0] plate-post))
(def plate-post-bl (translate [(+ (/ mount-width -2) plate-post-adj) (+ (/ mount-height -2) plate-post-adj) 0] plate-post))
(def plate-post-br (translate [(- (/ mount-width  2) plate-post-adj) (+ (/ mount-height -2) plate-post-adj) 0] plate-post))

; fat web post for very steep angles between thumb and finger clusters
; this ensures the walls stay somewhat thicker
(def fat-post-size 1.2)
(def fat-web-post' (cube fat-post-size fat-post-size web-thickness))
(def fat-web-post (->> fat-web-post'
                       (translate [0 0 (+ (/ web-thickness -2)
                                          plate-thickness)])))

(def upper-fat-web-post-bot-lower-z-off (- (+ (- (/ web-thickness 1)) plate-thickness (/ fat-post-size 2))))
(def upper-fat-web-post-bot-lower-y-off (/ mount-height -2))

(def upper-support-blocker-height 3.5)
(def behind-cutout-height (+ keyswitch-height single-plate-wall-thickness single-plate-wall-thickness))

(defn upper-support-blocker' [width]
    (let [post-offset upper-fat-web-post-bot-lower-z-off post-width fat-post-size width width] 
    (union
      (translate [0 (+ (- (/ upper-support-blocker-height 2)) (/ behind-cutout-height 2) (- (/ single-plate-wall-thickness 2)) 1) (- (- (/ width 2)) (+ post-offset (/ post-width 2)))]
       (cube (- mount-width (* 2 fat-post-size)) upper-support-blocker-height width))
    )
  )
)

(defn upper-behind-cutout' [width]
    (let [post-offset upper-fat-web-post-bot-lower-z-off post-width fat-post-size width width] 
    (union
      (translate [0 0 (- (+ post-offset (/ post-width 2)))]
       (single-plate-cutout' (+ post-offset (/ post-width 2))))
      (translate [0 (+ (- (/ web-thickness 2)) upper-fat-web-post-bot-lower-y-off) (- (- (/ width 2)) (+ post-offset (/ post-width 2)))]
       (cube (- mount-width (* 2 fat-post-size)) web-thickness width))
      (translate [0 (- (/ single-plate-wall-thickness 2)) (- (- (/ width 2)) (+ post-offset (/ post-width 2)))]
       (cube (- mount-width (* 2 fat-post-size)) behind-cutout-height width))
    )
  )
)

(def upper-behind-cutout (upper-behind-cutout' (- upper-post-offset upper-fat-web-post-bot-lower-z-off fat-post-size)))
(def upper-support-blocker (upper-support-blocker' (- upper-post-offset upper-fat-web-post-bot-lower-z-off fat-post-size)))

(def upper-fat-web-post-top (rotate-x (deg2rad (- 90)) (translate [0 upper-post-offset (+ (- (/ web-thickness 2)) 0)] fat-web-post')))
(def upper-fat-web-post-bot (rotate-x (deg2rad (- 90)) (translate [0 upper-post-offset (- (/ web-thickness 2) 0)] fat-web-post')))
(def upper-fat-web-post-bot-out (rotate-x (deg2rad (- 90)) (translate [0 upper-post-offset (- (/ web-thickness 2) 0)] fat-web-post')))

(def upper-fat-web-post-bot-lower (rotate-x (deg2rad (- 90)) (translate [0 upper-fat-web-post-bot-lower-z-off (- (/ web-thickness 2))] fat-web-post')))

(def fat-post-adj (/ fat-post-size 2))
(def fat-web-post-tr (translate [(- (/ mount-width  2) fat-post-adj) (- (/ mount-height  2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-tl (translate [(+ (/ mount-width -2) fat-post-adj) (- (/ mount-height  2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-bl (translate [(+ (/ mount-width -2) fat-post-adj) (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-br (translate [(- (/ mount-width  2) fat-post-adj) (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-tm (translate [                                 0  (- (/ mount-height  2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-bm (translate [                                 0  (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
(def upper-fat-web-post-tl (translate [(+ (/ mount-width -2) fat-post-adj) (- (/ mount-height  2) 0) 0] upper-fat-web-post-top))
(def upper-fat-web-post-tl-lower (translate [(+ (/ mount-width -2) fat-post-adj) (+ (/ mount-height -2) 0) 0] upper-fat-web-post-top))
(def upper-fat-web-post-bl (translate [(+ (/ mount-width -2) fat-post-adj) (/ mount-height -2) 0] upper-fat-web-post-bot))
(def upper-fat-web-post-bl-lower (translate [(+ (/ mount-width -2) fat-post-adj) (+ (/ mount-height -2) 0) 0] upper-fat-web-post-bot-lower))
(def upper-fat-web-post-tr (translate [(- (/ mount-width  2) fat-post-adj) (/ mount-height  2) 0] upper-fat-web-post-top))
(def upper-fat-web-post-tr-lower (translate [(- (/ mount-width  2) fat-post-adj) (+ (/ mount-height -2) 0) 0] upper-fat-web-post-top))
(def upper-fat-web-post-br (translate [(- (/ mount-width  2) fat-post-adj) (/ mount-height -2) 0] upper-fat-web-post-bot))
(def upper-fat-web-post-br-lower (translate [(- (/ mount-width  2) fat-post-adj) (/ mount-height -2) 0] upper-fat-web-post-bot-lower))

(def short-post-bl-lower (translate [0 (- short-post-size) 0] short-post-bl))
(def short-post-br-lower (translate [0 (- short-post-size) 0] short-post-br))
(def short-post-back-bl-lower (translate [0 (- short-post-size) 0] short-post-back-bl))
(def short-post-back-br-lower (translate [0 (- short-post-size) 0] short-post-back-br))

(def fat-web-post-bl-lower (translate [0 (- fat-post-size) 0] fat-web-post-bl))
(def fat-web-post-br-lower (translate [0 (- fat-post-size) 0] fat-web-post-br))

(def trackball-post (->> (cube fat-post-size fat-post-size sensor-height)
                       (translate [0 0 (+ (/ web-thickness -2)
                                          plate-thickness)])))

(def trackball-post-tr (translate [(- (/ tb-mount-length  2) fat-post-adj) (- (/ tb-mount-width  2) fat-post-adj) 0] trackball-post))
(def trackball-post-br (translate [(- (/ tb-mount-length  2) fat-post-adj) (+ (/ tb-mount-width -2) fat-post-adj) 0] trackball-post))
(def trackball-post-bl (translate [(+ (/ tb-mount-length -2) fat-post-adj) (+ (/ tb-mount-width -2) fat-post-adj) 0] trackball-post))
(def trackball-post-tl (translate [(+ (/ tb-mount-length -2) fat-post-adj) (- (/ tb-mount-width  2) fat-post-adj) 0] trackball-post))

(defn triangle-hulls [& shapes]
  (apply union
         (map (partial apply hull)
              (partition 3 1 shapes))))

(defn piramid-hulls [top & shapes]
  (apply union
         (map (partial apply hull top)
              (partition 2 1 shapes))))
;;;;;;;;;;;;
;; Thumbs ;;
;;;;;;;;;;;;

(def thumborigin
  (map + (key-position-orig 1 cornerrow [(/ mount-width 2) 
                                    (- (/ mount-height 2)) 
                                    0])
       thumb-pos))

"need to account for plate thickness which is baked into thumb-_-place rotation & move values
when plate-thickness was 2
need to adjust for difference for thumb-z only"
(def thumb-design-z 2)
(def thumb-z-adjustment (- (if (> plate-thickness thumb-design-z)
                                 (- thumb-design-z plate-thickness)
                                 (if (< plate-thickness thumb-design-z)
                                       (- thumb-design-z plate-thickness) 
                                       0)) 
                            1.1))
(def thumb-x-rotation-adjustment -12) ; globally adjust front/back tilt of thumb keys

(defn apply-thumb-geometry [rot-x-fn rot-y-fn rot-z-fn move-fn rot move shape]
  (->> 
    (->> shape
       (move-fn [0 0 thumb-z-adjustment])                   ;adapt thumb positions for increased plate
       (rot-x-fn (deg2rad thumb-x-rotation-adjustment)) ;adjust angle of all thumbs to be less angled down towards user since key is taller
       
       (rot-x-fn (deg2rad (nth rot 0)))
       (rot-y-fn (deg2rad (nth rot 1)))
       (rot-z-fn (deg2rad (nth rot 2)))
       (move-fn move))

       (rot-x-fn (deg2rad (nth thumb-rot 0)))
       (rot-y-fn (deg2rad (nth thumb-rot 1)))
       (rot-z-fn (deg2rad (nth thumb-rot 2)))

       (move-fn thumborigin)
     ))

(defn thumb-place [rot move shape]
  (apply-thumb-geometry rotate-x rotate-y rotate-z translate rot move shape))

(defn thumb-position [rot move position]
  (apply-thumb-geometry rotate-around-x rotate-around-y rotate-around-z (partial map +) rot move position))

(defn thumb-place-shifted [rot move shape] (shift-model (thumb-place rot move shape)))

(def trackball-y-rotate -72)
(def trackball-x-rotate 13)
(def trackball-z-rotate 30)
(def trackball-thumb-offset [-4.5 20.0 12])

(defn apply-trackball-geometry [translate-fn rotate-x-fn rotate-y-fn rotate-z-fn shape]
  (->> shape
    (translate-fn bottom-trim-origin)
    (translate-fn [0 0 (- tb-mount-offset)])
    (rotate-z-fn (deg2rad mount-z-rotate))
    (rotate-x-fn (deg2rad mount-x-rotate))
    (rotate-y-fn (deg2rad mount-y-rotate))
    (rotate-y-fn (deg2rad trackball-y-rotate))
    (rotate-x-fn (deg2rad trackball-x-rotate))
    (rotate-z-fn (deg2rad trackball-z-rotate))
    (thumb-place [0 0 0] trackball-thumb-offset)
  )
)

(defn trackball-place [shape]
  (apply-trackball-geometry translate
                      (fn [angle obj] (rotate angle [1 0 0] obj))
                      (fn [angle obj] (rotate angle [0 1 0] obj))
                      (fn [angle obj] (rotate angle [0 0 1] obj))
                      shape))

(defn trackball-place-shifted [shape] (shift-model (trackball-place shape)))

(defn trackball-pos [row col] (and (= row real-lastrow) (= col firstcol)))
(defn skip-pos [left row col] (and left (trackball-pos row col)))

(defn thumb-ur-uplace [shape] (key-upper-place thumb-ur-extra-dist thumb-ur-x-off thumb-ur-z-off thumb-ur-x-rot thumb-ur-rot shape))
(defn thumb-u-uplace [shape] (key-upper-place-zrot thumb-u-extra-dist thumb-u-x-off thumb-u-z-off thumb-u-z-rot thumb-u-x-rot thumb-u-rot shape))
(defn thumb-ub-uplace [shape] (key-upper-place thumb-ub-extra-dist thumb-ub-x-off thumb-ub-z-off thumb-ub-x-rot thumb-ub-rot shape))
(defn thumb-urr-uplace [shape] (key-upper-place thumb-urr-extra-dist thumb-urr-x-off thumb-urr-z-off thumb-urr-x-rot thumb-urr-rot shape))

; convexer
(defn thumb-r-place [shape] (thumb-place thumb-r-rot thumb-r-move shape)) ; right
(defn thumb-ur-place [shape] (thumb-place thumb-r-rot thumb-r-move (thumb-ur-uplace shape))) ; upper-right
(defn thumb-urr-place [shape] (thumb-place thumb-r-rot thumb-r-move (thumb-urr-uplace shape))) ; upper-right right
(defn thumb-m-place [shape] (thumb-place thumb-m-rot thumb-m-move shape)) ; middle
(defn thumb-u-place [shape] (thumb-place thumb-r-rot thumb-r-move (thumb-u-uplace shape))) ; upper-right left
(defn thumb-ub-place [shape] (thumb-place thumb-r-rot thumb-r-move (thumb-ub-uplace shape))) ; upper-right back

; convexer
(defn thumb-r-place' [border shape] ((if border thumb-place thumb-place-shifted) thumb-r-rot thumb-r-move shape))
(defn thumb-ur-place' [border shape] ((if border thumb-place thumb-place-shifted) thumb-r-rot thumb-r-move (thumb-ur-uplace shape)))
(defn thumb-urr-place' [border shape] ((if border thumb-place thumb-place-shifted) thumb-r-rot thumb-r-move (thumb-urr-uplace shape)))
(defn thumb-m-place' [border shape] ((if border thumb-place thumb-place-shifted) thumb-m-rot thumb-m-move shape))
(defn thumb-u-place' [border shape] ((if border thumb-place thumb-place-shifted) thumb-r-rot thumb-r-move (thumb-u-uplace shape)))
(defn thumb-ub-place' [border shape] ((if border thumb-place thumb-place-shifted) thumb-r-rot thumb-r-move (thumb-ub-uplace shape)))

(defn thumb-layout [left shape]
  (union
    (thumb-r-place shape)
    (when left (union
      (thumb-u-place shape)
      (thumb-ur-place shape)
    ))
    (thumb-urr-place shape)
    (if left
      (thumb-ub-place shape)
      (thumb-m-place shape)
    )
    ;(thumb-l-place shape)
    ))

(defn thumb-upper-layout [left shape]
  (union
    (when left (union
      (thumb-u-place shape)
      (thumb-ur-place shape)
      (thumb-ub-place shape)
    ))
    (thumb-urr-place shape)
    ;(thumb-l-place shape)
  ))

(defn upper-layout [left shape]
  (union
    (key-place 0 1 shape)
    (key-place 1 1 shape)
    (key-place 2 1 shape)
    (key-place 3 1 shape)
    (key-place 4 1 shape)
    (key-place 0 2 shape)
    (thumb-upper-layout left shape)
  ))

(defn thumbcaps [left] (thumb-layout left
                   (if rendered-caps
                       (->> (import "../things/caps/MT3_1u_space_130.stl")
                            (rotate (deg2rad 90) [0 0 1])
                            (translate [0 0 sa-cap-bottom-height])
                            (color KEYCAP)
                       )
                       (sa-cap 1 2 5)
                   )
               )
)
(defn thumbcaps-cutout [left] (thumb-layout left (rotate (deg2rad -90) [0 0 1] (sa-cap-cutout 1))))
(defn thumb-space-below [left] (thumb-layout left switch-bottom))
(defn thumb-key-cutouts [mirror-internals] 
    (thumb-layout mirror-internals (single-plate-cut mirror-internals)))

;;;;;;;;;;
;; Case ;;
;;;;;;;;;;

(defn bottom [height p]
  (->> (project p)
       (extrude-linear {:height height :twist 0 :convexity 0})
       (translate [0 0 (/ height 2)])))

(defn bottom-hull [& p]
  (hull p (bottom 0.001 p)))

(def wall-border-z-offset -0.75)  ; length of the first downward-sloping part of the wall (negative)
(def wall-border-xy-offset 1.1)
(def wall-border-thickness 1)  ; wall thickness parameter

(def upper-case-out (- (+ upper-post-offset (/ fat-post-size 2)) swap-z))

(defn wall-locate0 [dx dy border] [(* dx (if border wall-border-thickness wall-thickness))
                                   (* dy (if border wall-border-thickness wall-thickness))
                                   0])
(defn wall-locate1 [dx dy border] [(* dx (if border wall-border-thickness wall-thickness))
                                   (* dy (if border wall-border-thickness wall-thickness))
                                   0])
(defn wall-locate2' [dx dy upper border extend] [(* dx (if border wall-border-xy-offset wall-xy-offset))
                                   (* dy (if border wall-border-xy-offset wall-xy-offset))
                                   (if upper (if (and border extend) (- upper-case-out) 0) (if border wall-border-z-offset wall-z-offset))])
(defn wall-locate2 [dx dy border] (wall-locate2' dx dy false border false))
(defn wall-locate3 [dx dy border] [(* dx (+ (if border wall-border-xy-offset wall-xy-offset) (if border wall-border-thickness wall-thickness))) 
                                   (* dy (+ (if border wall-border-xy-offset wall-xy-offset) (if border wall-border-thickness wall-thickness))) 
                                   (* 2 (if border wall-border-z-offset wall-z-offset))])

(defn connectors [left]
  (union
           ;; Row connections
           (for [column (range firstcol (dec lastcol))
                 row (range firstrow lastrow)]
             (let [
                    col-tr (if (= row cornerrow) 
                        (key-place column  row fat-web-post-bl)
                        (key-place column  row plate-post-tr)
                    )
                    col-br (if (= row cornerrow) 
                        (key-place column  row fat-web-post-tl)
                        (key-place column  row plate-post-br)
                    )
                    icol-tl (if (= row cornerrow)
                        (key-place (inc column)  row fat-web-post-br)
                        (key-place (inc column)  row plate-post-tl)
                    )
                    icol-bl (if (= row cornerrow) 
                        (key-place (inc column)  row fat-web-post-tr)
                        (key-place (inc column)  row plate-post-bl)
                    )
                  ]
             (when (and (or (not= column firstcol) (= row 1)) (not (skip-pos left row column)) (or (not= column (dec lastcol)) (= 2 row)))
               (triangle-hulls
                 icol-tl
                 col-tr
                 icol-bl
                 col-br)
             ))
           )

           ;; Column connections
           (for [column columns'
                 row (range firstrow cornerrow)]
             (let [
                   is-upper (= row (dec cornerrow))
                   irow-tl (if is-upper
                       (key-place column  (inc row) fat-web-post-br)
                       (key-place column  (inc row) plate-post-tl)
                   )
                   irow-tr (if is-upper
                       (key-place column  (inc row) fat-web-post-bl)
                       (key-place column  (inc row) plate-post-tr)
                   )
                  ]
             (when (not (skip-pos left (inc row) column))
             (triangle-hulls
               (key-place column      row  (if (not= row 3) fat-web-post-bl plate-post-bl))
               (key-place column      row  (if (not= row 3) fat-web-post-br plate-post-br))
               irow-tl
               irow-tr)))
           )

           ;; Diagonal connections
           (for [column (range (inc firstcol) (dec lastcol))
                 row (range firstrow cornerrow)]
             (let [
                     row-col-br (if (= row 1)
                         (key-place column  row fat-web-post-br)
                         (key-place column  row plate-post-br)
                     )
                     irow-col-tr (if (= row homerow)
                         (key-place column  (inc row) fat-web-post-bl)
                         (key-place column  (inc row) plate-post-tr)
                     )
                     irow-icol-tl (if (= row homerow)
                         (key-place (inc column) (inc row) fat-web-post-br)
                         (key-place (inc column) (inc row) plate-post-tl)
                     )
                     row-icol-bl (if (= row 1)
                         (key-place (inc column)  row fat-web-post-bl)
                         (key-place (inc column)  row plate-post-bl)
                     )
                  ]
               (when (not (and left (= row 2) (= column firstcol))) (triangle-hulls
                 row-col-br
                 irow-col-tr
                 row-icol-bl
                 irow-icol-tl
               ))
             )
           )

           ;; g-key connections
           (triangle-hulls ;diagonal
             (key-place      0       1  fat-web-post-bl)
             (key-place      0       1  fat-web-post-br)
             (key-place      0       2  fat-web-post-br-lower)
             (key-place      1       1  fat-web-post-bl)
             (key-place      1       2  plate-post-tl)
             )

           (triangle-hulls ;row
             (key-place      0       2  fat-web-post-br)
             (key-place      0       2  fat-web-post-bl)
             (key-place      1       2  plate-post-tl)
             (key-place      1       2  plate-post-bl)
             )

           (triangle-hulls ;fill n key gap
             (key-place      0       2  fat-web-post-bl)
             (key-place      1       2  plate-post-bl)
             (key-place      1       3  fat-web-post-br)
             )

           (triangle-hulls ;fill outer index gap
             (key-place      0       1  fat-web-post-bl-lower)
             (key-place      0       1  (translate [0 0 (- upper-case-out)] fat-web-post-bl-lower))
             (key-place      0       2  (translate [0 0 (- upper-case-out)] fat-web-post-br-lower))
             (key-place      0       2  fat-web-post-br-lower)
             )

           ;; bottom row to front wall/thumb-urr connections
           (triangle-hulls
             (key-place      3       3  fat-web-post-tr)
             (key-place      3       3  fat-web-post-tl)
             (thumb-urr-place (translate [0 0 (- swap-z upper-post-offset)] short-post-back-br-lower))
             (key-place      4       3  fat-web-post-tr)
             )

           (triangle-hulls
             (key-place      2       3  fat-web-post-tl)
             (key-place      2       3  fat-web-post-tr)
             (key-place      3       3  fat-web-post-tr)
             (thumb-urr-place (translate [0 0 (- swap-z upper-post-offset)] short-post-back-br-lower))
             (thumb-urr-place (translate [0 0 (- swap-z upper-post-offset)] short-post-back-bl-lower))
             )

           (triangle-hulls
             (key-place      2       3  fat-web-post-tr)
             (key-place      3       3  fat-web-post-tr)
             (key-place      1       3  fat-web-post-tl)
             (thumb-urr-place (translate [0 0 (- swap-z upper-post-offset)] short-post-back-br-lower))
             )
  )
)


(def v-short-post-back-br (translate [0 0 -0.8] (hull (translate [0 0 (- v-key-case-extend)] short-post-back-br) short-post-back-br)))


(defn thumb-connectors [left]
  (union
    (->> (triangle-hulls
             (thumb-urr-place fat-web-post-bl)
             (thumb-urr-place fat-web-post-br)
             (thumb-r-place plate-post-tr)
             (thumb-r-place plate-post-br)
         ) (color GRE))

    ;(when (not left)
    ;  (union
    ;    (->> (triangle-hulls
    ;             (trackball-place trackball-post-bl)
    ;             (trackball-place trackball-post-br)
    ;             (thumb-m-place plate-post-tl)
    ;             (thumb-m-place plate-post-tr)
    ;         ) (color GRE))

    ;    (->> (triangle-hulls
    ;             (key-place 1 cornerrow v-short-post-back-br)
    ;             (thumb-r-place plate-post-tr)
    ;             (thumb-urr-place short-post-bl)
    ;         ) (color GRE))

    ;    (->> (triangle-hulls
    ;             (thumb-m-place plate-post-tr)
    ;             (thumb-r-place plate-post-tl)
    ;             (trackball-place trackball-post-br)
    ;             (trackball-place trackball-post-tr)
    ;         ) (color GRE))

    ;    (->> (triangle-hulls
    ;             (thumb-r-place plate-post-tl)
    ;             (thumb-r-place plate-post-tr)
    ;             (trackball-place trackball-post-tr)
    ;             (key-place 1 cornerrow v-short-post-back-br)
    ;         ) (color GRE))

    ;    (->> (triangle-hulls
    ;             (trackball-place trackball-post-tr)
    ;             (key-place 1 cornerrow v-short-post-back-br)
    ;             (key-place 0 homerow (translate [0 0 (- upper-case-out)] fat-web-post-bl))
    ;         ) (color GRE))

    ;    (->> (triangle-hulls
    ;             (trackball-place trackball-post-tl)
    ;             (key-place 0 homerow (translate [0 0 0] upper-fat-web-post-tr-lower))
    ;             (trackball-place trackball-post-tr)
    ;             (key-place 0 homerow (translate [0 0 0] upper-fat-web-post-tl-lower))
    ;         ) (color GRE))
    ;  )
    ;)

    (when left (letfn
                 [
                   (case-post-br [shape] (translate (wall-locate1 1 -1 true) shape))
                   (case-br [shape] (union shape (case-post-br shape) (translate [0 0 (- upper-case-out)] (case-post-br shape))))
                 ]
    (union
      (->> (triangle-hulls
               (thumb-u-place fat-web-post-bl)
               (thumb-r-place plate-post-bl)
               (thumb-u-place fat-web-post-br)
               (thumb-r-place plate-post-tl)
           ) (color GRE))

      (->> (triangle-hulls
               (thumb-u-place fat-web-post-br)
               (thumb-ur-place fat-web-post-bl)
               (thumb-r-place plate-post-tl)
           ) (color RED))

      (->> (triangle-hulls
               (thumb-ub-place fat-web-post-bl)
               (thumb-r-place plate-post-bl)
               (thumb-ub-place fat-web-post-br)
           ) (color GRE))

      (->> (triangle-hulls
               (thumb-ub-place fat-web-post-br)
               (thumb-u-place fat-web-post-bl)
               (thumb-r-place plate-post-bl)
               (thumb-r-place plate-post-tl)
           ) (color RED))

      (->> (triangle-hulls
               (thumb-ur-place fat-web-post-bl)
               (thumb-ur-place fat-web-post-br)
               (thumb-r-place plate-post-tl)
               (thumb-r-place plate-post-tr)
           ) (color GRE))

      (->> (triangle-hulls
               (thumb-u-place fat-web-post-br)
               (thumb-u-place plate-post-tr)
               (thumb-ur-place fat-web-post-bl)
               (thumb-ur-place plate-post-tl)
           ) (color RED))

      (->> (triangle-hulls
               (thumb-u-place fat-web-post-bl)
               (thumb-u-place plate-post-tl)
               (thumb-ub-place fat-web-post-br)
               (thumb-ub-place plate-post-tr)
           ) (color RED))

      (->> (triangle-hulls
        ;(key-place 1 cornerrow (translate [0 0 0] plate-post-tr))
        (thumb-r-place short-post-tr)
        (thumb-urr-place (union (translate [0 0 -2] short-post-bl) short-post-bl))
        (thumb-ur-place (union (translate [0 0 -2] short-post-br) short-post-br))
        ;(thumb-r-place pl
        ) (color RED))

      (->> (triangle-hulls
        (key-place 1 cornerrow (translate [0 0 0] (hull (translate [0 0 (- v-key-case-extend) short-post-back-br]) short-post-back-br)))
        (thumb-urr-place (union (translate [0 0 -2] short-post-bl) short-post-bl))
        (thumb-ur-place (union (translate [0 0 -2] short-post-br) short-post-br))
        ) (color RED))

      (->> (triangle-hulls
        (key-place 1 cornerrow (translate [0 0 0] (hull (translate [0 0 (- v-key-case-extend) fat-web-post-br]) fat-web-post-br)))
        (key-place 0 homerow (translate [0 0 0] fat-web-post-bl))
        (thumb-ur-place plate-post-br)
        ) (color RED))

      ;(->> (triangle-hulls
      ;         (key-place (inc firstcol)    cornerrow  short-post-bl)
      ;         (key-place (inc firstcol)    cornerrow  short-post-tl)
      ;         (thumb-ur-place  (case-br fat-web-post-br))
      ;     ) (color RED) )

      ;(->> (triangle-hulls
      ;         (key-place (inc firstcol)    cornerrow  short-post-bl)
      ;         (thumb-r-place  short-post-tr)
      ;         (thumb-ur-place  short-post-br)
      ;     ) (color RED) )

      ;(->> (triangle-hulls
      ;         (thumb-r-place plate-post-tr)
      ;         (key-place (inc firstcol) cornerrow web-post-br)
      ;         (key-place (inc firstcol) cornerrow web-post-bl)
      ;     ) (color NBL))

      ;(->> (triangle-hulls
      ;         (key-place firstcol    (dec cornerrow)  web-post-bl)
      ;         (key-place firstcol    (dec cornerrow)  web-post-br)
      ;         (thumb-ur-place  upper-fat-web-post-tl-lower)
      ;         (thumb-ur-place  upper-fat-web-post-tr-lower)
      ;     ) (color ORA) )

      ;(->> (triangle-hulls
      ;         (key-place firstcol    (dec cornerrow)  web-post-bl)
      ;         (key-place firstcol    (dec cornerrow)  web-post-br)
      ;         (key-place (inc firstcol)    (dec cornerrow)  web-post-bl)
      ;         (thumb-ur-place  upper-fat-web-post-tl-lower)
      ;     ) (color ORA) )

      ;(->> (triangle-hulls
      ;         (key-place firstcol    (dec cornerrow)  web-post-br)
      ;         (thumb-ur-place  upper-fat-web-post-tr-lower)
      ;         (thumb-ur-place  upper-fat-web-post-tl-lower)
      ;         (key-place (inc firstcol)    (dec cornerrow)  web-post-bl)
      ;     ) (color ORA) )

      ;(->> (triangle-hulls
      ;         (key-place firstcol    (dec cornerrow)  web-post-br)
      ;         (thumb-ur-place  upper-fat-web-post-tr-lower)
      ;         (key-place (inc firstcol)    cornerrow  short-post-tl)
      ;         (key-place (inc firstcol)    (dec cornerrow)  web-post-bl)
      ;         (key-place firstcol    (dec cornerrow)  web-post-br)
      ;     ) (color BLU) )

      ;(->> (triangle-hulls
      ;         (key-place (inc firstcol)    cornerrow  short-post-tl)
      ;         (key-place (inc firstcol)    (dec cornerrow)  short-post-bl)
      ;         (thumb-ur-place  (case-br fat-web-post-br))
      ;     ) (color BLU) )

      ; partially fills  N and B keys sockets, do not use unless you cut those back out
      ; (->> (triangle-hulls
      ;          (thumb-u-place plate-post-br)
      ;          (thumb-u-place web-post-tr)
      ;          (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) web-post-tl))
      ;      ) (color ORA))
      ; partially fills  N and B keys sockets, do not use unless you cut those back out
      ; (->> (triangle-hulls
      ;          (thumb-u-place plate-post-br)
      ;          (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) web-post-tl))
      ;          (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) web-post-bl))
      ;      ) (color BLU))

      ;(->> (triangle-hulls
      ;         (thumb-u-place plate-post-br)
      ;         (thumb-u-place web-post-tr)
      ;         (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) web-post-bl))
      ;     ) (color YEL))

      ;(->> (triangle-hulls
      ;         (thumb-u-place fat-web-post-tr)
      ;         (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) fat-web-post-tl))
      ;         (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) fat-web-post-bl))
      ;     ) (color BLA))


      ;(->> (triangle-hulls
      ;         (thumb-m-place plate-post-tl)
      ;         (thumb-l-place web-post-tr)
      ;         (thumb-u-place web-post-br)
      ;     ) (color DGR))

      ;(->> (triangle-hulls
      ;         (thumb-u-place web-post-bl)
      ;         (thumb-l-place web-post-tr)
      ;         (thumb-l-place web-post-tl)
      ;     )
      ;     (color BLU))

      ;(->> (triangle-hulls
      ;        (thumb-u-place web-post-bl)
      ;        (thumb-u-place web-post-br)
      ;        (thumb-l-place web-post-tr)
      ;    )
      ;     (color NBL))

      ;(->> (if use_hotswap_holder 
      ;       (triangle-hulls
      ;         (thumb-u-place plate-post-tl)
      ;         (thumb-l-place plate-post-tl)
      ;         (thumb-u-place plate-post-bl)
      ;       )
      ;       (triangle-hulls
      ;         (thumb-u-place web-post-tl)
      ;         (thumb-l-place web-post-tl)
      ;         (thumb-u-place web-post-bl)
      ;       )
      ;     ) (color RED))))
    )))

    (when (not left)
      ; top two
      (->> (if use_hotswap_holder 
               (triangle-hulls
                 (thumb-m-place plate-post-tr)
                 (thumb-m-place plate-post-br)
                 (thumb-r-place plate-post-tl)
                 (thumb-r-place plate-post-bl))
               (triangle-hulls
                 (thumb-m-place web-post-tr)
                 (thumb-m-place web-post-br)
                 (thumb-r-place web-post-tl)
                 (thumb-r-place web-post-bl))
             ) (color RED))
    )

    ;(->> (if use_hotswap_holder 
    ;         (triangle-hulls
    ;           (thumb-m-place plate-post-tl)
    ;           (thumb-l-place plate-post-tr)
    ;           (thumb-m-place plate-post-bl)
    ;           (thumb-l-place plate-post-br)
    ;           (thumb-m-place plate-post-bl))
    ;         (triangle-hulls
    ;           (thumb-m-place web-post-tl)
    ;           (thumb-l-place web-post-tr)
    ;           (thumb-m-place web-post-bl)
    ;           (thumb-l-place web-post-br)
    ;           (thumb-m-place web-post-bl))
    ;       ) (color ORA))

    ;(hull  ; between thumb m and thumb keys
    ;  (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) web-post-bl))
    ;  (thumb-m-place web-post-tr)
    ;  (thumb-m-place web-post-tl))

    ;(->> (piramid-hulls                                          ; top ridge thumb side
    ;  (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) fat-web-post-bl))
    ;  (key-place 0 cornerrow (translate (wall-locate2 -1 0 false) fat-web-post-bl))
    ;  (key-place 0 cornerrow web-post-bl)
    ;  ;(thumb-r-place web-post-tr)
    ;  (thumb-r-place web-post-tl)
    ;  (thumb-m-place fat-web-post-tr)
    ;  (thumb-m-place fat-web-post-tl)
    ;  (thumb-l-place fat-web-post-tr)
    ;  (key-place 0 cornerrow (translate (wall-locate2 -1 0 false) fat-web-post-bl))
    ;  ) (color BLA))

    ;(->> (triangle-hulls
    ;  (key-place 0 cornerrow fat-web-post-br)
    ;  (key-place 0 cornerrow fat-web-post-bl)
    ;  (thumb-r-place web-post-tl)
    ;  (key-place 1 cornerrow web-post-bl)
    ;  (key-place 1 cornerrow web-post-br)) (color BLU))
    (->> (triangle-hulls
      (key-place 1 cornerrow (translate [0 0 -0.8] (hull (translate [0 0 (- v-key-case-extend)] short-post-back-tl) short-post-back-tl)))
      (key-place 1 cornerrow (translate [0 0 -0.8] (hull (translate [0 0 (- v-key-case-extend)] short-post-back-tr) short-post-back-tr)))
      (thumb-urr-place (translate [0 0 -1] short-post-bl))
      (key-place 1 cornerrow (translate [0 0 -0.8] (hull (translate [0 0 (- v-key-case-extend)] short-post-back-br) short-post-back-br)))
      ;(thumb-r-place plate-post-tr)
      ) (color NBL))
    ;(->> (triangle-hulls
    ;  (thumb-urr-place upper-fat-web-post-tl-lower)
    ;  (key-place 1 cornerrow web-post-br)
    ;  (key-place 2 real-lastrow web-post-bl)
    ;  ) (color NBL))
    ;(->> (triangle-hulls
    ;  (thumb-urr-place upper-fat-web-post-tl-lower)
    ;  (key-place 2 real-lastrow web-post-bl)
    ;  (key-place 2 real-lastrow web-post-br)
    ;  ) (color NBL))
    ;(->> (triangle-hulls
    ;  (thumb-urr-place upper-fat-web-post-tl-lower)
    ;  (key-place 3 real-lastrow web-post-bl)
    ;  (key-place 2 real-lastrow web-post-br)
    ;  ) (color NBL))
    ;(->> (triangle-hulls
    ;  (thumb-urr-place upper-fat-web-post-tl-lower)
    ;  (thumb-urr-place upper-fat-web-post-bl-lower)
    ;  (key-place 1 real-lastrow web-post-br)
    ;  ) (color NBL))
    ;(->> (triangle-hulls
    ;  (thumb-urr-place fat-web-post-bl)
    ;  (key-place 1 real-lastrow web-post-br)
    ;  (key-place 1 real-lastrow web-post-bl)
    ;  (thumb-r-place fat-web-post-tr)
    ;  (thumb-urr-place fat-web-post-bl)
    ;  ) (color NBL))
    ;;(->> (hull
    ;;  (key-place 1 cornerrow web-post-br)
    ;;  (key-place 3 real-lastrow web-post-bl)
    ;;  (key-place 2 real-lastrow web-post-br)
    ;;  (key-place 2 real-lastrow web-post-bl)
    ;;  ) (color NBL))
    ;(when (not left)
    ;  (->> (hull
    ;    (thumb-m-place web-post-tr)
    ;    (thumb-m-place web-post-tl)
    ;    (key-place 1 real-lastrow web-post-bl)
    ;    (trackball-place trackball-post-bl)
    ;    (trackball-place trackball-post-br)
    ;    ;(thumb-l-place web-post-tr)
    ;    ) (color NBL)))
    ;(when (not left)
    ;  (->> (hull
    ;    (thumb-m-place web-post-tr)
    ;    (thumb-m-place web-post-tl)
    ;    (key-place 1 real-lastrow web-post-bl)
    ;    (key-place 1 real-lastrow web-post-br)
    ;    (thumb-r-place web-post-tl)
    ;    ) (color NBL)))
    ;;(->> (triangle-hulls
    ;;  (key-place 2 lastrow web-post-tl)
    ;;  ; (thumb-r-place fat-web-post-tr)
    ;;  ; (key-place 2 lastrow web-post-bl)
    ;;  (thumb-r-place fat-web-post-br)) (color PUR))
  ))

; dx1, dy1, dx2, dy2 = direction of the wall. '1' for front, '-1' for back, '0' for 'not in this direction'.
; place1, place2 = function that places an object at a location, typically refers to the center of a key position.
; post1, post2 = the shape that should be rendered
(defn wall-brace'' [place1 dx1 dy1 post1 upper1 border1 extend1
                  place2 dx2 dy2 post2 upper2 border2 extend2]
  (let [
         dx1 (if (and (not border1) upper1) dx1 dx1)
         dy1 (if (and (not border1) upper1) 0 dy1)
         dx2 (if (and (not border2) upper2) dx2 dx2)
         dy2 (if (and (not border2) upper2) 0 dy2)
       ]
  "If you want to change the wall, use this.
   place1 means the location at the keyboard, marked by key-place or thumb-xx-place
   dx1 means the movement from place1 in x coordinate, multiplied by wall-xy-locate.
   dy1 means the movement from place1 in y coordinate, multiplied by wall-xy-locate.
   post1 means the position this wall attached to place1.
         xxxxx-br means bottom right of the place1.
         xxxxx-bl means bottom left of the place1.
         xxxxx-tr means top right of the place1.
         xxxxx-tl means top left of the place1.
   place2 means the location at the keyboard, marked by key-place or thumb-xx-place
   dx2 means the movement from place2 in x coordinate, multiplied by wall-xy-locate.
   dy2 means the movement from place2 in y coordinate, multiplied by wall-xy-locate.
   post2 means the position this wall attached to place2.
         xxxxx-br means bottom right of the place2.
         xxxxx-bl means bottom left of the place2.
         xxxxx-tr means top right of the place2.
         xxxxx-tl means top left of the place2.
   How does it work?
   Given the following wall
       a ==\\ b
            \\
           c \\ d
             | |
             | |
             | |
             | |
           e | | f
   In this function a: usually the wall of a switch hole.
                    b: the result of hull and translation from wall-locate1
                    c: the result of hull and translation from wall-locate2
                    d: the result of hull and translation from wall-locate3
                    e: the result of bottom-hull translation from wall-locate2
                    f: the result of bottom-hull translation from wall-locate3"
  (union
    (->> (hull
      (hull
        (place1 (translate (wall-locate1 dx1 dy1 border1) post1))
        ; (place1 (translate (wall-locate2 dx1 dy1 border) post1))
        (place1 post1)
      )
      (hull
        (when extend1 (place1 (translate [0 0 (- upper-case-out)] post1)))
        (place1 (translate (wall-locate2' dx1 dy1 upper1 border1 extend1) post1))
        (place2 (translate (wall-locate2' dx2 dy2 upper2 border2 extend2) post2))
        (when extend2 (place2 (translate [0 0 (- upper-case-out)] post2)))
      )
      (hull
        (place2 (translate (wall-locate1 dx2 dy2 border2) post2))
        ; (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        (place2 post2)
      ))
    (color YEL))
    (when (and (not border1) (not border2))
      (->> (bottom-hull
        (place1 (translate (wall-locate2' dx1 dy1 upper1 border1 extend1) post1))
        ; (place1 (translate (wall-locate2 dx1 dy1 border) post1))
        (place2 (translate (wall-locate2' dx2 dy2 upper2 border1 extend2) post2))
        ; (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        )
        (color ORA))
    )
  )))

(defn wall-brace' [place1 dx1 dy1 post1 upper1
                  place2 dx2 dy2 post2 upper2
                  border]
  (wall-brace'' place1 dx1 dy1 post1 upper1 border false place2 dx2 dy2 post2 upper2 border false))

(defn wall-brace-upper-extend [place1 dx1 dy1 post1 
                  place2 dx2 dy2 post2
                  border]
  (let [
         dx1 (if border dx1 dx1)
         dy1 (if border dy1 0)
         dx2 (if border dx2 dx2)
         dy2 (if border dy2 0)
       ]
    (wall-brace'' place1 dx1 dy1 post1 true border true
                    place2 dx2 dy2 post2 true border true
                    )
  )
)


(defn wall-brace-upper [place1 dx1 dy1 post1 
                  place2 dx2 dy2 post2
                  border]
    
  (wall-brace' place1 0 0 post1 true
                  place2 0 0 post2 true
                  border)
 
)

(defn wall-brace [place1 dx1 dy1 post1 
                  place2 dx2 dy2 post2
                  border]
  (wall-brace' place1 dx1 dy1 post1 false
                  place2 dx2 dy2 post2 false
                  border))

(defn wall-brace-deeper [place1 dx1 dy1 post1 
                         place2 dx2 dy2 post2
                         border]
  "try to extend back wall further back for certain sections"
  (union
    (->> (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      ; (place1 (translate (wall-locate3 dx1 dy1 border) post1))
      (place1 (translate (wall-locate3 dx1 dy1 border) post1))

      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      ; (place2 (translate (wall-locate3 dx2 dy2 border) post2))
      (place2 (translate (wall-locate3 dx2 dy2 border) post2))
      )
    (color BLU))
    (if (not border)
      (->> (bottom-hull
        (place1 (translate (wall-locate3 dx1 dy1 border) post1))
        ; (place1 (translate (wall-locate3 dx1 dy1 border) post1))
  
          (place2 (translate (wall-locate3 dx2 dy2 border) post2))
          ; (place2 (translate (wall-locate3 dx2 dy2 border) post2))
          )
        (color YEL))
    )
  ))

(defn wall-brace-back [place1 dx1 dy1 post1 
                       place2 dx2 dy2 post2
                       border]
  (union
    (->> (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      ; (place1 (translate (wall-locate3 dx1 dy1 border) post1))
      (place1 (translate (wall-locate2 dx1 dy1 border) post1))

      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      ; (place2 (translate (wall-locate3 dx2 dy2 border) post2))
      (place2 (translate (wall-locate2 dx2 dy2 border) post2))
      )
    (color PUR))
    (if (not border)
      (->> (bottom-hull
          (place1 (translate (wall-locate2 dx1 dy1 border) post1))
          (place1 (translate (wall-locate2 dx1 dy1 border) post1))
  
        (place2 (translate (wall-locate3 dx2 dy2 border) post2))
        (place2 (translate (wall-locate3 dx2 dy2 border) post2))
        )
       (color MAG))
    )
  )
)

(defn wall-brace-right [place1 dx1 dy1 post1 
                       place2 dx2 dy2 post2
                       border]
  (union
    (->> (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      ;(place1 (translate (wall-locate3 dx1 dy1 border) post1))
      (place1 (translate (wall-locate2 dx1 dy1 border) post1))

      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      (place2 (translate (wall-locate3 dx2 dy2 border) post2))
      (place2 (translate (wall-locate2 dx2 dy2 border) post2))
      )
    (color CYA))
    (if (not border)
      (->> (bottom-hull
          (place1 (translate (wall-locate2 dx1 dy1 border) post1))
          (place1 (translate (wall-locate2 dx1 dy1 border) post1))
  
        (place2 (translate (wall-locate3 dx2 dy2 border) post2))
        (place2 (translate (wall-locate3 dx2 dy2 border) post2))
        )
       (color NBL))
    )
  )
)

(defn wall-brace-left [place1 dx1 dy1 post1 
                       place2 dx2 dy2 post2
                       border]
  (union
    (->> (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      (place1 (translate (wall-locate3 dx1 dy1 border) post1))
      (place1 (translate (wall-locate2 dx1 dy1 border) post1))

      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      ; (place2 (translate (wall-locate3 dx2 dy2 border) post2))
      (place2 (translate (wall-locate2 dx2 dy2 border) post2))
      )
    (color CYA))
    (if (not border)
      (->> (bottom-hull
          (place1 (translate (wall-locate3 dx1 dy1 border) post1))
          (place1 (translate (wall-locate3 dx1 dy1 border) post1))
  
        (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        )
       (color NBL))
    )
  )
)

(defn wall-brace-less [place1 dx1 dy1 post1 
                       place2 dx2 dy2 post2
                       border]
  (union
    (->> (hull
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      ; (place1 (translate (wall-locate2 dx1 dy1 border) post1))
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      ; (place2 (translate (wall-locate2 dx2 dy2 border) post2))
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      )
    (color YEL))
    (if (not border)
      (->> (bottom-hull
        (place1 (translate (wall-locate1 dx1 dy1 border) post1))
        ; (place1 (translate (wall-locate2 dx1 dy1 border) post1))
        (place2 (translate (wall-locate1 dx2 dy2 border) post2))
        ; (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        )
        (color ORA))
    )
  ))

(defn key-wall-brace-less [x1 y1 dx1 dy1 post1 
                      x2 y2 dx2 dy2 post2
                      border]
  (wall-brace-less (partial (if border key-place key-place-shifted) x1 y1) dx1 dy1 post1
                   (partial (if border key-place key-place-shifted) x2 y2) dx2 dy2 post2
                   border))

(defn key-wall-brace [x1 y1 dx1 dy1 post1 
                      x2 y2 dx2 dy2 post2
                      border]
  (wall-brace (partial (if border key-place key-place-shifted) x1 y1) dx1 dy1 post1
              (partial (if border key-place key-place-shifted) x2 y2) dx2 dy2 post2
              border))

(defn key-wall-brace-trackball [x1 y1 dx1 dy1 post1 
                      x2 y2 dx2 dy2 post2
                      border]
  (wall-brace (partial (if border key-place key-place-shifted) x1 y1) dx1 dy1 post1
              (partial (if border key-place key-place-shifted) x2 y2) dx2 dy2 post2
              border))

(defn key-wall-brace-left [x1 y1 dx1 dy1 post1 
                           x2 y2 dx2 dy2 post2
                           border]
  (wall-brace-left
              (partial (if border key-place key-place-shifted) x1 y1) dx1 dy1 post1
              (partial (if border key-place key-place-shifted) x2 y2) dx2 dy2 post2
              border))

(defn key-wall-brace-back [x1 y1 dx1 dy1 post1 
                           x2 y2 dx2 dy2 post2
                           border]
  (wall-brace-back
              (partial (if border key-place key-place-shifted) x1 y1) dx1 dy1 post1
              (partial (if border key-place key-place-shifted) x2 y2) dx2 dy2 post2
              border))

(defn key-wall-brace-deeper [x1 y1 dx1 dy1 post1 
                             x2 y2 dx2 dy2 post2
                             border]
  (wall-brace-deeper
              (partial (if border key-place key-place-shifted) x1 y1) dx1 dy1 post1
              (partial (if border key-place key-place-shifted) x2 y2) dx2 dy2 post2 
              border))

(defn key-corner [x y loc border]
  (case loc
    :tl (key-wall-brace x y 0  1 fat-web-post-tl x y -1 0 fat-web-post-tl border)
    :tr (key-wall-brace x y 0  1 fat-web-post-tr x y  1 0 fat-web-post-tr border)
    :bl (key-wall-brace x y 0 -1 fat-web-post-bl x y -1 0 fat-web-post-bl border)
    :br (key-wall-brace x y 0 -1 fat-web-post-br x y  1 0 fat-web-post-br border)))

(defn upper-key-case-right-wall [place] (union
    (->> (wall-brace-upper-extend place  0   -1 fat-web-post-br place 1  0 fat-web-post-br true) (color BRO)) ; corner
    (->> (wall-brace-upper-extend place  1   0  fat-web-post-br place 1  0 fat-web-post-tr true) (color BRO))
    (->> (wall-brace-upper-extend place  1   0  fat-web-post-tr place 0  1 fat-web-post-tr true) (color BRO)) ; corner
))

(defn upper-key-case-top-wall [place] (union
    (->> (wall-brace-upper-extend place  0   1  fat-web-post-tr place 0  1 fat-web-post-tl true) (color BRO))
))

(defn upper-key-case-left-wall [place] (union
    (->> (wall-brace-upper-extend place  0   1  fat-web-post-tl place -1 0 fat-web-post-tl true) (color BRO)) ; corner
    (->> (wall-brace-upper-extend place  -1  0  fat-web-post-tl place -1 0 fat-web-post-bl true) (color BRO))
    (->> (wall-brace-upper-extend place  -1  0  fat-web-post-bl place 0 -1 fat-web-post-bl true) (color BRO)) ; corner
))

(defn upper-key-case-back-wall [place] (union
    (hull
      (place upper-fat-web-post-tl)
      (place upper-fat-web-post-tr)
      (place upper-fat-web-post-bl)
      (place upper-fat-web-post-br)
    )
))

(defn upper-key-case [place] (union
    (upper-key-case-right-wall place)
    (upper-key-case-top-wall place)
    (upper-key-case-left-wall place)
    (upper-key-case-back-wall place)
  )
)

(defn right-wall [border]
  (union 
    ;(key-corner (dec lastcol) firstrow :tr border)
    ;(for [y (range 3 lastrow)] (key-wall-brace (dec lastcol)      y  -1 0 web-post-bl (dec lastcol) y -1 0 web-post-tl border))

    (wall-brace-upper      (partial key-place' border (dec lastcol) 1)  1  1 upper-fat-web-post-tr-lower (partial key-place' border (dec lastcol) 1)  1  1 upper-fat-web-post-br-lower border)
    (wall-brace'           (partial key-place' border (dec lastcol) 1)  0  0 upper-fat-web-post-br-lower true (partial key-place' border (dec lastcol) 2)  1  0 fat-web-post-tr false border)
    (key-wall-brace        (dec lastcol) 2 1  0 fat-web-post-tr (dec lastcol) 2 1  0 fat-web-post-br border)
    (key-wall-brace        (dec lastcol) 2 1  0 fat-web-post-br (dec lastcol) 3 -1  0 fat-web-post-bl border)
    (key-wall-brace        (dec lastcol) 3 -1  0 fat-web-post-bl (dec lastcol) 3 -1  0 fat-web-post-tl border)
    (key-corner (dec lastcol) cornerrow :tl border)

    (when border (union
      (upper-key-case-right-wall (partial key-place' border (dec lastcol) 1))
    ))
   )
)

(defn back-wall [border]
  (union
    ; (key-wall-brace 0 0 0 1 web-post-tl          0  0 0 1 web-post-tr border)
    (for [c (range firstcol (dec ncols))] 
                  (union 
                    (wall-brace-upper        (partial key-place' border c firstrow)  1  1 upper-fat-web-post-tl-lower (partial key-place' border c firstrow)  1  1 upper-fat-web-post-tr-lower border)
                    (when border (union
                      (upper-key-case-top-wall (partial key-place' border c firstrow))
                      (upper-key-case-back-wall (partial key-place' border c firstrow))
                    ))
                  )
    )
    (for [c (range 1 (dec ncols))]
                  (union 
                    (->> (wall-brace-upper (partial key-place' border c firstrow) 0 1 upper-fat-web-post-tl-lower (partial key-place' border (dec c) firstrow) 0 1 upper-fat-web-post-tr-lower border) (color PUR))
                    (when border (union
                      (->> (wall-brace-upper-extend (partial key-place' border c firstrow)  0   1  fat-web-post-tl (partial key-place' border (dec c) firstrow) 0 1 fat-web-post-tr true) (color BRO)) ; corner
                      (hull
                        ((partial key-place' border c firstrow) upper-fat-web-post-tl)
                        ((partial key-place' border c firstrow) upper-fat-web-post-bl)
                        ((partial key-place' border (dec c) firstrow) upper-fat-web-post-tr)
                        ((partial key-place' border (dec c) firstrow) upper-fat-web-post-br)
                      )
                    ))
                  )
    )
  )
)

(defn trackball-wall [border] (let [
        key-place (if border key-place key-place-shifted)
        trackball-place (if border trackball-place trackball-place-shifted)
       ] 
  (union
    (wall-brace' (partial key-place firstcol homerow) -1 0 upper-fat-web-post-tr-lower true trackball-place -1 0 trackball-post-tl false border)
    (wall-brace trackball-place -1 0 trackball-post-tl trackball-place -1 0 trackball-post-bl border)
    (wall-brace trackball-place -1 0 trackball-post-bl (partial thumb-m-place' border) -1 0 fat-web-post-tl border)
)))

(defn left-wall [left border]
  (let [key-place' (partial key-place' border)]
  (union 
    ; (key-wall-brace  0 0  -1 0 web-post-tl 0 1 -1 0 web-post-bl border)

    ;(for [y (range firstrow (- lastrow 2))] (key-wall-brace firstcol      y  -1 0 web-post-tl firstcol y -1 0 web-post-bl border))
    ;(for [y (range (inc firstrow) (- lastrow 2))] (key-wall-brace firstcol (dec y) -1 0 web-post-bl firstcol y -1 0 web-post-tl border))
    ;(->> (wall-brace-upper-extend (partial key-place' firstcol 2)  -1  0 fat-web-post-br (partial key-place' firstcol 2)  0 0 fat-web-post-tr border) (color BRO))
    (when border (union
      (upper-key-case-left-wall (partial key-place' firstcol 1))
      (upper-key-case (partial key-place' firstcol 2))
    ))

    (->> (wall-brace-upper (partial key-place' firstcol 1)  -1  0 upper-fat-web-post-tl-lower (partial key-place' firstcol 2)  0 0 upper-fat-web-post-tr-lower border) (color YEL))
    ;(->> (wall-brace-upper (partial key-place' firstcol 1)  -1  0 upper-fat-web-post-bl-lower (partial key-place' firstcol 2)  0 0 upper-fat-web-post-tr-lower border) (color YEL))
    ;(->> (wall-brace-upper (partial key-place' firstcol 2)  -1  0 upper-fat-web-post-br-lower (partial key-place' firstcol 2)  -1  0 upper-fat-web-post-tr-lower border) (color YEL))

    (if left 
      (union 
        (->> (wall-brace-upper (partial key-place' firstcol 2)  -1  0 upper-fat-web-post-tr-lower (partial thumb-ur-place' border)  -1  0 upper-fat-web-post-tl-lower border) (color BRO))

        (when border (union
          (color BRO (hull
            (key-place' firstcol 2 upper-fat-web-post-tr-lower)
            (thumb-ur-place' border upper-fat-web-post-tl)
            (thumb-ur-place' border upper-fat-web-post-tl-lower)
          ))

          (color BRO (hull
            (key-place' firstcol 2 (translate [0 0 (- upper-case-out)] fat-web-post-br))
            (thumb-ur-place' border (translate [0 0 (- swap-z upper-post-offset)]  short-post-back-tl))
          ))
          ;(key-wall-brace firstcol (- lastrow 2) 0 -1 web-post-bl firstcol (- lastrow 2) 0 -1 web-post-br border)
          ;(key-wall-brace firstcol (- lastrow 2) 0 -1 web-post-br (inc firstcol) (- lastrow 2) -1 -1 web-post-bl border)
          ;(key-wall-brace (inc firstcol) (- lastrow 2) -1 -1 web-post-bl (inc firstcol) (dec lastrow) -1 0 web-post-tl border)
          ;(key-wall-brace (inc firstcol) (dec lastrow) -1 0 web-post-tl (inc firstcol) (dec lastrow) -1 0 web-post-bl border)
      ))) 
      (union)
      ;(trackball-wall border)
    )
  )
  )
)

(defn front-wall [left border]
  (let [ 
        thumb-r-place (if border thumb-r-place (partial thumb-r-place' border)) 
        thumb-urr-place (if border thumb-urr-place (partial thumb-urr-place' border)) 
        key-place (if border key-place key-place-shifted) 
       ]
  (union 
    (key-wall-brace 4 cornerrow 0 1 fat-web-post-tr      4  cornerrow 0 1 fat-web-post-tl border)
    ;(for [x (range 5 (dec ncols))] (key-wall-brace x cornerrow 0 -1 fat-web-post-bl (dec x) cornerrow 0 -1 fat-web-post-br border))
    ;(key-wall-brace 3 real-lastrow 0   -1 fat-web-post-bl     3   real-lastrow 0.5 -1 fat-web-post-br border)
    ;(key-wall-brace 3 real-lastrow 0.5 -1 fat-web-post-br 4 cornerrow -1 0 fat-web-post-bl border)
    ;(key-wall-brace 4 cornerrow -1 0 fat-web-post-bl 4 cornerrow 0 -1 fat-web-post-bl border)

    ; extra wall for lower index key
    (when border (letfn [
                     (new-post [post] (hull post (translate [0 0 (- v-key-case-extend)] post)))
                   ]
                   (union
                     (key-wall-brace 1 3 0 -1 (new-post fat-web-post-br) 1 3 1 0  (new-post fat-web-post-br) border)
                     (key-wall-brace 1 3 1 0  (new-post fat-web-post-br) 1 3 1 0  (new-post fat-web-post-tr) border)
                     (key-wall-brace 1 3 1 0  (new-post fat-web-post-tr) 1 3 0 1  (new-post fat-web-post-tr) border)
                     (key-wall-brace 1 3 0 1  (new-post fat-web-post-tr) 1 3 0 1  (new-post fat-web-post-tl) border)
                     (key-wall-brace 1 3 0 1  fat-web-post-tl 1 3 -1 0 fat-web-post-tl border)
                     (key-wall-brace 1 3 -1 0 fat-web-post-tl 1 3 -1 0 fat-web-post-bl border)
                   )))

    (->> 
        (union 
          (wall-brace' thumb-r-place 0 -1 fat-web-post-br false thumb-urr-place 0 0 upper-fat-web-post-br-lower true border)
          (wall-brace' thumb-urr-place 0 0 upper-fat-web-post-br-lower true thumb-urr-place 0 0 upper-fat-web-post-tr-lower true border)
          (wall-brace' thumb-urr-place 0 0 upper-fat-web-post-tr-lower true (partial key-place 4 3) 1 1 fat-web-post-tr false border)
        )
      (color RED)
    )
  ))
)

(defn bottom-corner-alpha [shape] (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) shape)))

(defn thumb-wall [left border]
  (let []
  (union 
    ; thumb walls
    ;(->> (wall-brace-right (partial thumb-r-place' border)  0 -1 fat-web-post-br (partial thumb-r-place' border)  0 -1 fat-web-post-bl border) (color ORA))

    (when border (union
      (upper-key-case (partial thumb-urr-place' border))
    ))

    (when left (union
      (->> (wall-brace (partial thumb-r-place' border)  0 -1 fat-web-post-br (partial thumb-r-place' border)  0 -1 fat-web-post-bl border) (color ORA))

      (when border (union
        (->> (wall-brace''        (partial thumb-r-place' border)  -1  -1 fat-web-post-bl false border false (partial thumb-ub-place' border)  0  -1 fat-web-post-bl false border false) (color BRO))

        (upper-key-case-left-wall (partial thumb-ub-place' border))

        (upper-key-case-top-wall (partial thumb-ub-place' border))

        (->> (wall-brace-upper-extend        (partial thumb-ub-place' border)  0  1 fat-web-post-tr (partial thumb-u-place' border)  0  1 fat-web-post-tl border) (color BRO))

        (upper-key-case-top-wall (partial thumb-u-place' border))

        (->> (wall-brace-upper-extend        (partial thumb-u-place' border)  0 1 fat-web-post-tr (partial thumb-ur-place' border)  0  1 fat-web-post-tl border) (color BRO))

        (upper-key-case-top-wall (partial thumb-ur-place' border))

        (upper-key-case-right-wall (partial thumb-ur-place' border))

        (upper-key-case-back-wall (partial thumb-u-place' border))
        (upper-key-case-back-wall (partial thumb-ur-place' border))
        (upper-key-case-back-wall (partial thumb-ub-place' border))

        (hull
          (thumb-u-place' border upper-fat-web-post-tr)
          (thumb-u-place' border upper-fat-web-post-br)
          (thumb-ur-place' border upper-fat-web-post-tl)
          (thumb-ur-place' border upper-fat-web-post-bl)
        )

        (hull
          (thumb-u-place' border upper-fat-web-post-tl)
          (thumb-u-place' border upper-fat-web-post-bl)
          (thumb-ub-place' border upper-fat-web-post-tr)
          (thumb-ub-place' border upper-fat-web-post-br)
        )
      ))
    ))

    (when left (union
      (->> (wall-brace'        (partial thumb-r-place' border)  -1  -1 fat-web-post-bl false (partial thumb-ub-place' border)  0  0 upper-fat-web-post-bl-lower true border) (color BRO))
      (->> (wall-brace-upper        (partial thumb-ub-place' border)  -1  0 upper-fat-web-post-bl-lower (partial thumb-ub-place' border)  -1  0 upper-fat-web-post-tl-lower border) (color BRO))
      (->> (wall-brace-upper        (partial thumb-ub-place' border)  -1  0 upper-fat-web-post-tl-lower (partial thumb-ub-place' border)  0 1  upper-fat-web-post-tl-lower border) (color GRE))
      (->> (wall-brace-upper        (partial thumb-ub-place' border)  0  1 upper-fat-web-post-tl-lower (partial thumb-ub-place' border)  0  1 upper-fat-web-post-tr-lower border) (color BRO))
      (->> (wall-brace-upper        (partial thumb-ub-place' border)  0  1 upper-fat-web-post-tr-lower (partial thumb-u-place' border)  0  1 upper-fat-web-post-tl-lower border) (color BRO))
      (->> (wall-brace-upper        (partial thumb-u-place' border)  0  1 upper-fat-web-post-tl-lower (partial thumb-u-place' border)  0  1 upper-fat-web-post-tr-lower border) (color BRO))
      (->> (wall-brace-upper        (partial thumb-u-place' border)  0  1 upper-fat-web-post-tr-lower (partial thumb-ur-place' border)  0  1 upper-fat-web-post-tl-lower border) (color BRO))
      ;(->> (wall-brace-upper        (partial thumb-u-place' false)  0  1 upper-fat-web-post-tr-lower (partial thumb-ur-place' false)  0  1 upper-fat-web-post-tl-lower false) (color BRO))
      ;(hull
      ;  ((if border key-place key-place-shifted) firstcol (- lastrow 2) web-post-bl)
      ;  (thumb-ur-place' border upper-fat-web-post-tl-lower)
      ;)

      ;(->> (wall-brace'        (partial thumb-ur-place' border)  0  0 upper-fat-web-post-tl-lower true (partial (if border key-place key-place-shifted) firstcol (- lastrow 2))  0 0  wall-post-bl false border) (color BRO))
      ;(->> (key-wall-brace  firstcol (- lastrow 2) -1 -1 web-post-bl firstcol (- lastrow 2) -1 0 web-post-bl false) (color BRO))
    ))
  

    (when (not left) (union
      ; thumb corners
      (->> (wall-brace        (partial thumb-m-place' border) -1  0 fat-web-post-bl (partial thumb-m-place' border)  0 -1 fat-web-post-bl border) (color NBL))

      (->> (wall-brace        (partial thumb-m-place' border)  0 -1 fat-web-post-br (partial thumb-m-place' border)  0 -1 fat-web-post-bl border) (color YEL))
      (->> (wall-brace-left        (partial thumb-m-place' border)  0 -1 fat-web-post-br (partial thumb-m-place' border)  0 -1 fat-web-post-bl border) (color YEL))
      (->> (wall-brace        (partial thumb-m-place' border) -1  0 fat-web-post-tl (partial thumb-m-place' border) -1  0 fat-web-post-bl border) (color GRY))

      (->> (wall-brace-right (partial thumb-r-place' border)  0 -1 fat-web-post-br (partial thumb-r-place' border)  0 -1 fat-web-post-bl border) (color ORA))
      (->> (wall-brace (partial thumb-r-place' border)  0 -1 fat-web-post-br (partial thumb-r-place' border)  0 -1 fat-web-post-bl border) (color ORA))

      ; thumb tweeners
      (->> (wall-brace (partial thumb-r-place' border)  0 -1 fat-web-post-bl (partial thumb-m-place' border)  0 -1 fat-web-post-br border) (color PIN))
      (->> (wall-brace-deeper (partial thumb-r-place' border)  0 -1 fat-web-post-bl (partial thumb-m-place' border)  0 -1 fat-web-post-br border) (color PIN))
      (->> (wall-brace        (partial thumb-r-place' border)  0 -1 fat-web-post-bl (partial thumb-m-place' border)  0 -1 fat-web-post-br border) (color PIN))

    ))

    ; (->> (wall-brace-deeper thumb-u-place  0  1 fat-web-post-tr bottom-corner-alpha  0  1 fat-web-post-tl border) (color PIN))
    ;(when (not track-ball) (->> (wall-brace        (partial thumb-l-place' border)  0  1 fat-web-post-tr bottom-corner-alpha  0  1 fat-web-post-tl border) (color PIN)))
  ))
)

(defn case-walls [left]
  (union
    (right-wall false)
    (back-wall false)
    (left-wall left false)
    (front-wall left false)
    (thumb-wall left false)
  )
)

(defn case-top-border [left]
  (union
    (right-wall true)
    (back-wall true)
    (left-wall left true)
    (front-wall left true)
    (thumb-wall left true)
  )
)

;;;;;;;;;;;;;;;;;;;
;; Screw Inserts ;;
;;;;;;;;;;;;;;;;;;;

; Screw insert definition & position
(defn screw-insert-shape [res rot bottom-radius top-radius height]
  (let [ shape       (->> (binding [*fn* res]
                       (cylinder [bottom-radius top-radius] height))
                     )
         x          (* 2 top-radius)
         y          (* 0.75 bottom-radius)
         z          (+ 0.01 height)
         cut-shape   (if (= TRIANGLE-RES res)
                         (rotate (deg2rad 30) [0 0 1]
                             (difference (rotate (deg2rad -30) [0 0 1] shape)
                                         (translate [0 y 0] (cube x y z))))
                         shape
                     )
         final-shape (rotate (deg2rad rot) [0 0 1] cut-shape)
  ]
  final-shape)
)

(defn screw-insert [res rot column row bottom-radius top-radius height offset]
  (let [
         orig-position (shift-model-position (key-position column row [0 0 0]))
         position (map + offset [(first orig-position) (second orig-position) (/ height 2)])
       ]
    (->> (screw-insert-shape res rot bottom-radius top-radius height)
         (translate position)
         )))

(defn screw-insert-thumb [res rot bottom-radius top-radius height offset]
  (let [
         orig-position (shift-model-position (thumb-position [0 0 0] offset [0 0 0] ))
         position [(first orig-position) (second orig-position) (/ height 2)]
       ]
    (->> (screw-insert-shape res rot bottom-radius top-radius height)
         (translate position)
         )))

(defn screw-insert-relative-z [res rot column row bottom-radius top-radius height offset]
  (let [
         orig-position (key-position column row [0 0 0])
         position (map + (shift-model-position (map + offset orig-position)) [0 0 (- (/ height 2))])
       ]
    (->> (screw-insert-shape res rot bottom-radius top-radius height)
         (translate position)
         )))

(defn screw-insert-relative-z-thumb [res rot bottom-radius top-radius height offset]
  (let [
         orig-position (thumb-position [0 0 0] offset [0 0 0] )
         position (map + (shift-model-position orig-position) [0 0 (- (/ height 2))])
       ]
    (->> (screw-insert-shape res rot bottom-radius top-radius height)
         (translate position)
         )))


(def screw-insert-bottom-offset 0)
(defn screw-insert-all-shapes [bottom-radius top-radius height left]
  (union 
    (->> (screw-insert ROUND-RES 0 2             1 bottom-radius top-radius height [ -8.5   -8.0 screw-insert-bottom-offset]) (color RED)) ; top middle
    ;(->> (screw-insert ROUND-RES 0 0             1 bottom-radius top-radius height [-3.25  5    screw-insert-bottom-offset]) (color PIN)) ; left FIXME
    ;(->> (screw-insert ROUND-RES 0 0             3 bottom-radius top-radius height [-13 6    screw-insert-bottom-offset]) (color NBL)) ; trackball
    (if left
      (->> (screw-insert-thumb ROUND-RES 0 bottom-radius top-radius height (map + thumb-r-move [-7.5 -24.5 0])) (color BRO)) ; thumb
      (->> (screw-insert-thumb ROUND-RES 0 bottom-radius top-radius height (map + thumb-r-move [-38 35.5 0])) (color BRO)) ; thumb
    )
    (->> (screw-insert ROUND-RES 0 (dec lastcol)       1 bottom-radius top-radius height [ -3  -10  screw-insert-bottom-offset]) (color PUR)) ; top right
    (->> (screw-insert-thumb ROUND-RES 0 bottom-radius top-radius height (map + thumb-r-move [6.5 -0.5 0])) (color BLA)) ; bottom middle
    (->> (screw-insert ROUND-RES 0 (dec lastcol) (- lastrow 1) bottom-radius top-radius height [ 1.7     -2.90  screw-insert-bottom-offset]) (color YEL)) ; bottom right
)) 

(def screw-insert-height 4.1) ; Hole Depth Y: 4.4
(def screw-insert-radius (/ 5.0 2)) ; Hole Diameter C: 4.1-4.4

(defn screw-insert-holes [left] (screw-insert-all-shapes
                          screw-insert-radius 
                          screw-insert-radius 
                          (* screw-insert-height 1.5)
                          left
                        ))

(def screw-insert-wall-thickness 4)
(defn screw-insert-outers [left] (screw-insert-all-shapes
                           (+ screw-insert-radius screw-insert-wall-thickness) 
                           (+ screw-insert-radius screw-insert-wall-thickness) 
                           screw-insert-height
                           left
                         ))

(defn top-screw-insert-all-shapes [res bottom-radius top-radius height]
  (union 
    ;(->> (screw-insert-relative-z res  115 3             1 bottom-radius top-radius height [ -8.75  5.75 (+ (- 0) hide-top-screws) ]) (color RED)) ; top middle
    ;(->> (screw-insert res -114 0             3 bottom-radius top-radius height [ -6   11    (+ 58.75 hide-top-screws)]) (color NBL)) ; left
    ;(->> (screw-insert res  -15 0       lastrow bottom-radius top-radius height [ 12.5 -2.25 (+ 52 hide-top-screws)]) (color CYA)) ; bottom thumb
    ; (->> (screw-insert res  -23 3       lastrow bottom-radius top-radius height [ -12.5  -4.5 (+ 49 hide-top-screws)]) (color GRE)) ; bottom middle
    (->> (screw-insert-relative-z res -119.0 0             1 bottom-radius top-radius height [ -4.0 -13 (+ (- 11.5) hide-top-screws)]) (color PIN)) ; left-top
    (->> (screw-insert-relative-z-thumb res -18.0             bottom-radius top-radius height [ 9.5 -9.5  (+ (+ 17.5) hide-top-screws)]) (color BRO)) ; thumb
    (->> (screw-insert-relative-z res  65 4       2 bottom-radius top-radius height       [ 1    11 (+ (+ 1.5)  hide-top-screws)]) (color PUR)) ; top right
    (->> (screw-insert-relative-z res -22 3       3 bottom-radius top-radius (* height 1) [0 -13.5 (+ 1.2 hide-top-screws)]) (color GRE)) ; bottom right
))

(defn top-screw-insert-round-shapes [bottom-radius top-radius height]
    (top-screw-insert-all-shapes
      ROUND-RES
      bottom-radius
      top-radius
      height
))

(defn top-screw-insert-triangle-shapes [bottom-radius top-radius height]
  (top-screw-insert-all-shapes
      TRIANGLE-RES
      bottom-radius
      top-radius
      height
  )
)

(def top-screw-length 16)               ; M2/M3 screw thread length
(def top-screw-insert-height 10)        ; M2/M3 screw insert length 3.5, use higher value to cut through angled things

; (def top-screw-insert-radius (/ 3.0 2)) ; M2 screw insert diameter
; (def top-screw-radius (/ 2.1 2))        ; M2 screw diameter
; (def top-screw-head-radius (/ 3.6 2))  ; M2 screw head diameter (3.4 plus some clearance)

(def top-screw-insert-radius (/ 4 2)); M3 screw insert diameter
(def top-screw-radius (/ 2.95 2))       ; M3 screw diameter
(def top-screw-head-radius (/ 4.6 2)) ; M3 screw head diameter (4.4 plus some clearance)

(def top-screw-clear-length (- top-screw-length top-screw-insert-height))
(def top-screw-block-height 4)
(def top-screw-block-wall-thickness 7)
(def top-screw-insert-wall-thickness 1.3)

(def top-screw (top-screw-insert-round-shapes
                      top-screw-radius
                      top-screw-radius
                      top-screw-length
                    ))

(def top-screw-insert-holes
    (union
        ; actual threaded insert hole
        (translate [0 0 0]
            (top-screw-insert-round-shapes
                top-screw-insert-radius
                top-screw-insert-radius
                top-screw-insert-height
            ))

        ; clearance and possible drainage hole through top of case
        (translate [0 0 2]
            top-screw)

        ; screw head clearance
        (translate [0 0 (+ (- top-screw-length) 2 2)]
            (top-screw-insert-round-shapes 
                      top-screw-head-radius
                      top-screw-head-radius
                      (* 0.5 top-screw-length)
            ))
    ))

(def top-screw-insert-outers 
    (difference
        (top-screw-insert-round-shapes 
            (+ top-screw-insert-radius top-screw-insert-wall-thickness)
            (+ top-screw-insert-radius top-screw-insert-wall-thickness)
            top-screw-insert-height
        )
        top-screw
    )
)

(def top-screw-block-outers 
    (difference
        ; screw head stop
        (translate [0 0 (- top-screw-insert-height)]
          (top-screw-insert-triangle-shapes 
              (+ top-screw-insert-radius top-screw-block-wall-thickness) 
              (+ top-screw-insert-radius top-screw-block-wall-thickness) 
              top-screw-block-height
          )
        )
        top-screw
    )
)

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; USB Controller Holder ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;
;; PCB Holder ;;
;;;;;;;;;;;;;;;;
(def pcb-holder-vertical true)
(def pcb-holder-x 42.2)
(def pcb-holder-y 36.8)
(def pcb-holder-z 8)
(def pcb-holder-z-rotate 5)
(def trrs_r 2.55)
(def usb_c_x 9.3)
(def usb_c_z 4.5)

(def pcb-holder-bottom-offset 
  (if pcb-holder-vertical 
    (- (/ pcb-holder-x 2) 0.75)
    (/ pcb-holder-z -4) ; TODO solve magic number puzzle here
  )
)

(def pcb-holder-offset-coordinates
  ; (if use_hotswap_holder
    [-29 50.75 (+ pcb-holder-bottom-offset 2)]
    ; [-15.5 50.9 pcb-holder-bottom-offset]
  ; )
)
(defn pcb-holder-place [shape]
  (if pcb-holder-vertical
    (->> shape
         (rotate (deg2rad 90) [0 1 0])
         (translate pcb-holder-offset-coordinates)
         (rotate (deg2rad pcb-holder-z-rotate) [0 0 1])
    )
    (->> shape
         (translate pcb-holder-offset-coordinates)
         (rotate (deg2rad pcb-holder-z-rotate) [0 0 1])
    )
  )
)

(def pcb-holder
  (pcb-holder-place
    (color SLT
      (translate [(/ pcb-holder-x -2) (- pcb-holder-y) 0]
      ; (if pcb-holder-vertical
        (import "../things/printable_shield_left.stl")
        ; (import "../things/usb_holder_w_reset_cutout.stl")
      ; )
      )
    )
  )
)

(def pcb-holder-screw-post-z 20)
(def pcb-holder-screw-post
  (pcb-holder-place
    (color SLT
      (union
        (translate [(- (/ pcb-holder-x  2) 3.5) (+ (- pcb-holder-y) 3) (/ pcb-holder-screw-post-z -2)]
          (cube (* screw-insert-radius 4) (* screw-insert-radius 4) pcb-holder-screw-post-z)
        )
        ; (translate [(+ (/ pcb-holder-x -2) 3.5) (- screw-insert-radius) 0] (with-fn 150 (cylinder screw-insert-radius 6)))
      )
    )
  )
)

(def pcb-holder-cut-vertical
  (pcb-holder-place
    (translate [0 (/ 10 -2) (/ pcb-holder-z 2)]
      (union
        ; PCB board cutout
        (translate [-2 -1 -3.65] (cube 38 10 1.65))

        ; more general PCB components cutout
        (translate [ 1 0 0] (cube (* pcb-holder-x 0.75) 10 (- pcb-holder-z 2)))

        ; usb-c
        (translate [-3.5 0 -1.25] (union
          (translate [-2.5 0 0] (rotate (deg2rad 90) [1 0 0] (with-fn 150 (cylinder (/ usb_c_z 2) 30))))
          (translate [ 0 0 0] (cube (- usb_c_x usb_c_z) 30 usb_c_z))
          (translate [ 2.5 0 0] (rotate (deg2rad 90) [1 0 0] (with-fn 150 (cylinder (/ usb_c_z 2) 30))))
        ))

        ; trrs
        (translate [13.5 0 -0.25] (rotate (deg2rad 90) [1 0 0] (with-fn 150 (cylinder trrs_r 30))))

        ; screw holes
        (translate [0 (- (/ pcb-holder-y -2) 1.5) 0]
          (translate [(+ (/ pcb-holder-x -2) 3.5) (- (/ pcb-holder-y  2) 0  ) -5] (with-fn 150 (cylinder screw-insert-radius 9))) ;top
          (translate [(- (/ pcb-holder-x  2) 3.5) (+ (/ pcb-holder-y -2) 9.5) -5] (with-fn 150 (cylinder screw-insert-radius 20))) ;bottom
        )
      )
    )
  )
)

(def pcb-holder-space
  (color RED
    ; (translate [-25 -15 0]
      ; (if pcb-holder-vertical
        pcb-holder-cut-vertical
      ;   null
      ; )
    ; )
  )
)

;;;;;;;;;;;;;;;;;;
;; Bottom Plate ;;
;;;;;;;;;;;;;;;;;;

(def bottom-plate-thickness 3.7)
(def screw-insert-fillets-z 3)

(def screw-insert-bottom-plate-bottom-radius (/ 6.0 2))
(def screw-insert-bottom-plate-top-radius    (/ 3.0 2))
(defn screw-insert-holes-bottom-plate [left] (screw-insert-all-shapes 
                                       screw-insert-bottom-plate-top-radius 
                                       screw-insert-bottom-plate-top-radius 
                                       99
                                       left
                                     ))

(defn screw-insert-fillets-bottom-plate [left] (screw-insert-all-shapes
                                         screw-insert-bottom-plate-bottom-radius 
                                         screw-insert-bottom-plate-top-radius 
                                         screw-insert-fillets-z
                                         left
                                       ))


(defn screw-insert-wrist-rest [bottom-radius top-radius height]
    (for [x (range 0 9)
          y (range 0 9)]
        (translate [(* x 5) (* y 5) 0]
          (screw-insert-shape
            ROUND-RES
            0
            bottom-radius
            top-radius
            height)
        )
    )
)

(defn screw-insert-wrist-rest-four [bottom-radius top-radius height]
    (for [x (range 0 2)
          y (range 0 2)]
        (translate [(* x 20) (* y 20) 0]
          (screw-insert-shape
            ROUND-RES
            0
            bottom-radius
            top-radius
            height)
        )
    )
)

(def wrist-shape-connector-width 67)
(def wrist-shape-connector-half-width (/ wrist-shape-connector-width 2))
(def wrist-shape-connector (polygon [[(- wrist-shape-connector-half-width 11) 10] 
                                     [ 30 -20] 
                                     [-30 -20] 
                                     [(- wrist-shape-connector-half-width) 10]]))
(def wrist-shape 
    (union 
        (translate [0 -45 0] (cube 60 55 bottom-plate-thickness))
        (translate [0 0 (- (/ bottom-plate-thickness -2) 0.05)]
                   (hull (->> wrist-shape-connector
                              (extrude-linear {:height 0.1 :twist 0 :convexity 0}))
                         (->> wrist-shape-connector
                              (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                              (translate [0 0 bottom-plate-thickness])) ))
    )
)

; begin heavily modified crystalhand wrist rest code
(def wrist-rest-x-angle 16)
(def wrist-rest-y-angle-adj 0)   ; additional tenting angle for wrist rest
(def wrist-rest-z-height-adj 28) ; additional z height for wrist rest

;magic numbers to tweak how well the gel wrist rest is held
(def wrist-rest-recess-depth 4)
(def wrist-rest-recess-x-scale 4.25)
(def wrist-rest-recess-y-scale 4.33)

(def wrirst-rest-base-zheight (* 2.01 wrist-rest-recess-depth))
(def wrist-rest-right-base
    (let [
          wrist-rest-cut-bottom (translate [0 0 -150]
                                    (cube 300 300 300))
          zheight-cut (* 1.01 wrirst-rest-base-zheight)
          shape-curve-cut (scale [1.1, 1, 1]
                              (->> (cylinder 7 zheight-cut)
                                   (with-fn 250)
                                   (translate [0 -13.4 0]))
                              (->> (cube 18 10 zheight-cut)
                                   (translate [0 -12.4 0])))
          shape (scale [wrist-rest-recess-x-scale 
                        wrist-rest-recess-y-scale 
                        1]
                    (union
                        (difference
                            (scale [1.3, 1, 1]
                                   (->> (cylinder 10 wrirst-rest-base-zheight)
                                        (with-fn 250)
                                        (translate [0 0 0])
                                        (color BLU)))
                            shape-curve-cut
                        )
                        (->> (cylinder 6.8 wrirst-rest-base-zheight)
                             (with-fn 250)
                             (translate [-6.15 -0.98 0])
                             (color YEL))
                        (->> (cylinder 6.8 wrirst-rest-base-zheight)
                             (with-fn 250)
                             (translate [6.15 -0.98 0])
                             (color ORA))
                        (->> (cylinder 5.9 wrirst-rest-base-zheight)
                             (with-fn 250)
                             (translate [-6.35 -2 0])
                             (color PUR))
                        (scale [1.01, 1, 1]
                               (->> (cylinder 5.9 wrirst-rest-base-zheight)
                                    (with-fn 250)
                                    (translate [6.35 -2. 0])
                                    (color GRE)))
                    )
                )
        ]
        (difference (->> shape
                         (rotate (deg2rad 180) [0 0 1])
                    )
                    wrist-rest-cut-bottom
        )
    )
)

(defn wrist-rest-angler [shape]
    (let [wrist-rest-y-angle (+ (* tenting-angle 45) (- 10))
          angled-shape (->> shape
                            (rotate  (/ (* pi wrist-rest-x-angle)     180) [1 0 0])
                            (rotate  (/ (* pi wrist-rest-y-angle)     180) [0 1 0])
                            (rotate  (/ (* pi wrist-rest-y-angle-adj) 180) [0 1 0])
                            (translate [0 0 (+ wrist-rest-z-height-adj 
                                               wrirst-rest-base-zheight)])
                       )
         ]
         angled-shape
    )
)

(def wrist-rest-right
    (let [
           outline (scale [1.08 1.08 1] 
                       wrist-rest-right-base
                   )
           recess-cut (translate [0 0 (- (/ wrirst-rest-base-zheight 2)
                                   wrist-rest-recess-depth)]
                    wrist-rest-right-base
                )
           top (difference
                   outline
                   recess-cut
               )
           top-angled (wrist-rest-angler top)
           base  (translate [0 0 150]
                     (extrude-linear { :height 300 } 
                         (project 
                             (scale [0.999 0.999 1] 
                                 top-angled)
                         )
                     )
                 )
           base-cut (hull top-angled
                          (translate [0 0 300] base)
                    )
           base-trimmed (difference base
                                    base-cut
                        )
         ]
         (union top-angled
                base-trimmed
                ; (debug thingy)
         )
    )
)
; end heavily modified crystalhand wrist rest code


(defn case-walls-bottom [left] (cut 
                           (translate [0 0 0] 
                                      (case-walls left)
                           )
                       ))
(defn case-walls-bottom-projection [left] (project
                                    (union
                                     (extrude-linear {:height 0.01
                                                      :scale 0.995
                                                      :center true} 
                                         (case-walls-bottom left)
                                     )
                                     (extrude-linear {:height 0.01
                                                      :scale 1.05
                                                      :center true} 
                                         (case-walls-bottom left)
                                     )
                                    ) 
                                  ))

;;;;;;;;;;;;;;;;;
;; Sensor Case ;;
;;;;;;;;;;;;;;;;;

(defn eps [x] (+ x 0.1))

(def pcb-width-actual 1.6)
(def pcb-length 28.5)
(def pcb-height 21.5)

(def lens-thickness 3.4)
(def lens-clearance 14.5)
(def pcb-thickness 4.1)

(def pcb-lens-thickness (+ lens-thickness pcb-thickness))

(def case-thickness 2.5)
(def cable-offset 1.4)
(def cables-width 20.8)
(def cables-height 3.35)
(def lens-width (- pcb-length (* 2 2.7)))
(def dowel-width 2.5)
(def dowel-depth 1.9)
(def dowel-offset (/ 13.5 2))
(def cable-holder-length lens-width)
(def cable-holder-depth 2.7)
(def cable-holder-height 10)

(def sensor-holder-distance 23.85)

(def M2-insert-height 4)
(def M2-insert-rad (/ 3.53 2))
(def M2-washer-rad (/ 5.45 2))
(def M2-screw-rad (/ 2.25 2))

(def cover-insert-wall-thickness 1.2)


(def cover-insert-total-rad (+ M2-insert-rad cover-insert-wall-thickness))

(def lens-adapter-width 3.55)
(def lens-adapter-thickness 1.2)

;TODO rename
(def total-thickness (+ pcb-thickness (* 2 case-thickness)))
(def total-height pcb-height)
(def total-length (+ pcb-length (* 2 case-thickness)))

(def total-cable-holder-length (+ cable-holder-length (* 2 case-thickness)))
(def total-cable-holder-height (+ cable-holder-height (* 2 case-thickness)))
(def total-cable-holder-width (+ cable-holder-depth (* 2 case-thickness)))

(def base
  (union
    (difference
      (cube total-length total-thickness total-height)

      ; PCB cutout
      (translate [0 0 0] (cube pcb-length pcb-thickness total-height))

      ; space for lens dowels
      (translate [dowel-offset (+ (/ dowel-depth 2) (/ pcb-thickness 2)) 0] (cube dowel-width dowel-depth total-height))
      (translate [(- dowel-offset) (+ (/ dowel-depth 2) (/ pcb-thickness 2)) 0] (cube dowel-width dowel-depth total-height))

      ; lens cutout
      (translate [0 (- (+ (/ case-thickness 2) (/ pcb-thickness 2))) 0] (cube lens-width case-thickness total-height))

      ; cables cutout
      (translate [0 (+ (/ case-thickness 2) (/ pcb-thickness 2)) (+ (- (/ cables-height 2)) (/ total-height 2))] (cube cables-width case-thickness cables-height))

    )

    ; lens adapter
    (translate [(+ (/ lens-adapter-width 2) (/ lens-width 2)) (- (+ (/ (+ lens-thickness lens-adapter-thickness) 2) (/ pcb-thickness 2))) 0] (cube lens-adapter-width (+ lens-thickness lens-adapter-thickness) total-height))
    (translate [(- (+ (/ lens-adapter-width 2) (/ lens-width 2))) (- (+ (/ (+ lens-thickness lens-adapter-thickness) 2) (/ pcb-thickness 2))) 0] (cube lens-adapter-width (+ lens-thickness lens-adapter-thickness) total-height))
    (difference
      (translate [0 (- (+ (/ lens-adapter-thickness 2) (/ pcb-thickness 2) lens-thickness)) 0] (cube lens-width lens-adapter-thickness total-height))
      (translate [0 (- (+ (/ lens-adapter-thickness 2) (/ pcb-thickness 2) lens-thickness)) 0] (cube lens-clearance lens-adapter-thickness total-height))

    )

    ; M2 insert adapter
    (union 
      (translate [(- (/ sensor-holder-distance 2)) (+ (/ M2-insert-height 2) (/ pcb-thickness 2)) 0] (rotate-x (- (deg2rad 90)) (cylinder cover-insert-total-rad M2-insert-height)))
      (translate [(/ sensor-holder-distance 2) (+ (/ M2-insert-height 2) (/ pcb-thickness 2)) 0] (rotate-x (- (deg2rad 90)) (cylinder cover-insert-total-rad M2-insert-height)))
    )
  )
)

(def cable-holder 
  (difference
    (cube total-cable-holder-length total-cable-holder-width total-cable-holder-height)
    (translate [0 0 case-thickness] (cube cable-holder-length cable-holder-depth total-cable-holder-height))
  )
)

(def sensor-case (with-fn ROUND-RES (let [
                       M2-screw-length (+ lens-thickness total-thickness)
                       M2-screw-offset (+ (- (/ M2-screw-length 2)) (/ pcb-thickness 2) case-thickness)
                       M2-screw (rotate-x (- (deg2rad 90)) (cylinder M2-screw-rad M2-screw-length))
                      ] 
  (difference 
    (union
      base
      ;cable-holder
    )

    ; M2 screw holes
    (translate [(- (/ sensor-holder-distance 2)) M2-screw-offset 0] M2-screw)
    (translate [(/ sensor-holder-distance 2) M2-screw-offset 0] M2-screw)

    ; M2 insert holes (back)
      (translate [(- (/ sensor-holder-distance 2)) (+ (/ M2-insert-height 2) (/ pcb-thickness 2)) 0] (rotate-x (- (deg2rad 90)) (cylinder M2-insert-rad M2-insert-height)))
      (translate [(/ sensor-holder-distance 2) (+ (/ M2-insert-height 2) (/ pcb-thickness 2)) 0] (rotate-x (- (deg2rad 90)) (cylinder M2-insert-rad M2-insert-height)))

    ; hole to make room for M2 nut
      (translate [(- (/ sensor-holder-distance 2)) (- (+ (/ M2-insert-height 2) (/ pcb-thickness 2) lens-thickness)) 0] (rotate-x (- (deg2rad 90)) (cylinder M2-washer-rad M2-insert-height)))
      (translate [(/ sensor-holder-distance 2) (- (+ (/ M2-insert-height 2) (/ pcb-thickness 2) lens-thickness)) 0] (rotate-x (- (deg2rad 90)) (cylinder M2-washer-rad M2-insert-height)))
  ))))

(defn bottom-trim-align [shape] (translate (map + bottom-trim-origin [0 0 trim]) shape))

(def lens-adapter-protrude (- (+ lens-thickness lens-adapter-thickness) case-thickness))
(def M2-insert-protrude (- M2-insert-height case-thickness))

(def sensor-case-cutout-back-clearance 10)

(def sensor-case-cutout-height (+ total-thickness lens-adapter-protrude M2-insert-protrude sensor-case-cutout-back-clearance))

(def sensor-case-cutout (bottom-trim-align (translate [0 0 (- (/ sensor-case-cutout-height 2))] (cube total-length total-height sensor-case-cutout-height))))

(def sensor-case-aligned (bottom-trim-align (translate [0 0 (- (+ (/ total-thickness 2) lens-adapter-protrude))] (rotate-x (deg2rad (- 90)) sensor-case))))

;;;;;;;;;;;;;;;
;; Trackball ;;
;;;;;;;;;;;;;;;

(def dowells (union
              (rotated_dowell 0)
              (rotated_dowell 120)
              (rotated_dowell 240))
  )
(def vertical-hold 0) ; Millimeters of verticle hold after the curviture of the sphere ends to help hold the ball in

(def cup (
           difference
           (union
            (sphere (/ outer-width 2)) ; Main cup sphere
            (color BLU (translate [0, 0, (/ vertical-hold 2)] (cylinder (/ outer-width 2) vertical-hold))) ; add a little extra to hold ball in
           )
           (sphere (/ trackball-width-plus-bearing 2))
           (translate [0, 0, (+ (/ outer-width 2) vertical-hold)] (cylinder (/ outer-width 2) outer-width)) ; cut out the upper part of the main cup spher
           )
  )
(def bottom-trim ; trim the bottom off of the cup to get a lower profile
  (translate bottom-trim-origin (translate [0 0 (/ trim 2)] (cube outer-width outer-width trim)))
  )

(defn trackball-mount-rotate [thing] (rotate (deg2rad 0) [0 0 1]
                                     (rotate (deg2rad 0) [1 0 0]
                                     (rotate (deg2rad 0) [0 1 0]
                                     thing))))

(def sensor-holder-outer-rad (/ 7 2))

(def sensor-cable-out-clearance 23.5)
(def sensor-cable-below-clearance 2.00)

(def sensor-case-wall-thickness 2.00)
(def sensor-case-clearance 0.3)

(def buffer-dist 0.5)

(def trackswitch-cover-clearance 2)
(def trackswitch-total-radius (+ top-screw-insert-radius top-screw-insert-wall-thickness))
(def trackswitch-cover-mount-cut-gap 0.85)
(def trackswitch-cover-mount-cut-screw-clearance 10)
(def M2-head-rad (/ 4.00 2))
(def M2-head-depth 1.5)
(def M2-screw-length 12)
(def M2-short-screw-length 7)

(def trackswitch-cover-mount-cut-depth (+ trackswitch-cover-mount-cut-screw-clearance M2-head-depth))
(def trackswitch-cover-mount-cut (translate [0 0 (/ trackswitch-cover-mount-cut-depth 2)] (screw-insert-shape ROUND-RES 0 M2-washer-rad M2-washer-rad trackswitch-cover-mount-cut-depth)))
(def trackswitch-cover-screw-cut (translate [0 0 (- (/ M2-screw-length 2))] (screw-insert-shape ROUND-RES 0 M2-screw-rad M2-screw-rad M2-screw-length)))

(defn sensor-holder-arm-outer' [outer-height outer-radius] (union 
                                                             (translate [0 0 (- (/ outer-height 2))] (screw-insert-shape ROUND-RES 0 outer-radius outer-radius outer-height))
                                                             (translate [0 0 trackswitch-cover-mount-cut-gap] trackswitch-cover-mount-cut)
                                                             (translate [0 0 trackswitch-cover-mount-cut-gap] trackswitch-cover-screw-cut)
                                                             ))

(defn sensor-holder-arm-inner' [outer-height inner-height inner-radius] (translate [0 0 (- (/ inner-height 2) outer-height)] (screw-insert-shape ROUND-RES 0 inner-radius inner-radius inner-height)))

(def sensor-holder-height 2.0)

(def sensor-cover-insert-height (+ sensor-cable-out-clearance sensor-height))
(def sensor-cover-insert-top-extra-height 2.90)
(def sensor-cover-insert-bot-extra-height 6.15)
(def sensor-cover-insert-top-offset [(+ (/ sensor-width 2) trackswitch-total-radius buffer-dist) (- trackswitch-total-radius (/ sensor-length 3)) 0])
(def sensor-cover-insert-bot-offset [0 (+ (/ sensor-length 2) trackswitch-total-radius buffer-dist) 0])
(def sensor-cover-insert-screw-length 8)

(def sensor-holder-arm-outer (sensor-holder-arm-outer' sensor-holder-height sensor-holder-outer-rad))
(def sensor-holder-arm-inner (sensor-holder-arm-inner' sensor-holder-height sensor-screw-length M2-insert-rad))
(defn sensor-holder-cover-arm-outer [top] (sensor-holder-arm-outer' (+ sensor-cover-insert-height (if top sensor-cover-insert-top-extra-height sensor-cover-insert-bot-extra-height)) cover-insert-total-rad))
(defn sensor-holder-cover-arm-inner [top] (sensor-holder-arm-inner' (+ sensor-cover-insert-height (if top sensor-cover-insert-top-extra-height sensor-cover-insert-bot-extra-height)) sensor-cover-insert-screw-length  M2-insert-rad))

(defn sensor-holder-places [outer]
  (let [
         arm (if outer sensor-holder-arm-outer sensor-holder-arm-inner)
       ]
    (translate (map + bottom-trim-origin [0 0 (+ (/ trim 2))])
               (rotate (deg2rad 90) [0 0 1]
                 (union
                   (translate [0 (- (/ sensor-holder-distance 2)) 0] arm)
                   (->>
                    arm
                    (mirror [0 1 0])
                    (translate [0 (/ sensor-holder-distance 2) 0])
                   )
                 )
               )
    )
  )
)
 
(def sensor-holder-outer (sensor-holder-places true))
(def sensor-holder-inner (sensor-holder-places false))

(defn sensor-hole-angle [shape] (
                                  ->> shape
                                      (rotate (deg2rad sensor-z-rotate) [0 0 1])
                                      (rotate (deg2rad sensor-x-rotate) [1 0 0])
                                      (rotate (deg2rad sensor-y-rotate) [0 1 0])
                                      ))
(defn dowell-angle [shape] (
                             ->> shape
                                 ;(rotate (deg2rad (+ 45)) [0 0 1])
                                 ;(rotate (deg2rad -30) [0 1 0])
                                 ;(rotate (deg2rad 25) [1 0 0])
                                 ))

(def rotated-dowells
  (dowell-angle
   (translate [0 0 (- (/ holder-thickness' 2))] dowells)
   ))

(def rotated-bottom-trim     (sensor-hole-angle
                               bottom-trim))

(def sensor-cutout-height (* sensor-height 3))
(def sensor-cutout     (let [wall-thickness (+ sensor-case-wall-thickness sensor-case-clearance)] (sensor-hole-angle (translate (map + bottom-trim-origin [(/ wall-thickness 2) (- (/ wall-thickness 2)) (- (/ trim 2) (/ sensor-cutout-height 2))])
                               (cube (+ sensor-length wall-thickness) (+ lens-width wall-thickness) sensor-cutout-height)))))

(defn filler-rotate [p] (
                         ->> p
                             (trackball-mount-rotate)
                             ;                       (rotate (deg2rad 0) [0 1 0])
                             (rotate (deg2rad 20) [0 0 1])
                             (rotate (deg2rad 30) [0 1 0])
                         ))

(def trackswitch-offset-x-rot -3)

; align with the top dowell
(defn trackswitch-place [p] (->> p
                               (rotate (deg2rad 90) [1 0 0])
                               (translate [0 (/ outer-width 2) 0])
                               (rotate (deg2rad trackswitch-offset-x-rot) [1 0 0])
                               (rotate (deg2rad 90) [0 0 1])
                             ))

(def top-trackswitch-insert-height 4.6)
(def washer-thickness 0.5)
(def top-trackswitch-insert-screw-head-depth 2.35)
(def top-trackswitch-insert-screw-head-washer-depth (+ washer-thickness top-trackswitch-insert-screw-head-depth))
(def top-trackswitch-insert-washer-rad (/ 7.0 2))

(def top-trackswitch-insert-extra-buffer 0.95)

(def trackswitch-insert-inset 1.0)
(def trackswitch-insert-z-adj 0.30)

(def trackswitch-insert-extra-height 1.85)

(def top-trackswitch-insert-buffer (+ top-trackswitch-insert-extra-buffer trackswitch-insert-inset))
(def top-trackswitch-insert-thickness (+ (* trackswitch-total-radius 2) 0.2 top-trackswitch-insert-buffer))
(def top-trackswitch-insert (translate [0 (/ top-trackswitch-insert-thickness 2) (+ (/ top-trackswitch-insert-height 2))] (cube (+ mount-height (* top-trackswitch-insert-extra-buffer 2)) top-trackswitch-insert-thickness top-trackswitch-insert-height)))

(def trackswitch-cutout-height 20)

(def trackswitch-insert-height 6.0)

(def trackswitch-cover-insert-height 26.5)

(defn trackswitch-insert [radius height]
   (->> (screw-insert-shape ROUND-RES 0 radius radius height)
        (translate [0 (- (- (/ mount-width 2)) trackswitch-total-radius trackswitch-insert-inset) (/ height 2)])))

(defn trackswitch-cover-insert [radius height]
   (->> (screw-insert-shape ROUND-RES 0 radius radius height)
        (translate [0 (- (- (/ mount-width 2)) (+ (* 3 cover-insert-total-rad) top-trackswitch-insert-extra-buffer 0.2) trackswitch-insert-inset) (/ height 2)])))

(def trackswitch-insert-buff 0.1)

(def trackswitch-mount-top-offset (- (- 3.0) plate-thickness))

(def trackswitch-mount
  (difference
    (union
      (translate [0 0 (- plate-thickness)] (single-plate' false true))
      (translate [0 (- (+ (* trackswitch-total-radius 2) (/ mount-width 2) top-trackswitch-insert-buffer)) trackswitch-mount-top-offset] top-trackswitch-insert)
    )
    (union
      (->> (trackswitch-insert (+ top-screw-radius trackswitch-insert-buff) top-trackswitch-insert-height)
           (translate [(- (/ mount-height 2) trackswitch-total-radius) 0 (- (- 3.0) plate-thickness)])
      )
      (->> (trackswitch-insert (+ top-screw-radius trackswitch-insert-buff) top-trackswitch-insert-height)
           (translate [(- trackswitch-total-radius (/ mount-height 2)) 0 (- (- 3.0) plate-thickness)])
      )
   
      (->> (trackswitch-insert (+ top-trackswitch-insert-washer-rad trackswitch-insert-buff) top-trackswitch-insert-screw-head-washer-depth)
           (translate [(- (/ mount-height 2) trackswitch-total-radius) 0 (- (- 3.0) plate-thickness)])
      )
      (->> (trackswitch-insert (+ top-trackswitch-insert-washer-rad trackswitch-insert-buff) top-trackswitch-insert-screw-head-washer-depth)
           (translate [(- trackswitch-total-radius (/ mount-height 2)) 0 (- (- 3.0) plate-thickness)])
      )
  ))
)

(def trackswitch-mount-cutout
  (translate [0 0 (/ trackswitch-cutout-height 2)] (cube mount-height mount-height trackswitch-cutout-height))
)

; visualize trackball, dowells, mount
(def trackball-debug (debug (union 
                    (sensor-hole-angle sensor-case-aligned)
                    (trackswitch-place (union (debug trackswitch-mount) (debug trackswitch-mount-cutout)))
                    (sensor-hole-angle sensor-case-cutout)
                    rotated-dowells
                    (sphere (/ trackball-width-plus-bearing 2))
                    )
    )
)

(def trackball-mount
  (union 
    (difference
      (union 
        (trackswitch-place (union
          (->> (trackswitch-insert trackswitch-total-radius (+ trackswitch-insert-height trackswitch-insert-extra-height))
               (translate [(- (/ mount-height 2) trackswitch-total-radius) 0 (- trackswitch-insert-z-adj)])
          )
          (->> (trackswitch-insert trackswitch-total-radius (+ trackswitch-insert-height trackswitch-insert-extra-height))
               (translate [(- trackswitch-total-radius (/ mount-height 2)) 0 (- trackswitch-insert-z-adj)])
          )
        ))

        (trackball-mount-rotate cup)
        (filler-rotate cup)
      )

      ; Subtract out the bottom trim clearing a hole for the sensor
      rotated-bottom-trim
    )
  )
)

(def trackball-cutout
   (union
    (union
      (->> (trackswitch-insert top-screw-insert-radius trackswitch-insert-height)
           (translate [(- (/ mount-height 2) trackswitch-total-radius) 0 (- trackswitch-insert-z-adj)])
           trackswitch-place
      )
      (->> (trackswitch-insert top-screw-insert-radius trackswitch-insert-height)
           (translate [(- trackswitch-total-radius (/ mount-height 2)) 0 (- trackswitch-insert-z-adj)])
           trackswitch-place
      )
    )

    (trackswitch-place trackswitch-mount-cutout)

    ; subtract out room for the axels
    rotated-dowells
    (sensor-hole-angle sensor-holder-outer)
    (sphere (/ trackball-width-plus-bearing 2))
   )
  )

(defn trackball-rotate [shape]
  (->> shape
    (rotate (deg2rad trackball-y-rotate) [0 1 0])
    (rotate (deg2rad trackball-x-rotate) [1 0 0])
    (rotate (deg2rad trackball-z-rotate) [0 0 1])
    (thumb-place [0 0 0] trackball-thumb-offset))
)

;;;;;;;;;;;;
;; Models ;;
;;;;;;;;;;;;

(defn model-switch-plate-cutouts [mirror-internals]
  (shift-model (difference
    (union
      (key-places single-plate-blank)
      (debug (case-top-border mirror-internals))
      (if use_flex_pcb_holder flex-pcb-holders)
      (connectors mirror-internals)
      (thumb-layout mirror-internals single-plate-blank)
      (thumb-connectors mirror-internals)
    )
  ))
)

(def model-wrist-rest-right-holes
    (if adjustable-wrist-rest-holder-plate
        (difference wrist-rest-right
                    (translate [-10 -5 0] 
                               (screw-insert-wrist-rest-four screw-insert-radius
                                                             screw-insert-radius
                                                             999))
                    (translate [-11 39 (- (/ bottom-plate-thickness 2) 0.1)] wrist-shape)
                    (translate [ 11 39 (- (/ bottom-plate-thickness 2) 0.1)] wrist-shape)
        )
        wrist-rest-right
    )
)

(defn model-bottom-plate [left]
  (let [screw-cutouts         (translate [0 0 (/ bottom-plate-thickness -1.99)] 
                                         (screw-insert-holes-bottom-plate left))
        screw-cutouts-fillets (translate [0 0 (/ bottom-plate-thickness -1.99)] 
                                         (screw-insert-fillets-bottom-plate left))
        wrist-rest-adjust-holes-off [-10 -120 0] 
        wrist-rest-adjust-fillets (translate wrist-rest-adjust-holes-off
                                         (screw-insert-wrist-rest screw-insert-bottom-plate-bottom-radius
                                                                  screw-insert-bottom-plate-top-radius
                                                                  screw-insert-fillets-z))
        wrist-rest-adjust-holes (translate wrist-rest-adjust-holes-off
                                         (screw-insert-wrist-rest screw-insert-bottom-plate-top-radius
                                                                  screw-insert-bottom-plate-top-radius
                                                                  (+ bottom-plate-thickness 0.1)))
        bottom-plate-blank (extrude-linear {:height bottom-plate-thickness}
                               (union
                                   (difference
                                       (project 
                                          (extrude-linear {:height 0.01
                                                           :scale  0 ;scale 0 creates a filled plate from the case walls
                                                           :center true} 
                                              (case-walls-bottom left)
                                          )
                                       )
                                       (if recess-bottom-plate
                                           (case-walls-bottom-projection left)
                                       )
                                   )
                                   (project
                                       (if adjustable-wrist-rest-holder-plate 
                                                  (translate [10 -55 0] wrist-shape))
                                   )
                                   (project
                                       (if (and recess-bottom-plate (= controller-holder 1))
                                           (hull usb-holder-cutout)
                                       )
                                   )
                               )
                           )
       ]
    (difference ;(union 
                    bottom-plate-blank
                    ; (translate [8 -100 0] 
                    ;     (debug model-wrist-rest-right-holes)
                    ; )
                ;)
                screw-cutouts
                screw-cutouts-fillets
                (translate [0 0 (/ bottom-plate-thickness 2.01)] 
                    top-screw-insert-holes)
                (model-switch-plate-cutouts false)
                (if adjustable-wrist-rest-holder-plate
                    (union 
                      (translate [0 0 (* -1.01 (/ screw-insert-fillets-z 4))] 
                          wrist-rest-adjust-fillets)
                      wrist-rest-adjust-holes
                    )
                )
    )
  )
)

(defn usb-holder-shift [shape] (let [orig-position (shift-model-position (key-position firstcol firstrow [0 0 0])) 
                                    position (map + usb-holder-offset [(first orig-position) (second orig-position) 0])] 
  (translate position (rotate-z (deg2rad usb-holder-z-rotate) shape))))

(defn model-case-walls-right-base [mirror-internals]
    (union
      (when use_flex_pcb_holder flex-pcb-holders)
      (difference (union (case-walls mirror-internals)
                         (screw-insert-outers mirror-internals)
                         top-screw-block-outers
                         (when (= controller-holder 2) pcb-holder-screw-post)
                  )
                  (usb-holder-shift (if mirror-internals (mirror [0 0 0] (usb-holder-space (not mirror-internals))) (usb-holder-space (not mirror-internals))))
                  (when (not testing) (model-switch-plate-cutouts mirror-internals))
                  (screw-insert-holes mirror-internals)
                  top-screw-insert-holes
      )
      (when testing (debug (usb-holder-shift (if mirror-internals (mirror [0 0 0] (usb-holder (not mirror-internals))) (usb-holder (not mirror-internals))))))
    )
)

(defn model-case-walls-right [mirror-internals]
  (union
  (difference
    (union (model-case-walls-right-base mirror-internals))
    (when (not mirror-internals) (shift-model (trackball-rotate (union trackball-cutout sensor-cutout))))
    (when (not testing) (union
      (when recess-bottom-plate
        (union
            (translate [0 0 (- (+ 20 bottom-plate-thickness))] 
                       (cube 350 350 40))
            (translate [0 0 (- (/ bottom-plate-thickness 2))] 
                           (scale [1.01 1.01 1.15] (model-bottom-plate mirror-internals)))
        )
        (translate [0 0 -20] (cube 350 350 40))
      )
      (shift-model 
        (union
          caps-cutout
          (thumbcaps-cutout mirror-internals)
          (thumb-key-cutouts mirror-internals)
          (if (not (or use_hotswap_holder use_solderless)) 
              (union key-space-below
                    thumb-space-below))
          (if use_hotswap_holder (thumb-layout mirror-internals (hotswap-case-cutout mirror-internals)))
        ))
      )
  ))
    (when testing (debug (shift-model (trackball-rotate (union
                                                                   ;trackball-cutout
                                                                   sensor-cutout
                                                                  )))))
  )
)

(defn switch-plates-right [mirror-internals]
  (union (difference
    (union 
      (shift-model 
        (union 
          (difference 
            (union
              (case-top-border mirror-internals)
              (color CYA (connectors mirror-internals))
              (thumb-connectors mirror-internals)
              (when testing (union
                (debug caps-cutout)
                (debug (thumbcaps-cutout mirror-internals))
                (debug (upper-layout mirror-internals upper-behind-cutout))
              ))
            )
            (when testing (union
              caps-cutout
              (thumbcaps-cutout mirror-internals)
            ))
            (key-places single-plate-cutout)
            ; make extra room to fit in the hotswap in the tight space behind the 'v' key
            (key-place 1 3 (upper-behind-cutout' (- v-key-case-extend v-key-case-wall-thickness)))
            (upper-layout mirror-internals upper-behind-cutout)
            (thumb-layout mirror-internals single-plate-cutout)
            (when (and (not testing) (not mirror-internals)) (trackball-rotate sensor-cutout))
          )
          (when (not testing) (key-places (single-plate mirror-internals)))
          (when (not testing) (thumb-layout mirror-internals (single-plate mirror-internals)))
          (when (not mirror-internals) (trackball-rotate trackball-mount))
        )
      )
      (when top-screw-insert-top-plate-bumps top-screw-insert-outers)
    )
    (when (not mirror-internals) (shift-model (trackball-rotate trackball-cutout)))
    (when (not testing) (union
      (when top-screw-insert-top-plate-bumps (model-case-walls-right-base mirror-internals))
      (shift-model (union 
        caps-cutout
        (thumbcaps-cutout mirror-internals)
        ;(thumb-key-cutouts mirror-internals)
        (when use_hotswap_holder (thumb-layout mirror-internals (hotswap-case-cutout mirror-internals)))
        (when use_hotswap_holder (key-places (hotswap-case-cutout mirror-internals)))
    ))))
  ) (when testing (shift-model (trackball-rotate (debug trackball-debug)))))
)

(defn thumb-test [mirror-internals]
  (difference
    (union 
      (shift-model 
        (union 
          (difference 
            (union
              (thumb-wall mirror-internals true)
              (thumb-connectors mirror-internals)
              (when testing (union
                (debug (thumbcaps-cutout mirror-internals))
                (debug (thumb-upper-layout mirror-internals upper-behind-cutout))
              ))
            )
            (when testing (union
              (thumbcaps-cutout mirror-internals)
            ))
            (thumb-upper-layout mirror-internals upper-behind-cutout)
            (thumb-layout mirror-internals single-plate-cutout)
            (when (not mirror-internals) (trackball-rotate sensor-cutout))
          )
          (when (not testing) (thumb-layout mirror-internals (single-plate mirror-internals)))
          (when (not mirror-internals) (trackball-rotate trackball-mount))
        )
      )
    )
    (when (not mirror-internals) (shift-model (trackball-rotate trackball-cutout)))
    (when (not testing) (union
      (shift-model (union 
        (thumbcaps-cutout mirror-internals)
        ;(thumb-key-cutouts mirror-internals)
        (when use_hotswap_holder (thumb-layout mirror-internals (hotswap-case-cutout mirror-internals)))
    ))))
  )
)


(defn model-switch-plates-right [mirror-internals]
  (difference
    ; (union 
      (switch-plates-right mirror-internals)
    ; )
    top-screw-insert-holes
  )
  ; (debug top-screw))
  ; (debug top-screw))
  ; (debug top-screw))
  ; (debug top-screw))
  ; (debug top-screw))
)

(defn model-right [mirror-internals]
  (difference
    (union
      ;(key-places (single-plate mirror-internals))
      (if use_flex_pcb_holder flex-pcb-holders)
      connectors
      ;(thumb-layout (single-plate mirror-internals))
      thumb-connectors
      (union (difference (case-walls mirror-internals) (usb-holder-space (not mirror-internals)))
             ;screw-insert-holes) 
       (screw-insert-outers mirror-internals)))
    
    ;(if recess-bottom-plate
    ;    (union
    ;        (translate [0 0 (- (+ 20 bottom-plate-thickness))] 
    ;                   (cube 350 350 40))
    ;        (translate [0 0 (- (/ bottom-plate-thickness 2))] 
    ;                   (scale [1.005 1.005 1.15] model-bottom-plate))
    ;    )
    ;    (translate [0 0 -20] (cube 350 350 40))
    ;)
    ;
    ;caps-cutout
    ;thumbcaps-cutout
    ;(thumb-key-cutouts mirror-internals)
    ;(if (not (or use_hotswap_holder use_solderless)) 
    ;(if (not (or use_hotswap_holder use_solderless)) 
    ;(if (not (or use_hotswap_holder use_solderless)) 
    ;(if (not (or use_hotswap_holder use_solderless)) 
    ;    (union key-space-below
    ;           thumb-space-below))
    ;(if use_hotswap_holder (thumb-layout (hotswap-case-cutout mirror-internals)))
    ;(if use_hotswap_holder (key-places (hotswap-case-cutout mirror-internals)))
  ))

;;;;;;;;;;;;;
;; Outputs ;;
;;;;;;;;;;;;;

;(spit "things/single-plate.scad"
;      (write-scad (single-plate false)))
;
;; (spit "things/left-wall.scad"
;;       (write-scad (left-wall false)))
;
;; (spit "things/thumb-connectors.scad"
;;     (write-scad 
;;         (difference 
;;             (union (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) (single-plate false)))
;;                    (thumb-wall true)
;;                    thumb-connectors
;;                    (thumb-layout (single-plate false))
;;                    ; thumbcaps
;;             )
;;             (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) (hotswap-case-cutout false)))
;;             ; caps-cutout
;;             thumbcaps-cutout
;;             (thumb-key-cutouts false)
;;             (if (not (or use_hotswap_holder use_solderless))
;;                 thumb-space-below)
;;             (if use_hotswap_holder (thumb-layout (hotswap-case-cutout false)))
;;         )))
;

(defn cura-fix [shape] (union
  (translate [0 0 0.005] (cube 1 1 0.01)) ; make sure cura doesn't change the height when loaded and provide an alignment reference
  shape
))

(when (not testing)
  (spit "things/switch-plates-right.scad"
        (write-scad (cura-fix (model-switch-plates-right false))))
  (spit "things/upper-support-blockers-right.scad"
        (write-scad (cura-fix (shift-model (upper-layout false upper-support-blocker)))))
  (spit "things/case-walls-right.scad"
        (write-scad (model-case-walls-right false)))
  (spit "things/thumb-test-right.scad"
        (write-scad (cura-fix (thumb-test false))))
  (spit "things/upper-support-blockers-thumb-test-right.scad"
        (write-scad (cura-fix (shift-model (thumb-upper-layout false upper-support-blocker)))))

  (spit "things/switch-plates-left.scad"
        (write-scad (cura-fix (mirror [-1 0 0] (model-switch-plates-right true)))))
  (spit "things/upper-support-blockers-left.scad"
        (write-scad (cura-fix (mirror [-1 0 0] (shift-model (upper-layout true upper-support-blocker))))))
  (spit "things/case-walls-left.scad"
        (write-scad (mirror [-1 0 0] (model-case-walls-right true))))
  (spit "things/thumb-test-left.scad"
        (write-scad (cura-fix (mirror [-1 0 0] (thumb-test true)))))
  (spit "things/upper-support-blockers-thumb-test-left.scad"
        (write-scad (cura-fix (mirror [-1 0 0] (shift-model (thumb-upper-layout true upper-support-blocker))))))

  (spit "things/ardumicro-holder.scad"
        (write-scad (usb-holder false)))
  (spit "things/promicro-holder.scad"
        (write-scad (usb-holder true)))

  (spit "things/bottom-plate-right.scad"
        (write-scad (model-bottom-plate false)))
  (spit "things/wrist-rest-right-holes.scad"
        (write-scad model-wrist-rest-right-holes))

  (spit "things/bottom-plate-left.scad"
        (write-scad (mirror [-1 0 0] (model-bottom-plate true))))
  (spit "things/wrist-rest-left-holes.scad"
        (write-scad (mirror [-1 0 0] model-wrist-rest-right-holes)))

  (spit "things/sensor-case.scad"
        (write-scad sensor-case))
)

;; (spit "things/switch-plate-cutouts.scad"
;;       (write-scad (model-switch-plate-cutouts false)))
;
;
;(spit "things/right.scad"
;      (write-scad (model-right false)))
;(spit "things/left.scad"
;      (write-scad (mirror [-1 0 0] (model-right true))))
;
;(spit "things/bottom-plate-right.scad"
;      (write-scad model-bottom-plate))
;(spit "things/bottom-plate-left.scad"
;      (write-scad (mirror [-1 0 0] model-bottom-plate)))
;
;; (spit "things/wrist-rest-right-base.scad"
;;       (write-scad wrist-rest-right-base))
;(spit "things/wrist-rest-right-holes.scad"
;  (if adjustable-wrist-rest-holder-plate
;    (write-scad model-wrist-rest-right-holes)
;    (write-scad wrist-rest-right)
;  )
;)

;(spit "things/trackswitch-mount.scad"
;      (write-scad
;        trackswitch-mount))

(defn id [shape] shape)

(when testing (spit "things/test.scad"
      (write-scad
            ;PRO TIP, commend out everything but caps & thumbcaps to play with geometry of keyboard, it's MUCH faster
            ;(debug
            ;;(color BLU
              ;usb-holder
              ;model-bottom-plate
            ;)
            (union ;(union (model-case-walls-right false))
                   ;(union (sa-cap-cutout 1) (debug (single-plate true)))
                   ;(shift-model (key-place 1 3 (single-plate-extra-cutout' (+ web-thickness (- v-key-case-extend v-key-case-wall-thickness)))))
                   ;sensor-case-aligned
                   ;(debug sensor-case-cutout)

                   ;trackswitch-mount
                   ;(debug trackswitch-mount-cutout)
                   ;trackball-mount

                   ;wrist-shape
                   ;(model-bottom-plate true)
                   (model-switch-plates-right false)
                   ;(hotswap-case-cutout false)
                   ;(shift-model (thumb-upper-layout true upper-support-blocker))
                   ;(debug (thumb-test true))
                   ;caps-cutout
                   ;(thumbcaps-cutout true)
                   ;(thumbcaps-cutout true)
                   ;(difference
                   ;  (union (single-plate true) (upper-key-case id))
                   ;  (union single-plate-cutout)
                   ;  (union upper-behind-cutout)
                   ;)
                   ;(union (sa-cap-cutout 1))

                   ;(union short-post-bl-lower)
                   ;(union short-post-back-bl-lower)
                   ;(debug upper-fat-web-post-bl-lower)
                   ;(debug upper-fat-web-post-tl-lower)
                   ;(debug upper-fat-web-post-br-lower)
                   ;(debug upper-fat-web-post-tr-lower)
                   ;(union (translate [0 0 (+ upper-post-offset)] upper-fat-web-post-tl-lower))

                   ;(debug (union upper-behind-cutout))
                   ;(union (union upper-support-blocker))
                   ;(translate [0 0 0] short-post-back-br)
                   ;(translate [0 0 (- v-key-case-extend)] short-post-back-br)
                   ;(union short-post-bl)
                   ;(union short-post-back-bl)
                   ;(debug (single-plate false))
                   ;(key-places (single-plate false))
                   ;(thumbcaps-cutout true)

                   ;(single-plate true)
                   ;fat-web-post-br
                   ;(color BLA single-plate-extra-cutout)
                   ;(color GRE upper-fat-web-post-bl-lower)
                   ;(color GRE upper-fat-web-post-br-lower)
                   ;(color GRE upper-fat-web-post-tl-lower)
                   ;(color GRE upper-fat-web-post-tr-lower)
                   ;(color WHI upper-behind-cutout)
                   ;(debug (single-plate false))
            )
			;(union usb-holder usb-holder-cutout usb-holder-space)
			;(union (usb-holder true))
            ;(model-switch-plates-right true)
            ; (union plate-post-br short-post-bl)
            ; (debug (single-plate false))

            ;(model-right false)
			;(translate [0 0 (- plate-thickness)] (single-plate false))
			;(color YEL (shift-model (translate thumborigin (cylinder 10 30))))
            ;(sa-cap-cutout 1)
            ;(union
            ;  (single-plate false)
            ;  upper-fat-web-post-tl
            ;  upper-fat-web-post-tr
            ;  ;upper-fat-web-post-bl
            ;  upper-fat-web-post-bl-lower
            ;  ;upper-fat-web-post-br
            ;  upper-fat-web-post-br-lower
            ;)
            ;(key-upper-place (single-plate false))
			;(translate [0 0 0] (thumb-test false))
            ;sensor-case
			;trackswitch-mount
            ;(union
            ;  top-screw-insert-holes
            ;  (debug top-screw-insert-outers)
            ;  (debug top-screw-block-outers)
            ;)
            ; (color ORA (model-exo-plates-right false))

            ; (debug top-screw)
            ;caps
            ; (debug caps-cutout)
            ;thumbcaps
            ; (debug (import "../things/v4caps.stl"))
            ; (debug thumbcaps-cutout)
            ; (debug key-space-below)
            ; (debug thumb-space-below)
            ; (if use_hotswap_holder(debug (thumb-space-hotswap false)))
            ; (debug top-screw-block-outers)

            ;(debug pcb-holder)
            ; (debug pcb-holder-space)

            ;(debug usb-holder)
            ; (debug usb-holder-cutout)

            ; (translate [0 0 (- (/ bottom-plate-thickness 2))]
                ; (debug model-bottom-plate)
                ; (translate [8 -100 (- (/ bottom-plate-thickness 2))] 
                    ; (color BRO model-wrist-rest-right-holes)
                ; 
            ; )
      )))
