# Processing for Android Wiki
http://wiki.processing.org/w/Android

# Develop Sketch Files
Open ManetSketch.pde in the Processing for Android IDE (processing-2.0b7).
Switch to Android mode by clicking "Java" box on right and selecting "Android".

# Run App through Processing IDE
In Processing IDE, click either:
Sketch -> Run in Emulator
Sketch -> Run on Device

# Export Android Project Code
In Processing IDE, click:
File -> Export Android Project

This will need to be done each time the pde files are updated.

# Build Debug apk
cd <path>/android-manet-visualizer/ManetSketch/android
ant debug

# Build and Sign Release apk
cd <path>/android-manet-visualizer/ManetSketch/android
ant release
jarsigner -verbose -sigalg MD5withRSA -digestalg SHA1 -keystore /home/dev/Desktop/KEYSTORE/span.keystore ./bin/ManetSketch-release-unsigned.apk spankey

# zipalign Release
zipalign -v 4 ./bin/ManetSketch-release-unsigned.apk ./bin/ManetSketch.apk

# Verify Signature
jarsigner -verify ./bin/ManetSketch.apk


