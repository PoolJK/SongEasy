#! /bin/bash
#  sips -Z 96 btn_options.png --out resized.png
# keep aspect ratio 
echo Cut image into pieces in subdirectories ...
file=$(basename $1)
path=$(dirname $1)
echo Datei: $file
echo Pfad: $path
# create three horizontal slices
convert $1  -crop 1x3@ +repage +adjoin $path/"$file"_tile_%d.png
# cut middle slice vertical in equal halfes
convert $path/"$file"_tile_1.png  -crop 2x1@ +repage +adjoin $path/"$file"_tile1_%d.png
echo done!