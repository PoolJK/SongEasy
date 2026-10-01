#! /bin/bash
#  sips -Z 96 btn_options.png --out resized.png
# create smaller sizes for image buttons instead of toggle buttons
echo Creating resized images in subdirectories ...
file=$(basename $1)
path=$(dirname $1)
echo Datei: $file
echo Pfad: $path
convert  $1 -resize 24 -units PixelsPerInch -density 120 $path/drawable-ldpi/$file
convert  $1 -resize 32 -units PixelsPerInch -density 160 $path/drawable-mdpi/$file
convert  $1 -resize 48 -units PixelsPerInch -density 240 $path/drawable-hdpi/$file
convert  $1 -resize 64 -units PixelsPerInch -density 320 $path/drawable-xhdpi/$file
convert  $1 -resize 96 -units PixelsPerInch -density 490 $path/drawable-xxhdpi/$file
convert  $1 -resize 144 -units PixelsPerInch -density 640 $path/drawable-xxxhdpi/$file
echo done!
echo Copying files to resources ...
cp $path/drawable-ldpi/$file ../../app/src/main/res/drawable-ldpi
cp $path/drawable-mdpi/$file ../../app/src/main/res/drawable-mdpi
cp $path/drawable-hdpi/$file ../../app/src/main/res/drawable-hdpi
cp $path/drawable-xhdpi/$file ../../app/src/main/res/drawable-xhdpi
cp $path/drawable-xxhdpi/$file ../../app/src/main/res/drawable-xxhdpi
cp $path/drawable-xxxhdpi/$file ../../app/src/main/res/drawable-xxxhdpi
echo done!
