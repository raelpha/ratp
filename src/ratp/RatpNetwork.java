package ratp;

import global.Constants;
import lines.Line;
import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import station.Station;
import voyageur.AgentVoyageur;
import voyageur.VoyageurConstants;
import voyageur.VoyageurDonnees;

import java.awt.*;
import java.util.Map;

public class RatpNetwork extends SimState {

    public Continuous2D yard = new Continuous2D(VoyageurConstants.Discretisation, Constants.FIELD_SIZE, Constants.FIELD_SIZE);

    /**linesGeomVectorField contains all the LineString of the network*/
    //Map<String,GeomVectorField> linesGeomVectorField = new HashMap<>();

    /**We WILL make another Graph here for passenger interconnection*/
    //public GeomPlanarGraph passengerNetwork = new GeomPlanarGraph();

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

    /*public GeomVectorField geomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    public GeomVectorFieldPortrayal geomVectorFieldPortrayal = new GeomVectorFieldPortrayal();
    public void setupPortrayal(){
        geomVectorFieldPortrayal.setField(geomVectorField);
        geomVectorFieldPortrayal.setPortrayalForAll(new GeomPortrayal(){
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
                MasonGeometry geometry = (MasonGeometry) object;
                paint = Color.decode(geometry.getStringAttribute("color"));
                filled = true;
                scale = 0.000003D;
                super.draw(object, graphics, info);
            }
        });*/
}
