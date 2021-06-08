package ratp;

import global.Constants;
import lines.Line;
import rame.Rame;
import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import station.Station;
import voyageur.AgentVoyageur;
import voyageur.VoyageurConstants;
import voyageur.VoyageurDonnees;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;

import java.awt.*;
import java.util.Map;

public class RatpNetwork extends SimState {


    Map<String, Line> lines = LinesDirectory.getInstance().lines;

    public RatpNetwork(long seed){
        super(seed);

        /*for (String s : Constants.listOfLinesNames){
            linesGeomVectorField.put(s, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }*/
        //FileImporter.shapeFileImporterByLine("lines/lines", linesGeomVectorField);

    }
    public Pair<String, GeomVectorField> getLine(String name){
        GeomVectorField l = lines.get(name).geomVectorField;
        Pair returnValue = new Pair <String, GeomVectorField>(name,l);
        return returnValue;
    }

    private void addAgent(String lineName){
        Rame r = new Rame(this, lineName);
        getLine("1").getRight().addGeometry(r.getGeometry());
        this.schedule.scheduleRepeating(r);
    }

    public void start() {
        super.start();
        addAgent("1");
    }

    public void start()
    {
        super.start();
        yard.clear();
        for(int i = 0; i < 20; i++) addVoyageur(StationsDirectory.getInstance().getStation("8", "Balard"));
    }

    public void addVoyageur(Station currentStation){
        AgentVoyageur a = new AgentVoyageur(currentStation, yard);
        schedule.scheduleRepeating(a);
    }

    public void addVoyageur(VoyageurDonnees vD, Station currentStation){
        new AgentVoyageur(vD, currentStation, yard);
    }

    public void removeVoyageur(AgentVoyageur voyageur){
        yard.remove(voyageur);

    }
}
