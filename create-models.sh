# based on similar changes from alexoterof in okke-formsma/dactyl-manuform-tight

# MAC
OPEN_SCAD='/Applications/OpenSCAD.app/Contents/MacOS/OpenSCAD'

# WINDOWS
# OPEN_SCAD='openscad.com'

echo 'Generating things/*.scad files using '$(lein -v)
echo '(load-file "src/dactyl_keyboard/dactyl.clj")' | lein repl > /dev/null 2>&1

echo 'Rendering things/*.stl files from things/*.scad using ...'
$OPEN_SCAD -v
$OPEN_SCAD -o things/right-plate-cut.stl things/right-plate-cut.scad >/dev/null 2>&1 &
$OPEN_SCAD -o things/right.stl things/right.scad >/dev/null 2>&1 &
#$OPEN_SCAD -o things/left.stl  things/left.scad >/dev/null 2>&1 &

echo 'Please wait for STL files to output in things/ directory!'

# echo 'Removing intermediary things/*.scad files...'
# rm -f things/right-bottom-plate.scad
# rm -f things/right.scad
# rm -f things/left.scad
