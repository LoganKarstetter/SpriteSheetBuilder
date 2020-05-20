import java.awt.*;

/**
 * @author Logan Karstetter
 * Date: 2020
 */
public class SpriteSheetManager
{
    /** The default width and height of each sprite in pixels */
    private static final byte DEFAULT_SPRITE_SIZE = 30;
    /** The height of the sprite header in pixels */
    private static final byte SPRITE_HEADER = 1;
    /** The ugly control color used to fill empty space in the sprite sheet background */
    private static final Color CTRL_COLOR = new Color( 239, 11, 244, 255 );

    /** The object used to assemble sprite sheets from source images */
    private SpriteSheetBuilder builder;
    /** The object used to parse sprite sheets back into their original images */
    private SpriteSheetParser parser;

    /**
     * The SpriteSheetManager class is used to build and parse sprite sheets with
     * respect to a provided sprite size variable used to determine the width and
     * height of each sprite.
     * @param spriteSize The desired positive, non-zero sprite width and height.
     */
    public SpriteSheetManager( int spriteSize )
    {
        //Validate and store the sprite size
        if( spriteSize <= 0 )
        {
            System.out.println( "Invalid provided sprite size: " + spriteSize + ".\nDefaulting to sprite size: " + DEFAULT_SPRITE_SIZE );
        }
        int filteredSpriteSize = ( spriteSize <= 0 ) ? DEFAULT_SPRITE_SIZE : spriteSize;

        //Initialize the sprite sheet builder and parser
        builder = new SpriteSheetBuilder( filteredSpriteSize, SPRITE_HEADER, CTRL_COLOR );
        parser  = new SpriteSheetParser(  filteredSpriteSize, SPRITE_HEADER, CTRL_COLOR );
    }

    /**
     * Build a sprite sheet from images found in the given source directory
     * and output the result in the given destination directory.
     * @param sourceDirectory The path to the directory containing images.
     * @param destinationDirectory The path to the output directory.
     */
    public void buildSpriteSheet( String sourceDirectory, String destinationDirectory )
    {
        builder.buildSpriteSheet( sourceDirectory, destinationDirectory );
    }

    /**
     * Parse an existing sprite sheet into individual sprite images and optionally
     * output the sprite images to the given destination directory.
     * @param spriteSheetPath The path to the sprite sheet to parse.
     * @param destinationDirectory The path to the output directory or null if output is not desired.
     */
    public void parseSpriteSheet( String spriteSheetPath, String destinationDirectory )
    {
        parser.parseSpriteSheet( spriteSheetPath, destinationDirectory );
    }
}
