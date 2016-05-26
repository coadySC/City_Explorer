package fourthyearp.cian.mapsforge_3;

/**
 * Created by cian on 18/05/2016.
 */

// personal edges used for graph. Ie not the hipster 'GraphEdge'
public class Edge {

    private String id1;
    private String id2;
    private int value;

    public Edge(String _id1, String _id2, int _value){
        id1=_id1;
        id2=_id2;
        value=_value;
    }

    public String getVertex1(){     return id1;}
    public String getVertex2(){     return id2;}
    public int getValue(){          return value;}
    public int getValueByPercentage(double percentageChange){   return (int)(value*percentageChange);}

}
