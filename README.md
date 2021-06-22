# Lane Detector

Uses OpenCV to detect lane markers on the highway and highlight them.

https://user-images.githubusercontent.com/12748/122929438-f101fa00-d338-11eb-840f-8dfd92d3a22e.mp4

## Usage

    Options:
    --inputFilePath, -i [./samples/seattle.mp4] -> Path to input file { String }
    --outputFilePath, -o [./output.mov] -> Path to output file { String }
    --showLive, -s [false] -> Opens a window showing the live encoding
    --debug, -d [false] -> Debug
    --help, -h -> Usage info 

## Requirements

- OSX with Quicktime installed. Other platforms can probably make use of FFMPEG but you’ll need to swap out the codec to match the extension.