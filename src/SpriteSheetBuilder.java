import com.sun.org.apache.xpath.internal.functions.FuncFalse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Logan Karstetter
 * Date: 2020
 */
public class SpriteSheetBuilder
{
    /** The width and height of the parsed sprites in pixels */
    private int spriteSize;
    /** The height of the sprite header in pixels */
    private byte spriteHeaderSize;
    /** The color populating empty, excess space in the sprite sheet */
    private Color controlColor;
    /** The map used to store loaded images, where the keys are the image names */
    private LinkedHashMap<String, BufferedImage> imageMap;

    /**
     * The SpriteSheetBuilder class is used to build sprite sheets.
     * @param spriteSize The width and height of each sprite in pixels.
     * @param spriteHeaderSize The height of the sprite header in pixels.
     * @param controlColor The color used to denote excess, empty, ignorable space in the sprite sheet.
     */
    public SpriteSheetBuilder(int spriteSize, byte spriteHeaderSize, Color controlColor )
    {
        //Store the sprite input arguments
        this.spriteSize       = spriteSize;
        this.spriteHeaderSize = spriteHeaderSize;
        this.controlColor     = controlColor;

        //Initialize the image map
        imageMap = new LinkedHashMap<>();
    }

    /**
     * Build a sprite sheet from the images found in the given source directory.
     * Each image exceeding the size of a single sprite will be split into many
     * sprite-sized chunks. The result will be output in the given destination
     * directory as a .png file.
     * @param sourceDirectory The path to the directory containing images relative to src.
     * @param destinationDirectory The path to the output directory relative to src.
     */
    public void buildSpriteSheet( String sourceDirectory, String destinationDirectory )
    {
        //Load the images into the imageMap
        loadSourceImages( sourceDirectory );

        //If no images were loaded, return early
        if( imageMap.size() == 0 )
        {
            System.out.println( "No supported image files found in: " + sourceDirectory );
            return;
        }

        //Create an array list of the images
        ArrayList<BufferedImage> imageChunks = new ArrayList<>( imageMap.values() );

        //Determine the dimensions of the sprite sheet in image chunks
        int widthInChunks  = 0;
        int heightInChunks = 0;

        //If the square root is an integer, create a square sprite sheet
        double squareRootResult = Math.sqrt( imageMap.keySet().size() );
        if( Math.floor( squareRootResult ) == squareRootResult )
        {
            widthInChunks  = ( int )squareRootResult;
            heightInChunks = ( int )squareRootResult;
        }
        else
        {
            widthInChunks  = ( int )Math.floor( squareRootResult );
            heightInChunks = ( int )Math.ceil( ( double )imageChunks.size() / widthInChunks );
        }

        //Create the sprite sheet image, get the graphics object
        BufferedImage spriteSheet = new BufferedImage( ( widthInChunks * spriteSize ), ( heightInChunks * ( spriteSize + spriteHeaderSize ) ), BufferedImage.TYPE_INT_ARGB );
        Graphics spriteSheetGFX   = spriteSheet.getGraphics();

        //Fill in the background with the control color
        spriteSheetGFX.setColor( controlColor );
        spriteSheetGFX.fillRect(0, 0, spriteSheet.getWidth(), spriteSheet.getHeight() );

        //Add each image chunk to the sprite sheet
        Iterator<BufferedImage> iterator = imageChunks.iterator();
        for( int row = 0; ( row < heightInChunks ) && iterator.hasNext(); row++ )
        {
            for( int column = 0; ( column < widthInChunks ) && iterator.hasNext(); column++ )
            {
                spriteSheetGFX.drawImage( iterator.next(), ( column * spriteSize ), ( row * ( spriteSize + spriteHeaderSize ) ), null );
            }
        }


        try
        {
            //Write the sprite sheet to a .png file, don't use jpeg because it has lossy compression
            ImageIO.write( spriteSheet, "png", new File( destinationDirectory + "/spritesheet_" + spriteSize + ".png" ) );
        }
        catch( IOException exception )
        {
            System.out.println( "Error writing file: spritesheet_" + spriteSize + ".png" );
            exception.printStackTrace();
        }
        catch( IllegalArgumentException exception )
        {
            System.out.println( "Unable to write to: " + destinationDirectory + "/spritesheet_" + spriteSize + ".png" );
            exception.printStackTrace();
        }
    }

    /**
     * Encrypt the name of the given image into a row of header pixels immediately above the image.
     * @param image The image to encrypt the header pixels into.
     * @param imageName The name of the image to be encrypted.
     * @return The new image containing an extra row of pixels for the header.
     */
    private BufferedImage encryptImageNameHeader( BufferedImage image, String imageName )
    {
        //Determine the number of pixels required, four characters can be stored in a single pixel,
        //however including an alpha channel can cause the colors to shift, so we'll store only three
        int pixelsRequired = ( int )Math.ceil( imageName.length() / 3.0 );

        //Verify the image name will fit in the header
        if( pixelsRequired > spriteSize )
        {
            System.out.println( "Error encrypting: " + imageName + ", sprite size limitation, requires " + pixelsRequired + " pixels." );
            return null;
        }

        //Get the image's graphics for drawing
        Graphics imageGraphics = image.getGraphics();
        imageGraphics.setColor( controlColor );
        imageGraphics.fillRect(pixelsRequired, 0, spriteSize - pixelsRequired, 1 );

        //Create a character iterator for the image name, step the iterator back once
        CharacterIterator iterator = new StringCharacterIterator( imageName );

        //Draw pixels to the header with their RGBA values set to the bytes
        for( int x = 0; x < pixelsRequired; x++ )
        {
            int red   = ( iterator.current() != CharacterIterator.DONE ) ? ( int )iterator.current() : 0;
            int green = ( iterator.next()    != CharacterIterator.DONE ) ? ( int )iterator.current() : 0;
            int blue  = ( iterator.next()    != CharacterIterator.DONE ) ? ( int )iterator.current() : 0;

            //Create the new color with the byte values and draw a pixel
            imageGraphics.setColor( new Color( red, green, blue ) );
            imageGraphics.fillRect(x, 0, 1, 1 );

            //Move the iterator index forward for the red color next cycle
            iterator.next();
        }

        return image;
    }

    /**
     * Encrypt the image name into the least significant bits of the image starting at pixel ( 0, 0 ).
     * Write the bits of the image name left to right, if the string is too long an error is thrown
     * and the image is returned unchanged. It should be noted that this operation is non-reversible.
     * @param image The image to encrypt the string into.
     * @param imageName The string to be encrypted.
     * @return The image with the string encrypted, or just the image if the string name was too long.
     */
    private BufferedImage encryptImageNameLSB( BufferedImage image, String imageName )
    {
        //Verify the imageChunk is capable of storing enough bits
        if( imageName.length() > ( image.getWidth() * image.getHeight() ) )
        {
            System.out.println( "Error encrypting image name, size limitation: " + imageName );
            return null;
        }

        //Iterate over each byte in the string, create a string to hold the result
        String binaryString = "";
        for( byte bite : imageName.getBytes() )
        {
            //Convert each byte into a binary string of length 8
            String binary = Integer.toBinaryString( bite );
            while( binary.length() < 8 )
            {
                binary = "0".concat( binary );
            }

            //Concatenate the binary string
            binaryString = binaryString.concat( binary );
        }

        //Iterate over each pixel in the image, left to right
        int binaryIndex = 0;
        for( int y = 0; ( y < image.getHeight() ) && ( binaryIndex < binaryString.length() ); y++)
        {
            for( int x = 0; ( x < image.getWidth() ) && ( binaryIndex < binaryString.length() ); x++ )
            {
                int rgb = image.getRGB(x, y);

                //Set or clear the least significant bit to the value of the binary string index
                if( binaryString.substring(binaryIndex, binaryIndex + 1 ).equals( "1" ) )
                {
                    //       R G B A
                    rgb |= 0x00000100;
                }
                else
                {
                    //       R G B A
                    rgb &= 0xfffffeff;
                }

                //Set the RGB value of the pixel
                image.setRGB(x, y, rgb);
                binaryIndex++;
            }
        }

        return image;
    }

    /**
     * Loads images from the given directory and if necessary, splits them into sprite sized chunks,
     * before storing them in the image map used to build sprite sheets.
     * @param directory The image directory with respect to the class path.
     */
    private void loadSourceImages( String directory )
    {
        //Inform the user the sprite sheeting is being built
        System.out.println( "Building from directory: " + directory );

        //Get a set of image files from the directory
        File[] imageFiles = null;
        try
        {
            imageFiles = new File( directory ).listFiles();
        }
        catch( NullPointerException exception )
        {
            System.out.println( "Error image directory does not exist: " + directory );
            exception.printStackTrace();
        }

        //Print an error if no files are found
        if( imageFiles == null )
        {
            System.out.println( "No image files found at: " + directory );
        }
        else
        {
            //Load each image from the directory
            for( File image : imageFiles)
            {

                //Verify the image is a supported format
                if( SpriteUtil.isImageFileSupported( image ) )
                {
                    System.out.println( "Loading image: " + image.getName() );
                    try
                    {
                        BufferedImage loadedImage = SpriteUtil.loadImage( image.toURI().toURL() );

                        if( loadedImage != null )
                        {
                            //Strip the extension from the image name
                            String imageName = image.getName().substring( 0, image.getName().indexOf( '.' ) );

                            //Slice the image into sprite-sized chunks, and add the chunks to the imageMap
                            splitImage( loadedImage, imageName );
                        }
                    }
                    catch ( MalformedURLException exception )
                    {
                        System.out.println( "Error malformed URL for image: " + image.getName() );
                        exception.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Split the image into a series of sprite-sized image chunks. Each chunk is encrypted with the name
     * of the image plus NAME.NUMBER.SRC_IMAGE_WIDTH.SRC_IMAGE_HEIGHT.
     * @param image The image to split into chunks.
     * @param imageName The name of the image to split.
     */
    private void splitImage( BufferedImage image, String imageName )
    {
        //Determine how many sprite chunks are required for the source image, use mod to correct rounding in integer division
        int totalRows    = ( image.getHeight() / spriteSize ) + ( ( image.getHeight() % spriteSize ) == 0 ? 0 : 1 );
        int totalColumns = ( image.getWidth()  / spriteSize ) + ( ( image.getWidth()  % spriteSize ) == 0 ? 0 : 1 );

        //Track the image index
        int index = 0;

        //Split the image into sprite-sized chunks
        int sourceX1      = 0;
        int sourceY1      = 0;
        int sourceX2      = 0;
        int sourceY2      = 0;
        int destinationX2 = 0;
        int destinationY2 = 0;

        for( int row = 0; row < totalRows; row++ )
        {
            for( int column = 0; column < totalColumns; column++ )
            {
                //Create a new sized image, factor in the thickness of the header
                BufferedImage imageChunk = new BufferedImage( spriteSize, spriteSize + spriteHeaderSize, BufferedImage.TYPE_INT_ARGB );

                //Set and bounds check the source points
                sourceX1 = Math.min( image.getWidth(),  ( column * spriteSize ) );
                sourceY1 = Math.min( image.getHeight(), ( row    * spriteSize ) );
                sourceX2 = Math.min( image.getWidth(),  ( column * spriteSize ) + spriteSize );
                sourceY2 = Math.min( image.getHeight(), ( row    * spriteSize ) + spriteSize );

                //If the image isn't divisible by the sprite size and less pixels remain than the sprite size, only draw the remaining pixels
                destinationX2 = ( ( sourceX2 == image.getWidth()  ) && ( sourceX2 % spriteSize != 0 ) ) ? ( sourceX2 % spriteSize ) : spriteSize;
                destinationY2 = ( ( sourceY2 == image.getHeight() ) && ( sourceY2 % spriteSize != 0 ) ) ? ( sourceY2 % spriteSize ) : spriteSize;

                //Draw a chunk of the original image as a new image, leave the header empty
                imageChunk.getGraphics().drawImage( image, 0, spriteHeaderSize, destinationX2, destinationY2 + spriteHeaderSize, sourceX1, sourceY1, sourceX2, sourceY2, null );

                //Encrypt the name of the image into a header row of pixels, add .NAME.NUMBER.SRC_IMAGE_WIDTH.SRC_IMAGE_HEIGHT
                String imageChunkName = ( imageName + "." + index + "." + image.getWidth() + "." + image.getHeight() );
                imageChunk = encryptImageNameHeader( imageChunk, imageChunkName );

                //Add the image to the imageMap assuming it is not already present
                if( !imageMap.containsKey( imageName ) )
                {
                    imageMap.put( imageChunkName, imageChunk );
                }
                else
                {
                    //Inform the user of an error
                    System.out.println( "Error imageMap already contains: " + imageChunkName );
                }

                //Increment the index
                index++;
            }
        }
    }
}
