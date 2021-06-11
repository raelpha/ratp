package voyageur;

import ratp.RatpNetwork;

import ratp.directory.StationsDirectory;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;
import station.Gare;
import station.Station;


import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import station.Station;



// la rame doit instantier les voyageurs avec la bonne station courante
public class AgentVoyageur implements Steppable, Delayed {

    public int colere;
    Station destination;
    Station stationCourante;
    Queue<Pair<Station, List<Station>>> cheminEnvisage;
    private long   momentDeLiberation;
    int nChangementCheminenvisage;

    int etat = 0;
    double x,y;
    double goalX, goalY;
    double movementDirectionX;
    double movementDirectionY;

    // Réapparition d'un voyageur au sortir d'une rame
    public AgentVoyageur(VoyageurDonnees donnees, Station stationCourante, Continuous2D yard){
        destination = donnees.destination;
        cheminEnvisage = donnees.cheminEnvisage;
        // TODO : récup list dest
        colere = donnees.colere;
        InitialisationDansStation(stationCourante, yard);
    }

    public AgentVoyageur(Station stationCourante, Continuous2D yard){
        InitialisationDansStation(stationCourante, yard);
        //destination = DeterminerDestination();
        //System.out.println("Je suis à : " + stationCourante.name + " et je veux aller à : " + destination.name);
//destination=StationsDirectory.getInstance().getStation("1","Château de Vincennes");
//cheminEnvisage = trouverChemin(stationCourante, destination);
        destination = Math.random() > 0.5f ? StationsDirectory.getInstance().getStation("13", "Les Agnettes") : StationsDirectory.getInstance().getStation("13", "Garibaldi");
        System.out.println("Je suis à : " + stationCourante.name + " " + stationCourante.lineNumber + " et je veux aller à : " + destination.name + " " + destination.lineNumber);
        cheminEnvisage = trouverChemin(stationCourante, destination);

        System.out.println("Nbr chgt : " + nChangementCheminenvisage);

        Random R = new Random();
        int rand = (int)(R.nextGaussian()*80);
        if(rand < 0) colere = 0;
        else if(rand > VoyageurConstants.colereMax) colere = VoyageurConstants.colereMax;
        else colere = rand;
        for(Station s : cheminEnvisage){
            System.out.println(s.name + " " + s.lineNumber);
        }
    }

    public void InitialisationDansStation(Station station, Continuous2D yard){
        this.stationCourante = station;
        // TODO : prévenir la station d'ou on veut aller
        Double2D stationPosGeom = new Double2D(stationCourante.location.getX(), stationCourante.location.getY());
        Double2D stationPosCont = ConversionGeomToContinuous(stationPosGeom);
        Double2D location = GetRandomPointCircle(stationPosCont, VoyageurConstants.maximumDistanceStation);
        //var stationPos = ConversionGeomToContinuous(new Double2D(station.location.getX(), station.location.getY()));
        yard.setObjectLocation(this, location);
        //yard.setObjectLocation(this, stationPos);
        x = location.x;
        y = location.y;
    }

    private Double2D ConversionGeomToContinuous(Double2D c){
        return new Double2D(c.x*456374.2102649563 + 621637.6325538646, c.y*(-441528.2983009379) - 2640599.3936298448);
    }

    // On suppose qu'un voyageur ne peut pas être instancié si sa liste est vide
    @Override
    public void step(SimState simState) {
        RatpNetwork ratpState = (RatpNetwork) simState;

        // Attente du départ
        // Soit on décide d'une position(0), soit se déplace(1), soit on reste fixe(2)
        if(etat == 0){
            if(Math.random() < VoyageurConstants.probabiliteDeBouger){
                // Random point
                Double2D stationPosGeom = new Double2D(stationCourante.location.getX(), stationCourante.location.getY());
                Double2D stationPosCont = ConversionGeomToContinuous(stationPosGeom);
                Double2D goal = GetRandomPointCircle(stationPosCont, VoyageurConstants.maximumDistanceStation);
                goalX = goal.x;
                goalY = goal.y;
                // Calcul direction
                Double2D movementDirection = GetDirection(x,y,goalX,goalY);
                movementDirectionX = movementDirection.x;
                movementDirectionY = movementDirection.y;
                etat = 1;
                return;
            }
        }
        if(etat == 1){
            Move(ratpState.yard);
            if(isArrive()){
                etat = 0;
            }
        }

        if(Math.random() < colere * VoyageurConstants.probabiliteIncidentVoyageur){
            // incident voyageur, avertir la station courante
        }
    }

    private Double2D GetRandomPointCircle(Double2D point, double distance){
        /**
         * Décider d'un endroit où aller dans le rayon possible de la station
         */
        // Coordonnées randoms en polaire
        double r = distance * Math.sqrt(Math.random());
        double theta = Math.random() * 2 * Math.PI;
        // Conversion en cartésien
        double x = point.getX() + r * Math.cos(theta);
        double y = point.getY() + r * Math.sin(theta);
        return new Double2D(x, y);
    }

    private Double2D GetDirection(double fromX, double fromY, double toX, double toY){
        double movementDirectionX = toX - fromX;
        double movementDirectionY = toY - fromY;
        // Normalisation
        double magnitude = Math.sqrt(movementDirectionX*movementDirectionX
                + movementDirectionY*movementDirectionY);
        movementDirectionX = movementDirectionX / magnitude;
        movementDirectionY = movementDirectionY / magnitude;
        return new Double2D(movementDirectionX, movementDirectionY);
    }

    private void Move(Continuous2D yard){
        x += movementDirectionX*VoyageurConstants.vitesse;
        y += movementDirectionY*VoyageurConstants.vitesse;
        yard.setObjectLocation(this, new Double2D(x,y));
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
            int previousCheminSize = cheminEnvisage.size();
            int previousNChangement = nChangementCheminenvisage;
            trouverChemin(stationCourante, destination);
            addToColere(VoyageurConstants.augmentationColereStationFermee
            + VoyageurConstants.augmentationColereParStationSupplementaire*(cheminEnvisage.size() - previousCheminSize)
            + VoyageurConstants.augmentationColereParNvChgtLigne*(nChangementCheminenvisage - previousNChangement));
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
        List<Gare> gareList = StationsDirectory.getInstance().getAllGares();
        List<Station> stations = new ArrayList<>();
        for(Gare gare : gareList){
            stations.addAll(gare.stations.values());
        }
        int n = stations.size();
        int rand = (int)RandomRange(0,n-1);
        return stations.get(rand);
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
        public List<Station> destinations;

        public Node(Station station, int cout, double heuristique, Node previousNode, List<Station> destinations){
            this.station = station;
            this.cout = cout;
            this.heuristique = heuristique;
            this.previousNode = previousNode;
            this.destinations = destinations;
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

    private Queue<Pair<Station, List<Station>>> trouverChemin(Station departS, Station arriveeS){
        Node depart = new Node(departS, 0, 0, null, null);
        Queue<Node> stationFermees = new LinkedList<>();
        PriorityQueue<Node> stationOuvertes = new PriorityQueue<>(new NodeComparator());
        stationOuvertes.add(depart);
        while(!stationOuvertes.isEmpty()){
            Node n = stationOuvertes.poll();
            if(n.station == arriveeS){
                // reconstituer chemin
                return BuildPath(n);
            }
            List<Pair<Station, List<Station>>> voisins_destinations = StationsDirectory.getInstance().getAdjacentStationsWithDestination(n.station);
            for(Pair<Station, List<Station>> voisin_destinations : voisins_destinations){
                Station s = voisin_destinations.getLeft();
                List<Station> destinations = voisin_destinations.getRight();
                if(!(stationFermees.contains(s) || ExisteCoutInferieur(stationOuvertes, s, n.cout+1))){
                    int cout = 1;
                    if(s.lineNumber != n.station.lineNumber){
                        cout = VoyageurConstants.coutChgtStation;
                    }
                    Node newN = new Node(s, n.cout+cout, Distance(s, arriveeS), n, destinations);
                    stationOuvertes.add(newN);
                }
            }
            stationFermees.add(n);
        }
        System.out.println("Erreur, pas de chemin trouvé.");
        return null;
    }

    private Queue<Pair<Station, List<Station>>> BuildPath(Node arrivee){
        nChangementCheminenvisage = 0;
        List<Pair<Station, List<Station>>> stationPath = new ArrayList<>();
        Node currentNode = arrivee;
        String previousLine = currentNode.station.lineNumber;
        List<Station> previousDestinations = currentNode.destinations;
        while(currentNode.previousNode != null){
            if(currentNode.previousNode.station.lineNumber != currentNode.station.lineNumber){
                nChangementCheminenvisage++;
            }
            stationPath.add(new Pair(currentNode.station, currentNode.destinations));
            currentNode = currentNode.previousNode;
            if(previousLine.equals(currentNode.station.lineNumber)){
                currentNode.destinations = previousDestinations;
            }
            previousLine = currentNode.station.lineNumber;
            previousDestinations = currentNode.destinations;
        }
        Collections.reverse(stationPath);
        System.out.println("J'emprunterai le chemin suivant : ");
        for(Pair<Station, List<Station>> station_destination : stationPath){
            System.out.println(station_destination.getLeft().name + " " + station_destination.getLeft().lineNumber);
            System.out.println("Train à destinations de : ");
            for(Station s : station_destination.getRight()){
                System.out.println("    " + s.name + ", " + s.lineNumber);
            }
        }
        return new LinkedList<>(stationPath);
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
}
