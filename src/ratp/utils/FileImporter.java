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
            lines.get(mg.getStringAttribute("line")).addGeometry(mg);
            //stations.put(mg.getStringAttribute("origin")+mg.getStringAttribute("line"), new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
            //stations.addGeometry();
            //MasonGeometry mg_station = mg.get;//TODO: NO !
            String debukfbdj =  mg.getGeometry().toString();
            try {
               if(mg.getGeometry().getGeometryType() != "MultiLineString") {
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

                   stations.addGeometry(origin_station_mg);
                   stations.addGeometry(destination_station_mg);
               }
                int dnjs = 9;
            } catch (ParseException var6) {
                System.out.println("Bogus line string" + var6);
            }
            String dsjhk = "jfdb";
            //stations.get(mg.getStringAttribute("origin")+mg.getStringAttribute("line")).addGeometry(mg_station);
            //TODO:Ajouter la destination aussi !
        }

        Envelope MBR = new Envelope();

        //TODO: not the best practice
        for(String line : Constants.listOfLinesNames){
            MBR.expandToInclude(lines.get(line).getMBR());
        }

        MBR.expandToInclude(stations.getMBR());

       //MBR.expandBy(MBR.getHeight()*0.1, MBR.getWidth()*0.5*0.1);

        //TODO: not the best practice
        for(String line : Constants.listOfLinesNames){
            lines.get(line).setMBR(MBR);
        }



        stations.setMBR(MBR);

    }

}
