import sun.security.x509.PolicyInformation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author Logan Karstetter
 * Date: 2020
 */
public class SpriteUtil
{
    /** The list of supported image formats */
    private static final String[] supportedImageFormats = { ".png", ".jpg", ".jpeg" };

    /**
     * An enumeration to structure the naming of sprites embedded in a sprite sheet.
     * The supported structure is as follows: NAME.NUMBER.SRC_IMAGE_WIDTH.SRC_IMAGE_HEIGHT.
     */
    public enum SpriteInfo
    {
        /** The name of the sprite */
        NAME,
        /** The number of the sprite relative to the order it was cut from the source image */
        NUMBER,
        /** The width of the source image, in pixels, that this sprite was extract from */
        SRC_IMAGE_WIDTH,
        /** The height of the source image, in pixels, that this sprite was extract from */
        SRC_IMAGE_HEIGHT,
        /** The number of SpriteInfo fields */
        NUM_SPRITE_INFO
    }

    /**
     * Determine if the file image is supported by the SpriteSheetBuilder and
     * SpriteSheetParser tools. The following image file formats are supported:
     * png, jpg, and jpeg.
     * @param image The file image to check.
     * @return True if the image is supported, or false otherwise.
     */
    public static boolean isImageFileSupported( File image )
    {
        //Return immediately if the image file is null
        if( image == null )
        {
            return false;
        }

        //Determine if the image format is supported
        for( String fileFormat : supportedImageFormats )
        {
            if( image.getName().toLowerCase().endsWith( fileFormat ) )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Loads a single image from the given path, preserving any transparency.
     * @param imageURL The URL to the image to load, including the file extension, relative to the classpath.
     * @return A loaded BufferedImage, or null if the image could not be found.
     */
    public static BufferedImage loadImage( URL imageURL )
    {
        try
        {
            //Read in the image
            BufferedImage readImage = ImageIO.read( imageURL );

            //Create a copy of the image to ensure it becomes a managed image
            int transparency = readImage.getColorModel().getTransparency();
            BufferedImage copyImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage( readImage.getWidth(), readImage.getHeight(), transparency );

            //Create a graphics context to re-draw the image
            Graphics g2d = copyImage.createGraphics();
            g2d.drawImage( readImage, 0, 0, null );
            g2d.dispose();

            //Return the read image
            return readImage;
        }
        catch( IOException exception )
        {
            System.out.println( "Error loading file: " + imageURL.toString() );
            exception.printStackTrace();
        }
        catch( IllegalArgumentException exception )
        {
            System.out.println( "Unable to find file: " + imageURL.toString() );
            exception.printStackTrace();
        }

        return null;
    }
}
