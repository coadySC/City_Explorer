package fourthyearp.cian.mapsforge_3;

import java.util.ArrayList;
import java.util.List;

import es.usc.citius.hipster.graph.GraphEdge;
import es.usc.citius.hipster.util.Function;

/**
 * Created by cian on 02/05/2016.
 */
public class CostFromEdge implements Function<Edge, Integer> {

    List<String> vertexA;
    List<String> vertexB;
    List<Double> percent;

    public CostFromEdge(){
        vertexA = new ArrayList<String>();
        vertexB = new ArrayList<String>();
        percent = new ArrayList<Double>();
    }

    private void addEdge(String a, String b, Double _percent){
        vertexA.add(a);
        vertexB.add(b);
        percent.add(_percent);
    }
    public void addEdges(List<GraphEdge> edges, Double _percent){
        for(int i=0; i<edges.size(); i++){
            addEdge(edges.get(i).getVertex1().toString(), edges.get(i).getVertex2().toString(), _percent);
        }
        // nulls separate from other changes.
        addEdge(null, null, null);
    }
    public void rollback(){
        if(!vertexA.isEmpty()) {

            int i=vertexA.size() - 2;

            // start at size minus 2 to avoid the null at the very end
            for (i=i; i>=0; i--) {
                if(vertexA.get(i)==null) {
                    break;
                }
            }
            // remove to last change
            vertexA = vertexA.subList(0, i);
            vertexB = vertexB.subList(0, i);
            percent = percent.subList(0, i);
        }
    }
    private double findPercent(String a, String b){

        for(int i=vertexA.size()-1; i>=0; i--){

            if(vertexA!=null &&(
                    (vertexA.get(i).toString().equals(a) && vertexB.get(i).toString().equals(b)) ||
                    (vertexA.get(i).toString().equals(b) && vertexB.get(i).toString().equals(a))))
                return percent.get(i);
        }
        // wasn't there
        return 1;
    }
    public Integer apply(Edge input) {

        // if either vertex contains state 1 and if either vertex contains state 2
        if(vertexA.contains(input.getVertex1()) || vertexB.contains(input.getVertex1())) {
            if(vertexA.contains(input.getVertex2()) || vertexB.contains(input.getVertex2())) {
                    return (int)(input.getValue() * findPercent(input.getVertex1(), input.getVertex2()));
            }
        }
        return input.getValue();
    }
}