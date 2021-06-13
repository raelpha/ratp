package ratp;

import global.Constants;
import lines.Line;
import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import station.Gare;
import station.Station;
import voyageur.AgentVoyageur;
import voyageur.VoyageurConstants;
import voyageur.VoyageurDonnees;


import java.util.List;
import java.util.Map;

public class RatpNetwork extends SimState {

    public Continuous2D yard = new Continuous2D(VoyageurConstants.Discretisation, Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    Map<String, Line> lines = LinesDirectory.getInstance().lines;

    public RatpNetwork(long seed){
        super(seed);

        /*for (String s : Constants.listOfLinesNames){
            linesGeomVectorField.put(s, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }*/
        //FileImporter.shapeFileImporterByLine("lines/lines", linesGeomVectorField);

    }

    public void start()
    {
        super.start();
        yard.clear();
        //StationsDirectory.getInstance().getStation("8", "Balard")
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
