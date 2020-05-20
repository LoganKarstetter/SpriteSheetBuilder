/**
 * @author Logan Karstetter
 * Date: 2020
 */
public class Main
{
    /** The command line identifier for building a sprite sheet */
    private static final String buildArg = "-b";
    /** The command line identifier for printing the help text */
    private static final String helpArg  = "-h";
    /** The command line identifier for parsing a sprite sheet */
    private static final String parseArg = "-p";

    /**
     * The main function.
     * @param args The command line arguments used to build and parse sprites.
     */
    public static void main( String[] args )
    {
        //Read command line arguments
        if( args.length > 0 )
        {
            //Determine whether the desired action is building, parsing, or help
            switch( args[ 0 ] )
            {
                case buildArg: //Intentional fallthrough
                case parseArg:

                    //Inform the user if they put in invalid arguments
                    if( args.length != 4 )
                    {
                        System.out.println( "Script requires exactly 4 command line arguments, got " + args.length + ". Run with -h to see options.\nFound arguments:" );
                        for( String argument : args )
                        {
                            System.out.print( "\"" + argument + "\" " );
                        }
                        break;
                    }

                    try
                    {
                        //Create a SpriteSheetManager and build/parse
                        SpriteSheetManager spriteSheetManager = new SpriteSheetManager( Integer.parseInt( args[ 1 ] ) );

                        if( args[ 0 ].equalsIgnoreCase( buildArg ) )
                        {
                            spriteSheetManager.buildSpriteSheet( args[ 2 ], args[ 3 ] );
                        }
                        else
                        {
                            spriteSheetManager.parseSpriteSheet( args[ 2 ], args[ 3 ] );
                        }
                    }
                    catch( NumberFormatException exception )
                    {
                        System.out.println( "Invalid non-numeric sprite size: " + args[ 1 ] );
                    }
                    break;

                case helpArg:
                    System.out.println( "\nSpriteSheetBuilder - Created by Logan Karstetter\n"
                                      + "\nGeneral Information and Tips:"
                                      + "\n\t- The source and destination directory arguments are required to be wrapped in quotes \" \"."
                                      + "\n\t- The builder and parser only support the following image file formats: '.png', '.jpg', '.jpeg'."
                                      + "\n\t- The builder sweeps the entire source directory for files (non-recursive), don't have any undesired images in there."
                                      + "\n\t- The builder and parser are not made to handle building and parsing sprite sheets within sprite sheets. That won't work, so don't do it."
                                      + "\n\t- The parser can only parse sheets built by this tool, the spriteSize argument must match the size used to build (_# at end of built file name)."
                                      );

                    System.out.println( "\nCommand Line Options:"
                                      + "\n\t-b spriteSize \"sourceDirectory\" \"destinationDirectory\" 'Builds a sprite sheet with the images found in the source directory. Outputs to the destination directory.'"
                                      + "\n\t-p spriteSize \"spriteSheetPath\" \"destinationDirectory\" 'Parses a sprite sheet into individual sprites. Outputs the images in the destination directory.'"
                                      + "\n\t-h 'Prints this help text.'"
                                      );
                    break;

                default:
                    System.out.println( "Unexpected command line arguments. Run with -h to see options." );
                    break;
            }
        }
        else //No command line arguments, run as if in IDE
        {
            //Create a SpriteSheetManager
            SpriteSheetManager spriteSheetManager = new SpriteSheetManager( 30 );

            //Build a sprite sheet from the "Images" directory
            spriteSheetManager.buildSpriteSheet( "src/Images/", "src/Sprites/" );

            //Parse the sprite sheet and output the result into the "Sprites" directory
            spriteSheetManager.parseSpriteSheet( "src/Sprites/spritesheet_30.png", "src/Sprites/" );
        }
    }
}
