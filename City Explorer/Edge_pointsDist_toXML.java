package fourthyearp.cian.mapsforge_3;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by cian on 05/05/2016.
 */
public class Edge_pointsDist_toXML {

    XmlPullParser pull;
    int eventT;

    final double Deg_2_Rad = 0.0174532925199433;

    SQLiteDatabase db=null;

    Cursor cursor;
    double aLat;
    double aLon;
    boolean multipleExist=false;
    boolean alreadyParsed = false;

    Context context;

    BufferedWriter out;

    int limitTesting;




    // Constructor
    public Edge_pointsDist_toXML(Context mainActivityContext, SQLiteDatabase _db, XmlPullParser _pull)throws XmlPullParserException, IOException{
        context = mainActivityContext;
        MyDBOpenHelper.getInstance(mainActivityContext);
        db = _db;
        pull = _pull;
        Log.i("In", "Edge_pointsDist_toXML");






        if(MyDBOpenHelper.getInstance().peekBulkIndex()==0) {

            // make the writer
            File outFile = new File(Environment.getExternalStorageDirectory(), "Edges.xml");
            try {


                if (outFile.exists()) {
//                outFile.delete();
                    // /storage/emulated/0/DCIM/Edges.xml
//                    Log.i("HERE", "EXISTS");
//                    Log.i("absolute path", outFile.getAbsolutePath());
//                    Log.i("canonical path", outFile.getCanonicalPath());
//                    Log.i("get path", outFile.getPath());
//                    Log.i("parent file", "" + outFile.getParentFile());

//                if(outFile.length()>0)
                    Log.i("length of", outFile.length() + "");
                }
                if (!outFile.exists()) {
//                outFile.delete();
                    Log.i("GONE", "GONE");
                }


                out = new BufferedWriter(new FileWriter(outFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (db == null)
                db = MyDBOpenHelper.getInstance(context).getReadableDatabase();

            System.out.println("Graph_Example created and nodes added, now begin constructing it");
//        Log.i("Map size", map.size()+"");

            if (!alreadyParsed)
                // parse ways to contain distance
                parseEdges();
            System.out.println("Finished Parsing edges");
//        System.out.println("Missing vertices = "+testMissingVertices);

            if (alreadyParsed)
                Log.i("already parsed", "YES");

        }


    }


    //_________________________________________________________________________________
    //___________________________                          ____________________________
    //___________________________     start to methods     ____________________________
    //_________________________________________________________________________________




    // cycles through file, parsing along the way
    public void parseEdges() throws XmlPullParserException, IOException{
        alreadyParsed=true;
        Log.i("In", "parseEdges");

//        limitTesting=0;

        writeFileInfoAndOpenOsmTag();
        // while not at end of file -> getting each next event type at every check
        while((eventT=pull.next())!=pull.END_DOCUMENT){
//            limitTesting++;
            /*if(i++%100000==0) {
                System.out.println("___________i:>" + i + "<_____________");
            }*/
            // if end of a tag is..
            if(eventT==pull.START_TAG) {

                // a) a node -> right now, only ways...change to file with both
                // nothing


                // b) a way
                /*else*/ if(pull.getName().contains("way")){
//                    Log.i("At","Start way");
//                    if(i++%1500==0)System.out.println("++++++++++++++++Node count roughly:"+i+"++++++++++++++++++");
                    processWay();
//                    if(!reachedWays)System.out.println("++++++++++++++++\n++++++++++++++++ways reached. Passed/past first edge into graph:"+i+"++++++++++++++++++\n++++++++++++++++");
//                    reachedWays = true;
                }

                // c) a route
                // break;  // -> break out of loop. We are past the nodes and ways.
            }

//            // if end of a tag is..
//              ; //do nothing
//            }

        }
        System.out.println("+++++++++++++>>>>>Finished Parsing edges");

        // Finished reading from the file&db
        out.close();
    }

    //________________________________________________________________________________
    //___________________________                         ____________________________
    //___________________________ process tag information ____________________________
    //________________________________________________________________________________


    public void processWay() throws XmlPullParserException, IOException{

        String prev = null;
        multipleExist=false; // way does not have an existing edge yet
        while((eventT=pull.next())!=pull.END_DOCUMENT/* && limitTesting<50*/) {
//            limitTesting++;

            // if not at end_tag of way..(and not at tags which return nulls)
            if(eventT==pull.END_TAG) {

//                if(pull.getName().contains("way")){
//                    Log.i("At close way","</way>");
//                    Log.i("m exist starts as",multipleExist+"");
//                }
                //  ..and has a/another node which is..
                if (pull.getName().contains("nd")) {
//                    Log.i("In","processWay");
                    // a) first vertex..
                    if (prev == null) {
                        // store in 'prev'
                        prev = pull.getAttributeValue(0);
//                        Log.i("first", prev);
                    }
                    // b) a consecutive vertex..
                    else {
                        prev = checkEdge(prev, pull.getAttributeValue(0));
                        cursor.close();
//                        if(prev!=null)
//                            Log.i("middle", prev);
//                        else
//                            Log.i("first", "NULL");
                    }

                }
                else if(pull.getName().contains("way")){
//                Log.i("Exist?                 ","GOT HERE!!!!__________________________");
                    // write end vertex and close 'way', if there is at least 1 edge
                    if(multipleExist) {
//                        Log.i("write node B with", "NO dist");
                        writeVertex(prev);
                        writeOpenWayTagOrClose(false);
//                        Log.i("last", prev);
                    }
                    break;
                }
            }
        }

    }





    //__________________________________________________________________________________
    //___________________________                           ____________________________
    //___________________________  place distances in ways  ____________________________
    //__________________________________________________________________________________




    public synchronized String checkEdge(String idA, String idB) throws IOException{

        // find existing nodes in database
        cursor = MyDBOpenHelper.getInstance().returnEdgeCursor(db, idA, idB);
        String temp;
//        Log.i("cursor count",""+cursor.getCount());

        // if cursor returns at least 1 vertex..
        if(cursor.getCount()>0) {
//            Log.i("count is:",""+cursor.getCount());
            // get first row
            cursor.moveToFirst();
            temp=cursor.getString(0);
            // ..if cursor returns both vertices..
            if(cursor.getCount()==2) {
//                Log.i("in","count==2");
//                Log.i("row 1, col 1---------:", "" + cursor.getString(0));
                // change multipleExist so at least 1 edge can be written && write the opening tag
//                Log.i("multiple exist =->", "" + multipleExist);
                if(!multipleExist){
//                    multipleExist = true;
//                    Log.i("multipleExist",multipleExist+"Changing to TRUE");
                    writeOpenWayTagOrClose(true);
                    multipleExist=true;
//                    Log.i("multipleExist changed", "");
                }



                // ..write vertex A with distance
//                Log.i("write node A with", "dist YES");
                writeVertex(idA);
                aLat = cursor.getDouble(1);
                aLon = cursor.getDouble(2);
                cursor.moveToNext();
                writeEdgeDistance(computeDistance(aLat, aLon, cursor.getDouble(1), cursor.getDouble(2)));
//                if(TESTedgesCount%10000==0){
//                    Log.i("edges at:",""+TESTedgesCount);
//                }
//                TESTedgesCount++;
//                Log.i(">>"+temp, cursor.getString(0)+"<<");
                // if row 2 is previous node -> they are returned in no order
                if(cursor.getString(0).contentEquals(idA)){
                    // change pointer to row 1/new vertex id
                    cursor.moveToFirst();
                }
            }
//            else
//                Log.i("Only One Returned", cursor.getString(0));

            // cursor of either size is now at last rows
            return cursor.getString(0);
        }

//        Log.i("In,returning","checkEdge,null______________________");
        // if has neither vertex
//        Log.i("None Returned!!, missing both", "NULL");
        return null;

    }


    //________________________________________________________________________________
    //___________________________                         ____________________________
    //___________________________    distance methods     ____________________________
    //________________________________________________________________________________


    public int computeDistance(double lat1,double lon1,double lat2,double lon2){
        //final double R = 6371; //km

        double c = Math.sqrt(Math.pow((lat2 - lat1) * Deg_2_Rad, 2) +
                Math.pow((lon2 - lon1) * Deg_2_Rad, 2));

        // return R(km) * c-formula * meters in a km
        return (int)(6371 * c*1000);
    }



    //_______________________________________________________________________________
    //___________________________                        ____________________________
    //___________________________    writing methods     ____________________________
    //_______________________________________________________________________________


    private void writeFileInfoAndOpenOsmTag() throws IOException{
        // writes file information and opening osm tag
        out.write(
        "<?xml version='1.0' encoding='UTF-8'?>\n"+
        "<osm version=\"0.6\">\n"+
        "<bounds minlat=\"53.178\" minlon=\"-6.569\" maxlat=\"53.512\" maxlon=\"-6.023\"/>\n"+
        "<osm>\n");
    }private void writeCloseOsmTag() throws IOException{
        // writes closing osm tag
        out.write("</osm>");
    }
    private void writeOpenWayTagOrClose(boolean openIt) throws IOException{
        // writes way opening or closing tags
        out.write("<" + (openIt ? "" : "/") + "way>\n");
    }
    private void writeVertex(String id) throws IOException{
        // writes node reference tags
        out.write("<nd ref=\""   +id+   "\"/>\n");
    }
    private void writeEdgeDistance(int distInMeters) throws IOException{
        // writes distance in meters tags
        out.write("<d m=\""   +distInMeters+   "\"/>\n");
    }


}


