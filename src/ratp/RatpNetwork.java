package ratp;

import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class RatpNetwork extends SimState {
    int WIDTH = 5600;
    int HEIGHT = 5600;
    GeomVectorField vectorField = new GeomVectorField(WIDTH, HEIGHT);

    URI absolute_shp = new File(new File(
            "data/ratp_rotated/ratp_pivotated.shp").getCanonicalPath()).toURI();
    URI absolute_db = new File(new File(
            "data/ratp_rotated/ratp_pivotated.dbf").getCanonicalPath()).toURI();

    public RatpNetwork(long seed) throws IOException {
        super(seed);
        Bag attributes = new Bag();
        attributes.add("line");
        attributes.add("stroke"); // TODO: Rename stroke to color
        attributes.add("sectionId");
        attributes.add("origin");
        attributes.add("destination");

        try {
            ShapeFileImporter.read(absolute_shp.toString(), absolute_db.toString(), vectorField, attributes);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
