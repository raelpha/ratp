package ratp;

import global.Constants;
import ratp.utils.FileImporter;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;

public class RatpNetwork extends SimState {

    /**linesGeomVectorField contains all the LineString of the network*/
    GeomVectorField linesGeomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);

    /**We WILL make another Graph here for passenger interconnection*/
    //TODO

    public RatpNetwork(long seed){
        super(seed);

        FileImporter.shapeFileImporter("ratp_rotated/ratp_pivotated", linesGeomVectorField);

    }
}
