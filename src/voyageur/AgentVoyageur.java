package voyageur;

import global.Constants;
import ratp.RatpNetwork;

import ratp.directory.StationsDirectory;
import sim.app.geo.campusworld.Agent;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;
import sim.util.Int2D;
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
public class AgentVoyageur implements Steppable{



    public int colere;

    public String getDestination() {
        return destination.name;
    }

    public String getStationCourante() {
        return stationCourante.name;
    }

    public long getTime() {
        return time;
    }

    public Station destination;
    public Station stationCourante;
    public Queue<Pair<Station, List<Station>>> cheminEnvisage;
    public long   time;
    //Constants.ATTENTE_MCT;
    int nChangementCheminenvisage;
    Queue<Pair<Station, List<Station>>> descentes;

    int etat = 0;
    double x, y;
    double goalX, goalY;
    double movementDirectionX;
    double movementDirectionY;

    public boolean enTrain = false;

    int updateColere = -1;

    double colereMoyenneAdjacente = -1;

    // Réapparition d'un voyageur au sortir d'une rame
    public AgentVoyageur(VoyageurDonnees donnees, Station stationCourante, Continuous2D yard) {
        destination = donnees.destination;
        cheminEnvisage = donnees.cheminEnvisage;
        colere = donnees.colere;
        colereMoyenneAdjacente = colere;
        InitialisationDansStation(stationCourante, yard);
    }

    public AgentVoyageur(Station stationCourante, Continuous2D yard,long delayTime){
        InitialisationDansStation(stationCourante, yard);

        //destination=StationsDirectory.getInstance().getStation("13","Saint-Denis - Université");
        destination = DeterminerDestination();
        //System.out.println("Je suis à : " + stationCourante.name + " " + stationCourante.lineNumber + " et je veux aller à : " + destination.name + " " + destination.lineNumber);
        cheminEnvisage = trouverChemin(stationCourante, destination);
        if(cheminEnvisage.isEmpty()){
            System.out.println("What");
            System.out.println("Je suis à : " + stationCourante.name + " " + stationCourante.lineNumber + " et je veux aller à : " + destination.name + " " + destination.lineNumber);
            System.out.println("Nbr chgt : " + nChangementCheminenvisage);
        }
        //System.out.println("Nbr chgt : " + nChangementCheminenvisage);

        Random R = new Random();
        int rand = (int) (R.nextGaussian() * 80);
        if (rand < 0) colere = 0;
        else if (rand > VoyageurConstants.colereMax) colere = VoyageurConstants.colereMax;
        else colere = rand;
        //System.out.println(colere);
    }

    public double getColereAllStations() {
        return StationsDirectory.getInstance().getColereAllStations();
    }

    public void SortirDeRame(Continuous2D yard, Station stationCourante){
        colereMoyenneAdjacente = colere;
        InitialisationDansStation(stationCourante, yard);
    }

    public void InitialisationDansStation(Station station, Continuous2D yard){
        this.stationCourante = station;
        Double2D stationPosGeom = new Double2D(stationCourante.location.getX(), stationCourante.location.getY());
        Double2D stationPosCont = ConversionGeomToContinuous(stationPosGeom);
        Double2D location = GetRandomPointCircle(stationPosCont, VoyageurConstants.maximumDistanceStation);
        yard.setObjectLocation(this, location);
        x = location.x;
        y = location.y;
        //
        etat = 0;
        enTrain = false;
    }

    private Double2D ConversionGeomToContinuous(Double2D c) {
        return new Double2D(c.x * 456374.2102649563 + 621637.6325538646, c.y * (-441528.2983009379) - 2640599.3936298448);
    }

    // On suppose qu'un voyageur ne peut pas être instancié si sa liste est vide
    @Override
    public void step(SimState simState) {
        if(enTrain){
            return;
        }
        RatpNetwork ratpState = (RatpNetwork) simState;

        if(etat == 0){
            if(Math.random() < VoyageurConstants.probabiliteDeBouger){
                // Random point
                Double2D stationPosGeom = new Double2D(stationCourante.location.getX(), stationCourante.location.getY());
                Double2D stationPosCont = ConversionGeomToContinuous(stationPosGeom);
                Double2D goal = GetRandomPointCircle(stationPosCont, VoyageurConstants.maximumDistanceStation);
                goalX = goal.x;
                goalY = goal.y;
                // Calcul direction
                Double2D movementDirection = GetDirection(x, y, goalX, goalY);
                movementDirectionX = movementDirection.x;
                movementDirectionY = movementDirection.y;
                etat = 1;
            }
        } else if(etat == 1){
            Move(ratpState.yard);
            if (isArrive()) {
                etat = 0;
            }
        }
        updateColere++;
        if(updateColere < VoyageurConstants.updateColere){
            return;
        }
        updateColere = 0;
        CalculerColereAdjacente(ratpState.yard);
        if(colereMoyenneAdjacente > colere){
            addToColere(1);//colere + (colereMoyenneAdjacente - colere) * VoyageurConstants.vitesseDeColerisation;
        }
        else if(colereMoyenneAdjacente < colere){
            addToColere(-1);
        }
    }

    private void CalculerColereAdjacente(Continuous2D yard){
        int sommeColeresAdjacente = 0;
        int avNb = 0;
        Bag nearObjects = yard.getNeighborsWithinDistance(new Double2D(x,y), VoyageurConstants.distanceInfluence, false, false);

        if(nearObjects != null){
            for(Object o : nearObjects){
                AgentVoyageur av = (AgentVoyageur) o;
                if(av != null && av.stationCourante.name == stationCourante.name){
                    sommeColeresAdjacente += av.colere;
                    avNb++;
                }
            }
        }
        colereMoyenneAdjacente = sommeColeresAdjacente / avNb;
        if(colereMoyenneAdjacente > colere){
            addToColere(1);//colere + (colereMoyenneAdjacente - colere) * VoyageurConstants.vitesseDeColerisation;
        }
        else if(colereMoyenneAdjacente < colere){
            addToColere(-1);
        }

    }

    private Double2D GetRandomPointCircle(Double2D point, double distance) {
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

    private Double2D GetDirection(double fromX, double fromY, double toX, double toY) {
        double movementDirectionX = toX - fromX;
        double movementDirectionY = toY - fromY;
        // Normalisation
        double magnitude = Math.sqrt(movementDirectionX * movementDirectionX
                + movementDirectionY * movementDirectionY);
        movementDirectionX = movementDirectionX / magnitude;
        movementDirectionY = movementDirectionY / magnitude;
        return new Double2D(movementDirectionX, movementDirectionY);
    }

    private void Move(Continuous2D yard) {
        x += movementDirectionX * VoyageurConstants.vitesse;
        y += movementDirectionY * VoyageurConstants.vitesse;
        yard.setObjectLocation(this, new Double2D(x, y));
    }

    private boolean isArrive() {
        return Math.sqrt((goalX - x) * (goalX - x) + (goalY - y) * (goalY - y)) < 0.5f;
    }

    private float RandomRange(float min, float max) {
        return min + (float) Math.random() * max;
    }

    // permet d'informer l'agent qu'une station a été fermée, si elle est sur son chemin il recalcule son trajet
    public void FermetureStation(Station station){
        Station previousStation = cheminEnvisage.peek().getLeft();
        Iterator<Pair<Station, List<Station>>> itr = cheminEnvisage.iterator();
        itr.next();
        while(itr.hasNext()){
            Station currentStation = itr.next().getLeft();
            if(previousStation.name == station.name && previousStation.lineNumber == station.lineNumber){
                if(currentStation.lineNumber != previousStation.lineNumber){
                    int previousCheminSize = cheminEnvisage.size();
                    int previousNChangement = nChangementCheminenvisage;
                    trouverChemin(stationCourante, destination);
                    addToColere(VoyageurConstants.augmentationColereStationFermee
                            + VoyageurConstants.augmentationColereParStationSupplementaire*(cheminEnvisage.size() - previousCheminSize)
                            + VoyageurConstants.augmentationColereParNvChgtLigne*(nChangementCheminenvisage - previousNChangement));

                    return;
                }
            }
        }
    }

    private void addToColere(int a) {
        colere += a;
        if (colere > VoyageurConstants.colereMax) {
            colere = VoyageurConstants.colereMax;
        }
        else if(colere < 0){
            colere = 0;
        }
    }

    // détermine une destination au hasard
    private Station DeterminerDestination(){

        List<Station> stations = new ArrayList<>(StationsDirectory.getInstance().stationsOuvertes);
        stations.remove(stationCourante);

        int n = stations.size();
        int rand = (int)RandomRange(0,n-1);

        //return StationsDirectory.getInstance().getStation("8","Pointe du Lac");
        return stations.get(rand);
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

    public Queue<Pair<Station, List<Station>>> trouverChemin(Station departS, Station arriveeS){
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
                        if(n.station.isFermee()){
                            // on veut changer de ligne, mais la ligne est cassée
                            continue;
                        }
                        cout = VoyageurConstants.coutChgtStation;
                    }
                    Node newN = new Node(s, n.cout+cout, Distance(s, arriveeS), n, destinations);
                    stationOuvertes.add(newN);
                }
            }
            stationFermees.add(n);
        }
        //System.out.println("Erreur, pas de chemin trouvé.");
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
        /*System.out.println("J'emprunterai le chemin suivant : ");
        for(Pair<Station, List<Station>> station_destination : stationPath){
            System.out.println(station_destination.getLeft().name + " " + station_destination.getLeft().lineNumber);
            System.out.println("Train à destinations de : ");
            for(Station s : station_destination.getRight()){
                System.out.println("    " + s.name + ", " + s.lineNumber);
            }
        }*/
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
