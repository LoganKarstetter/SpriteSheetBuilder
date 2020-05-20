# SpriteSheetBuilder
A sprite sheet building and parsing tool I created in May of 2020 to assist in making games. When building sprite sheets, the tool loads all the images in a given source directory. The loaded images are then split (if necessary) into "sprite sized" images, the size of which is an command line argument. The intent of this behavior is to avoid large gaps in empty space in the sprite sheet as large sprites tend to make efficient organization difficult. Whether the source images are split or not, the resulting sprite(s) are given a header row of pixels which contains the source image name and metadata embedded into the RGB colors. Any empty space in the sprite sheet is filled with an ugly purple "control color" to make it obvious what is and isn't a sprite. When parsing built sprite sheets, the tool loads the sprite sheet and cuts out each individual sprite. The header of each sprite is parsed and the resulting data is used to rebuild the source images. I think it is pretty neat.

How to run from the command line:
- Download the SpriteSheetBuilder.jar file found in this folder: "SpriteSheetBuilder/out/artifacts/SpriteSheetBuilder_jar".
- Run "java -jar SpriteSheetBuilder -arguments_go_here".

Here's the command line help text:
SpriteSheetBuilder - Created by Logan Karstetter

General Information and Tips:
        - The source and destination directory arguments are required to be wrapped in quotes " ".
        - The builder and parser only support the following image file formats: '.png', '.jpg', '.jpeg'.
        - The builder sweeps the entire source directory for files (non-recursive), don't have any undesired images in there.
        - The builder and parser are not made to handle building and parsing sprite sheets within sprite sheets. That won't work, so don't do it.
        - The parser can only parse sheets built by this tool, the spriteSize argument must match the size used to build (_# at end of built file name).

Command Line Options:
        -b spriteSize "sourceDirectory" "destinationDirectory" 'Builds a sprite sheet with the images found in the source directory. Outputs to the destination directory.'
        -p spriteSize "spriteSheetPath" "destinationDirectory" 'Parses a sprite sheet into individual sprites. Outputs the images in the destination directory.'
        -h 'Prints this help text.'
        
If anyone ever uses the tool and has questions let me know (like that will ever happen).

Logan
