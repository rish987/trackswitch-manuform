# Trackswitch Manuform -- A Hyper-Ergonomic Trackball-Enabled Dactyl with a Crazy Mouse Level Shift Mechanism

This keyboard is an attempt to make a best-of-both-worlds combination between the
modular (and support-saving) design of [dereknheiley's compactyl manuform](https://github.com/dereknheiley/compactyl) (with adjustable wrist rest mounts)
and the trackball-enabled design of the [noahprince22's tracktyl manuform](https://github.com/noahprince22/tractyl-manuform-keyboard),
forking the firmware from [Schievel1's dactyl_manuform_r_track](https://github.com/Schievel1/dactyl_manuform_r_track).

It also incorporates some novel features:
- Drawing inspiration from the vertical actuations of [JesusFreke's lalboard](https://github.com/JesusFreke/lalboard), the keys above and below the home row are positioned such that actuating them involves the continuation of a single motion (i.e. extending or curling the fingers) rather than two separate motions (extending then pressing down or curling then pressing down, as is the case with a conventional flat keyboard layout). Less muscle memory + shorter travel distance = quicker and more comfortable typing!
- I've yeeted keys that I find exceed a certain (low) threshold of difficulty to press on a standard ortholinear layout. These include the (QWERTY) `n` and `b` keys, as well as all the keys outwards from the pinky column.
- To make up for this lack of keys, the left side of this keyboard features a powerful thumb cluster with five keys (four of which are vertically actuated: one by the base of the thumb (actuated by curling the thumb inwards), two by the tip of the thumb (actuated by extending the thumb upwards), and another by the thumb knuckle (actuated by moving the thumb outwards)) that allow for fast level-shifting.
- The right side incorporates a trackball mount with a mechanism that allows the trackball to act as a "switch" that can be pressed down to both enable mouse movement and activate the mouse button layer. I refer to this collective assembly as the "trackswitch".
- Both the trackswitch and trackball sensor have fully parameterized mounts with mounting mechanisms that allow their distance from the trackball to be micro-adjustable.
- Fully parameterized case mounts for the arduino micro and pro-micro MCUs.

<!--
![Trackswitch Manuform preview](images/trackswitch-manuform.png)
-->

# Build Guide

## Tools

- 3D printer (I use a modestly upgraded Ender 3)
- Raspberry Pi with [Octoprint](https://octoprint.org/) for remote printing (recommended)
- Soldering Iron with [M2, M3 insert tips](https://www.amazon.com/gp/product/B08B17VQLD/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1)
- Solder fume extractor
- Safety goggles
- Multimeter with continuity beep
- Hot glue gun
- Heat gun and heat shrink tubing
- Wire cutters
- Needle-nose pliers
- Caliper (digital recommended)
- [0.6mm nozzle](https://www.amazon.com/gp/product/B093SKXHL3/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&th=1) (recommended for faster prints)

## Bill-of-Materials

Part | Price | Comments
-----|-------|----
[Arduino micro with headers](https://www.amazon.com/gp/product/B00AFY2S56/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&th=1) | $23.88 | This is needed only for the right (trackball) half of the keyboard (you can get away with a pro micro on the left half).
[Pro Micro MCU](https://www.amazon.com/gp/product/B08THVMQ46/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $19.88 | For the left half. BE CAREFUL WITH THESE! They're pretty flimsy and I've broken the USB headers on muliple of them, so it's good to get a few in case something happens.
[PMW3360 Motion Sensor](https://www.tindie.com/products/jkicklighter/pmw3360-motion-sensor/) | $29.99 | Trackball motion sensor.
[Key switches x35](https://www.amazon.com/gp/product/B07X3WKM54/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $34.99 | If you're new to mechanical keyboards Gateron browns are probably a safe option.
[Keyswitch with a good amount of actuation force](https://www.amazon.com/gp/product/B078FMPZ8R/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $20.99 | This is for the trackswitch -- some force is needed to push the trackball up, but too much force can make the motion less smooth; I recommend getting a sample pack like this one so that you have a few alternatives to try. In my case I ended up going with TODO.
[Trackball](https://www.aliexpress.us/item/3256803106743416.html?spm=a2g0o.productlist.main.7.559e75b6LrrWPq&algo_pvid=5b156845-75aa-4609-92bb-10b4919b35ad&algo_exp_id=5b156845-75aa-4609-92bb-10b4919b35ad-3&pdp_ext_f=%7B%22sku_id%22%3A%2212000025055404434%22%7D&pdp_npi=2%40dis%21USD%2123.46%2111.03%21%21%21%21%21%402102186a16738183211058438d0674%2112000025055404434%21sea&curPageLogUid=fiwnZejyx836) | $11.99
[Dowel Pins](https://www.amazon.com/gp/product/B07M63KXKS/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $7.49
[Miniature Bearings](https://www.amazon.com/gp/product/B00ZHSQX42/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $10.99
[Silicone wrist rests](https://www.amazon.com/gp/product/B01LYBFIJA/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&th=1) | $9.99 | Nice and soft and squishy and comfy!
[PLA 3D Printer Filament (at least TODO grams)](https://www.amazon.com/gp/product/B07PGY2JP1/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&th=1) | $17.63 | 
[Keycaps x35](https://www.amazon.com/gp/product/B07SJKMNWC/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $15.55 | If you have a resin printer, printing keycaps is also apparently an option.
[1N4148 diodes x35](https://www.amazon.com/gp/product/B079KJ91JZ/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $5.99 |
[Hot swap sockets x36](https://www.amazon.com/gp/product/B096WZ6TJ5/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $14.50 | 
[MicroUSB to USB cable](https://www.amazon.com/gp/product/B01NA9UCVQ/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $7.99 | To connect keyboard to computer (make sure the length is enough for your own setup!).
[TRRS cable](https://www.amazon.com/gp/product/B07FFW8YZR/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $8.99 | This connects both halves of the keyboard, so make sure to get one that is long enough for whatever split you are going to make.
[Jumper wires](https://www.amazon.com/gp/product/B08151TQHG/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $8.99 |
[Thin solder wire](https://www.amazon.com/gp/product/B076QG9N13/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $11.59 | 
[Solid core jumper wire](https://www.amazon.com/gp/product/B07TX6BX47/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&th=1) | $15.99 | 
[TRRS connectors](https://www.amazon.com/gp/product/B07KY862P6/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $6.99 | 
[Reset buttons](https://www.amazon.com/gp/product/B07CG6HVY9/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $8.99 | 
[M2, M3 threaded inserts](https://www.amazon.com/gp/product/B07WH59N6T/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) | $18.99 | You will only need 22x M3 and 2x M2 inserts for this build, but these are very commonly used in 3D printing projects so it's good to have a complete set. In my particular build I used [these M3 inserts](https://www.amazon.com/gp/product/B08T7M2H4S/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&psc=1) left over from a HeroMe Gen6 build.
[M2, M3 screws and washers](https://www.amazon.com/gp/product/B07F74JHBD/ref=ppx_yo_dt_b_search_asin_title?ie=UTF8&th=1) | $28.99 | Again, good to have a full set for other 3D printer projects.


Total: $352.37

This is a bit of an overestimate, considering that a lot of the listings above come with
more pieces/material than needed (and you may already have some of these things lying around).
Personally, I like to have two keyboards (one for at home and another for on-the-go/redundancy in case one breaks),
and if you want to build a second keyboard,
the items you will need to re-purchase are the keycaps, trackball, Arduino micro, Pro Micro (possibly), PMW3360, and silicone wrist wrests
for an additonal $111.28. Two of these keyboards for less than $231.83 each sounds like a pretty good deal to me!

## Preliminary Measurements

The code in this repo is parameterized to the parts I have purchased above,
however, as any responsible consumer knows, if one already has (or is able to find a better deal on)
similar-enough alternatives, it is indeed the ethically correct choice to make use of them!

Listed below are the parameters that can be tweaked to accommodate such alternatives,
with accompanying images of how to take the correct measurements.

### `trackball-width`

The diameter of the trackball. When measuring, turn it a bit in the calipers to make sure that you have captured the full diameter.

<!--
![trackball-width measurement](images/trackball-width.png)
-->

### `dowell-height`

The height of the dowels (apologies for the inconsistent spelling).

<!--
![dowell-height measurement](images/dowell-height.png)
-->

### `sa-height1`

Distance from the bottom of the keyswitch brims (where they contact the case) to the bottom of the keycap.

<!--
![sa-height1 measurement](images/sa-height1.png)
-->

### `sa-height2`

Distance from the bottom of the keyswitch brims (where they contact the case) to the top of the keycap.

<!--
![sa-height2 measurement](images/sa-height2.png)
-->

### `sa-length`

Width/length of the keycap base (assuming a square keycap).

<!--
![sa-length measurement](images/sa-length.png)
-->

### `sa-length2`

Width/length of the keycap top (assuming a square keycap).

<!--
![sa-length2 measurement](images/sa-length2.png)
-->

### `M3-insert-rad`

Radius of the M3 brass insert. If your insert has one flat half and one knurled half like mine,
use the radius of the flat half. Otherwise, go a little smaller than the actual radius
(so it will be able to insert properly and not just slide through).

<!--
![M3-insert-rad measurement](images/M3-insert-rad.png)
-->

### `M3-insert-height`

Height of the M3 brass insert, you may want to add a bit of buffer to this.

<!--
![M3-insert-height measurement](images/M3-insert-height.png)
-->

### `M2-insert-rad`, `M2-insert-height`

Repeat the above measurements with the M2 insert.

### `M3-screw-head-depth`

Height of the head of the M3 screw.

<!--
![M3-screw-head-depth measurement](images/M3-screw-head-depth.png)
-->

### `M3-washer-rad`

Outer radius of the M3 washer.

<!--
![M3-washer-rad measurement](images/M3-washer-rad.png)
-->

### `M2-screw-head-depth`, `M2-washer-rad`

Repeat the above measurements with the M2 screw/washer.

<!--
![ measurement](images/.png)
-->





## Making Adjustments/CAD Generation

### How to Generate the CAD Files

This keyboard is designed using the [OpenSCAD software](https://www.openscad.org/),
which compiles OpenSCAD code (`.scad` files) into an output `.stl` 3D model file
that you can then feed into a 3D Printer slicer to output the `.gcode`
file containing instructions on how to move the print axes/feed the extruder/heat the hotend+bed to actually print the thing.
However, the OpenSCAD language is fairly niche and doesn't have a lot of user-friendliness support outside of its custom IDE.

For this reason, most iterations of Dactyl Manuforms have been implemented primarily or entirely in
Clojure and transpiled to OpenSCAD (`.scad`) using the [`scad-clj` package](https://github.com/farrellm/scad-clj)
(you don't have to install this yourself, `lein auto generate` below should do it for you).
For this transpilation, you will need an additonal piece of sofware called [leiningen](https://leiningen.org/).

Once everything is installed, run
```bash
lein auto generate
```
which will populate the `things` directory with various `.scad` files.
Now, as you modify the `.clj` files, leinegen will automatically run 
and produce a new `.scad` file after you save any `.clj` file.

The parameterization is split into two files:
- `src/dactyl_keyboard/dactyl.clj` -- the main file where the keyboard is parameterized
- `src/usb_holder.clj` -- parameterization of the MCU holders

### Testing Changes (the `testing` variable)

At the very top of the file, you will find the `testing` variable.
Set this to true while you make changes to the overall keyboard layout
(you can just swap it with the line above to override it).
This will produce just enough code in the `things/test.scad` file
for you to have a good enough idea of how the keyboard is going to look
without including so much detail that it becomes impossible to view in
the OpenSCAD preview.

To control what actually is output for testing,
got to the `(when testing (spit "things/test.scad" ...`
block near the end of the file.
There, you can just paste in various parts from the
`(when (not testing) ...` output block right above (you can skip the `cura-fix` wrappers)
to get the test output of that part.

Once you're done, be sure to set `testing` to `false` again so that
the full parts incorporating your recent changes are actually output.

### Parameters to Tweak

Note that the code is unfortunately a bit messy at the moment, and there
may be several parameters lying around that I haven't bothered to maintain
or have silently deprecated. If any parameter isn't explicitly mentioned in this guide,
it probably means that you shouldn't mess with it
(unless of course you've read the code and know exactly what that would entail).

Below are the most likely parameters you'll want to tweak in order to get
this keyboard to exactly fit the shape of your hand:

#### Adjusting Key Offsets

- `column-offset` -- The xyz-offsets of each of the columns (but you should keep the x value at 0).

- `thumb-pos` -- The xyz-offsets of the thumb key.

#### Adjusting the Vertical Keys

The vertically actuated keys are placed using the function `key-vert-place`,
which takes a few parameters that can alter its position relative to the
default (i.e. all-paramters-set-to-zero) positon.
This default position aligns
the bottom edge of the top of the vertical switch's keycap
with
the top edge of the top of a normal horizontally-placed switch's keycap,
such that the vertical keyswitch is at a 90 degree angle relative to
the normal one.

To place the vertical keys, there are various parameters
whose names follow the pattern of the upper switch's "code"
with the following suffixes, where the paired animation shows
how adjusting this parameter will affect the placement of the vertical key.

- `*-rot`
<!--
![vert-rot](images/vert-rot.gif)
-->
- `*-extra-dist`
<!--
![vert-extra-dist](images/vert-extra-dist.gif)
-->
- `*-x-off`
<!--
![vert-x-off](images/vert-x-off.gif)
-->
- `*-x-rot`
<!--
![vert-x-rot](images/vert-x-rot.gif)
-->
- `*-z-off`
<!--
![vert-z-off](images/vert-z-off.gif)
-->
- `*-z-rot`
<!--
![vert-z-rot](images/vert-z-rot.gif)
-->

#### Adjusting the Above Keys

See `above-rot`, `above-extra-dist`, `above-x-rot`, `above-z-off`, `above-z-rot`.

#### Adjusting the Below Keys

See `below-rot`, `below-extra-dist`, `below-x-rot`, `below-z-rot`.

#### Adjusting the Left-of-Index Keys

For the vertical key to the left of the RH homerow index key (`h` on the QWERTY layout), see
`h-rot`, `h-extra-dist`, `h-x-off`, `h-x-rot`, `h-z-off`, `h-z-rot`.

For the vertical key to the upper-left of the RH homerow index key (`y` on the QWERTY layout), see
`y-rot`, `y-extra-dist`, `y-x-off`, `y-x-rot`, `y-z-off`, `y-z-rot`.

#### Adjusting the Thumb Cluster Layout

See the prefixes in the image below:

<!--
![thumb-codes](images/thumb-codes.png)
-->

#### Changing the Height and Tent Angle

You can alter the overall height of the keyboard with the `keyboard-z-offset` variable.

Another important aspect of a split keyboard is what's known as the "tent angle",
which is set with the `tenting-angle` variable.
This affects the degree to which the halves are "tented" upwards so that your wrists
can assume a more natural angle than being completely flat.





## Slicing and Printing

### Switch Plates

#### Prototyping

In order to truly know if your parameterizations provide a good fit for your hand,
you will have to print the switch plates and try it out.
Unfortunately this keyboard is not at all adjustable once printed (aside from the wrist wrests), 
so you will likely incur some wasted prints during the process of refining this.

The output contains the
`things/thumb-test-right.scad`
and
`things/thumb-test-left.scad`
files to allow you to print the thumb clusters by themselves
so that you can iterate on those with minimal waste.

I plan to soon also add a way to just print the homerow and `thumb-c` switch plates
in a coherent prototyping part so you can quickly check their relative positions.
However, if you can't wait for that, your only option
is to print the entire switch plate.

#### Slicer Settings

Printing the switch plates absolutely requires supports.
I recommend using the "tree supports" provided by Cura Slicer.
These supports have always worked perfectly for me and
are particularly easy to remove from the model.
Be sure to carefully check the generated supports before printing,
I have noticed that the slicer sometimes attempts to create tree supports in thin air.
If you observe this, I recommend turning the "Tree Support Branch Diameter Angle" setting
down to 1.0 degree so the supports are less wide at the base.

I also recommend using a brim when printing. This will come off with the supports,
so you don't have to worry about it leaving any ugly edges on the model.

Lastly, I have discovered that it is perfectly fine to print this model with
a 0.6mm nozzle and at 0.28mm layer height. This will substantially cut down your print time
relative to the default 0.4mm nozzle with 0.2mm layer height.
You may lose some of the finer details on the plates, but I ended up not making use of them anyways.

Otherwise, I use all of the default Cura settings.

#### Aligning the Support Blockers

Before loading in any models, go to `Preferences -> Configure Cura` and uncheck "Automatically drop models to the build plate".

Now, load in `switch-plates-{left or right}.stl`
and the corresponding`vert-support-blockers-{left or right}.stl`.
The support blockers are meant to prevent unnecessary and difficult-to-remove supports
from being generated in the vertical keys.
Cura might automatically rotate one of the models, so if this happens,
rotate it to the same orientation as the other part.
Now, try to approximately align the support blockers with the switch plates.

You'll notice on the build plate two tiny squares. Try to microadjust now and align so those squares coincide
(you don't have to be too perfect).

<!--
![alignment](images/alignment.png)
-->

Now, select the support blocker model, go to "Per Model Settings" and select "Don't support overlaps".
Then, select all (ctrl-A), right-click and select "Group Models" so that everything will move together as you do the final orientations.

#### Orientation

When orienting the switch plate, make sure to rotate it so there aren't any sharp corners
that will be printed as single points in a layer -- that is, all corners should be printed
as part of a straight line of filament.
Double-check in the layers preview after slicing that this is really the case!
Accommodating this requirement will likely result in some additional support material,
but this is well-worth it to prevent failed corners, or, in the worst case, a failed print.

#### Printing

I highly, highly recommend having an [Octoprint](https://octoprint.org/) setup with a camera for this.
If you use the 0.6mm nozzle as recommended above the print should take no more than 12 hours.
You should start the print early in the morning (as soon as you wake up).
It's probably a good idea to smear your buildplate with glue before printing to help prevent any bed adhesion problems.
Unless you have good safety precautions [like thermal fuses](https://www.youtube.com/watch?v=tTJfASOHojo)
installed on your 3D printer,
don't be tempted to leave your printer running overnight -- if for whatever reason the print fails halfway through,
diagnose the problem, sigh, and try again the next morning.

In any case, I also recommend having an AI failure detection system like [Gadget](https://octoeverywhere.com/launch) set up.

#### Removing Supports

The first step to removing supports is to PUT YOUR SAFETY GLASSES ON.
After you have them on, put your hand on your face to make sure they're really there.

### Case Walls

### MCU Holders

### Trackswitch Mount

### PMW3360 Mount

### Bottom Plates

### Wrist Rest Holders





<!-- TODO -->

## Electronics

### Right Side

### Left Side

### Hotswap Layout

Lay out the hotswap sockets in the approximate shape of the keyboard.

### Pre-cutting the Row and Column Lines

#### MCU Probes

#### Alignment, marking, and cutting

### Soldering

WARNING: Be sure to solder in a well-ventilated area with a fume extractor and safety goggles ON!

#### Pre-solder the sockets

#### Preparing the diodes

#### Pre-solder the diodes and column probes

#### Solder the Row and Column Lines

#### Solder the MCU Probes

#### Solder the PMW3360 Sensor

#### Install the Reset Button (MCU Assembly)

#### Install the TRRS Connector (MCU Assembly)

#### Inserting the Inserts

This is technically part of the assembly step, but is best to do right now while you have your solder iron out.




<!-- TODO -->

## Assembly

### Inserting the Hot-Swap Sockets

### Mounting the Trackswitch

### Mounting the PMW3360

### Mounting the Trackball

### Keycap Mods

### Installing the Switch Plates

### Installing the MCU Assembly

### Installing the Base Plate

### Mounting the Wrist Rests

<!-- TODO -->




## Firmware

### Installing Software (QMK, AVRDude)

### Tweaking the Key Layout

### Tweaking the Mouse Behavior

#### Special Mouse Keys

### Flashing the MCUs



<!-- TODO -->

## License

Copyright © 2015-2023 Rishikesh Vaishnav, Matthew Adereth, Noah Prince, Tom Short, Leo Lou, Okke Formsma, Derek Nheiley

The source code for generating the models is distributed under the [GNU AFFERO GENERAL PUBLIC LICENSE Version 3](LICENSE).

The generated models are distributed under the [Creative Commons Attribution-ShareAlike 4.0 International (CC BY-SA 4.0)](LICENSE-models).
