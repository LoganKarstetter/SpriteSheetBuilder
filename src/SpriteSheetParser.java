import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;

/**
 * @author Logan Karstetter
 * Date: 2020
 */
public class SpriteSheetParser
{
    /** The width and height of the parsed sprites in pixels */
    private int spriteSize;
    /** The height of the sprite header in pixels */
    private byte spriteHeaderSize;
    /** The color populating empty, excess space in the sprite sheet */
    private Color controlColor;
    /** The map used to store parsed sprites, where the keys are the sprite names */
    private LinkedHashMap<String, BufferedImage> spriteMap;

    /**
     * The SpriteSheetParser class is used to parse existing sprite sheets into individual sprites.
     * @param spriteSize The width and height of each sprite, in pixels, to be parsed.
     * @param spriteHeaderSize The height of the sprite header in pixels.
     * @param controlColor The color used to denote excess, empty, ignorable space in the sprite sheet.
     */
    public SpriteSheetParser(int spriteSize, byte spriteHeaderSize, Color controlColor )
    {
        //Store the sprite input arguments
        this.spriteSize       = spriteSize;
        this.spriteHeaderSize = spriteHeaderSize;
        this.controlColor     = controlColor;

        //Initialize the sprite map
        spriteMap = new LinkedHashMap<>();
    }

    /**
     * Parses the given sprite sheet into individual sprite images. If a destination
     * directory is provided, the sprites are output into the given folder as .png files.
     * @param spriteSheetPath The path to the sprite sheet relative to relative to src.
     * @param destinationDirectory The output directory relative to src, or null if output is not desired.
     */
    public void parseSpriteSheet( String spriteSheetPath, String destinationDirectory )
    {
        //Create a sprite sheet file and verify the image format is supported
        File spriteSheetFile = new File( spriteSheetPath );

        if( !SpriteUtil.isImageFileSupported( spriteSheetFile ) )
        {
            System.out.println( "Unable to load: " + spriteSheetPath + ". Files must be one of the following formats: .png, .jpg, or .jpeg." );
            return;
        }

        //Inform the user the sprite sheeting is being parsed
        System.out.println( "Parsing: " + spriteSheetPath );

        //Load the sprite sheet from the given path
        BufferedImage spriteSheet = null;
        try
        {
            spriteSheet = SpriteUtil.loadImage( spriteSheetFile.toURI().toURL() );
        }
        catch( MalformedURLException exception )
        {
            System.out.println( "Error malformed URL for: " + spriteSheetPath );
            exception.printStackTrace();
        }

        //Verify the sprite sheet is not null
        if( spriteSheet == null )
        {
            return;
        }

        //Determine how many sprites wide and high the sprite sheet is
        int numColumns = ( spriteSheet.getWidth()  / ( spriteSize ) );
        int numRows    = ( spriteSheet.getHeight() / ( spriteSize + spriteHeaderSize ) );

        //Define variables to store sprite sheet area coordinates
        int sourceX1 = 0;
        int sourceX2 = 0;
        int sourceY1 = 0;
        int sourceY2 = 0;

        //Split the sprite sheet into individual sprites
        for( int row = 0; row < numRows; row++ )
        {
            for( int column = 0; column < numColumns; column++ )
            {
                //Create a new sprite image
                BufferedImage sprite = new BufferedImage( spriteSize, spriteSize + spriteHeaderSize, BufferedImage.TYPE_INT_ARGB );

                //Calculate the area to cut from the sprite sheet
                sourceX1 = ( column   * spriteSize );
                sourceX2 = ( sourceX1 + spriteSize );
                sourceY1 = ( row      * ( spriteSize + spriteHeaderSize ) );
                sourceY2 = ( sourceY1 + ( spriteSize + spriteHeaderSize ) );

                //Copy the sprite out of the sprite sheet, extract the sprite name
                sprite.getGraphics().drawImage( spriteSheet, 0, 0, sprite.getWidth(), sprite.getHeight(), sourceX1, sourceY1, sourceX2, sourceY2, null );
                String spriteName = decryptImageNameHeader( sprite );

                //If the sprite name doesn't exist, the copied area is control color filler, which should be ignored
                if( spriteName.isEmpty() )
                {
                    continue;
                }

                //Trim away any possible control color filler surrounding the sprite
                sprite = trimSpriteImage( sprite );

                //If the sprite is null, but the previous name check succeeded, the sprite is malformed
                if( sprite == null )
                {
                    System.out.println( "Error parsing malformed sprite: " + spriteName );
                    continue;
                }

                //Extract the literal sprite name, number, and dimensions of the original image from the sprite name variable
                //The sprite name must be formatted as follows: NAME.NUMBER.SRC_IMAGE_WIDTH.SRC_IMAGE_HEIGHT
                String[] spriteInfo = spriteName.split( "\\." );
                if( spriteInfo.length != SpriteUtil.SpriteInfo.NUM_SPRITE_INFO.ordinal() )
                {
                    System.out.println( "Error parsing mis-formatted sprite name: " + spriteName );
                    continue;
                }

                try
                {
                    //Get the name of the sprite source image
                    String srcSpriteName = spriteInfo[ SpriteUtil.SpriteInfo.NAME.ordinal() ];

                    //Convert the numeric components of the sprite name into integer values
                    int srcSpriteNumber  = Integer.parseInt( spriteInfo[ SpriteUtil.SpriteInfo.NUMBER.ordinal()           ].trim() );
                    int srcImageWidth    = Integer.parseInt( spriteInfo[ SpriteUtil.SpriteInfo.SRC_IMAGE_WIDTH.ordinal()  ].trim() );
                    int srcImageHeight   = Integer.parseInt( spriteInfo[ SpriteUtil.SpriteInfo.SRC_IMAGE_HEIGHT.ordinal() ].trim() );

                    //If the sprite is not already in the sprite map, add it with a new blank image
                    if( !spriteMap.containsKey( srcSpriteName ) )
                    {
                        spriteMap.put( srcSpriteName, new BufferedImage( srcImageWidth, srcImageHeight, BufferedImage.TYPE_INT_ARGB ) );
                    }

                    //Retrieve the sprite image under construction
                    BufferedImage spriteImage = spriteMap.get( srcSpriteName );

                    //Determine the column and row, with respect to sprite size, to draw the sprite into the image
                    int spriteColumn = srcSpriteNumber % ( int )Math.ceil( ( double )srcImageWidth / spriteSize );
                    int spriteRow    = srcSpriteNumber / ( int )Math.ceil( ( double )srcImageWidth / spriteSize );

                    //Draw the sprite into the image
                    spriteImage.getGraphics().drawImage( sprite, ( spriteColumn * spriteSize ), ( spriteRow * spriteSize ), ( ( spriteColumn * spriteSize ) + sprite.getWidth() ), ( ( spriteRow * spriteSize ) + sprite.getHeight() ), 0, 0, sprite.getWidth(), sprite.getHeight(), null );
                }
                catch ( NumberFormatException exception )
                {
                    System.out.println( "Error converting sprite info: " + spriteName );
                    exception.printStackTrace();
                    continue;
                }
            }
        }

        //If the destination directory is not null, output the sprite map images as .png files
        if( destinationDirectory != null )
        {
            //Inform the user the sprite sheeting is being parsed
            System.out.println( "Output to directory: " + destinationDirectory );

            //Inform the user if no sprites were parsed
            if( spriteMap.keySet().isEmpty() )
            {
                System.out.println( "No images could extracted from: " + spriteSheetPath );
            }

            for ( String spriteName : spriteMap.keySet() )
            {
                try
                {
                    //Write the sprite sheet to a .png file, don't use jpeg because it has lossy compression
                    ImageIO.write( spriteMap.get( spriteName ), "png", new File( destinationDirectory + "/" + spriteName + ".png" ) );
                }
                catch( IOException exception )
                {
                    System.out.println( "Error writing file: " + spriteName + ".png" );
                    exception.printStackTrace();
                }
                catch( IllegalArgumentException exception )
                {
                    System.out.println( "Unable to write to: " + destinationDirectory + "/" + spriteName + ".png" );
                    exception.printStackTrace();
                }
            }
        }
    }

    /**
     * Extract the red, green, and blue color values from the header pixels
     * and convert them to characters to determine the sprite name.
     * @param sprite The sprite image to extract from.
     * @return The name of the sprite.
     */
    private String decryptImageNameHeader( BufferedImage sprite )
    {
        //Create a StringBuilder to assemble the sprite name
        StringBuilder imageName = new StringBuilder();

        //Iterate until a pixel with the control color is found, or the max sprite width is reached
        for( int x = 0; ( x < spriteSize ) && ( controlColor.getRGB() != sprite.getRGB(x, 0 ) ); x++ )
        {
            Color pixelRGB = new Color( sprite.getRGB(x, 0 ) );
            imageName.append( ( char )pixelRGB.getRed()   );
            imageName.append( ( char )pixelRGB.getGreen() );
            imageName.append( ( char )pixelRGB.getBlue()  );
        }

        return imageName.toString();
    }

    /**
     * Strip away the header and any excess space denoted by the presence of the control color
     * from the sprite image. It should be assumed that the header is still present.
     * @param sprite The sprite image to be trimmed.
     * @return A sub-image of the original sprite, returns null if the image is all control color.
     */
    private BufferedImage trimSpriteImage( BufferedImage sprite )
    {
        //Trim any excess space denoted by the presence of the CTRL_COLOR
        int newWidth  = 0;
        int newHeight = spriteHeaderSize;

        while( ( newWidth < spriteSize ) && ( sprite.getRGB( newWidth, spriteHeaderSize ) != controlColor.getRGB() ) )
        {
            newWidth++;
        }
        while( ( newHeight < ( spriteSize + spriteHeaderSize ) ) && ( sprite.getRGB( 0, newHeight ) != controlColor.getRGB() ) )
        {
            newHeight++;
        }

        //If the new height or width is zero, the image is entirely the control color
        if( ( newWidth == 0 ) || ( ( newHeight - spriteHeaderSize ) == 0 ) )
        {
            return null;
        }

        //Replace the sprite with a sub-image containing no empty space
        sprite = sprite.getSubimage(0, spriteHeaderSize, newWidth, newHeight - spriteHeaderSize );

        return sprite;
    }
}
