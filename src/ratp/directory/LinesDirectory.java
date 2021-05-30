package ratp.directory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import global.Constants;
import lines.Line;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinesDirectory {

    private static LinesDirectory INSTANCE = new LinesDirectory();

    public static LinesDirectory getInstance()
    {
        return INSTANCE;
    }

    public static void initialize()
    {
        LinesDirectory s = getInstance();
    }

    /**lines is a <int lineId, Line line> mapping the line id to a Line*/
    public Map<String, Line> lines = new HashMap<>();

    private LinesDirectory(){
        for(String lineNumber : Constants.listOfLinesNames){
            lines.put(lineNumber, new Line(lineNumber));
        }
        loadLines(lines);
    }

    public static void loadLines(Map<String, Line> lines){

        GeomVectorField allLines = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);

        Bag attributes = new Bag();
        attributes.addAll(Constants.LINE_DEFAULTATTRIBUTES);
        try {
            URI absolute_shp = new File(new File(
                    Constants.LINES_FILESNAMES+".shp").getCanonicalPath()).toURI();
            URI absolute_db = new File(new File(
                    Constants.LINES_FILESNAMES+".dbf").getCanonicalPath()).toURI();
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
            lines.get(mg.getStringAttribute("line")).geomVectorField.addGeometry(section_mg);

            //Adding stations
            try {
                if (mg.getGeometry().getGeometryType().equals("LineString")) {
                    Point origin_station_point = ((LineString) rdr.read(mg.getGeometry().toString())).getStartPoint();
                    Point destination_station_point = ((LineString) rdr.read(mg.getGeometry().toString())).getEndPoint();

                    MasonGeometry origin_station_mg = new MasonGeometry(origin_station_point);
                    origin_station_mg.addStringAttribute("type", "station");
                    origin_station_mg.addStringAttribute("name", mg.getStringAttribute("origin"));
                    origin_station_mg.addStringAttribute("line", mg.getStringAttribute("line"));
                    origin_station_mg.addStringAttribute("color", mg.getStringAttribute("color"));

                    MasonGeometry destination_station_mg = new MasonGeometry(destination_station_point);
                    destination_station_mg.addStringAttribute("type", "station");
                    destination_station_mg.addStringAttribute("name", mg.getStringAttribute("destinatio"));
                    destination_station_mg.addStringAttribute("line", mg.getStringAttribute("line"));
                    destination_station_mg.addStringAttribute("color", mg.getStringAttribute("color"));

                    //Quickfix, because the two ends of the sections are added, we do not add a station if it's been added before
                    if(!lines.get(mg.getStringAttribute("line")).geomVectorField.getGeometries().contains(origin_station_mg))
                        lines.get(mg.getStringAttribute("line")).geomVectorField.addGeometry(origin_station_mg);

                    //We add a reference (from StationsDirectory) to the map of stations in the line
                    lines.get(mg.getStringAttribute("line")).stations.put(
                            mg.getStringAttribute("origin"),
                            StationsDirectory.getInstance().superStations.get(mg.getStringAttribute("origin")).stations.get(mg.getStringAttribute("origin"))
                    );


                    //Quickfix, because the two ends of the sections are added, we do not add a station if it's been added before
                    if(!lines.get(mg.getStringAttribute("line")).geomVectorField.getGeometries().contains(destination_station_mg))
                        lines.get(mg.getStringAttribute("line")).geomVectorField.addGeometry(destination_station_mg);


                    //We add a reference (from StationsDirectory) to the map of stations in the line
                    lines.get(mg.getStringAttribute("line")).stations.put(
                            mg.getStringAttribute("destinatio"),
                            StationsDirectory.getInstance().superStations.get(mg.getStringAttribute("destinatio")).stations.get(mg.getStringAttribute("destinatio"))
                    );


                }
            } catch (ParseException e) {
                System.out.println("Bogus line string: " + e);
            }
        }

        //Envelope Minimum Bounding Rectangle
        Envelope MBR = new Envelope();

        //We find the envelope containing all the lines (by expanding it to fit each line)
        for (String lineNumber : Constants.listOfLinesNames) {
            MBR.expandToInclude(lines.get(lineNumber).geomVectorField.getMBR());
        }

        //A quickfix to shrink the width
        MBR.expandBy(MBR.getHeight()*0.1, MBR.getWidth()*0.5*0.1);

        //We assign the obtained maximum MBR for all the lines (and stations)
        for (String lineNumber : Constants.listOfLinesNames) {
            lines.get(lineNumber).geomVectorField.setMBR(MBR);
        }

    }

    /*On Debug*/
    public static void main(String[] args){
        LinesDirectory ld = LinesDirectory.getInstance();
        int i =0;
    }

}
