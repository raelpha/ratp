package voyageur;

import ratp.RatpNetwork;

import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.GeomPlanarGraph;
import station.Station;
import station.SuperStation;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


// la rame doit instantier les voyageurs avec la bonne station courante
public class AgentVoyageur implements Steppable, Delayed {

    public int colere;
    Station destination;
    Station stationCourante;
    Queue<Station> cheminEnvisage;
    private long   momentDeLiberation;


    boolean attente = false;
    int etat = 0;
    double x,y;
    double goalX, goalY;
    double movementDirectionX;
    double movementDirectionY;

    // Réapparition d'un voyageur au sortir d'une rame
    public AgentVoyageur(VoyageurDonnees donnees, Station stationCourante){
        destination = donnees.destination;
        cheminEnvisage = donnees.cheminEnvisage;
        colere = donnees.colere;
        this.stationCourante = stationCourante;
        etat = 1;
    }

    public AgentVoyageur(Station stationCourante){
        destination = DeterminerDestination();
        cheminEnvisage = trouverChemin(stationCourante, destination);
        this.stationCourante = stationCourante;
        // colere random ?
    }

    @Override
    public void step(SimState simState) {
        RatpNetwork ratpState = (RatpNetwork) simState;
        if(attente){
            // vérifier si il est arrivé
            if (cheminEnvisage.isEmpty()){
                //ratpState.yard.remove(this);
                return;
            }
            // Si non :
            // prévenir la station courante de sa prochaine destination
            if (attente == false){
                //stationCourante.tellDestination();
                attente = true;
                return;
            }
        }
        // Attente du départ
        // Soit on décide d'une position(0), soit se déplace(1), soit on reste fixe(2)
        if(etat == 0){
            if(Math.random() < VoyageurConstants.probabiliteDeBouger){
                StartMove();
                etat = 1;
                return;
            }
        }
        if(etat == 1){
            Move();
            if(isArrive()){
                etat = 0;
            }
        }

        if(Math.random() < colere * VoyageurConstants.probabiliteIncidentVoyageur){
            // incident voyageur, avertir la station courante
        }
    }

    private void StartMove(){
        /**
         * Décider d'un endroit où aller dans le rayon possible de la station
         */
        // Coordonnées randoms en polaire
        double r = VoyageurConstants.maximumDistanceStation * Math.sqrt(Math.random());
        double theta = Math.random() * 2 * Math.PI;
        // Conversion en cartésien
        goalX = stationCourante.location.getX() + r * Math.cos(theta);
        goalY = stationCourante.location.getY() + r * Math.sin(theta);
        movementDirectionX = goalX - x;
        movementDirectionY = goalY - y;
        // Normalisation
        double magnitude = Math.sqrt(movementDirectionX*movementDirectionX
                + movementDirectionY*movementDirectionY);
        movementDirectionX = movementDirectionX / magnitude;
        movementDirectionY = movementDirectionY / magnitude;
    }

    private void Move(){
        x += movementDirectionX*VoyageurConstants.vitesse;
        y += movementDirectionY*VoyageurConstants.vitesse;
    }

    private boolean isArrive(){
        return Math.sqrt((goalX - x)*(goalX - x) + (goalY - y)*(goalY - y)) < 0.5f;
    }

    private float RandomRange(float min, float max){
        return min + (float)Math.random()*max;
    }

    // permet d'informer l'agent qu'une station a été fermée, si elle est sur son chemin il recalcule son trajet
    public void FermetureStation(Station station){
        if(cheminEnvisage.contains(station)){
            trouverChemin(stationCourante, destination);
            addToColere(VoyageurConstants.augmentationColereStationFermee);
        }
    }

    private void addToColere(int a){
        colere += a;
        if(colere > VoyageurConstants.colereMax){
            colere = VoyageurConstants.colereMax;
        }
    }

    // détermine une destination au hasard
    private Station DeterminerDestination(){

        return null;
    }



    @Override
    public long getDelay(TimeUnit unit) {
        long diff = momentDeLiberation - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        int resultat = -1;
        if (o instanceof AgentVoyageur) {
            AgentVoyageur agentVoyageur = (AgentVoyageur) o;
            if (this.momentDeLiberation < agentVoyageur.momentDeLiberation) {
                resultat = -1;
            } else {
                if (this.momentDeLiberation > agentVoyageur.momentDeLiberation) {
                    resultat = 1;
                } else {
                    resultat = 0;
                }
            }
        }
        return resultat;
    }



    class Node{
        public Station station;
        public double heuristique;
        public int cout;
        public Node previousNode;

        public Node(Station station, int cout, double heuristique, Node previousNode){
            this.station = station;
            this.cout = cout;
            this.heuristique = heuristique;
            this.previousNode = previousNode;
        }
    }

    class NodeComparator implements Comparator<Node>
    {
        @Override
        public int compare(Node n1, Node n2)
        {
            return n1.heuristique+n1.cout > n2.heuristique+n2.cout ? 1 : -1;
        }
    }

    private Queue<Station> trouverChemin(Station departS, Station arriveeS){
        Node depart = new Node(departS, 0, 0, null);
        Queue<Node> stationFermees = new LinkedList<>();
        PriorityQueue<Node> stationOuvertes = new PriorityQueue<>(new NodeComparator());
        stationOuvertes.add(depart);
        while(!stationOuvertes.isEmpty()){
            Node n = stationOuvertes.poll();
            if(n.station == arriveeS){
                // reconstituer chemin
                return BuildPath(n);
            }

            for(Station s : StationsDirectory.getInstance().getAdjacentStations(n.station)){
                if(!(stationFermees.contains(s) || ExisteCoutInferieur(stationOuvertes, s, n.cout+1))){
                    Node newN = new Node(s, n.cout+1, Distance(s, arriveeS), n);
                    stationOuvertes.add(newN);
                }
            }
            stationFermees.add(n);
        }
        System.out.println("Erreur, pas de chemin trouvé.");
        return null;
    }


    private Queue<Station> BuildPath(Node arrivee){
        List<Station> stationPath = new ArrayList<>();
        stationPath.add(arrivee.station);
        Node currentNode = arrivee;
        while(currentNode.previousNode != null){
            currentNode = currentNode.previousNode;
            stationPath.add(currentNode.station);
        }
        Collections.reverse(stationPath);
        return (Queue<Station>)stationPath;
    }

    private double Distance(Station s1, Station s2){
        return Math.sqrt(Math.pow(s1.location.getX() - s2.location.getX(), 2) + Math.pow(s1.location.getY() - s2.location.getY(), 2));
    }

    private boolean ExisteCoutInferieur(PriorityQueue<Node> nodes, Station s, int coutS) {
        for(Node n : nodes){
            if(n.station == s && n.cout <= coutS){
                return true;
            }
        }
        return false;
    }
    /*
    private List<Node> trouverChemin(Node depart, Node destination){
        AStar aStar = new AStar();
        List<GeomPlanarGraphDirectedEdge> edges = aStar.astarPath(depart, destination);
        List<Node> chemin = new ArrayList<>();
        for(var edge : edges){
            chemin.add(edge.getFromNode());
        }
        chemin.add(destination);
        return chemin;
    }
    */
}
