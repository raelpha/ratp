package ratp;

import lines.Line;
import ratp.directory.LinesDirectory;
import sim.engine.SimState;

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

}
