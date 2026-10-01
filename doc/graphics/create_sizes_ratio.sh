#! /bin/bash
#  sips -Z 96 btn_options.png --out resized.png
# keep aspect ratio 
echo Creating resized images in subdirectories ...
file=$(basename $1)
path=$(dirname $1)
echo Datei: $file
echo Pfad: $path
convert  $1 -background none -geometry x32 -density 120 $path/drawable-ldpi/$file
convert  $1 -background none -geometry x48 -density 160 $path/drawable-mdpi/$file
convert  $1 -background none -geometry x72 -density 240 $path/drawable-hdpi/$file
convert  $1 -background none -geometry x96 -density 320 $path/drawable-xhdpi/$file
convert  $1 -background none -geometry x144 -density 490 $path/drawable-xxhdpi/$file
convert  $1 -background none -geometry x192 -density 640 $path/drawable-xxxhdpi/$file
echo done!
echo Copying files to resources ...
cp $path/drawable-ldpi/$file ../../app/src/main/res/drawable-ldpi
cp $path/drawable-mdpi/$file ../../app/src/main/res/drawable-mdpi
cp $path/drawable-hdpi/$file ../../app/src/main/res/drawable-hdpi
cp $path/drawable-xhdpi/$file ../../app/src/main/res/drawable-xhdpi
cp $path/drawable-xxhdpi/$file ../../app/src/main/res/drawable-xxhdpi
cp $path/drawable-xxxhdpi/$file ../../app/src/main/res/drawable-xxxhdpi
echo done!