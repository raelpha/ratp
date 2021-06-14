package ratp;

import global.Constants;
import lines.Line;
import rame.RameFactory;
import rame.Rame;
import ratp.directory.LinesDirectory;
import ratp.directory.SchedulesDirectory;
import ratp.directory.StationsDirectory;
import ratp.directory.SchedulesDirectory.*;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.Double2D;
import station.Gare;
import station.Station;
import voyageur.AgentVoyageur;
import voyageur.VoyageurConstants;
import voyageur.VoyageurDonnees;
import sim.app.geo.masoncsc.util.Pair;
import sim.field.geo.GeomVectorField;
import sim.util.geo.MasonGeometry;

import java.util.*;


import java.util.List;
import java.util.Map;

public class RatpNetwork extends SimState {

    public Continuous2D yard = new Continuous2D(VoyageurConstants.Discretisation, Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    Map<String, Line> lines = LinesDirectory.getInstance().lines;
    RameFactory factory = RameFactory.getInstance();

    public RatpNetwork(long seed) {
        super(seed);

        /*for (String s : Constants.listOfLinesNames){
            linesGeomVectorField.put(s, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }*/
        //FileImporter.shapeFileImporterByLine("lines/lines", linesGeomVectorField);
        //this.schedule.scheduleRepeating(a);
    }

    public AgentVoyageur addVoyageur (Station currentStation){
        AgentVoyageur a = new AgentVoyageur(currentStation, yard,Constants.ATTENTE_MCT);
        //System.out.println("liste avant : "+currentStation.getListAttenteRame());
        currentStation.getListAttenteRame().add(a);
        //System.out.println("liste att : "+currentStation.getListAttenteRame());
        schedule.scheduleRepeating(a);
        return a;
    }


    public double getAllColere() {
        double colere = 0;
        for (Map.Entry<String, Line> entry : this.lines.entrySet()) {
            for (Map.Entry<String, Station> entryStations : entry.getValue().stations.entrySet()) {
                colere += entryStations.getValue().getColereStation();
            }
        }
        return colere / 305;
    }

    public int getAllRameStopped() {
        return factory.getNbRameStopped();
    }

    /* Exemple of Model use: to adapt with your constants */
    public void setVitessePassenger(float val) {
        VoyageurConstants.vitesse = val;
    }

    public float getVitessePassenger() {
        return VoyageurConstants.vitesse;
    }
    /* ------------------------------------------------- */


    public Pair<String, GeomVectorField> getLine(String name){
        GeomVectorField l = lines.get(name).geomVectorField;
        Pair returnValue = new Pair <String, GeomVectorField>(name,l);
        return returnValue;
    }

    public GeomVectorFieldPortrayal getPortrayal (String name){
        return lines.get(name).geomVectorFieldPortrayal;
    }

    private void addAgent(String lineName, List<Schedule> schedules){
        Rame r = new Rame(this, lineName, schedules);
        MasonGeometry rameGeometry = r.getGeometry();
        rameGeometry.addAttribute("type", "rame");
        rameGeometry.addAttribute("direction", Integer.toString(schedules.get(0).direction));
        rameGeometry.addAttribute("rame", r);
        getLine(lineName).getRight().addGeometry(r.getGeometry());
        this.schedule.scheduleRepeating(r);

    }

    public void start() {
        super.start();
        yard.clear();
        //for(int i = 0; i < 20; i++) addVoyageur(StationsDirectory.getInstance().getStation("8", "Balard"));
        //for(int i = 0; i < 20; i++) addVoyageur(StationsDirectory.getInstance().getStation("13", "Liège"));
        for (Map.Entry<String, Gare> g: StationsDirectory.getInstance().gares.entrySet()) {
          this.schedule.scheduleRepeating(g.getValue());
            for (Map.Entry<String, Station> entry : g.getValue().stations.entrySet()) {
                this.schedule.scheduleRepeating(entry.getValue());
            }
        }
        // ce code est la fameuse factory qui crée un rame pour chaque schedule (attention les rame qui arrive en face s'arrêteront)
        factory.setBaseRame(this);
        this.schedule.scheduleRepeating(factory);

        //ce code permet de tester en ajouter des rames à l'unité, il suffit de préciser la ligne et la liste de schedule de la rame
        //SchedulesDirectory sd = SchedulesDirectory.getInstance();
        //List<Schedule> schedules = sd.schedules.get("1").get("La Défense -> Château de Vincennes");
        //addAgent("1", schedules);
        //addAgent("1", sd.schedules.get("1").get("Château de Vincennes -> La Défense"));
        //addAgent("3", sd.schedules.get("3").get("Pont de Levallois - Bécon -> Gallieni"));
        //addAgent("6");
    }


    public void removeVoyageur(AgentVoyageur voyageur){
        yard.remove(voyageur);
    }

    public void setPerimetreStations(float val) {
        VoyageurConstants.maximumDistanceStation = val;
    }

    public float getPerimetreStations() {
        return VoyageurConstants.maximumDistanceStation;
    }

    public void setAugmentationColereStationFermee(int val) {
        VoyageurConstants.augmentationColereStationFermee = val;
    }

    public int getAugmentationColereStationFermee() {
        return VoyageurConstants.augmentationColereStationFermee;
    }

    public void setAugmentationColereParStationSupplementaire(int val) {
        VoyageurConstants.augmentationColereParStationSupplementaire = val;
    }

    public int getAugmentationColereParStationSupplementaire() {
        return VoyageurConstants.augmentationColereParStationSupplementaire;
    }

    public void setAugmentationColereParChgtLigneStationSupplementaire(int val) {
        VoyageurConstants.augmentationColereParNvChgtLigne = val;
    }

    public int getAugmentationColereParChgtLigneStationSupplementaire() {
        return VoyageurConstants.augmentationColereParNvChgtLigne;
    }
}
