package ratp.utils;

import com.vividsolutions.jts.geom.Envelope;
import global.Constants;
import sim.io.geo.ShapeFileImporter;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public static void shapeFileImporterByLine(String name, Map<String,GeomVectorField> lines){
        for(String line : Constants.listOfLinesNames){
            lines.put(line, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }

        GeomVectorField allLines = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);

        Bag attributes = new Bag();
        attributes.addAll(defaultAttributes);
        try {
            URI absolute_shp = new File(new File(
                    "data/"+name+".shp").getCanonicalPath()).toURI();
            URI absolute_db = new File(new File(
                    "data/"+name+".dbf").getCanonicalPath()).toURI();
            ShapeFileImporter.read(absolute_shp.toString(), absolute_db.toString(), allLines, attributes);
        } catch (Exception e) {
            System.out.println(e);
        }

        for(Object o : allLines.getGeometries()){
            MasonGeometry mg = (MasonGeometry) o;
            lines.get(mg.getStringAttribute("line")).addGeometry(mg);
        }

        Envelope MBR = new Envelope();

        for(String line : Constants.listOfLinesNames){
            MBR.expandToInclude(lines.get(line).getMBR());
        }

        for(String line : Constants.listOfLinesNames){
            lines.get(line).setMBR(MBR);
        }
    }

}
