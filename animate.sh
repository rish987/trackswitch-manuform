#!/usr/bin/env bash
operation="$1"
param="$2"
steps=50

function generate {
    for (( step=0; step<=$steps; step++ ))
    do
        echo "generating step $step/$steps..."
        lein run $steps $step $1
    done
}

function animation_dir {
    echo "things/animation/$1"
}

function scad_dir {
    echo "$(animation_dir $1)/scad"
}

function png_dir {
    echo "$(animation_dir $1)/png"
}

function render {
    for (( step=0; step<=$steps; step++ ))
    do
        echo "rendering step $step/$steps..."
        openscad -o "$(png_dir $1)/$step.png" --colorscheme "Tomorrow Night" --projection o "$(scad_dir $1)/$step.scad"
    done
}

function gif {
    all_pngs=""
    for (( step=0; step<=$steps; step++ ))
    do
        all_pngs="$all_pngs $(png_dir $1)/$step.png"
    done
    out="$(animation_dir $param)/$param.gif"
    convert -delay 10 -loop 0 $all_pngs "$out"
    convert "$out" -delay 10 -coalesce -duplicate 1,-2-1 -quiet -layers OptimizePlus -loop 0 "$out"
}

all_operations="generate render gif"
operations="$operation"
if [ "$operation" == "all" ]; then
    operations="$all_operations"
fi

all_params="x-rot"
params="$param"
if [ "$param" == "all" ]; then
    params="$all_params"
fi

for param in $all_params; do
    mkdir -p "$(scad_dir $param)"
    mkdir -p "$(png_dir $param)"
done

for param in $params; do
    for operation in $operations; do
        case $operation in
            "generate")
                generate $param
                ;;
            "render")
                render $param
                ;;
            "gif")
                gif $param
                ;;
        esac
    done
done
