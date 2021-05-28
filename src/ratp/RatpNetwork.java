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
    Map<String,GeomVectorField> linesGeomVectorField = new HashMap<>();;

    /**stationsGeomVectorField contains all the POINTS of the network*/
    GeomVectorField stationsGeomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);

    /**We WILL make another Graph here for passenger interconnection*/
    //public GeomPlanarGraph passengerNetwork = new GeomPlanarGraph();

    public RatpNetwork(long seed){
        super(seed);

        for (String s : Constants.listOfLinesNames){
            linesGeomVectorField.put(s, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }
        FileImporter.shapeFileImporterByLine("network/ratp", linesGeomVectorField, stationsGeomVectorField);


    }

}
