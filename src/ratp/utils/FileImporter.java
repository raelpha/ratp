package ratp.utils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
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

    //TODO: Move this in Constants
    //TODO: WARNING ! "destination" = "destinatio" (yeah, wtf ?)
    public static List<String> defaultAttributes = Arrays.asList("line", "stroke", "sectionId","origin","destinatio");

    public static void shapeFileImporterByLine(String name, Map<String,GeomVectorField> lines, GeomVectorField stations){
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

        WKTReader rdr = new WKTReader();

        for(Object o : allLines.getGeometries()){
            MasonGeometry mg = (MasonGeometry) o;

            //Adding lines
            lines.get(mg.getStringAttribute("line")).addGeometry(mg);

            //Adding stations
            try {
                //TODO: Issue with line 14: multilinesting to linestring !
               if(mg.getGeometry().getGeometryType() == "LineString") {
                   Point origin_station_point = ((LineString) rdr.read(mg.getGeometry().toString())).getStartPoint();
                   Point destination_station_point = ((LineString) rdr.read(mg.getGeometry().toString())).getEndPoint();

                   MasonGeometry origin_station_mg = new MasonGeometry(origin_station_point);
                   origin_station_mg.addStringAttribute("name", mg.getStringAttribute("origin"));
                   origin_station_mg.addStringAttribute("line", mg.getStringAttribute("line"));
                   origin_station_mg.addStringAttribute("stroke", mg.getStringAttribute("stroke"));

                   MasonGeometry destination_station_mg = new MasonGeometry(destination_station_point);
                   destination_station_mg.addStringAttribute("name", mg.getStringAttribute("destinatio"));
                   destination_station_mg.addStringAttribute("line", mg.getStringAttribute("line"));
                   destination_station_mg.addStringAttribute("stroke", mg.getStringAttribute("stroke"));

                   if(!stations.getGeometries().contains(origin_station_mg))
                       stations.addGeometry(origin_station_mg);
                   if(!stations.getGeometries().contains(destination_station_mg))
                        stations.addGeometry(destination_station_mg);
               }
            } catch (ParseException var6) {
                System.out.println("Bogus line string" + var6);
            }
        }

        Envelope MBR = new Envelope();

        for (Map.Entry<String, GeomVectorField> l : lines.entrySet()) {
            MBR.expandToInclude(l.getValue().getMBR());
        }

        MBR.expandToInclude(stations.getMBR());

        MBR.expandBy(MBR.getHeight()*0.1, MBR.getWidth()*0.5*0.1);

        for (Map.Entry<String, GeomVectorField> l : lines.entrySet()) {
            l.getValue().setMBR(MBR);
        }

        stations.setMBR(MBR);
    }

}
