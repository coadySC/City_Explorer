package fourthyearp.cian.mapsforge_3;

/**
 * Created by cian on 28/04/2016.
 */
public final class Constants {

    // database
    public static final String DATABASE_NAME = "nodes_db";
    public static final String DATABASE_TABLE = "nodes_table";
    public static final int DATABASE_VERSION = 2;
    public static final String NODE_ID = "_id";
    public static final String NODE_LATITUDE = "lat";
    public static final String NODE_LONGITUDE = "lon";

    public static final String BULK_LOAD_SQL = "INSERT INTO "+ Constants.DATABASE_TABLE+" VALUES(?,?,?);";;

    // area limit (from location)
    public static final double LAT_DIST_LIMIT = 0.0208;
    public static final double LON_DIST_LIMIT = 0.0341;

    // messages
    public static final String WELCOME_MSG =
            "Welcome to City Explorer \n" +
            "The walking application to expand your path.\n\n" +
            "For information about the application press the i on the map";
    public static final String INFORMATION_MSG =
            "Information - How to use this app.\n" +
            "Press the 'Go' button to create a route to walk. Your location will be represented as a marker\n\n" +
            "Press the arrows to increase/decrease your desired route length.\n" +
            "Reducing distance or time to zero will fetch you another route.\n" +
            "Tap the distance(between - and +) to alternate between distance and route duration.\n" +
            "To zoom in and out, touch the buttons on the bottom right of map.";
    public static final String NEUTRAL_MSG = "OK";

    public static final String NO_LOCATION_MSG = "Make sure location is turned on";
    public static final String STILL_CALCULATING_MSG = "Still calculating route";

}
