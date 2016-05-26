package fourthyearp.cian.mapsforge_3;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.usc.citius.hipster.algorithm.Algorithm;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.GraphEdge;
import es.usc.citius.hipster.graph.GraphSearchProblem;
import es.usc.citius.hipster.graph.HashBasedHipsterGraph;
import es.usc.citius.hipster.model.problem.SearchProblem;

/**
 * Created by cian on 02/05/2016.
 */
public class Graph_WithNodes {

    private static Graph_WithNodes instance = null;
    HashBasedHipsterGraph graph;
    private final double Deg_2_Rad = 0.0174532925199433;
    private List<Edge> alteredEdges = null;
    private int shortestLength = 0;
    private int longestLength = 0;
    private Algorithm.SearchResult searchResult = null;

    // Route memory
    private List<List<Edge>> alteredRoutes = null; // route changes
    private Map<Integer, List<String>> memorisedRoutes; // longest routes under each km increment visited   // here. do not need this is get rollback working(*correctly)

    private Graph_WithNodes(){
        graph = HashBasedHipsterGraph.create();
        alteredEdges = new ArrayList<Edge>();
        alteredRoutes = new ArrayList<List<Edge>>();
    }

    public static Graph_WithNodes instance(){
        if(instance==null)
            instance = new Graph_WithNodes();
        return instance;
    }

    public boolean addNode(String id){
        return graph.add(id);
    }

    public void connectEdge(String a, String b, int d){
        graph.connect(a,b,d);
    }

    public void setShortestRoute(String a, String b){


        SearchProblem p = GraphSearchProblem.startingFrom(a)
                .in(graph)
                .takeCostsFromEdges()
//                .extractCostFromEdges(new CostFromEdge())
                .build();
        searchResult = Hipster.createDijkstra(p).search(b);
//        Log.i("->In setShortestRoute",""+searchResult);
//        Log.i("->In setShortestRoute",""+searchResult.getOptimalPaths());
        Log.i("->In setShortestRoute",""+searchResult.getOptimalPaths().get(0));
    }

    public double computeDistance(double lat1,double lon1,double lat2,double lon2){
        //final double R = 6371; //km

        double c = Math.sqrt(Math.pow((lat2 - lat1) * Deg_2_Rad, 2) +
                Math.pow((lon2 - lon1) * Deg_2_Rad, 2));

        // return R(km) * c-formula * meters in a km
        return (6371 * c*1000);
    }

    private void scaleEdgesUp(List<String> pathNodes, double percent){


        // if list of altered routes is null create it
        if(alteredRoutes==null)
            alteredRoutes = new ArrayList<List<Edge>>();


        // create a new route(List<Edge>) to alter each time
        alteredEdges = new ArrayList<Edge>();

        for(int i=1;     pathNodes!=null && i<pathNodes.size()-1;     i=i+2){

            replaceVertex((Iterable<GraphEdge>) graph.edgesOf(pathNodes.get(i)), pathNodes.get(i), percent, pathNodes.get(i-1));
        }

        alteredRoutes.add(alteredEdges);

    }

    private void replaceVertex(Iterable<GraphEdge> edgesIter, String thisState, double value, String nextState){

        // Removing the vertex/state makes it's iterable unusable
        // Must remove it after fetching it's adjacent vertices
        List<Edge> edges = new LinkedList<Edge>();

        GraphEdge temp;

        Iterator<GraphEdge> iter = edgesIter.iterator();

        // fetch neighbouring vertices
        while(iter.hasNext())
        {
            temp = iter.next();
            edges.add(new Edge(temp.getVertex1().toString(), temp.getVertex2().toString(), Integer.parseInt(temp.getEdgeValue().toString())));
        }
        // Remove & insert state/vertex
        graph.remove(thisState);
        graph.add(thisState);

        // Reconnect neighbouring vertices
        for(int i=0; i<edges.size(); i++) {
            if (edges.get(i).getVertex1().contentEquals(nextState) || edges.get(i).getVertex2().contentEquals(nextState)) {
                    graph.connect(thisState, nextState, edges.get(i).getValueByPercentage(value));

                    // add to alteredEdges list<>
                    alteredEdges.add(edges.get(i));
            }
            else {
                graph.connect(edges.get(i).getVertex1(), edges.get(i).getVertex2(), edges.get(i).getValue());
            }
        }

    }

    //_____________________________________________________________________________________________________________
    //__________________________________________                      _____________________________________________
    //__________________________________________ through methods: end _____________________________________________
    //_____________________________________________________________________________________________________________

    public List<String> getResultStates(String a, String b, double distanceInM){

        // if creating a new(smallest) route or creating a smaller route..
        if(distanceInM==0.0 || distanceInM<= longestLength) {

            // if distance is zero, revert graph to original state..
            if(distanceInM==0.0){
                Log.i("->In getResultStates", "distance limit is 0");
                // ..restoreGraph
//                revertGraphState(a, b, 0);
                setShortestRoute(a, b);

                // ..refresh shortest length and longest length
                shortestLength = parsePathLength(Algorithm.recoverActionPath(searchResult.getGoalNode()));
                longestLength = shortestLength;
                // write shortest route to new memory
                memorisedRoutes = new HashMap<Integer, List<String>>();
                memorisedRoutes.put((shortestLength / 100) * 100, new ArrayList<>((List<String>) searchResult.getOptimalPaths().get(0)));// here. do not need this is get rollback working(*correctly)
                Log.i("->In getResultStates", "recorded route distances:" + memorisedRoutes.keySet());

                Log.i("->In getResultStates", "recorded route distances:" + searchResult.getOptimalPaths().get(0));
            }
            // if looking for shorter route..
            else{
                Log.i("->In getResultStates","lowered distance limit is "+distanceInM);
                // ..revert to previous state

                Log.i("->In getResultStates", "recorded route distances:" + memorisedRoutes.keySet());
                Log.i("->In getResultStates", "matching recorded distance:" + memorisedRoutes.get((int) distanceInM));


                Log.i("->In getResultStates", "looking for " + (int) distanceInM + " in " + memorisedRoutes.keySet());
                Log.i("->In setShortestRoute", "Do they match??????" + memorisedRoutes.containsKey((int) distanceInM));

                return memorisedRoutes.get((int)distanceInM);
//                revertGraphState(a,b,(int)distanceInM);
//                findLongestRoute(a, b, (int) distanceInM);
            }

        }
        // otherwise if creating a longer route..
        else if(!memorisedRoutes.containsKey((int)distanceInM)){
            Log.i("->In getResultStates","increased distance limit is "+distanceInM);
            findLongestRoute(a, b, (int) distanceInM);
        }
        // return longest path within distance limit (or minimum path in the case of getting a new route)
        return (List<String>) searchResult.getOptimalPaths().get(0);

        // here. here. here. here.
        // this return is why the route slightly changes upon revisiting a distance.
        // -> it is returning nodes for route just above limit, should return previous...but error at the moment.
    }

    // obtain shortest path length from searchResult actions
    public int getPathLength(){
        return shortestLength;
    }

    private int parsePathLength(List<Integer> actions){
        int total = 0;
        for(int i=0; i<actions.size(); i++){
            total = total + (int)actions.get(i);
        }
        return total;
    }


    private void findLongestRoute(String a, String b, int distanceInM) {

        // repeated process for increasing accuracy
        longestRoute2(a, b, distanceInM, 10);
        longestRoute2(a, b, distanceInM, 100);

        if(!alteredRoutes.isEmpty())
            Log.i("here1","last of list:"+alteredRoutes.get(alteredRoutes.size()-1).toString());
    }
    private void longestRoute2(String a, String b, int distanceInM, int scaleToDivideBy){

        // while still under distance preference
        for(int i=0; longestLength<distanceInM && i<10; i++) {

            // write previous route to memory
            memorisedRoutes.put(distanceInM, (List<String>) searchResult.getOptimalPaths().get(0));// here. do not need this is get rollback working(*correctly)
            Log.i("->In longestRoute2", "" + searchResult.getOptimalPaths().get(0));

            // replace route edge values with their value * percentage (rounded to 2 decimal places)
            // but also to increase by 100 times (low accuracy)
            scaleEdgesUp((List<String>) searchResult.getOptimalPaths().get(0), ((double) ((distanceInM * 100) / longestLength)) / scaleToDivideBy);

            setShortestRoute(a, b);
            longestLength = parsePathLength(Algorithm.recoverActionPath(searchResult.getGoalNode()));
        }

        // if have gone too far..
        if(longestLength > distanceInM){
            // rollback once
//            revertGraphState(a, b, distanceInM); // here. change/fix
            longestLength = parsePathLength(Algorithm.recoverActionPath(searchResult.getGoalNode()));
        }
    }

    //_____________________________________________________________________________________________________________
    //__________________________________________                      _____________________________________________
    //__________________________________________ scaling down methods _____________________________________________
    //_____________________________________________________________________________________________________________


    // Roll back to acceptable graph state
    private void revertGraphState(String a, String b, int distanceInM){
        // roll graph back to a state below distance limit

        while(!alteredRoutes.isEmpty() && distanceInM < longestLength){
            // ..roll graph back one state
            revertOneState1(alteredRoutes.get(alteredRoutes.size() - 1), (List<String>) searchResult.getOptimalPaths().get(0));
            // ..remove the change from record
            alteredRoutes.remove(alteredRoutes.size() - 1);

            // alter graph accordingly
            setShortestRoute(a,b);
            longestLength = parsePathLength(Algorithm.recoverActionPath(searchResult.getGoalNode()));

        }

    }

    private void revertOneState1(List<Edge> rollback,List<String> pathNodes){

        for(int i=1; i<rollback.size(); i=i+2) {

            graph.edgesOf(pathNodes.get(i));
            rollback.get(i).getVertex1();
            rollback.get(i).getValue();
            rollback.get(i).getVertex2();

            revertOneState2((Iterable<GraphEdge>) graph.edgesOf(pathNodes.get(i)),
                    rollback.get(i).getVertex1(), rollback.get(i).getValue(), rollback.get(i).getVertex2());
        }

    }
    private void revertOneState2(Iterable<GraphEdge> edgesIter, String thisState, int value,String nextState){

        // create a new route(List<Edge>) to alter each time
        List<Edge> edges = new LinkedList<Edge>();

        GraphEdge temp;

        Iterator<GraphEdge> iter = edgesIter.iterator();

        // Fetch neighbouring vertices
        while(iter.hasNext())
        {
            temp = iter.next();
            edges.add(new Edge(temp.getVertex1().toString(), temp.getVertex2().toString(), Integer.parseInt(temp.getEdgeValue().toString())));
        }

        // Remove & insert state/vertex
        graph.remove(thisState);
        graph.add(thisState);

        boolean found = false;
        // Reconnect neighbouring vertices
        for(int i=1; i<edges.size(); i++) {

            if (edges.get(i).getVertex1().contentEquals(nextState) || edges.get(i).getVertex2().contentEquals(nextState)) {
                graph.connect(thisState, nextState, value);
                found=true;
            }
            else
                graph.connect(edges.get(i).getVertex1(), edges.get(i).getVertex2(), edges.get(i).getValue());
        }
//        if(!found)
        Log.i("->In rollback 3","Found? "+found);
    }



}
