package fourthyearp.cian.mapsforge_3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cian on 28/04/2016.
 */
public class MyDBOpenHelper extends SQLiteOpenHelper{

    private boolean alreadyInserted;

    private int bulkIndex;

    private SQLiteStatement prep=null;
    private MapNode[]batch;
    private boolean isOpen = false;
    private String[]idLatLon = new String[]{Constants.NODE_ID, Constants.NODE_LATITUDE, Constants.NODE_LONGITUDE};

    private double latUpper;
    private double lonUpper;
    private double latLower;
    private double lonLower;

    private final double closestDistanceBoundary = 0.0005;

    private static MyDBOpenHelper instance = null;

    // The command which creates a database in SQLite.
    private static final String CREATE_DATABASE_TABLE =
            "CREATE TABLE " +
                    Constants.DATABASE_TABLE +
                    " (" +
                    Constants.NODE_ID + " INTEGER PRIMARY KEY ASC, " +// "text not null, "
                    Constants.NODE_LATITUDE + " TEXT, " +
                    Constants.NODE_LONGITUDE + " TEXT);" ;

//    private static final String CREATE_UNIQUE_INDEX =
//            " CREATE UNIQUE INDEX " +indexname+ " ON " +
//                    tblTest+"("+id+");";

    private static final String INSERT_OR_IGNORE_START = "INSERT OR IGNORE INTO "+
            Constants.DATABASE_TABLE + "(";


    private MyDBOpenHelper(Context c){
        super(c, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
        bulkIndex=0;
        batch = new MapNode[1000];
        alreadyInserted=false;
//        db.execSQL(CREATE_DATABASE_TABLE /*+CREATE_UNIQUE_INDEX*/);
//        prep = db.compileStatement(Constants.BULK_LOAD_SQL);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_DATABASE_TABLE);
        bulkIndex=0;
        batch = new MapNode[1000];
        prep = db.compileStatement(Constants.BULK_LOAD_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + Constants.DATABASE_TABLE); // removes table from database(deletes it)
        onCreate(db);
    }

    public boolean wasAlreadyBulkLoaded(){
        return alreadyInserted;
    }

    public void setBoundary(double _locationLat, double _locationLon){

        latUpper =_locationLat + Constants.LAT_DIST_LIMIT;
        lonUpper =_locationLon + Constants.LON_DIST_LIMIT;
        latLower =_locationLat - Constants.LAT_DIST_LIMIT;
        lonLower =_locationLon - Constants.LON_DIST_LIMIT;
    }

    public void insertNode(SQLiteDatabase db, String node_id, String lat, String lon) {

        alreadyInserted=true;

        // reduces map down to an area around location
        if(withinBounds(Double.parseDouble(lat), Double.parseDouble(lon))) {

            // (continue to) prepare SQLite statement
            batch[bulkIndex] = new MapNode(node_id, lat, lon);
            bulkIndex++;

            // After loading nodeBulk fully(or end of file)..
            // ..insert as batch
            if (bulkIndex == batch.length)
                insertNodeBulk(db);
        }

    }
    public void insertNodeBulk(SQLiteDatabase db){
        try {// prepare statement
         if(prep==null)
             prep = db.compileStatement(Constants.BULK_LOAD_SQL);
         db.beginTransaction();
         // insert in batches of 1000(maximum)
         for (int i = 0; i < bulkIndex; i++) { // have as <bulkIndex to allow for endOfFile batches
             prep.clearBindings();
             prep.bindString(1, batch[i].getId());
             prep.bindDouble(2, batch[i].getLat());
             prep.bindDouble(3, batch[i].getLon());
             prep.executeInsert();
         }
         db.setTransactionSuccessful();

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
            bulkIndex = 0;
        }
    }

    public Cursor returnEdgeCursor(SQLiteDatabase db, String node_idA, String node_idB/*, boolean keepOpen*/){//, String node_idB){

        bulkIndex++;

        return db.query(Constants.DATABASE_TABLE
                ,idLatLon
                , Constants.NODE_ID+"=? OR "+ Constants.NODE_ID+"=?"
                , new String[]{node_idA, node_idB}
                ,null,null,null);
    }



    // singleton helper
    public static synchronized MyDBOpenHelper getInstance(Context c){
        if(instance==null) {
            instance = new MyDBOpenHelper(c.getApplicationContext());
        }
        return instance;
    }

    public  static synchronized MyDBOpenHelper getInstance(){
        return instance;
    }


    public Cursor getClosestNodes(SQLiteDatabase db, double lat, double lon){

        Cursor c = null;

        for(int i=0; c==null || c.getCount()<1; i++){
            // using i as a multiplier to widen the search area
            c= db.query(Constants.DATABASE_TABLE
                    ,idLatLon
                    , Constants.NODE_LATITUDE+"<? AND "+ Constants.NODE_LONGITUDE+">? AND "+ Constants.NODE_LATITUDE+">? AND "+ Constants.NODE_LONGITUDE+"<?"
                    , new String[]{""+(lat+(i*closestDistanceBoundary)), ""+(lon-(i*closestDistanceBoundary)), // max boundary -> longitudes are in minus, so further minus them to be higher
                                    ""+(lat-(i*closestDistanceBoundary)), ""+(lon+(i*closestDistanceBoundary))}// min boundary -> longitudes are in minus, so add to them to be lower
                    ,null,null,null);
        }
        return c;
    }

    public int peekBulkIndex(){
        return bulkIndex;
    }

    private boolean withinBounds(double lat, double lon){
        // if between lat upper and lower, AND between lon upper and lower
        if(lat<latUpper && lat>latLower && lon<lonUpper && lon>lonLower)
            return true;
        return false;
    }

    public List<LatLong> returnRouteNodes(SQLiteDatabase db, List<String> routeIds){


        Log.i("here", "in retournRouteNodes");

        List<LatLong> route = new ArrayList<LatLong>();
        Cursor c=null;

        Log.i("In->db open helper","In Return nodes: route size"+ routeIds.size());

        // get one at a time also helps with it's returned order
        for (int i = 0; i < routeIds.size(); i++) {
            c = db.query(Constants.DATABASE_TABLE
                    , new String[]{Constants.NODE_LATITUDE, Constants.NODE_LONGITUDE}
                    , Constants.NODE_ID + "=?"
                    , new String[]{routeIds.get(i).toString()}
                    , null, null, null);

            c.moveToFirst();
            route.add(new LatLong(c.getDouble(0), c.getDouble(1)));
        }
        if(c!=null)
            c.close();

        Log.i("here", ""+ route);

        return route;

    }

}
