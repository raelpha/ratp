package ratp;

import global.Constants;
import lines.Line;
import rame.RameFactory;
import rame.Rame;
import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import ratp.directory.SchedulesDirectory.*;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.Bag;
import station.Gare;
import station.Station;
import voyageur.AgentVoyageur;
import voyageur.VoyageurConstants;
import sim.app.geo.masoncsc.util.Pair;
import sim.field.geo.GeomVectorField;
import sim.util.geo.MasonGeometry;

import java.util.*;

public class RatpNetwork extends SimState {

    public Continuous2D yard = new Continuous2D(VoyageurConstants.Discretisation, Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    Map<String, Line> lines = LinesDirectory.getInstance().lines;
    RameFactory factory = RameFactory.getInstance();

    public RatpNetwork(long seed) {
        super(seed);

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

    public float getAllRameStopped() {
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

    private void removeAllRame(){
        factory.clear();
        Iterator lineIt = lines.values().iterator();
        while(lineIt.hasNext()){
            List<MasonGeometry> toDelete = new ArrayList<>();
            Line elem = (Line) lineIt.next();
            Bag obj = elem.geomVectorField.getGeometries();
            Iterator objIt = obj.iterator();
            while(objIt.hasNext()){
                MasonGeometry mgElem = (MasonGeometry) objIt.next();
                if(mgElem.hasAttribute("type") && mgElem.getStringAttribute("type").equals("rame")){
                    toDelete.add(mgElem);
                }
            }
            Iterator removeIt = toDelete.iterator();
            while(removeIt.hasNext()){
                elem.geomVectorField.removeGeometry((MasonGeometry) removeIt.next());
            }
        }

    }

    public void start() {
        super.start();
        removeAllRame();
        yard.clear();
        for (Map.Entry<String, Gare> g: StationsDirectory.getInstance().gares.entrySet()) {
          this.schedule.scheduleRepeating(g.getValue());
            for (Map.Entry<String, Station> entry : g.getValue().stations.entrySet()) {
                this.schedule.scheduleRepeating(entry.getValue());
            }
        }
        // ce code est la fameuse factory qui cr??e un rame pour chaque schedule (attention les rame qui arrive en face s'arr??teront)
        factory.setBaseRame(this);
        this.schedule.scheduleRepeating(factory);

        //ce code permet de tester en ajouter des rames ?? l'unit??, il suffit de pr??ciser la ligne et la liste de schedule de la rame
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

    public void setRameMaxSpeed(double val){
        Constants.rameMaxSpeed = val;
    }

    public double getRameMaxSpeed(){
        return Constants.rameMaxSpeed;
    }

    public void setRameAcceleration(double val){
        Constants.rameAcceleration = val;
    }

    public double getRameAcceleration(){
        return Constants.rameAcceleration;
    }

    public void setRameBraking(double val){
        Constants.rameBraking = val;
    }

    public double getRameBraking(){
        return Constants.rameBraking;
    }

    public void setGeneratingMode(int val){
        System.out.println(val);
        Constants.generateMode = val;
    }

    public int getGeneratingMode(){
        return Constants.generateMode;
    }

    public void setAttenteRame(int val){
        Constants.attenteRame = val;
    }

    public int getAttenteRame(){
        return Constants.attenteRame;
    }




    public void setColereMoyenneDepart(int val) {
        VoyageurConstants.colereMoyenneDeDepart = val;
    }

    public int getColereMoyenneDepart() {
        return VoyageurConstants.colereMoyenneDeDepart;
    }

}
