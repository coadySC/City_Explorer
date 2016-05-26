package fourthyearp.cian.mapsforge_3;

import android.database.Cursor;
import android.content.Context;
import android.os.Environment;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import es.usc.citius.hipster.algorithm.Algorithm;
//import java.util.List;

/**
 * Created by cian on 27/04/2016.
 */
public class WaysParser {

    XmlPullParserFactory factory;
    XmlPullParser pull;
    int eventT;

//    Graph_WithNodes graph;
//    HashMap<String, MapNode> map;
    boolean dbExists;
    MapNode nearest;
    double nearest_distance;
    double contender_distance;
    final double Deg_2_Rad = 0.0174532925199433;
    int graphInsertions=0;

    List<String> nodeIds;
    List<List<String>> resultStates;
    Algorithm.SearchResult result;
    List<LatLong> routeNodes;
    int addedToGraph=0;
    HashMap nodes;
    static SQLiteDatabase db=null;
//    MyDBOpenHelper dbHelper;
//    boolean dataExistsTest;

//    BTree tree;
    String prev = "";
    String current = "";
    int distance;
    int pathLength;

    static Context context;


    // Constructor
    public WaysParser(Context mainActivityContext)throws XmlPullParserException, IOException{
        System.out.println("____In WaysParser____");
        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        pull = factory.newPullParser();
        context = mainActivityContext;

        // open the database as writable
        db = MyDBOpenHelper.getInstance(context).getWritableDatabase();
        // set the boundary to filter nodes by(for going into database)
        MyDBOpenHelper.getInstance().setBoundary(/*location.latitude, location.longitude);//*/53.346701, -6.260139);

        /*if(!MyDBOpenHelper.getInstance().wasAlreadyBulkLoaded()) {
            File nodeFile = new File(Environment.getExternalStorageDirectory(), "NodesOnly.xml");// here -> put this in resources later in project (might save compiling and loading for now)
            FileInputStream nodesIn = new FileInputStream(nodeFile);
            MyDBOpenHelper.getInstance().onUpgrade(db, 2, 2);
            // parse nodes and store in database
            pull.setInput(nodesIn, null);
            parseNodesToDB();
            nodesIn.close();
        }*/
        // open the database as readable
        /*if(db==null)
            db = MyDBOpenHelper.getInstance(context).getReadableDatabase();*/
    //--------------------------------------------------------------------------------------------
        // parse edges and build graph
//        // here. test. creating ways with distances
//        File wayFile = new File(Environment.getExternalStorageDirectory(), "WaysOnly.xml");// here -> put this in resources later in project (might save compiling and loading for now)
//        FileInputStream waysIn = new FileInputStream(wayFile);pull.setInput(waysIn, null);
////      parseEdges_hmm_changed();
//        TESTmakeEdgeXML(mainActivityContext);
////        System.out.println("Finished Parsing edges");
//        waysIn.close();
////        System.out.println("Missing vertices = "+testMissingVertices);
    //--------------------------------------------------------------------------------------------
        // createGraph
        File edgeFile = new File(Environment.getExternalStorageDirectory(), /*"dublin.map");//*/"Edges.xml");// here -> put this in resources later in project (might save compiling and loading for now)
        FileInputStream edgesIn = new FileInputStream(edgeFile);
        pull.setInput(edgesIn, null);
        parseEdges();
        edgesIn.close();
        closeDB();
    }


    //_________________________________________________________________________________
    //___________________________                          ____________________________
    //___________________________     start to methods     ____________________________
    //_________________________________________________________________________________

    // cycles through file, parsing along the way
    public void parseNodesToDB() throws XmlPullParserException, IOException{

        /* here code online had XmlPullParser.END_DOCUMENT...why not the local xml pull parser?
          . Quicker to call actual class for constants??? here */

//        /*eventT = */pull.getEventType(); // don't necessarily need to get it, just pass it?
//         //or do I even need to do that?..just go to get next?
        int i = 0;// here, just for testing


//        dbHelper.onCreate(db);
//        dbHelper.onUpgrade(db,2,3);

        // while not at end of file -> getting each next event type at every check
        while((eventT=pull.next())!=pull.END_DOCUMENT) {
            // if start of a tag is..
            if (eventT == pull.END_TAG) {
                if(i++%10000==0)
                    System.out.println("___________i:>" + i + "<_____________");

                // a) a node -> right now, only ways...change to file with both
                if (pull.getName().contains("node"))
                    placeNodeInDB();

                // b) a way
//                else if(pull.getName().contains("way"))
//                    break; // here

                // c) a route
                // break;  // -> break out of loop. We are past the nodes and ways.
            }

//            // if end of a tag is..
//              ; //do nothing
        }
        // Finish loading nodes when end of file is reached
//        MyDBOpenHelper.getInstance(context).insertNodeBulk(db);
        System.out.println("\n" +
                "+++++++++++++>>>>>                                   <<<<<+++++++++++++" +
                "+++++++++++++>>>>> Finished parsing and adding nodes <<<<<+++++++++++++" +
                "\n+++++++++++++>>>>>                                   <<<<<+++++++++++++");

        // Finished reading from the file and inserting into db
    }

    // cycles through file, parsing along the way
//    public void parseNodes() throws XmlPullParserException, IOException{
//
//        // while not at end of file -> getting each next event type at every check
//        while((eventT=pull.next())!=pull.END_DOCUMENT) {
//            // if start of a tag is..
//            if (eventT == pull.END_TAG) {
////                if(i++%10000==0)
////                    System.out.println("___________i:>" + i + "<_____________");
//
//                // a) a node -> right now, only ways...change to file with both
//                if (pull.getName().contains("node"))
//                    addNode();
//
//                // b) a way
////                else if(pull.getName().contains("way"))
////                    break; // here
//
//                // c) a route
//                // break;  // -> break out of loop. We are past the nodes and ways.
//            }
//
////            // if end of a tag is..
////              ; //do nothing
//        }
//        // Finish loading nodes when end of file is reached
////        dbHelper.insertNodeBulk(db);
//        System.out.println("\n" +
//                "+++++++++++++>>>>>                                   <<<<<+++++++++++++" +
//                "+++++++++++++>>>>> Finished parsing and adding nodes <<<<<+++++++++++++" +
//                "\n+++++++++++++>>>>>                                   <<<<<+++++++++++++");
//
//        // Finished reading from the file and inserting into db
//    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // cycles through file, parsing along the way
    public void parseEdges() throws XmlPullParserException, IOException{
        Log.i("in parseEdges","from Edges.xml to graph");

        /* here code online had XmlPullParser.END_DOCUMENT...why not the local xml pull parser?
          . Quicker to call actual class for constants??? here */

        ///*eventT = */pull.getEventType(); // don't necessarily need to get it, just pass it?
                                            // or do I even need to do that?..just go to get next?
//        addedToGraph = 0;// here, just for testing
//        dbHelper.onCreate(db);

        boolean reachedWays = false;
        // while not at end of file -> getting each next event type at every check
        while((eventT=pull.next())!=pull.END_DOCUMENT){
            // if end of a tag is..
            if(eventT==pull.START_TAG) {

                // a) a node -> right now, only ways...change to file with both
                    // nothing

                // b) a way
                if(pull.getName().contains("way")){
//                    if(addedToGraph%15000==0)System.out.println("++++++++++++++++Node count roughly:"+addedToGraph+"++++++++++++++++++");
                    processWay(reachedWays);
//                    if(!reachedWays)System.out.println("++++++++++++++++\n++++++++++++++++ways reached. Passed/past first edge into graph:"+i+"++++++++++++++++++\n++++++++++++++++");
//                    reachedWays = true;
                }
                // c) a route
                // break;  // -> break out of loop. We are past the nodes and ways.
            }
            // if end of a tag is..
            // ..do nothing
        }
        // Finished reading from the file&db
    }

    // close tree and remove from memory
    public void cleanUp(){}

    //________________________________________________________________________________
    //___________________________                         ____________________________
    //___________________________ process tag information ____________________________
    //________________________________________________________________________________


    public void processWay(boolean reachedWays) throws XmlPullParserException, IOException{

//        Log.i("in","process Way");
        prev = null;
//                    System.out.println("here 0");
        boolean vertexBExists=false; // vertexB missing
//        Log.i("processWay", "here 0.0");
//        System.out.println("here 0");
        while((eventT=pull.next())!=pull.END_DOCUMENT) {
//            System.out.println("here 1");
            // if not at end_tag of way..(and not at tags which return nulls)
            if(eventT==pull.END_TAG) {
                //  ..and has a/another node which is..
                if (pull.getName().contains("nd")) {
//                    System.out.println("here 3");
                    // a) first vertex..
                    if (prev == null) {
//                    System.out.println("here 4");
                        // store in 'prev'
                        addVertexA(prev = pull.getAttributeValue(0));
                    }
                    // b) a consecutive vertex -> add and connect with distance..
                    else {
//                    System.out.println("here 5");
                        addedToGraph++;
                        connectEdge(prev, prev = pull.getAttributeValue(0),distance);
                    }

                } // if is the distance between two vertices
                else if(pull.getName().contentEquals("d"))
                    distance= Integer.parseInt(pull.getAttributeValue(0));
                else if(pull.getName().contains("way"))
                    break;
            }
        }



    }






//    public void getValidTag() throws XmlPullParserException, IOException{
//        pull.next();// to take in the current id (next line)
//        while(pull.getName()==null) // get valid element: cycle through nulls(empty space) until not null/reaches next tag/element (might not be ref, outer while loop responds to that)
//            pull.next();
//
//    }


    //_______________________________________________________________________________
    //___________________________                        ____________________________
    //___________________________  place nodes and ways  ____________________________
    //_______________________________________________________________________________



    public void placeNodeInDB(){
        MyDBOpenHelper.getInstance(/*context*/).insertNode(db, pull.getAttributeValue(0), // node id is at index 0
                pull.getAttributeValue(1), // latitude id is at index 1
                pull.getAttributeValue(2));// longitude id is at index 2
    }

//    public int placeEdgeInGraph(String idA, String idB){
//
//        MapEdge edge = new MapEdge(idA, dbHelper.getInstance(context).returnEdgeCursor(db, idA, idB));
//
//        // if edge is valid
//        if(edge.statusValue==0) {
//            double d = edge.computeDistance();
//
//            // else both exist, connect them to the graph
////            graph.connectEdge(idA, idB, d);
//        }
//
//        return edge.getStatusValue();
//
//    }


    //________________________________________________________________________________
    //___________________________                         ____________________________
    //___________________________    distance methods     ____________________________
    //________________________________________________________________________________






//    public Collection AToB(LatLong a, LatLong b){
//
//        Log.i("here.","1");
//
//        if(!db.isOpen())
//            db = MyDBOpenHelper.getInstance(context).getReadableDatabase();
//
//        // get closest nodes to A
//        Cursor c = MyDBOpenHelper.getInstance().getClosestNodes(db, a.latitude, a.longitude);
//
//        // get the closest node from c(closest to A)
//        MapNode nearestA = findClosestFromList(c, a);
//
//
//        //get closest nodes to B -> write over c
//        c = MyDBOpenHelper.getInstance().getClosestNodes(db, b.latitude, b.longitude);
//        db.close();
//        // get the closest node from c(closest to B)
//        /*MapNode nearest = */findClosestFromList(c, b);
//
//        c.close();
//
//        Log.i("node a:","("+nearestA.getId()+","+nearestA.getLat()+nearestA.getLon()+")");
//        Log.i("node b:","("+nearest.getId()+","+nearest.getLat()+nearest.getLon()+")");
//
//
////                        // a loop/circuit way of nodes to test graph
////        /*<nd ref="674054624"/>
////        <nd ref="678566300"/>
////        <nd ref="678566303"/>
////        <nd ref="678566305"/>
////        <nd ref="678566316"/>
////        <nd ref="2396014039"/>
////        <nd ref="2396014040"/>
////        <nd ref="674054643"/>
////        <nd ref="674054644"/>
////        <nd ref="674054645"/>
////        <nd ref="674054629"/>
////        <nd ref="674054675"/>
////        <nd ref="674054671"/>
////        <nd ref="674054673"/>
////        <nd ref="674054672"/>
////        <nd ref="674054631"/>
////        <nd ref="674054630"/>
////        <nd ref="674054624"/>*/
////
////        Algorithm.SearchResult res= graph.shortestRoute("674054624", "674054644");
////        return res.getOptimalPaths();
////
//////        return graph.getShortestRoute("674054624", "674054644");
//        // => null error because this needs location to be called. other doesn't...change other one at some point. here.
//        Log.i("here.", "4-calling twice");
//        Graph_WithNodes.instance().shortestRoute("658429", "389365");//"31015271", "389365");//"31053762");
//            return null;
//    }
//    private MapNode findClosestFromList(Cursor c, LatLong p){
//        c.moveToFirst();
//        nearest = new MapNode(c.getString(0), c.getDouble(1), c.getDouble(2));
//        nearest_distance = computeDistance(p.latitude, p.longitude, nearest.getLat(), nearest.getLon());
//        for(int i=1; i<c.getCount(); i++){// starting i at 1
//            c.moveToNext();
//            // if current node is closer..
//            if((    contender_distance = computeDistance(p.latitude, p.longitude, c.getDouble(1), c.getDouble(2))    )<nearest_distance){
//                // ..replace 'nearest' with this node
//                nearest_distance = contender_distance;
//                nearest = new MapNode(c.getString(0), c.getDouble(1), c.getDouble(2));
//            }
//        }
//        return nearest;
//    }





    public void closeDB() {
        db.close();
    }

    /*public void addNode() {

//        Log.i("Attribute name", pull.getAttributeName(0));

        MapNode n = new MapNode(
                pull.getAttributeValue(0), // node id is at index 0
                pull.getAttributeValue(1), // latitude id is at index 1
                pull.getAttributeValue(2));// longitude id is at index 2
        n.getId();
        map.put(n.getId(), n);

        graph.addNode(n);
    }*/

    public void connectEdge(String a, String b, int distanceInMeters){

        // add the second end( of this edge to graph)
        if(Graph_WithNodes.instance().addNode(b)    ==true)
            graphInsertions++;
        // connect map vertices together..
        Graph_WithNodes.instance().connectEdge(a, b, distanceInMeters);
//        Log.i("connected at",""+Graph_WithNodes.instance().graph.edgesOf(b));
    }
    public void addVertexA(String id){
        if(Graph_WithNodes.instance().addNode(id)    == true)
//        Log.i("in","addVertex");
            graphInsertions++;
    }

    /*public void testRouting(){

        Cursor c = MyDBOpenHelper.getInstance(context).returnEdgeCursor(db, "386679","385742");
        Log.i("386679 present?", c.getCount()+"");
        MapNode n = new MapNode("386679", c, false);

        graph.shortestRoute(n, new MapNode("385742", c, false));

    }*/





    public double computeDistance(double lat1,double lon1,double lat2,double lon2){
        //final double R = 6371; //km

        double c = Math.sqrt(Math.pow((lat2 - lat1) * Deg_2_Rad, 2) +
                Math.pow((lon2 - lon1) * Deg_2_Rad, 2));

        // return R(km) * c-formula * meters in a km
        return (6371 * c*1000);
    }



    public void TESTmakeEdgeXML(Context c){
        Log.i("in", "TESTmakeEdgeXML");

        try {
            new Edge_pointsDist_toXML(c, db, pull);
        }
        catch(Exception e){e.printStackTrace();}
    }
    /*public List<LatLong> TESTreturnRoute(String a, String b){

//        return result.get(0);
        db = MyDBOpenHelper.getInstance(context).getReadableDatabase();
//        Log.i("pure result",""+result);
//        Log.i("result states, size",""+resultStates.get(0).toString()+","+resultStates.get(0).size());
//        Log.i("result first state", "" + resultStates.get(0).get(0).toString());

//        Graph_WithNodes.instance().shortestRoute("658429", "389365");

        *//*return*//*routeNodes = MyDBOpenHelper.getInstance().returnRouteNodes(db, Graph_WithNodes.instance().getResultStates(a,b))  ;  //resultStates.get(0));
        // here do something
        return routeNodes;
    }*/

    public List<LatLong> returnRoute(String a, String b, double distanceInKm){

        db = MyDBOpenHelper.getInstance(context).getReadableDatabase();
//        Log.i("pure result",""+result);
//        Log.i("result states, size",""+resultStates.toString()+","+resultStates.size());
//        Log.i("result first state", "" + resultStates.get(0).toString());
        /*return*/routeNodes = MyDBOpenHelper.getInstance().returnRouteNodes(db, Graph_WithNodes.instance().getResultStates(a,b,distanceInKm*1000))  ;  //resultStates.get(0));
        // here do something
        return routeNodes;

    }
//    public int getRouteDistance(){
//        return
//    }

//    private int getTotal(String s){
//        int total = 0;
//        int index = 0;
//        for(int i=0; i<s.length(); i++){
//            if(s.charAt(i)==',') { // include closing square bracket to act as a (final) comma
//                total = total + Integer.parseInt(s.substring(index, i));
//                index=i+2;
//            }
//
//        }
//        return total+Integer.parseInt(s.substring(index));
//    }

//    private void getPathLength() {
//        String sentence = result.toString().substring(result.toString().indexOf("Actions:") + 12);// char at +11 is a '['
//        sentence = sentence.substring(0, sentence.indexOf(']') - 1); // Sentence is no numbers and commas closed with a ']'
//
////        pathLength = getTotal(Graph_WithNodes.instance().getPathLength());
//        Log.i("total path length", "" + Graph_WithNodes.instance().getPathLength());
//    }
    public int getMinPathLengthInM(){
        return Graph_WithNodes.instance().getPathLength();//pathLength;
    }


//    public getUpdatedRouteLength(LatLong l){
//
//        result = Graph_WithNodes.instance().shortestRoute("658429", "389365"/*"659807"*/);
//        resultStates = result.getOptimalPaths();
//
//    }

//    public List<String> getLongestPath(){
//
//        //   -> change km to m
////        return Graph_WithNodes.instance().longestRoute("658429", "389365", 3000);
//        return routeNodes = MyDBOpenHelper.getInstance().returnRouteNodes(db, Graph_WithNodes.instance().getResultStates(a,b,distanceInKm))  ;
////        return Graph_WithNodes.instance().getResultStates("658429", "389365", 3000);
//
//    }


}


