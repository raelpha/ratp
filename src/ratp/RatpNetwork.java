package ratp;

import global.Constants;
import ratp.utils.FileImporter;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;

public class RatpNetwork extends SimState {

    GeomVectorField linesGeomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);

    public RatpNetwork(long seed){
        super(seed);

        FileImporter.shapeFileImporter("ratp_rotated/ratp_pivotated", linesGeomVectorField);

    }
}
