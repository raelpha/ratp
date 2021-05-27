package ratp;

import global.Constants;
import ratp.utils.FileImporter;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.util.geo.GeomPlanarGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RatpNetwork extends SimState {

    /**linesGeomVectorField contains all the LineString of the network*/
    GeomVectorField linesGeomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    Map<String,GeomVectorField> linesGeomVectorFieldByLine = new HashMap<>();;

    /**We WILL make another Graph here for passenger interconnection*/
    // To be made as an union of linesNetwork
    public GeomPlanarGraph passengerNetwork = new GeomPlanarGraph();

    public List<GeomPlanarGraph> linesNetwork;



    public RatpNetwork(long seed){
        super(seed);

        for (String s : Constants.listOfLinesNames){
            linesGeomVectorFieldByLine.put(s, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }
        FileImporter.shapeFileImporterByLine("ratp_rotated/ratp_pivotated", linesGeomVectorFieldByLine);
    }



}
