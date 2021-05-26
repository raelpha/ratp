package ratp.utils;

import sim.io.geo.ShapeFileImporter;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class FileImporter {

    public static List<String> defaultAttributes = Arrays.asList("line", "stroke", "sectionId","origin","destination");

    public static void shapeFileImporter(String name, GeomVectorField gvf){
        Bag attributes = new Bag();
        attributes.addAll(defaultAttributes);
        try {
            URI absolute_shp = new File(new File(
                    "data/"+name+".shp").getCanonicalPath()).toURI();
            URI absolute_db = new File(new File(
                    "data/"+name+".dbf").getCanonicalPath()).toURI();
            ShapeFileImporter.read(absolute_shp.toString(), absolute_db.toString(), gvf, attributes);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
