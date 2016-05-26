package fourthyearp.cian.mapsforge_3;

import android.database.Cursor;
import android.util.Log;

/**
 * Created by cian on 27/04/2016.
 */
public class MapNode {

    private String id;
    private double lat;
    private double lon;

    public MapNode(String _id, double _lat, double _lon){
        id=_id;
        lat=_lat;
        lon=_lon;
    }
    public MapNode(String _id, String _lat, String _lon){
        id=_id;
        lat=Double.parseDouble(_lat);
        lon=Double.parseDouble(_lon);
    }

    public String getId(){  return id; }
    public double getLat(){ return lat;}
    public double getLon(){ return lon;}


}
