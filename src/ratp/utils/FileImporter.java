package ratp.utils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import global.Constants;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FileImporter {

    //TODO: Move this in Constants
    public static List<String> defaultAttributes = Arrays.asList("line", "stroke", "sectionId","origin","destinatio");

    public static void shapeFileImporterByLine(String name, Map<String,GeomVectorField> lines){

        //Initialize a GeomVectorField for each (hardcoded) line
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

        //Probably not the best way of doing it...
        WKTReader rdr = new WKTReader();

        for (Object o : allLines.getGeometries()) {
            MasonGeometry mg = (MasonGeometry) o;

            //Adding lines
            MasonGeometry section_mg = mg;
            section_mg.addStringAttribute("type", "section");
            lines.get(mg.getStringAttribute("line")).addGeometry(section_mg);

            //Adding stations
            try {
                if (mg.getGeometry().getGeometryType().equals("LineString")) {
                    Point origin_station_point = ((LineString) rdr.read(mg.getGeometry().toString())).getStartPoint();
                    Point destination_station_point = ((LineString) rdr.read(mg.getGeometry().toString())).getEndPoint();

                    MasonGeometry origin_station_mg = new MasonGeometry(origin_station_point);
                    origin_station_mg.addStringAttribute("type", "station");
                    origin_station_mg.addStringAttribute("name", mg.getStringAttribute("origin"));
                    origin_station_mg.addStringAttribute("line", mg.getStringAttribute("line"));
                    origin_station_mg.addStringAttribute("stroke", mg.getStringAttribute("stroke"));

                    MasonGeometry destination_station_mg = new MasonGeometry(destination_station_point);
                    destination_station_mg.addStringAttribute("type", "station");
                    destination_station_mg.addStringAttribute("name", mg.getStringAttribute("destinatio"));
                    destination_station_mg.addStringAttribute("line", mg.getStringAttribute("line"));
                    destination_station_mg.addStringAttribute("stroke", mg.getStringAttribute("stroke"));

                    //Quickfix, because the two ends of the sections are added, we do not add a station if it's been added before
                    if(!lines.get(mg.getStringAttribute("line")).getGeometries().contains(origin_station_mg))
                        lines.get(mg.getStringAttribute("line")).addGeometry(origin_station_mg);

                    //Quickfix, because the two ends of the sections are added, we do not add a station if it's been added before
                    if(!lines.get(mg.getStringAttribute("line")).getGeometries().contains(destination_station_mg))
                        lines.get(mg.getStringAttribute("line")).addGeometry(destination_station_mg);

                }
            } catch (ParseException e) {
                System.out.println("Bogus line string: " + e);
            }
        }

        //Envelope Minimum Bounding Rectangle
        Envelope MBR = new Envelope();

        //We find the envelope containing all the lines (by expanding it to fit each line)
        for (Map.Entry<String, GeomVectorField> l : lines.entrySet()) {
            MBR.expandToInclude(l.getValue().getMBR());
        }

        //A quickfix to shrink the width
        MBR.expandBy(MBR.getHeight()*0.1, MBR.getWidth()*0.5*0.1);

        //We assign the obtained maximum MBR for all the lines (and stations)
        for (Map.Entry<String, GeomVectorField> l : lines.entrySet()) {
            l.getValue().setMBR(MBR);
        }
    }

}
