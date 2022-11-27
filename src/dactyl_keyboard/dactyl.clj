(ns dactyl-keyboard.dactyl
  (:refer-clojure :exclude [use import])
  (:require [clojure.core.matrix :refer [array matrix mmul]]
            [clojure.string :as str]
            [scad-clj.scad :refer :all]
            [scad-clj.model :refer :all]))

(defn deg2rad [degrees]
  (* (/ degrees 180) pi))

(defn rotate-x [angle shape]
  (rotate angle [1 0 0] shape))

(defn rotate-y [angle shape]
  (rotate angle [0 1 0] shape))

(defn rotate-z [angle shape]
  (rotate angle [0 0 1] shape))

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
(def thumb-u false)               ; use upper thumb key

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
                  (= column 2)  [0   -0.65 -2.1  ] ;;middle
                  (= column 3)  [0   -3.65 2.4] ;;ring
                  (= column 4)  [0 -16  6  ] ;;pinky
                  (>= column 5) [0 -16  6  ] ;;pinky outer
                  :else [0 0 0]))

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

(def thumb-pos [-11 -2 4.5] )
(def thumb-rot [0 10 0] )

;;;;;;;;;;;;;;;;;;;;;;;
;; General variables ;;
;;;;;;;;;;;;;;;;;;;;;;;

(def firstrow 1)
(def firstcol 0)

(def lastrow (dec nrows))
(def real-lastrow (if bottom-row (dec nrows) (- nrows 2)))
(def cornerrow (dec lastrow))
(def lastcol (dec ncols))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Trackball variables ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def dowel-depth-in-shell 1)
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
(def dowell (union (cylinder (- (/ dowell-width 2) dowel-top-change) (+ dowell-height dowel-top-height) :fn 50) (cylinder (/ dowell-width 2) dowell-height :fn 50)))
(def bearing (cylinder (/ 8.5 2) 3)) ; Bearing is actually 6mm x 2.5mm, model it as 8.5mm x 3 to give it room to spin
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
(def bottom-trim-origin [0 0 (- (- (/ outer-width 2) (/ trim 2)))])

(def mount-x-rotate -42)
(def mount-y-rotate -30)
(def mount-z-rotate 0)

(def sensor-x-rotate 0)
(def sensor-y-rotate 0)
(def sensor-z-rotate 0)

(def mount-length 18)
(def mount-width 12)
(def mount-offset 3)

(def sensor-length 28)
(def sensor-width 21.5)
(def sensor-holder-width (/ sensor-width 2))
(def sensor-height 7.3)

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
        teeth-z-down   1.65
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
(def hotswap-holder
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
        pin-hole            (->> (cylinder (/ 3.3 2) 10)
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

(defn single-plate [mirror-internals]
 ; (render ;tell scad to try and cache this repetitive code, kinda screws up previews
  (let [top-wall (->> (cube mount-height 1.5 plate-thickness)
                      (translate [0
                                  (+ (/ 1.5 2) (/ keyswitch-height 2))
                                  (/ plate-thickness 2)]))
        left-wall (->> (cube 1.5 mount-width plate-thickness)
                       (translate [(+ (/ 1.5 2) (/ keyswitch-width 2))
                                   0
                                   (/ plate-thickness 2)]))
        plate-half (difference (union top-wall left-wall) 
                               switch-teeth-cutout
                               switch-dogbone-cutout)
        plate (union plate-half
                  (->> plate-half
                       (mirror [1 0 0])
                       (mirror [0 1 0]))
                  (if use_hotswap_holder hotswap-holder)
                  (if use_solderless solderless-plate)
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

(def sa-length 18.25)
(def sa-height 12.5)

(def sa-key-height-from-plate 7.39)
(def sa-cap-bottom-height (+ sa-key-height-from-plate plate-thickness))
(def sa-cap-bottom-height-pressed (+ 3 plate-thickness))

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
           cutout-y 1.95
           cutout-z-offset (- sa-cap-bottom-height-pressed 2.99)
           bl2 (case keysize 
                     1   (+ (/ sa-length 2) cutout-y)
                     1.5 (+ (/ sa-length 2) cutout-y)
                     2   (+ sa-length cutout-y))
           bw2 (case keysize
                     1   (+ (/ sa-length 2) cutout-x)
                     1.5 (+ (/ 27.94 2) cutout-x)
                     2   (+ (/ sa-length 2) cutout-x))
           keycap-cutout-xy (polygon [[bw2 bl2] [bw2 (- bl2)] [(- bw2) (- bl2)] [(- bw2) bl2]])
           key-cap-cutout (hull (->> keycap-cutout-xy
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 0.05]))
                                (->> keycap-cutout-xy
                                     (extrude-linear {:height 0.1 :twist 0 :convexity 0})
                                     (translate [0 0 sa-height])))
         ]
         (->> key-cap-cutout
              (translate [0 0 cutout-z-offset]))
    )
)

;;;;;;;;;;;;;;;;;;;;;;;;;
;; Placement Functions ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def columns (range firstcol (inc lastcol)))
(def columns' (range firstcol lastcol))
(def rows (range firstrow (inc lastrow)))

(defn apply-key-geometry [translate-fn rotate-x-fn rotate-y-fn column row shape]
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

(defn key-place [column row shape]
  (apply-key-geometry translate
                      rotate-x
                      rotate-y
                      column row shape))

(defn shift-model [model] (->> model
   (rotate tenting-angle [0 1 0])
   (translate [0 0 keyboard-z-offset])
))

(defn shift-model-position [position]
  (map + (rotate-around-y tenting-angle position) [0 0 keyboard-z-offset]))

(defn key-place-shifted [column row shape] (shift-model (key-place column row shape)))

(defn key-position [column row position]
  (apply-key-geometry (partial map +) rotate-around-x rotate-around-y column row position))

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
               :when (and (or (not track-ball) (not (and (= row real-lastrow) (= column firstcol))))
                       (or (and bottom-row (.contains [2 3] column))
                         (not= row lastrow))
                       (not (and (= column lastcol) (.contains [1 3] row)))
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

(def post-adj (/ post-size 2))
(def web-post-tr (translate [(- (/ mount-width  2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
(def web-post-tm (translate [                             0  (- (/ mount-height  2) post-adj) 0] web-post))
(def web-post-tl (translate [(+ (/ mount-width -2) post-adj) (- (/ mount-height  2) post-adj) 0] web-post))
(def web-post-bl (translate [(+ (/ mount-width -2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-bm (translate [                             0  (+ (/ mount-height -2) post-adj) 0] web-post))
(def web-post-br (translate [(- (/ mount-width  2) post-adj) (+ (/ mount-height -2) post-adj) 0] web-post))


; plate posts for connecting columns together without wasting material
; or blocking sides of hotswap sockets
(def plate-post-size 1.2)
(def plate-post-thickness (- web-thickness 2))
(def plate-post (->> (cube plate-post-size plate-post-size plate-post-thickness)
                   (translate [0 0 (+ plate-post-thickness (/ plate-post-thickness -1.5)
                                      )])))
(def plate-post-adj (/ plate-post-size 2))
(def plate-post-tr (translate [(- (/ mount-width  2) plate-post-adj) (- (/ mount-height  2) plate-post-adj) 0] plate-post))
(def plate-post-tm (translate [                                   0  (- (/ mount-height  2) plate-post-adj) 0] plate-post))
(def plate-post-tl (translate [(+ (/ mount-width -2) plate-post-adj) (- (/ mount-height  2) plate-post-adj) 0] plate-post))
(def plate-post-bl (translate [(+ (/ mount-width -2) plate-post-adj) (+ (/ mount-height -2) plate-post-adj) 0] plate-post))
(def plate-post-bm (translate [                                   0  (+ (/ mount-height -2) plate-post-adj) 0] plate-post))
(def plate-post-br (translate [(- (/ mount-width  2) plate-post-adj) (+ (/ mount-height -2) plate-post-adj) 0] plate-post))

; fat web post for very steep angles between thumb and finger clusters
; this ensures the walls stay somewhat thicker
(def fat-post-size 1.2)
(def fat-web-post (->> (cube fat-post-size fat-post-size web-thickness)
                       (translate [0 0 (+ (/ web-thickness -2)
                                          plate-thickness)])))

(def fat-post-adj (/ fat-post-size 2))
(def fat-web-post-tr (translate [(- (/ mount-width  2) fat-post-adj) (- (/ mount-height  2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-tm (translate [                                 0  (- (/ mount-height  2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-tl (translate [(+ (/ mount-width -2) fat-post-adj) (- (/ mount-height  2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-bl (translate [(+ (/ mount-width -2) fat-post-adj) (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-bm (translate [                                 0  (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
(def fat-web-post-br (translate [(- (/ mount-width  2) fat-post-adj) (+ (/ mount-height -2) fat-post-adj) 0] fat-web-post))
; wide posts for 1.5u keys in the main cluster


(def trackball-post (->> (cube fat-post-size fat-post-size sensor-height)
                       (translate [0 0 (+ (/ web-thickness -2)
                                          plate-thickness)])))

(def trackball-post-tr (translate [(- (/ mount-length  2) fat-post-adj) (- (/ mount-width  2) fat-post-adj) 0] trackball-post))
(def trackball-post-br (translate [(- (/ mount-length  2) fat-post-adj) (+ (/ mount-width -2) fat-post-adj) 0] trackball-post))
(def trackball-post-bl (translate [(+ (/ mount-length -2) fat-post-adj) (+ (/ mount-width -2) fat-post-adj) 0] trackball-post))
(def trackball-post-tl (translate [(+ (/ mount-length -2) fat-post-adj) (- (/ mount-width  2) fat-post-adj) 0] trackball-post))

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
  (map + (key-position 1 cornerrow [(/ mount-width 2) 
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
       (move-fn thumborigin)
       (move-fn move))

       (rot-x-fn (deg2rad (nth thumb-rot 0)))
       (rot-y-fn (deg2rad (nth thumb-rot 1)))
       (rot-z-fn (deg2rad (nth thumb-rot 2)))
     ))

(defn thumb-place [rot move shape]
  (apply-thumb-geometry rotate-x rotate-y rotate-z translate rot move shape))

(defn thumb-position [rot move position]
  (apply-thumb-geometry rotate-around-x rotate-around-y rotate-around-z (partial map +) rot move position))

(defn thumb-place-shifted [rot move shape] (shift-model (thumb-place rot move shape)))

(def trackball-x-rotate 60)
(def trackball-y-rotate 0)
(def trackball-z-rotate 30)
(def trackball-thumb-offset [-45 5 12])

(defn apply-trackball-geometry [translate-fn rotate-x-fn rotate-y-fn rotate-z-fn shape]
  (->> shape
    (translate-fn bottom-trim-origin)
    (translate-fn [0 0 (- mount-offset)])
    (rotate-z-fn (deg2rad mount-z-rotate))
    (rotate-x-fn (deg2rad mount-x-rotate))
    (rotate-y-fn (deg2rad mount-y-rotate))
    (rotate-x-fn (deg2rad trackball-x-rotate))
    (rotate-y-fn (deg2rad trackball-y-rotate))
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

(defn trackball-pos [row col] (and track-ball (and (= row real-lastrow) (= col firstcol))))

(def connectors
  (union
           ;; Row connections
           (for [column (range firstcol lastcol)
                 row (range firstrow lastrow)]
             (let [
                   tpost-tr (if (trackball-pos row column) 
                       (trackball-place trackball-post-tr)
                       (key-place column  row plate-post-tr)
                     )
                   tpost-br (if (trackball-pos row column) 
                       (trackball-place trackball-post-br)
                       (key-place column  row plate-post-br)
                     )
                  ]
             (when (or (not= column (dec lastcol)) (= 2 row))
             (if use_hotswap_holder
               (triangle-hulls
                 (key-place (inc column) row plate-post-tl)
                 tpost-tr
                 (key-place (inc column) row plate-post-bl)
                 tpost-br)
               (triangle-hulls
                 (key-place (inc column) row fat-web-post-tl)
                 tpost-tr
                 (key-place (inc column) row fat-web-post-bl)
                 tpost-br)
              )))
           )

           ;; Column connections
           (for [column columns'
                 row (range firstrow cornerrow)]
             (let [
                   tpost-tl (if (trackball-pos (inc row) column) 
                       (trackball-place trackball-post-tl)
                       (key-place column  (inc row) plate-post-tl)
                     )
                   tpost-tr (if (trackball-pos (inc row) column) 
                       (trackball-place trackball-post-tr)
                       (key-place column  (inc row) plate-post-tr)
                     )
                  ]
             (triangle-hulls
               (key-place column      row  web-post-bl)
               (key-place column      row  web-post-br)
               tpost-tl
               tpost-tr)))

           ;; Diagonal connections
           (for [column (range firstcol (dec lastcol))
                 row (range firstrow cornerrow)]
             (if use_hotswap_holder
               (if (or (not track-ball) (not (and (= row (dec real-lastrow)) (= column firstcol))))
                (triangle-hulls
                 (key-place      column       row  plate-post-br)
                 (key-place      column  (inc row) plate-post-tr)
                 (key-place (inc column)      row  plate-post-bl)
                 (key-place (inc column) (inc row) plate-post-tl))
                (triangle-hulls
                 (key-place      column       row  plate-post-br)
                 (trackball-place trackball-post-tr)
                 (key-place (inc column)      row  plate-post-bl)
                 (key-place (inc column) (inc row) plate-post-tl))
                )
               (when (or (not track-ball) (not (and (= row (dec real-lastrow)) (= column firstcol))))
               (triangle-hulls
                 (key-place      column       row  fat-web-post-br)
                 (key-place      column  (inc row) fat-web-post-tr)
                 (key-place (inc column)      row  fat-web-post-bl)
                 (key-place (inc column) (inc row) fat-web-post-tl)))
             )
           )
           
           ; top two to the main keyboard, starting on the left
           (when bottom-row
             (->> (if use_hotswap_holder
                  (triangle-hulls
                    (key-place 2 lastrow   plate-post-br)
                    (key-place 3 lastrow   plate-post-bl)
                    (key-place 2 lastrow   plate-post-tr)
                    (key-place 3 lastrow   plate-post-tl)
                    (key-place 3 cornerrow plate-post-bl)
                    (key-place 3 lastrow   fat-web-post-tl)
                    (key-place 3 cornerrow fat-web-post-bl)
                    (key-place 3 lastrow   fat-web-post-tr)
                    (key-place 3 cornerrow fat-web-post-br)
                    (key-place 3 lastrow   plate-post-tr)
                    (key-place 3 cornerrow plate-post-br)
                    (key-place 4 cornerrow plate-post-bl))
                  (triangle-hulls
                    (key-place 2 lastrow   fat-web-post-br)
                    (key-place 3 lastrow   fat-web-post-bl)
                    (key-place 2 lastrow   fat-web-post-tr)
                    (key-place 3 lastrow   fat-web-post-tl)
                    (key-place 3 cornerrow fat-web-post-bl)
                    (key-place 3 lastrow   fat-web-post-tr)
                    (key-place 3 cornerrow fat-web-post-br)
                    (key-place 4 cornerrow fat-web-post-bl)))
                (color BLA))
             (->> (if use_hotswap_holder
                  (triangle-hulls
                    (key-place 1 cornerrow plate-post-br)
                    (key-place 2 lastrow   plate-post-tl)
                    (key-place 2 cornerrow plate-post-bl)
                    (key-place 2 lastrow   fat-web-post-tl)
                    (key-place 2 cornerrow fat-web-post-bl)
                    (key-place 2 lastrow   fat-web-post-tr)
                    (key-place 2 cornerrow fat-web-post-br)
                    (key-place 2 lastrow   plate-post-tr)
                    (key-place 2 cornerrow plate-post-br)
                    (key-place 3 cornerrow plate-post-bl))
                  (triangle-hulls
                    (key-place 1 cornerrow fat-web-post-br)
                    (key-place 2 lastrow   fat-web-post-tl)
                    (key-place 2 cornerrow fat-web-post-bl)
                    (key-place 2 lastrow   fat-web-post-tr)
                    (key-place 2 cornerrow fat-web-post-br)
                    (key-place 3 cornerrow fat-web-post-bl)))
                (color GRE))

             (->> (if use_hotswap_holder
                  (triangle-hulls
                    (key-place 3 lastrow   plate-post-tr)
                    (key-place 3 lastrow   plate-post-br)
                    (key-place 3 lastrow   plate-post-tr)
                    (key-place 4 cornerrow plate-post-bl))
                  (triangle-hulls
                    (key-place 3 lastrow   fat-web-post-tr)
                    (key-place 3 lastrow   fat-web-post-br)
                    (key-place 3 lastrow   fat-web-post-tr)
                    (key-place 4 cornerrow fat-web-post-bl)))
                (color CYA))

             (->> (if use_hotswap_holder
                  (triangle-hulls
                    (key-place 1 cornerrow plate-post-br)
                    (key-place 2 lastrow   plate-post-tl)
                    (key-place 2 lastrow   plate-post-bl))
                     
                   (triangle-hulls
                    (key-place 1 cornerrow fat-web-post-br)
                    (key-place 2 lastrow   fat-web-post-tl)
                    (key-place 2 lastrow   fat-web-post-bl)))
                (color MAG))
           )
  )
)


; convexer
(defn thumb-u-place [shape] (thumb-place [22 -6.5 23] [-48 -2.5 -9] shape)) ; upper
(defn thumb-r-place [shape] (thumb-place [14 -35   10] [-4.5 -10 5] shape)) ; right
(defn thumb-m-place [shape] (thumb-place [ 8 -21.5 20] [-23 -15.2 -6] shape)) ; middle
(defn thumb-l-place [shape] (thumb-place [ 6  -5   25] [-43 -23.5 -11.5] shape)) ; left

; convexer
(defn thumb-u-place' [border shape] ((if border thumb-place thumb-place-shifted) [22 -6.5 23] [-48 -2.5 -9] shape)) ; upper
(defn thumb-r-place' [border shape] ((if border thumb-place thumb-place-shifted) [14 -35   10] [-4.5 -10 5] shape)) ; right
(defn thumb-m-place' [border shape] ((if border thumb-place thumb-place-shifted) [ 8 -21.5 20] [-23 -15.2 -6] shape)) ; middle
(defn thumb-l-place' [border shape] ((if border thumb-place thumb-place-shifted) [ 6  -5   25] [-43 -23.5 -11.5] shape)) ; left

(defn thumb-layout [shape]
  (union
    (thumb-r-place shape)
    (when (not track-ball) (thumb-u-place shape))
    (thumb-m-place shape)
    (thumb-l-place shape)))

(def thumbcaps (thumb-layout 
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
(def thumbcaps-cutout (thumb-layout (rotate (deg2rad -90) [0 0 1] (sa-cap-cutout 1))))
(def thumb-space-below (thumb-layout switch-bottom))
(defn thumb-key-cutouts [mirror-internals] 
    (thumb-layout (single-plate-cut mirror-internals)))

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

(defn wall-locate0 [dx dy border] [(* dx (if border wall-border-thickness wall-thickness))
                                   (* dy (if border wall-border-thickness wall-thickness))
                                   0])
(defn wall-locate1 [dx dy border] [(* dx (if border wall-border-thickness wall-thickness))
                                   (* dy (if border wall-border-thickness wall-thickness))
                                   0])
(defn wall-locate2 [dx dy border] [(* dx (if border wall-border-xy-offset wall-xy-offset))
                                   (* dy (if border wall-border-xy-offset wall-xy-offset))
                                   (if border wall-border-z-offset wall-z-offset)])
(defn wall-locate3 [dx dy border] [(* dx (+ (if border wall-border-xy-offset wall-xy-offset) (if border wall-border-thickness wall-thickness))) 
                                   (* dy (+ (if border wall-border-xy-offset wall-xy-offset) (if border wall-border-thickness wall-thickness))) 
                                   (* 2 (if border wall-border-z-offset wall-z-offset))])

(def thumb-connectors
  (union
    (when thumb-u (
      ; top one
      (->> (triangle-hulls
               (thumb-u-place plate-post-br)
               (thumb-u-place web-post-tr)
               (thumb-m-place plate-post-tl)
           ) (color GRE))

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

      (->> (triangle-hulls
               (thumb-u-place plate-post-br)
               (thumb-u-place web-post-tr)
               (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) web-post-bl))
           ) (color YEL))

      (->> (triangle-hulls
               (thumb-u-place fat-web-post-tr)
               (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) fat-web-post-tl))
               (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) fat-web-post-bl))
           ) (color BLA))


      (->> (triangle-hulls
               (thumb-m-place plate-post-tl)
               (thumb-l-place web-post-tr)
               (thumb-u-place web-post-br)
           ) (color DGR))

      (->> (triangle-hulls
               (thumb-u-place web-post-bl)
               (thumb-l-place web-post-tr)
               (thumb-l-place web-post-tl)
           )
           (color BLU))

      (->> (triangle-hulls
              (thumb-u-place web-post-bl)
              (thumb-u-place web-post-br)
              (thumb-l-place web-post-tr)
          )
           (color NBL))

      (->> (if use_hotswap_holder 
             (triangle-hulls
               (thumb-u-place plate-post-tl)
               (thumb-l-place plate-post-tl)
               (thumb-u-place plate-post-bl)
             )
             (triangle-hulls
               (thumb-u-place web-post-tl)
               (thumb-l-place web-post-tl)
               (thumb-u-place web-post-bl)
             )
           ) (color RED))))

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

    (->> (if use_hotswap_holder 
             (triangle-hulls
               (thumb-m-place plate-post-tl)
               (thumb-l-place plate-post-tr)
               (thumb-m-place plate-post-bl)
               (thumb-l-place plate-post-br)
               (thumb-m-place plate-post-bl))
             (triangle-hulls
               (thumb-m-place web-post-tl)
               (thumb-l-place web-post-tr)
               (thumb-m-place web-post-bl)
               (thumb-l-place web-post-br)
               (thumb-m-place web-post-bl))
           ) (color ORA))

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
      (thumb-r-place fat-web-post-tl)
      (thumb-r-place fat-web-post-tr)
      (key-place 1 cornerrow web-post-br)
      (when (not bottom-row) (key-place 3 real-lastrow web-post-bl))
      ) (color NBL))
    (when (not bottom-row)
      (->> (hull
        (key-place 1 cornerrow web-post-br)
        (key-place 3 real-lastrow web-post-bl)
        (key-place 2 real-lastrow web-post-br)
        (key-place 2 real-lastrow web-post-bl)
        ) (color NBL)))
    (when (not bottom-row)
      (->> (hull
        (thumb-m-place web-post-tr)
        (thumb-m-place web-post-tl)
        (key-place 1 real-lastrow web-post-bl)
        (trackball-place trackball-post-bl)
        (trackball-place trackball-post-br)
        (thumb-l-place web-post-tr)
        ) (color NBL)))
    (when (not bottom-row)
      (->> (hull
        (thumb-m-place web-post-tr)
        (thumb-m-place web-post-tl)
        (key-place 1 real-lastrow web-post-bl)
        (key-place 1 real-lastrow web-post-br)
        (thumb-r-place web-post-tl)
        ) (color NBL)))
    ;(->> (triangle-hulls
    ;  (key-place 2 lastrow web-post-tl)
    ;  ; (thumb-r-place fat-web-post-tr)
    ;  ; (key-place 2 lastrow web-post-bl)
    ;  (thumb-r-place fat-web-post-br)) (color PUR))
    (when bottom-row (->> (triangle-hulls
      (thumb-r-place web-post-br)
      (key-place 2 lastrow web-post-bl)
      (if use_hotswap_holder (key-place 3 lastrow plate-post-bl)
                      (key-place 3 lastrow web-post-bl))
      (key-place 2 lastrow web-post-br)) (color PIN)))
  ))

; dx1, dy1, dx2, dy2 = direction of the wall. '1' for front, '-1' for back, '0' for 'not in this direction'.
; place1, place2 = function that places an object at a location, typically refers to the center of a key position.
; post1, post2 = the shape that should be rendered
(defn wall-brace [place1 dx1 dy1 post1 
                  place2 dx2 dy2 post2
                  border]
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
      (place1 post1)
      (place1 (translate (wall-locate1 dx1 dy1 border) post1))
      ; (place1 (translate (wall-locate2 dx1 dy1 border) post1))
      (place1 (translate (wall-locate2 dx1 dy1 border) post1))
      (place2 post2)
      (place2 (translate (wall-locate1 dx2 dy2 border) post2))
      ; (place2 (translate (wall-locate2 dx2 dy2 border) post2))
      (place2 (translate (wall-locate2 dx2 dy2 border) post2))
      )
    (color YEL))
    (when (not border)
      (->> (bottom-hull
        (place1 (translate (wall-locate2 dx1 dy1 border) post1))
        ; (place1 (translate (wall-locate2 dx1 dy1 border) post1))
        (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        ; (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        )
        (color ORA))
    )
  ))

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
          (place1 (translate (wall-locate3 dx1 dy1 border) post1))
          (place1 (translate (wall-locate3 dx1 dy1 border) post1))
  
        (place2 (translate (wall-locate2 dx2 dy2 border) post2))
        (place2 (translate (wall-locate2 dx2 dy2 border) post2))
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

(defn right-wall [border]
  (union 
    (key-corner (dec lastcol) firstrow :tr border)
    (for [y (range firstrow 2)] (key-wall-brace-less (dec lastcol)      y  1 0 web-post-tr (dec lastcol) y 1 0 web-post-br border))
    (for [y (range 3 lastrow)] (key-wall-brace-less (dec lastcol)      y  1 0 web-post-tr (dec lastcol) y 1 0 web-post-br border))
    (key-wall-brace-less (dec lastcol) 1 1 0 web-post-br (dec lastcol) 2 1 1 web-post-tr border)
    (key-wall-brace-less (dec lastcol) 2 1 1 web-post-tr lastcol 2 0 1 web-post-tl border)
    (key-wall-brace-less lastcol 2 0 1 web-post-tl lastcol 2 0 1 web-post-tr border)
    ; top-right corner
    (key-wall-brace lastcol 2 0 1 web-post-tr lastcol 2 1 0 web-post-tr border)
    ; (key-wall-brace  0 0  -1 0 web-post-tl 0 1 -1 0 web-post-bl border)
    (key-wall-brace-less lastcol 2 1 0 web-post-tr lastcol 2 1 0 web-post-br border)
    ; bottom-left corner
    (key-wall-brace lastcol 2 1 0 web-post-br lastcol 2 0 -1 web-post-br border)
    (key-wall-brace-less lastcol 2 0 -1 web-post-br lastcol 2 0 -1 web-post-bl border)
    (key-wall-brace-less lastcol 2 0 -1 web-post-bl (dec lastcol) 2 1 -1 web-post-br border)
    (for [y (range 3 lastrow)] (key-wall-brace-less (dec lastcol) (dec y) 1 0 web-post-br (dec lastcol) y 1 0 web-post-tr border))
    (key-corner (dec lastcol) cornerrow :br border)
   )
)

(defn back-wall [border]
  (union
    ; (key-wall-brace 0 0 0 1 web-post-tl          0  0 0 1 web-post-tr border)
    (for [c (range firstcol (dec ncols))] 
                  (case c  0 (key-wall-brace c firstrow 0 1 web-post-tl          c  firstrow 0 1 web-post-tr border)
                           1 (key-wall-brace c firstrow 0 1 web-post-tl          c  firstrow 0 1 web-post-tm border)
                             (key-wall-brace c firstrow 0 1 web-post-tl          c  firstrow 0 1 web-post-tr border)
                  )
    )
    (for [c (range 1 (dec ncols))]
                  (case c 1 (key-wall-brace c firstrow 0 1 web-post-tl (dec c) firstrow 0 1 web-post-tr border)
                          2 (key-wall-brace c firstrow 0 1 fat-web-post-tl (dec c) firstrow 0 1 fat-web-post-tm border)
                          (->> (key-wall-brace c firstrow 0 1 fat-web-post-tl (dec c) firstrow 0 1 fat-web-post-tr border) (color PUR))
                  )
    )
  )
)

(defn left-wall [border]
  (let [
        key-place (if border key-place key-place-shifted)
        trackball-place (if border trackball-place trackball-place-shifted)
       ]
  (union 
    ; left-back-corner
    (->> (key-wall-brace firstcol firstrow 0 1 web-post-tl firstcol firstrow -1 0 web-post-tl border)
         (color GRE))
    ; (key-wall-brace  0 0  -1 0 web-post-tl 0 1 -1 0 web-post-bl border)

    (for [y (range firstrow (dec lastrow))] (key-wall-brace firstcol      y  -1 0 web-post-tl firstcol y -1 0 web-post-bl border))
    (for [y (range (inc firstrow) (if track-ball (dec lastrow) (lastrow)))] (key-wall-brace firstcol (dec y) -1 0 web-post-bl firstcol y -1 0 web-post-tl border))

    (when track-ball (union
      (wall-brace (partial key-place firstcol (dec real-lastrow)) -1 0 web-post-bl trackball-place -1 0 trackball-post-tl border)
      (wall-brace trackball-place -1 0 trackball-post-tl trackball-place -1 0 trackball-post-bl border)
      (wall-brace trackball-place -1 0 trackball-post-bl (partial thumb-l-place' border) 0 1 fat-web-post-tr border)
      ;(key-wall-brace firstcol (dec real-lastrow) 0 -1 fat-web-post-br (inc firstcol) (dec real-lastrow) 0 -1 fat-web-post-bl border)
      ;(key-wall-brace (inc firstcol) real-lastrow -1 0 fat-web-post-bl (inc firstcol)      real-lastrow  0 -1 fat-web-post-tl border)
      ;(key-wall-brace (inc firstcol)      real-lastrow  -1 0 fat-web-post-tl (inc firstcol) real-lastrow -1 0 fat-web-post-bl border)
      ;(key-wall-brace (inc firstcol) (dec real-lastrow) -1 0 fat-web-post-bl (inc firstcol) real-lastrow -1 0 fat-web-post-tl border)
      ;(wall-brace (partial key-place (inc firstcol) real-lastrow)  -1 0 fat-web-post-bl thumb-m-place 0 1 fat-web-post-tl border)
      ;(wall-brace thumb-m-place 0 1 fat-web-post-tl thumb-l-place 0 1 fat-web-post-tr border)
    ));

    ; thumb connector
    ; (->> (wall-brace (partial key-place 0 cornerrow) -1 0 web-post-bl thumb-l-place 0 1 fat-web-post-tr border) (color WHI))
  ))
)

(defn front-wall [border]
  (let [ 
        thumb-r-place (if border thumb-r-place (partial thumb-r-place' border)) 
        key-place (if border key-place key-place-shifted) 
       ]
  (union 
    (key-wall-brace 3 real-lastrow 0   -1 fat-web-post-bl     3   real-lastrow 0.5 -1 fat-web-post-br border)
    (key-wall-brace 3 real-lastrow 0.5 -1 fat-web-post-br 4 cornerrow 0.5 -1 fat-web-post-bl border)
    (for [x (range 4 (dec ncols))] (key-wall-brace x cornerrow 0 -1 fat-web-post-bl      x  cornerrow 0 -1 fat-web-post-br border)) ; TODO fix extra wall
    (for [x (range 5 (dec ncols))] (key-wall-brace x cornerrow 0 -1 fat-web-post-bl (dec x) cornerrow 0 -1 fat-web-post-br border))
    (->> (if bottom-row
        (wall-brace thumb-r-place 1 0 fat-web-post-br (partial key-place 3 lastrow) -1 0 web-post-bl border) 
        (union 
          (wall-brace thumb-r-place 0 -1 fat-web-post-br thumb-r-place 1 0 fat-web-post-br border) ; corner
          (wall-brace thumb-r-place 1 0 fat-web-post-br thumb-r-place 1 0 fat-web-post-tr border) 
          (wall-brace thumb-r-place 1 0 fat-web-post-tr (partial key-place 3 real-lastrow) -1 0 fat-web-post-bl border) 
        )
      )
      (color RED)
    )
  ))
)

(defn bottom-corner-alpha [shape] (key-place 0 cornerrow (translate (wall-locate1 -1 0 false) shape)))

(defn thumb-wall [border]
  (let [ thumb-o-place' (if thumb-u thumb-u-place' thumb-l-place') ]
  (union 
    ; thumb walls
    (->> ((if bottom-row wall-brace-right wall-brace-right) (partial thumb-r-place' border)  0 -1 fat-web-post-br (partial thumb-r-place' border)  0 -1 fat-web-post-bl border) (color ORA))
    (->> (wall-brace-deeper (partial thumb-m-place' border)  0 -1 fat-web-post-br (partial thumb-m-place' border)  0 -1 fat-web-post-bl border) (color YEL))
    (->> (wall-brace        (partial thumb-m-place' border)  0 -1 fat-web-post-br (partial thumb-m-place' border)  0 -1 fat-web-post-bl border) (color YEL))
    ; (->> (wall-brace-deeper thumb-l-place  0 -1 fat-web-post-br thumb-l-place  0 -1 fat-web-post-bl border) (color DGR))
    (->> (wall-brace-left   (partial thumb-l-place' border)  0 -1 fat-web-post-br (partial thumb-l-place' border)  0 -1 fat-web-post-bl border) (color DGR))
    (->> (wall-brace        (partial thumb-l-place' border)  0 -1 fat-web-post-bl (partial thumb-l-place' border)  0 -1 fat-web-post-br border) (color GRE))

    (->> (wall-brace        (partial thumb-l-place' border) -1  0 fat-web-post-tl (partial thumb-l-place' border) -1  0 fat-web-post-bl border) (color GRY))
    ; (->> (wall-brace-deeper thumb-l-place -1  0 fat-web-post-tl thumb-l-place -1  0 fat-web-post-bl border) (color GRY))
    ; (->> (wall-brace-deeper thumb-u-place  0  1 fat-web-post-tl thumb-u-place  0  1 fat-web-post-tr border) (color BRO))
    (->> (wall-brace        (partial thumb-o-place' border)  0  1 fat-web-post-tl (partial thumb-o-place' border)  0  1 fat-web-post-tr border) (color BRO))

    ; thumb corners
    (->> (wall-brace        (partial thumb-l-place' border) -1  0 fat-web-post-bl (partial thumb-l-place' border)  0 -1 fat-web-post-bl border) (color NBL))
    ; (->> (wall-brace-deeper thumb-l-place -1  0 fat-web-post-bl thumb-l-place  0 -1 fat-web-post-bl border) (color NBL))
    ; (->> (wall-brace-deeper thumb-u-place -1  0 fat-web-post-tl thumb-u-place  0  1 fat-web-post-tl border) (color PUR))
    (->> (wall-brace        (partial thumb-o-place' border) -1  0 fat-web-post-tl (partial thumb-o-place' border)  0  1 fat-web-post-tl border) (color PUR))

    ; thumb tweeners
    (->> (wall-brace-deeper (partial thumb-r-place' border)  0 -1 fat-web-post-bl (partial thumb-m-place' border)  0 -1 fat-web-post-br border) (color PIN))
    (->> (wall-brace        (partial thumb-r-place' border)  0 -1 fat-web-post-bl (partial thumb-m-place' border)  0 -1 fat-web-post-br border) (color PIN))
    (->> (wall-brace-deeper (partial thumb-m-place' border)  0 -1 fat-web-post-bl (partial thumb-l-place' border)  0 -1 fat-web-post-br border) (color MAG))
    (->> (wall-brace        (partial thumb-m-place' border)  0 -1 fat-web-post-bl (partial thumb-l-place' border)  0 -1 fat-web-post-br border) (color MAG))
    ; (->> (wall-brace-deeper thumb-u-place -1  0 fat-web-post-tl thumb-l-place -1  0 fat-web-post-tl border) (color CYA))
    (when thumb-u (->> (wall-brace        (partial thumb-o-place' border) -1  0 fat-web-post-tl (partial thumb-l-place' border) -1  0 fat-web-post-tl border) (color CYA)))

    ; (->> (wall-brace-deeper thumb-u-place  0  1 fat-web-post-tr bottom-corner-alpha  0  1 fat-web-post-tl border) (color PIN))
    (when (not track-ball) (->> (wall-brace        (partial thumb-o-place' border)  0  1 fat-web-post-tr bottom-corner-alpha  0  1 fat-web-post-tl border) (color PIN)))
  ))
)

(def case-walls
  (union
    (right-wall false)
    (back-wall false)
    (left-wall false)
    (front-wall false)
    (thumb-wall false)
  )
)

(def case-top-border
  (union
    (right-wall true)
    (back-wall true)
    (left-wall true)
    (front-wall true)
    (thumb-wall true)
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
         orig-position (thumb-position [0 0 0] [0 0 0] [0 0 0])
         position (map + (shift-model-position (map + offset orig-position)) [0 0 (- (/ height 2))])
       ]
    (->> (screw-insert-shape res rot bottom-radius top-radius height)
         (translate position)
         )))


(def screw-insert-bottom-offset 0)
(defn screw-insert-all-shapes [bottom-radius top-radius height]
  (union 
    (->> (screw-insert ROUND-RES 0 2             1 bottom-radius top-radius height [ -3   4.50 screw-insert-bottom-offset]) (color RED)) ; top middle
    (->> (screw-insert ROUND-RES 0 0             1 bottom-radius top-radius height [-3.25  5    screw-insert-bottom-offset]) (color PIN)) ; left
    (->> (screw-insert ROUND-RES 0 0             3 bottom-radius top-radius height [-13 6    screw-insert-bottom-offset]) (color NBL)) ; trackball
    (->> (screw-insert ROUND-RES 0 0       lastrow bottom-radius top-radius height [-19 -15.0  screw-insert-bottom-offset]) (color BRO)) ; thumb
    (->> (screw-insert ROUND-RES 0 lastcol       1 bottom-radius top-radius height [ 3  5  screw-insert-bottom-offset]) (color PUR)) ; top right
    (->> (screw-insert ROUND-RES 0 2       lastrow bottom-radius top-radius height [ -4.25     -3.5  screw-insert-bottom-offset]) (color BLA)) ; bottom middle
    (->> (screw-insert ROUND-RES 0 lastcol (- lastrow 1) bottom-radius top-radius height [ 4.25     -3.50  screw-insert-bottom-offset]) (color YEL)) ; bottom right
)) 

(def screw-insert-height 6.5) ; Hole Depth Y: 4.4
(def screw-insert-radius (/ 4.4 2)) ; Hole Diameter C: 4.1-4.4

(def screw-insert-holes ( screw-insert-all-shapes 
                          screw-insert-radius 
                          screw-insert-radius 
                          (* screw-insert-height 1.5)
                        ))

(def screw-insert-wall-thickness 4)
(def screw-insert-outers ( screw-insert-all-shapes 
                           (+ screw-insert-radius screw-insert-wall-thickness) 
                           (+ screw-insert-radius screw-insert-wall-thickness) 
                           screw-insert-height
                         ))

(defn top-screw-insert-all-shapes [res bottom-radius top-radius height]
  (union 
    (->> (screw-insert-relative-z res  115 3             1 bottom-radius top-radius height [ -9.5  5.75 (+ (- 0) hide-top-screws) ]) (color RED)) ; top middle
    (->> (screw-insert-relative-z res -124 0             1 bottom-radius top-radius height [ -6.5 -12 (+ (- 0) hide-top-screws)]) (color PIN)) ; left-top
    ;(->> (screw-insert res -114 0             3 bottom-radius top-radius height [ -6   11    (+ 58.75 hide-top-screws)]) (color NBL)) ; left
    (->> (screw-insert-relative-z-thumb res -7   bottom-radius top-radius height [-32.5 -22.50  (+ (- 3) hide-top-screws)]) (color BRO)) ; thumb
    (->> (screw-insert-relative-z res  145 (dec lastcol)       1 bottom-radius top-radius height [ -8.5    5.75 (+ 0.5  hide-top-screws)]) (color PUR)) ; top right
    ;(->> (screw-insert res  -15 0       lastrow bottom-radius top-radius height [ 12.5 -2.25 (+ 52 hide-top-screws)]) (color CYA)) ; bottom thumb
    ; (->> (screw-insert res  -23 3       lastrow bottom-radius top-radius height [ -12.5  -4.5 (+ 49 hide-top-screws)]) (color GRE)) ; bottom middle
    (->> (screw-insert-relative-z res -100 (dec lastcol)       3 bottom-radius top-radius (* height 1) [-8.5 10 (+ (- 1) hide-top-screws)]) (color GRE)) ; bottom right
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
(def top-screw-radius (/ 2.6 2))       ; M3 screw diameter
(def top-screw-head-radius (/ 4.6 2)) ; M3 screw head diameter (4.4 plus some clearance)

(def top-screw-clear-length (- top-screw-length top-screw-insert-height))
(def top-screw-block-height 4)
(def top-screw-block-wall-thickness 7)
(def top-screw-insert-wall-thickness 1.2)

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
(def usb-holder-vertical true)
(def usb-holder-stl 
  (if usb-holder-vertical
    (import "../things/usb_holder_vertical.stl")
    (import "../things/usb_holder_w_reset.stl")
  )
)
(def usb-holder-cutout-stl 
  (if usb-holder-vertical
    (import "../things/usb_holder_vertical_cutout.stl")
    (import "../things/usb_holder_w_reset_cutout.stl")
  )
)

(def usb-holder-clearance 0.3)
(def usb-holder-cutout-height 
  (if usb-holder-vertical 
    (+ 30.6 usb-holder-clearance)
    (+ 15 usb-holder-clearance)
  )
)

(def usb-holder-cutout-bottom-offset (/ usb-holder-cutout-height 2))

;TODO horizontal and vertical usb holders have different origin points
; because, i can't pic a standard origin for different versions
(def usb-holder-bottom-offset 
  (if usb-holder-vertical 
    (/ usb-holder-cutout-height 2)
    (/ usb-holder-clearance 2)
  )
)

(def usb-holder-z-rotate 1.5)
(def usb-holder-offset-coordinates
  (if use_hotswap_holder
    [-35.5 31.60 usb-holder-bottom-offset]
    [-15.5 47.95 usb-holder-bottom-offset]))
(defn usb-holder-place [shape]
  (->> shape
       (translate usb-holder-offset-coordinates)
       (rotate (deg2rad usb-holder-z-rotate) [0 0 1])
  ))
    
(def usb-holder (usb-holder-place usb-holder-stl))
(def usb-holder-cutout (usb-holder-place usb-holder-cutout-stl))
(def usb-holder-space
  (color RED
    (translate [0 0 usb-holder-cutout-bottom-offset]
      (extrude-linear {:height usb-holder-cutout-height :twist 0 :convexity 0}
        (offset usb-holder-clearance (projection {:cut false}
            (scale [1.001 1 1] usb-holder-cutout)
          )
        )
      )
    )
  )
)

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

(def bottom-plate-thickness 3)
(def screw-insert-fillets-z 2)

(def screw-insert-bottom-plate-bottom-radius (+ screw-insert-radius 0.9))
(def screw-insert-bottom-plate-top-radius    (- screw-insert-radius 0.3))
(def screw-insert-holes-bottom-plate ( screw-insert-all-shapes 
                                       screw-insert-bottom-plate-top-radius 
                                       screw-insert-bottom-plate-top-radius 
                                       99
                                     ))

(def screw-insert-fillets-bottom-plate ( screw-insert-all-shapes 
                                         screw-insert-bottom-plate-bottom-radius 
                                         screw-insert-bottom-plate-top-radius 
                                         screw-insert-fillets-z
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
(def wrist-shape-connector (polygon [[wrist-shape-connector-half-width 10] 
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
    (let [wrist-rest-y-angle (* tenting-angle 45)
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


(def case-walls-bottom (cut 
                           (translate [0 0 0] 
                                      case-walls
                           )
                       ))
(def case-walls-bottom-projection (project
                                    (union
                                     (extrude-linear {:height 0.01
                                                      :scale 0.995
                                                      :center true} 
                                         case-walls-bottom
                                     )
                                     (extrude-linear {:height 0.01
                                                      :scale 1.05
                                                      :center true} 
                                         case-walls-bottom
                                     )
                                    ) 
                                  ))

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
  (translate bottom-trim-origin (cube outer-width outer-width trim))
  )

(defn trackball-mount-rotate [thing] (rotate (deg2rad 0) [0 0 1]
                                     (rotate (deg2rad 0) [1 0 0]
                                     (rotate (deg2rad 0) [0 1 0]
                                     thing))))

(def sensor-holder-arm (translate [0 -0.5 0]
                                  (union
                                   (translate [0 (- (/ 4 2) (/ 1 2)) 1] (cube sensor-holder-width 4 2))
                                   (translate [0 0 (- (/ sensor-height 2))] (cube sensor-holder-width 1 sensor-height))
                                   (translate [0 (- (/ 4 2) (/ 1 2)) (- (+ sensor-height (/ 1 2)))] (cube sensor-holder-width 4 1))
                                   )))
(def sensor-holder
  (translate (map + bottom-trim-origin [0 0 (/ trim 2)])
             (rotate (deg2rad 90) [0 0 1]
             (union
              (translate [0 (- (/ sensor-length 2)) 0] sensor-holder-arm)
              (->>
               sensor-holder-arm
               (mirror [0 1 0])
               (translate [0 (/ sensor-length 2) 0])
               )
              )
             ))
  )

(defn sensor-hole-angle [shape] (
                                  ->> shape
                                      (rotate (deg2rad sensor-z-rotate) [0 0 1])
                                      (rotate (deg2rad sensor-x-rotate) [1 0 0])
                                      (rotate (deg2rad sensor-y-rotate) [0 1 0])
                                      ))
(defn dowell-angle [shape] (
                             ->> shape
                                 (rotate (deg2rad (+ 90 35)) [0 0 1])
                                 (rotate (deg2rad -30) [0 1 0])
                                 (rotate (deg2rad 25) [1 0 0])
                                 ))

(def rotated-dowells
  (union;dowell-angle
   (translate [0 0 (- (/ holder-thickness' 2))] dowells)
   ))

(def rotated-bottom-trim     (sensor-hole-angle
                               bottom-trim))
(def sensor-shape     (translate (map + bottom-trim-origin [0 0 (- (/ trim 2) (/ sensor-height 2) )])
                               (cube sensor-length sensor-width sensor-height)))

(defn filler-rotate [p] (
                         ->> p
                             (trackball-mount-rotate)
                             ;                       (rotate (deg2rad 0) [0 1 0])
                             (rotate (deg2rad 20) [0 0 1])
                             (rotate (deg2rad -30) [0 1 0])
                         ))

; align with the top dowell
(defn trackswitch-place [offset-x-rot p] (->> p
                               (rotate (deg2rad 90) [1 0 0])
                               (translate [0 (/ outer-width 2) 0])
                               (rotate (deg2rad offset-x-rot) [1 0 0])
                               (rotate (deg2rad 30) [0 0 1])
                             ))

(def trackswitch-offset-x-rot 23)
(def trackswitch-connector (cube mount-height 3 (/ plate-thickness 2)))

(defn trackball-mount [mirror-internals]
  (union
   (difference
    (union
     (trackswitch-place trackswitch-offset-x-rot (translate [0 0 (- plate-thickness)] (single-plate false)))
     (hull
       (trackswitch-place trackswitch-offset-x-rot (translate [0 (- (+ (/ mount-width 2) (/ 1.5 1))) (- (/ plate-thickness 4))] trackswitch-connector))
       (trackswitch-place trackswitch-offset-x-rot (translate [0 (- (+ (/ mount-width 2) (/ 1.5 1))) (- (- 0.5) (/ (* plate-thickness 5) 4))] trackswitch-connector))
       (trackswitch-place -4.5 (translate [0 0 (+ (- (/ plate-thickness 4)) 2) ] trackswitch-connector))
       (trackswitch-place -6.5 (translate [0 0 (+ (- (/ plate-thickness 4)) 2) ] trackswitch-connector))
     )
     (trackball-mount-rotate cup)
     (filler-rotate cup)
    )
    ; subtract out room for the axels
    rotated-dowells
    ; Subtract out the bottom trim clearing a hole for the sensor
    rotated-bottom-trim
   )
   (sensor-hole-angle
     sensor-holder)
  )
)

(defn trackball-rotate [shape]
  (->> shape
    (rotate (deg2rad 60) [1 0 0])
    (rotate (deg2rad 0) [0 1 0])
    (rotate (deg2rad 30) [0 0 1])
    (thumb-place [0 0 0] [-46 5 12]))
)

;;;;;;;;;;;;
;; Models ;;
;;;;;;;;;;;;

(defn model-switch-plate-cutouts [mirror-internals]
  (shift-model (difference
    (union
      (key-places single-plate-blank)
      (debug case-top-border)
      (if use_flex_pcb_holder flex-pcb-holders)
      connectors
      (thumb-layout single-plate-blank)
      thumb-connectors
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

(def model-bottom-plate
  (let [screw-cutouts         (translate [0 0 (/ bottom-plate-thickness -1.99)] 
                                         screw-insert-holes-bottom-plate)
        screw-cutouts-fillets (translate [0 0 (/ bottom-plate-thickness -1.99)] 
                                         screw-insert-fillets-bottom-plate)
        wrist-rest-adjust-fillets (translate [-12 -120 0] 
                                         (screw-insert-wrist-rest screw-insert-bottom-plate-bottom-radius
                                                                  screw-insert-bottom-plate-top-radius
                                                                  screw-insert-fillets-z))
        wrist-rest-adjust-holes (translate [-12 -120 0] 
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
                                              case-walls-bottom
                                          )
                                       )
                                       (if recess-bottom-plate
                                           case-walls-bottom-projection
                                       )
                                   )
                                   (project
                                       (if adjustable-wrist-rest-holder-plate 
                                                  (translate [8 -55 0] wrist-shape))
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

(def testing true)

(defn model-case-walls-right-base [mirror-internals]
    (union
      (when use_flex_pcb_holder flex-pcb-holders)
      (difference (union case-walls
                         screw-insert-outers
                         top-screw-block-outers
                         (when (= controller-holder 2) pcb-holder-screw-post)
                  )
                  (when (not testing) 
                    (model-switch-plate-cutouts mirror-internals)
                  ;(case controller-holder 1 usb-holder-space
                  ;                        2 pcb-holder-space
                  ;)
                  )
                  screw-insert-holes
                  top-screw-insert-holes
      )
    )
)

(defn model-case-walls-right [mirror-internals]
  ; (union
  (difference
    (model-case-walls-right-base mirror-internals)
    (when (not testing) (union
      (when recess-bottom-plate
        (union
            (translate [0 0 (- (+ 20 bottom-plate-thickness))] 
                       (cube 350 350 40))
            (translate [0 0 (- (/ bottom-plate-thickness 2))] 
                           (scale [1.01 1.01 1.15] model-bottom-plate))
        )
        (translate [0 0 -20] (cube 350 350 40))
      )
      (shift-model 
        (union
          caps-cutout
          thumbcaps-cutout
          (thumb-key-cutouts mirror-internals)
          (if (not (or use_hotswap_holder use_solderless)) 
              (union key-space-below
                    thumb-space-below))
          (if use_hotswap_holder (thumb-layout (hotswap-case-cutout mirror-internals)))
        ))
      )
  ))
  ; (debug top-screw))
)

(defn switch-plates-right [mirror-internals]
  (difference
    (union 
      (shift-model 
        (union
          (when (not testing) (key-places (single-plate mirror-internals)))
          case-top-border
          (when use_flex_pcb_holder flex-pcb-holders)
          (color CYA connectors)
          (when (not testing) (thumb-layout (single-plate mirror-internals)))
          thumb-connectors
          (trackball-rotate (trackball-mount mirror-internals))
        )
      )
      (when top-screw-insert-top-plate-bumps top-screw-insert-outers)
    )
    (when (not testing) (union
    (when top-screw-insert-top-plate-bumps (model-case-walls-right-base mirror-internals))
    (shift-model (union 
      (trackball-rotate sensor-shape)
      caps-cutout
      thumbcaps-cutout
      (thumb-key-cutouts mirror-internals)
      (when (not (or use_hotswap_holder use_solderless)) 
          (union key-space-below
                thumb-space-below))
      (when use_hotswap_holder (thumb-layout (hotswap-case-cutout mirror-internals)))
      (when use_hotswap_holder (key-places (hotswap-case-cutout mirror-internals)))
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
      (union (difference case-walls usb-holder-space)
             ;screw-insert-holes) 
       screw-insert-outers))
    
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
;(spit "things/switch-plates-right.scad"
;      (write-scad (model-switch-plates-right false)))
;(spit "things/switch-plates-left.scad"
;      (write-scad (mirror [-1 0 0]  (model-switch-plates-right true))))
;; (spit "things/switch-plate-cutouts.scad"
;;       (write-scad (model-switch-plate-cutouts false)))
;
;(spit "things/case-walls-right.scad"
;      (write-scad (model-case-walls-right false)))
;(spit "things/case-walls-left.scad"
;      (write-scad (mirror [-1 0 0] (model-case-walls-right true))))
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

(spit "things/test.scad"
      (write-scad
            ;PRO TIP, commend out everything but caps & thumbcaps to play with geometry of keyboard, it's MUCH faster
            ;(debug
            ;;(color BLU
              ;(model-case-walls-right false)
              ;model-bottom-plate
            ;)

            ;(model-right false)
			;(translate [0 0 (- plate-thickness)] (single-plate false))
			(translate [0 0 0] (model-switch-plates-right false))
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
      ))
