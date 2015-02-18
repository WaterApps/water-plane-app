water-plane-app
===============

This app is part of the Watershed Management Apps Center (WMAC) open source project at http://waterapps.org.
The purpose of this app is to help you visualize ground elevations while surveying an area.
Using freely-available, high-resolution elevation data, you can load an elevation file
(known as a "DEM": digital elevation map) from http://www.opentopography.org into the app by placing
it on your device's SD card. You will then see the elevation map overlaid on a Google map. A slider
at the bottom lets you raise and lower an imaginary level plane of water up and down through the terrain.
Areas lower than the water will be shaded, and areas higher than the water will be visible on the map.
Swipe the slider all the way to the right and to the left to see the highest and lowest places on a map.
Drop a marker and walk around and it will show you the elevation difference between your
location and the markers.

IMPORTANT NOTE:
For this app to run properly, you must have a Google Maps API key that matches your debug keystore.
Instructions on how to obtain one and place it in the application manifest are here:
https://developers.google.com/maps/documentation/android/start#the_google_maps_api_key
Without the correct API key, the map screen will be grayed out.