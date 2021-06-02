package ratp;

import lines.Line;
import rame.Rame;
import ratp.directory.LinesDirectory;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;

import java.util.Map;

public class RatpNetwork extends SimState {

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

}
