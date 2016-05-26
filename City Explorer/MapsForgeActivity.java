package fourthyearp.cian.mapsforge_3;

import android.app.Activity;
//import android.graphics.Paint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
//import android.graphics.Bitmap;

//import com.kitfox.svg.Polyline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.layer.overlay.Polygon;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.rendertheme.InternalRenderTheme;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.xmlpull.v1.XmlPullParserException;

//import org.mapsforge.core.graphics.Bitmap;


public class MapsForgeActivity extends Activity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_maps_forge1);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_maps_forge1, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



    // name of the map file in the external storage
    private final String MAPFILE = "dublin.map";
    private final LatLong centered = new LatLong(53.346701, -6.260139);
    private  LatLong destination=null;
    private boolean waitingOnCurrentLocation=false;//true; //here change for demo

    private LatLong startingP = new LatLong(53.346701, -6.260139);
    Location location=null;
    private LatLong endP = startingP;//new LatLong(53.346701, -6.260139);

    private double minDistance=0.0;
    private double routeKmSetting =0.0;
    private boolean kmNotMinutes = true;
    private double previousDistanceSetting = -1.0;
    Button button_dist;
    Context context = this;

    Toast noLocationToast=null;
    Toast calculatingToast = null;
    AlertDialog dialogBox;

    private Marker locationMarker = null;

    private static WaysParser parser;
    Collection path;
    Object[]pathPoints;

    private MapView mapView;
    private TileCache tileCache;
    private TileRendererLayer tileRendererLayer;
    Polyline routeLine = null;
    Polyline boundingBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

//        Graph_Example g = new Graph_Example();
//        g.logStuff();


        AndroidGraphicFactory.createInstance(this.getApplication());

        this.mapView = new MapView(this);
        setContentView(R.layout.activity_maps_forge1);
        this.mapView = (MapView) findViewById(R.id.mapView);

        this.mapView.setClickable(true);
        this.mapView.getMapScaleBar().setVisible(true);
        this.mapView.setBuiltInZoomControls(true);
        this.mapView.getMapZoomControls().setZoomLevelMax((byte) 20);
        this.mapView.getMapZoomControls().setZoomLevelMin((byte) 10);

        // create a tile cache of suitable size
        this.tileCache = AndroidUtil.createTileCache(this, "mapcache",
                mapView.getModel().displayModel.getTileSize(), 1f,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor());

        button_dist = (Button)findViewById(R.id.button_dist_duration);


//        button_go = (Button)findViewById(R.id.button_go);
//        button_inc = (Button)findViewById(R.id.button_inc);
//        button_dec = (Button)findViewById(R.id.button_dec);
//        button_info = (Button)findViewById(R.id.button_info);



//        button_go = (Button)findViewById(R.id.button_go);
        assignSingleClickListener((Button) findViewById(R.id.button_go));

        welcomeMessage();



        Bitmap bitmap;

//        Marker marker = MapviewUtils.createMarker(this, R.drawable.marker_blue, latLong);

        // create bitmap
//        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(R.layout.);
//        bitmap = AndroidGraphicFactory.convertToBitmap(
//                getResources().getDrawable(R.drawable., null))
//        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(
//                getResources().getDrawable(R.drawable.marker_blue));
//        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.marker_blue);

//        Bitmap bitmap = AndroidGraphicFactory.convertToAndroidBitmap(
//                new BitmaBitmapDrawable//(R.drawable.marker_blue, null)
//        );



    }

    @Override
    protected void onResume(){
        super.onResume();

        // here. does nothing. still runs onStart when I go back into app

    }

    @Override
    protected void onStart() {
        super.onStart();

        // here. will I repopulate the graph each time too?   could get rid of memory leaking? same with bit map?
        // here. recreate bit map each time?   to counter it not rendering fully?

// check documentation , race condition?
        // could be incompatible emulator


        this.mapView.getModel().mapViewPosition.setCenter(centered);
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) 12);

        // tile renderer layer using internal render theme
        MapDataStore mapDataStore = new MapFile(getMapFile());
        this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore,
                this.mapView.getModel().mapViewPosition, false, true, AndroidGraphicFactory.INSTANCE);
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.OSMARENDER);

        // only once a layer is associated with a mapView the rendering starts
        this.mapView.getLayerManager().getLayers().add(tileRendererLayer);

        // Set up parser and location listener
        runTEST();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (mBitmap != null && !mBitmap.isRecycled()) {
//            mapView..recycle();
//            mBitmap = null;
//        }

        this.mapView.destroyAll();
    }

    private File getMapFile() {
        // finding file on sd card
//        File file = new File(Environment.getExternalStorageDirectory(), MAPFILE);
//        return file;
        return new File(Environment.getExternalStorageDirectory(), MAPFILE); // here -> put this in resources later in project (might save compiling and loading for now)
    }

    private Marker createMarker(LatLong p, int resource)
    {
        Drawable drawableSource = getResources().getDrawable(resource);
        Bitmap bitmap = AndroidGraphicFactory.convertToBitmap(drawableSource);
        return new Marker(p, bitmap, 0, -bitmap.getHeight() / 2);
    }

    private void drawPolyline(List<LatLong> coordinates) {
        // if line has not been set up..
        if (routeLine==null){
            // .. set up the stroke to be drawn
            routeLine = new Polyline((org.mapsforge.core.graphics.Paint) getPaintStroke(Color.BLUE, 24, 8),
                    AndroidGraphicFactory.INSTANCE);
            // draw route on screen -> changes with additions/deletions to routeLine
//            this.mapView.getLayerManager().getLayers().add(routeLine);
        }
        // if line already exists..
        else {
            // ..clear polyline from map..
            this.mapView.getLayerManager().getLayers().remove(routeLine);
            // .. by clearing routeLine of points
            routeLine.getLatLongs().clear();
        }
        // add new line co-ordinates into a drawable polyline
        routeLine.getLatLongs().addAll(coordinates);
        // draw route on screen -> changes with additions/deletions to routeLine
        this.mapView.getLayerManager().getLayers().add(routeLine);
    }

    private Paint getPaintStroke(int colour, int dashLength, int strokeWidth) {
        // set up paint stroke using colour, dash style, and line width
        Paint stroke = AndroidGraphicFactory.INSTANCE.createPaint();
        stroke.setStyle(Style.STROKE);
        stroke.setColor(colour);
        stroke.setDashPathEffect(new float[]{dashLength, 16});
        stroke.setStrokeWidth(strokeWidth);
        return stroke;
    }


    //_________________________________________________________________________________________
    //_______________________________                           _______________________________
    //_______________________________      TESTING METHODS      _______________________________
    //_________________________________________________________________________________________
    private void runTEST() {



        // Create the Server which links layers
        try {
            System.out.println("->In Activity: Creating parser");
            parser = new WaysParser(this);
        } catch (XmlPullParserException | IOException io) {
            io.printStackTrace();
        }
        // Create location listener
        setUpLocation();

        // testing wait for location after a screen long hold/touch/tap
//        setUpLongTouch();

        // testing creating marker
//        testMarkerPlacing();

        // testing line drawing
//        testPolyline();

        // testing line drawing for nearby random points
//        testRandomPolyline();

//        // testing distance calculation
//        System.out.println("\n\n\n\n=====>>=====>>=====>>"+
//        getRouteDistance()//;
//        +"<<======<<=======<<=======\n\n\n\n");

//        if(location==null) {
//            waitingOnCurrentLocation = true;
//        }
//        else{

//        _route = parser.TESTreturnRoute();
//        System.out.println(">>>>>>>>>>>>>Route: "+route);
        //here here here left off here


        /*testShortestPath();
        Log.i("here.", "1-calling twice");
        TESTdrawRoute();
        minDistance = parser.getMinPathLength();
        Log.i("min distance",""+minDistance);
        minDistance = (minDistance - (minDistance%500))/1000;
        routeKmSetting = ((int)minDistance)-1;
        changeButtonText();*/

//        parser.TESTreturnRoute("658429", "389365", 3);
//        TESTdrawRoute();


//        }

        // testing shortest path drawing
//        testShortestPath();

        // print out path for testing, to see it.
//        Collection c = parser.AToB(/*pointA, pointB*/);s
//        System.out.println("amount of nodes travelled through___>___>___>"+c.size()+
//                            "Travelled through :::\n"+ c);

        // testing map tap/short-click
//        testTapDestination();   // --->  removes other touch options and overwhelms memory with listener.
//                                //  -->   Smaller file? wouldn't take filtered version I think..(keep author info?) does even load? tile, yes, memory increased when zoomed in.



    }

    private void testMarkerPlacing(){

//        if(location==null){
//
//            waitingOnCurrentLocation = true;
//        }
//        else if(waitingOnCurrentLocation){
//        Marker m = createMarker(new LatLong(53.346701, -6.260139), R.drawable.marker_blue);
//        mapView.getLayerManager().getLayers().add(m);
//        mapView.getLayerManager().getLayers().remove(m);
//        mapView.getLayerManager().getLayers().add(createMarker(new LatLong(53.346701, -6.260139), R.drawable.marker_blue));
        if(locationMarker != null)
            mapView.getLayerManager().getLayers().remove(locationMarker);
        locationMarker = createMarker(new LatLong(location.getLatitude(), location.getLongitude()), R.drawable.marker_blue);
        mapView.getLayerManager().getLayers().add(locationMarker);
//        }
    }

//    private void testPolyline(){//createPolyline(points);
//        points.add(new LatLong(53.346701, -6.260139));
//        points.add(new LatLong(53.346720, -6.260139));
//        points.add(new LatLong(53.346720, -6.260160));
//        points.add(new LatLong(53.346789, -6.260160));
//        points.add(new LatLong(53.346789, -6.260100));
//        points.add(new LatLong(53.346701, -6.260139));
//        createPolyline(points); // What?   worked strangely when added 2nd line for a while....nm follow only have to call once. can call before adding points to line even.
//    }
    private void drawBoundary(/*LatLong centre*/){
//        if(location==null)
//            waitingOnCurrentLocation=true;
//        else{
        /*points.add(new LatLong(centre.latitude+Constants.LAT_DIST_LIMIT, centre.longitude+Constants.LON_DIST_LIMIT));
        points.add(new LatLong(centre.latitude+Constants.LAT_DIST_LIMIT, centre.longitude-Constants.LON_DIST_LIMIT));
        points.add(new LatLong(centre.latitude-Constants.LAT_DIST_LIMIT, centre.longitude-Constants.LON_DIST_LIMIT));
        points.add(new LatLong(centre.latitude-Constants.LAT_DIST_LIMIT, centre.longitude+Constants.LON_DIST_LIMIT));
        points.add(new LatLong(centre.latitude+Constants.LAT_DIST_LIMIT, centre.longitude+Constants.LON_DIST_LIMIT));*/

        // if bounding box has not been created..
        if(boundingBox==null) {
            // ..create a boundary around a center point
            ArrayList<LatLong> boundaryPoints = new ArrayList<LatLong>();
            boundaryPoints.add(new LatLong(location.getLatitude() + Constants.LAT_DIST_LIMIT, location.getLongitude() + Constants.LON_DIST_LIMIT));
            boundaryPoints.add(new LatLong(location.getLatitude() + Constants.LAT_DIST_LIMIT, location.getLongitude() - Constants.LON_DIST_LIMIT));
            boundaryPoints.add(new LatLong(location.getLatitude() - Constants.LAT_DIST_LIMIT, location.getLongitude() - Constants.LON_DIST_LIMIT));
            boundaryPoints.add(new LatLong(location.getLatitude() - Constants.LAT_DIST_LIMIT, location.getLongitude() + Constants.LON_DIST_LIMIT));
            boundaryPoints.add(new LatLong(location.getLatitude() + Constants.LAT_DIST_LIMIT, location.getLongitude() + Constants.LON_DIST_LIMIT));

            // .. set up the stroke to be drawn
            boundingBox = new Polyline((org.mapsforge.core.graphics.Paint) getPaintStroke(Color.RED, 16, 4),
                    AndroidGraphicFactory.INSTANCE);
            // ..add boundary points to a drawable polyline
            boundingBox.getLatLongs().addAll(boundaryPoints);
        }
        // draw line on screen
        this.mapView.getLayerManager().getLayers().add(boundingBox);
    }

    private void removeBoundary(){
        Log.i("bounding box null?",""+boundingBox);
        if(boundingBox!=null)
            this.mapView.getLayerManager().getLayers().remove(boundingBox);
    }

//    private void testRandomPolyline(){
//        points.clear();
//        points.add(centered);
//        for(int i=0; i<6; i++)
//            randPointInRelation(points.get(i));
//        // WTF this worked several time before I added the below line...didn't call createPolyline and it worked??? then just stopped working. even after undo.
//        createPolyline(points);
//
//
//
//            // calling for a (possibly overlapping 'route')
////            RandPointRecursive(centered, 1);
//
//            // adding here also adds this point again at end for a closed loop/circuit
////            //wrong points.add(     new LatLong(RandPointRecursive(centered, 5)     );
//    }

//    private void randPointInRelation(LatLong previous){
//        points.add( new LatLong(previous.latitude  +((Math.random()*0.02)-0.01)   ,
//                                previous.longitude +((Math.random()*0.02)-0.01)   ));
//    }

//    private void RandPointRecursive(LatLong prev, int loops){
//        points.add(prev);
//
//        if(loops>0)
//            RandPointRecursive(new LatLong(
//                    prev.latitude+(Math.random()*200)-100,
//                    prev.longitude+(Math.random()*200)-100),
//                    loops--);
//    }

    //_________________________________________________________________________________________
    //_______________________________                           _______________________________
    //_______________________________DISTANCE excluding altitude_______________________________
    //_________________________________________________________________________________________

    /*
    perhaps will use the simplest distance formula. Not taking into account curvature or altitude. Using a 'flat earth'.
    Especially if the distance will have to be gotten extremely often. Below is an example, scroll down inside.
    /https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
    */


//    private double distanceAsCrow(LatLong point1, LatLong point2){
//        final double R = 6371;//or else 6368 OR 6378//km// for a large number of calculations, use float? =>uses less memory but is less accurate
//                                // http://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
//
//        //Radians = degrees * Pi/180    = degrees * Math.PI/180     = degrees * 0.0174532925199433(adjust for precision)
//        final double Deg_2_Rad = 0.0174532925199433;
//
//        double lat1 = point1.latitude;
//        double long1 = point1.longitude;
//        double lat2 = point2.latitude;
//        double long2 = point2.longitude;
//        double distLats = (lat2 - lat1) * Deg_2_Rad;
//        double distLongs = (long2 - long1) * Deg_2_Rad;
//
////        double a = Math.pow(Math.sin())
//        double a =  Math.pow(Math.sin(distLats), 2) +
//                    Math.pow(Math.sin(distLongs), 2) *
//                    Math.cos(lat1) * Math.cos(lat2);
//
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//
//        // return R(km) * c-formula
//        return /*6371 **/ c;
//        // return just c if multiplying whole route by R afterwards
//
//    }



//    private double getRouteDistance(){
//        double c = 0;
//
//        if(points.size()>1)
//            for(int i=0; i<points.size()-1; i++)
//                c= c + distanceAsCrow(points.get(i), points.get(i+1));
//        return 6371 *c; // careful to not multiply by 6371 twice
//    }


    //__________________________________________________________________________________________
    //_______________________________                            _______________________________
    //_______________________________    tap for co-ordinates    _______________________________
    //__________________________________________________________________________________________





//    private void testTapDestination(){
////        mapView.//mapView.onClick();//.getLayerManager().getLayers().;
//
//        mapView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                // set Destination
////                view.getLocationInWindow();
//
////                destination = new LatLong(motionEvent.getX(),motionEvent.getY());
//
////                calculateRoute(new LatLong(motionEvent.getX(), motionEvent.getY()));
//
//
//                return true;
//            }
//        });
//
////        MotionEvent mEvent = new view.MotionEvent();
//
////        MotionEventCompat
//    }


    /*@Override // Gets Latitude and Longitude of touched location.
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent object holds X-Y values
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            startingP=endP;
            endP = new LatLong(event.getX(), event.getY());
            // HERE    do I need to clean these up? or are they automatically destroyed when unreferenced?
            System.out.println("-_-_-_-_-_-_"+endP);
        }

        return super.onTouchEvent(event);
    }*/


    //__________________________________________________________________________________________
    //_______________________________                            _______________________________
    //_______________________________     route calculations     _______________________________
    //__________________________________________________________________________________________



//    private void setListener(){
//        EventListener.class. this.mapView;
//        }
//
//    }

//    public boolean onTap(){
//        return true;
//    }


//    public void testShortestPath() {
//
//        Log.i("here.", "3-calling twice");
//        if (location == null) {
//            waitingOnCurrentLocation = true;
//            // here. do a toast about turning location on
//
//        }
//        else {
////            path = parser.AToB();
////            Log.i("path:", path + "");
//
//            LatLong here = new LatLong(location.getLatitude(), location.getLongitude());
//
//            path = parser.AToB(here, new LatLong(53.3395434, -6.2571601));
//            pathPoints = path.toArray();
//            System.out.println("Collection is:"+path);
//            System.out.println("Path is:" + pathPoints);
//
//
//        /*
//        // here should I clear point/bitmap/something of previous points/lines???    (remove path from map)
//        for(int i=0; path!=null && i<path.size(); i++)
//            points.add(new LatLong(pathPoints.A, pathPoints.B));
//        points.add(new LatLong(53.346701, -6.260139));
//        points.add(new LatLong(53.346720, -6.260139));
//        points.add(new LatLong(53.346720, -6.260160));
//        points.add(new LatLong(53.346789, -6.260160));
//        points.add(new LatLong(53.346789, -6.260100));
//        points.add(new LatLong(53.346701, -6.260139));
//        createPolyline(points);*/
//        }
//
//    }



    //*********************************************************
    //*********************************************************
    //*********************************************************
    //*********************************************************
    //*********************************************************

//    this.map
//
//
//
//    FixedPixelCircle tappableCircle = new FixedPixelCircle(
//            latLong6,
//            20,
//            Utils.createPaint(
//                    AndroidGraphicFactory.INSTANCE.createColor(Color.GREEN),
//                    0, Style.FILL), null) {
//        @Override
//        public boolean onLongPress(LatLong geoPoint, LatLong viewPosition,
//                                   LatLong tapPoint) {
//            if (this.contains(viewPosition, tapPoint)) {
//                Toast.makeText(
//                        OverlayMapViewer.this,
//                        "The Circle was long pressed at "
//                                + geoPoint.toString(), Toast.LENGTH_SHORT)
//                        .show();
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public boolean onTap(LatLong geoPoint, LatLong viewPosition,
//                             LatLong tapPoint) {
//            if (this.contains(viewPosition, tapPoint)) {
//                Toast.makeText(OverlayMapViewer.this,
//                        "The Circle was tapped " + geoPoint.toString(),
//                        Toast.LENGTH_SHORT).show();
//                return true;
//            }
//            return false;
//        }
//    };
//
//    public void testRouting(){
//        MyDBOpenHelper.getInstance().
//    }


    //_________________________________________________________________________________________
    //_______________________________                           _______________________________
    //_______________________________   screen button presses   _______________________________
    //_________________________________________________________________________________________

    public void incDistance(View view){
        Log.i("routeKmSetting",routeKmSetting+" MinDistance: "+minDistance);
        // if route distance is at zero..
        if(routeKmSetting==0.0){
            // .. set it equal to shortest path length (rounded down)
            routeKmSetting = /*(int)*/minDistance;
            changeButtonText();
        }
        // if route distance is at below 16(km)..
        else if(routeKmSetting <minDistance*3){//50) {
            // increment it and change the displayed number
            routeKmSetting = (int)routeKmSetting+1;
            changeButtonText();
        }
    }
    public void decDistance(View view){
        // if route distance is not at 0..
        if(routeKmSetting!=0.0){
            // ..if route distance is longer than shortest path length
            if (routeKmSetting > minDistance){
                // decrement it
                routeKmSetting--;
//                // if route distance has lowered below shortest path length
                if(routeKmSetting<minDistance)
//                    changeButtonText();
                    routeKmSetting = minDistance;

                // might (appear to)add on 1.5 k if this is in
//            if(routeKmSetting <minDistance)
//                routeKmSetting++;

            }
            // ..if 'routeKmSetting' is equal to minDistance
            else
                routeKmSetting=0.0;
            // change the displayed number
            changeButtonText();
        }
    }
    public void alternateDistanceDuration(View view){
        kmNotMinutes=!kmNotMinutes;
        changeButtonText();
    }
    public void changeButtonText(){
        button_dist.setText(
                kmNotMinutes ?
                        (routeKmSetting + "km") :
                        returnRouteAsTime()
        );
    }
    public String returnRouteAsTime(){
        if(routeKmSetting %5==0)
            return (int) routeKmSetting /5+"h";
        if(routeKmSetting <5)
            return routeKmSetting *12+"m";
        return (int) routeKmSetting /5+"h"
                + (int)(routeKmSetting *12)%60+"m";
    }


    //_____________________________________________________________________________________
    //_______________________________                       _______________________________
    //_______________________________       messages        _______________________________
    //_____________________________________________________________________________________

    // Shows welcome message
    public void welcomeMessage(){

        // create dialog box and show welcome message
        AlertDialog.Builder dialogBoxBuilder = new AlertDialog.Builder(this);
        dialogBoxBuilder.setMessage(Constants.WELCOME_MSG);
        // gives dialog box have a single 'ok' button
        dialogBoxBuilder.setNeutralButton(Constants.NEUTRAL_MSG,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        dialogBoxBuilder.create().show();

        // replace welcome message with information message
        dialogBoxBuilder.setMessage(Constants.INFORMATION_MSG);
        dialogBox = dialogBoxBuilder.create();
    }
    // Shows application info. when 'i' button is clicked
    public void showInfo(View view){
        // show information message
        dialogBox.show();

    }




    //____________________________________________________________________________________
    //_______________________________                      _______________________________
    //_______________________________   location methods   _______________________________
    //____________________________________________________________________________________

    public void setUpLocation(){
        // get reference to system/phone's location manager
        LocationManager locationManager = (LocationManager)this.getSystemService(this.LOCATION_SERVICE);

        // create location listener
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location _location) {
                System.out.println("location is:"+_location);
                if(location ==null || location !=_location) {
                    location = _location;
                    renderLocationOnMap();
                }

                if(waitingOnCurrentLocation){

                    mapView.getModel().mapViewPosition.setCenter(new LatLong(location.getLatitude(),location.getLongitude()));
                    drawBoundary();
                    waitingOnCurrentLocation=false;
                }
                Log.i("on location change","+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_");
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {
//                if(location!=null)()
//                    drawBoundary();
                waitingOnCurrentLocation = true;
            }
            @Override
            public void onProviderDisabled(String provider) {
                // remove box highlighting available map area
                Log.i("location disabled","yes");
                removeBoundary();
            }
        };
        // attach the listener to manager
        try { // update every x milliseconds or every y meters moved
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, // gps => or can also be network provider
                    5000,//6000 * 5, // time(ms) between update
                    20, // distance(m) between update
                    locationListener);
        }catch(SecurityException e){e.printStackTrace();}
    }


    //____________________________________________________________________________________
    //_______________________________                      _______________________________
    //_______________________________    drawing methods   _______________________________
    //____________________________________________________________________________________

    public void renderLocationOnMap(){


        if(locationMarker == null)
            locationMarker = createMarker(new LatLong(location.getLatitude(), location.getLongitude()), R.drawable.marker_blue);
        else
            mapView.getLayerManager().getLayers().remove(locationMarker);
        mapView.getLayerManager().getLayers().add(locationMarker);
    }

    public void shortestPath(){
            // if we have a location for route source
//        System.out.println("1");
        if (location != null) {
//            System.out.println("2.,.");
            // if current distance setting is same as previous  &&  not at 0.0..
            // .. do nothing.
            // otherwise, find a new route
            if(routeKmSetting==0.0  ||  routeKmSetting!=previousDistanceSetting) {

                if(parser==null)
                Log.i("->In Activity","Parser is returning null. Check if files exist.");

                List<LatLong> route = parser.returnRoute("658429", "389365", routeKmSetting);//"658429", "389365"

//                for (int i = 0; i < route.size(); i++)
//                    route.add(route.get(i));
                drawPolyline(route);
//                System.out.println("___________________3>>>>>" + System.currentTimeMillis());


                // if requesting a new *shortest route..
                if(routeKmSetting==0.0)
                    // update minimum distance required to reach destination
                    setMinDistance();
                else // otherwise set previous distance requested
                    previousDistanceSetting = routeKmSetting;



                /*// if requesting a new shortest route..
                if(routeKmSetting==0.0)
                    minDistance = 0.0;
                // otherwise if minimum distance is not yet recorded..
                else if (minDistance == 0.0)
                    setMinDistance();*/
            }
//            Log.i("here min distance",""+minDistance);
        }
        // if we have no source location..
        else { // ..and the toast object is empty, create one
            if (noLocationToast == null) {
                noLocationToast = Toast.makeText(this, Constants.NO_LOCATION_MSG, Toast.LENGTH_LONG);
                noLocationToast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
            }// ..show toast alert
            noLocationToast.show();
        }


    }










    /*protected void onLongPress(final LatLong position) {
        float circleSize = 20 * this.mapView.getModel().displayModel
                .getScaleFactor();

        i += 1;

        FixedPixelCircle tappableCircle = new FixedPixelCircle(position,
                circleSize, GREEN, null) {

            int count = i;

            @Override
            public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas
                    canvas, LatLong topLeftPoint) {
                super.draw(boundingBox, zoomLevel, canvas, topLeftPoint);

                long mapSize = MercatorProjection.getMapSize(zoomLevel, this.displayModel.getTileSize());

                int pixelX = (int) (MercatorProjection.longitudeToPixelX(position.longitude, mapSize) - topLeftPoint.x);
                int pixelY = (int) (MercatorProjection.latitudeToPixelY(position.latitude, mapSize) - topLeftPoint.y);
                String text = Integer.toString(count);
                canvas.drawText(text, pixelX - BLACK.getTextWidth(text) / 2, pixelY + BLACK.getTextHeight(text) / 2, BLACK);
            }

            @Override
            public boolean onLongPress(LatLong geoPoint, LatLong viewPosition,
                                       LatLong tapPoint) {
                if (this.contains(viewPosition, tapPoint)) {
                    LongPressAction.this.mapView.getLayerManager()
                            .getLayers().remove(this);
                    LongPressAction.this.mapView.getLayerManager()
                            .redrawLayers();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onTap(LatLong geoPoint, LatLong viewPosition,
                                 LatLong tapPoint) {
                if (this.contains(viewPosition, tapPoint)) {
                    toggleColor();
                    this.requestRedraw();
                    return true;
                }
                return false;
            }

            private void toggleColor() {
                if (this.getPaintFill().equals(LongPressAction.GREEN)) {
                    this.setPaintFill(LongPressAction.RED);
                } else {
                    this.setPaintFill(LongPressAction.GREEN);
                }
            }
        };
    }*/




    public void setMinDistance(){

        minDistance = (   (double)(parser.getMinPathLengthInM()/100))   /10;
        Log.i("min distance",""+minDistance);

        routeKmSetting = minDistance;
//        minDistance = (minDistance - (minDistance%500))/1000;
//        routeKmSetting = ((int)minDistance)-1;
        changeButtonText();
    }

    public void assignSingleClickListener(Button button){


        SingleClickListener singleClickListener = new SingleClickListener() {
            @Override
            public void onOneClick(boolean beginCalculating) {
                if (beginCalculating) {
                    // calculate and draw the route
                    shortestPath();
                    // allow for clicks again
                    this.enable();
                }

                else{ // ..and the toast object is empty, create one
                    if (calculatingToast == null) {
                        calculatingToast = Toast.makeText(context, Constants.STILL_CALCULATING_MSG, Toast.LENGTH_LONG);
                        calculatingToast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, 0);
                    }// ..show toast alert
                    calculatingToast.show();
                }
            }
        };
        button.setOnLongClickListener(singleClickListener);


    }











}
