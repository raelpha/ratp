package voyageur;

import com.vividsolutions.jts.planargraph.PlanarGraph;
//import lines.Station;
import ratp.RatpNetwork;
import sim.app.geo.campusworld.CampusWorld;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.GeomPlanarGraph;
import station.*;
import java.beans.ConstructorProperties;
import java.lang.reflect.Array;
import java.util.*;

import sim.app.geo.gridlock.AStar;
import com.vividsolutions.jts.planargraph.Node;

import sim.util.geo.GeomPlanarGraphDirectedEdge;


public class AgentVoyageur implements Steppable {

    public int colere = 0;
    public static int id=0;
    public Node destination;
    Node stationCourante;
    List<Node> cheminEnvisage;
    Station stationActuelle;
    public List<Station> listStation;


    // Réapparition d'un voyageur au sortir d'une rame
    public AgentVoyageur(VoyageurDonnees donnees, Node stationCourante){
        destination = donnees.destination;
        cheminEnvisage = donnees.cheminEnvisage;
        colere = donnees.colere;
        this.stationCourante = stationCourante;
    }

    public AgentVoyageur(Station stationActuelle){
        this.stationActuelle=stationActuelle;
        this.id=id;
        id=id+1;
    }

    /*public AgentVoyageur(Node stationCourante){
        destination = DeterminerDestination();
        cheminEnvisage = trouverChemin(stationCourante, destination);
        this.stationCourante = stationCourante;
        // colere random ?
    }*/

    @Override
    public void step(SimState simState) {
        RatpNetwork campState = (RatpNetwork) simState;

        // la rame doit instantier les voyageurs avec la bonne station courante
        if(stationCourante.equals(cheminEnvisage.get(0))){
            cheminEnvisage.remove(0);
            // dire à la station courante que l'on souhaite aller à la station cheminEnvisage.get(0)
        }

        if(Math.random() < colere * VoyageurConstants.probabiliteIncidentVoyageur){
            // incident voyageur, avertir la station courante
        }
    }

    // permet d'informer l'agent qu'une station a été fermée, si elle est sur son chemin il recalcule son trajet
    /*public void FermetureStation(Node station){
        if(cheminEnvisage.contains(station)){
            trouverChemin(stationCourante, destination);
            addToColere(VoyageurConstants.augmentationColereStationFermee);
        }
    }*/

    private void addToColere(int a){
        colere += a;
        if(colere > VoyageurConstants.colereMax){
            colere = VoyageurConstants.colereMax;
        }
    }

    // détermine une destination au hasard
    private Node DeterminerDestination(GeomPlanarGraph graph){
        int index = (int)(Math.random() * graph.getNodes().size());
        Object[] nodes = graph.getNodes().toArray();
        return (Node)nodes[index];
    }
    /*
    class Node{
        public List<Node> neighbors;
        public Station station;
        public double heuristique;
        public int cout;
        public Node(Station station, int cout, double heuristique){
            this.station = station;
            this.cout = cout;
            this.heuristique = heuristique;
        }
    }
    class NodeComparator implements Comparator<Node> {
        @Override
        public int compare(Node n1, Node n2) {
            return n1.heuristique < n2.heuristique ? 1 : -1;
        }
    }
    */

    /*private List<Node> trouverChemin(Node depart, Node destination){
        AStar aStar = new AStar();
        List<GeomPlanarGraphDirectedEdge> edges = aStar.astarPath(depart, destination);
        List<Node> chemin = new ArrayList<>();
        for(var edge : edges){
            chemin.add(edge.getFromNode());
        }
        chemin.add(destination);
        return chemin;
    }*/

    /*private Queue<Station> trouverChemin(Station departS, Station arriveeS){
        Map<Station,Station> path
        Node depart = new Node(departS, 0, 0);
        Queue<Node> stationFermees = new LinkedList<>();
        PriorityQueue<Node> stationOuvertes = new PriorityQueue<>(new NodeComparator());
        stationOuvertes.add(depart);
        while(!stationOuvertes.isEmpty()){
            Node n = stationOuvertes.poll();
            if(n.station == arriveeS){
                // reconstituer chemin
                return BuildPath();
            }
            for(Station s : n.station.neighbors){
                if(!(stationFermees.contains(s) || ExisteCoutInferieur(stationOuvertes, s, n.cout+1))){
                    Node newN = new Node(s, n.cout+1, n.cout + Distance(s, arriveeS));
                    stationOuvertes.add(newN);
                }
            }
            stationFermees.add(n);
        }
        System.out.println("Erreur, pas de chemin trouvé.");
    }*/

    /*
    private Queue<Station> BuildPath(){
    }
    private double Distance(Station s1, Station s2){
        return Math.sqrt(Math.pow(s1.x - s2.x, 2) + Math.pow(s1.y - s2.y, 2));
    }
    private boolean ExisteCoutInferieur(PriorityQueue<Node> nodes, Station s, int coutS) {
        for(Node n : nodes){
            if(n.station == s && n.cout <= coutS){
                return true;
            }
        }
        return false;
    }
    */

}