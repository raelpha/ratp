package ratp.directory;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import global.Constants;
import lines.Line;
import sim.field.geo.GeomVectorField;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.util.geo.MasonGeometry;
import station.Gare;
import station.Station;
import station.SuperStation;

import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StationsDirectory {

    /**WARNING: SCHEDULES DIRECTIORY MUST BE INITIALIZED (DATA LOADED)*/

    private static StationsDirectory INSTANCE = new StationsDirectory();

    public static StationsDirectory getInstance()
    {
        return INSTANCE;
    }

    public static void initialize()
    {
        StationsDirectory s = getInstance();
    }

    public List<SuperStation> getAllSuperStations() {
        return allSuperStations;
    }

    private List<SuperStation> allSuperStations;

    Map<String, SuperStation> superStations;

    //geomVectorField used to store all Gare (SuperStations with > 1 station) geometries
    public GeomVectorField geomVectorFieldGare = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    // Field portrayal in which we're drawing
    public GeomVectorFieldPortrayal geomVectorFieldGarePortrayal =  new GeomVectorFieldPortrayal();



    /**
     * Constructor: fill allSuperStations from external file then create associated map
     */
    private StationsDirectory()
    {
        allSuperStations = allSuperStationsReader(Constants.STATIONS_FILENAME);
        superStations = fillSuperStationsMap(allSuperStations);
    }

    public Station getStation(String lineId, String stationName){
        return superStations.get(stationName).getStation(lineId);
    }

    public void instantiateStation(Line line, String stationName){
        superStations.get(stationName).stations.put(line.number, new Station(line, stationName));
    }

    public List<Station> getAdjacentStations(Station station){
        List<Station> adjacentStations = new ArrayList<>();
        //Get stations of the belonging superstation
        for (Map.Entry<String, Station> entry : superStations.get(station.name).stations.entrySet()) {
            if(entry.getValue()!=station)
                adjacentStations.add(entry.getValue());
        }
        //Get one or two neighbours from line
        for(Map.Entry<String, List<SchedulesDirectory.Schedule>> entry : SchedulesDirectory.getInstance().schedules.get(station.lineNumber).entrySet()){
            //System.out.println(entry.getKey() + "/" + entry.getValue());
            for(int i=0;i<entry.getValue().size();i++){
                List<SchedulesDirectory.Schedule> listOfStations = entry.getValue();
                if(listOfStations.get(i).station==station){
                    //On se met sur la station étudiée
                    //On ne regarde que la +1 car la -1 sera traitée dans l'autre direction
                    //Corner cases: 7b
                    if(i<entry.getValue().size()-1){
                        if(!adjacentStations.contains(listOfStations.get(i+1).station))
                            adjacentStations.add(listOfStations.get(i+1).station);
                    }
                }
            }
        }
        return adjacentStations;
    }

    public static List<SuperStation> allSuperStationsReader(String name){
        List<SuperStation> ss = new ArrayList<>();
        try {
            ss = Files.readAllLines(Paths.get(name))
                    .stream()
                    .skip(1) // to skip the header
                    .map(line -> new SuperStation(
                            Integer.parseInt(line.split(";")[0]),
                            line.split(";")[1]))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ss;
    }

    Map<String, SuperStation> fillSuperStationsMap(List<SuperStation> allStations){

        Map<String, SuperStation> superStations = new HashMap<>();

        for(SuperStation superStation : allStations){
            //If the map does not contains (yet)
            if(!superStations.containsKey(superStation.name)){
                superStations.put(superStation.name, superStation);
            }
        }
        return superStations;
    }

    public void addStationsToSuperStations(List<SchedulesDirectory.Schedule> allSchedules){
        for(SchedulesDirectory.Schedule schedule : allSchedules){
            superStations.get(schedule.station.name).stations.put(schedule.lineNumber,schedule.station);

            //We label the station as a terminus if the superStation<-station equals the schedule destination or origin
            if(schedule.destination == superStations.get(schedule.station.name).stations.get(schedule.lineNumber)
            || schedule.origin == superStations.get(schedule.station.name).stations.get(schedule.lineNumber)){
                superStations.get(schedule.station.name).stations.get(schedule.lineNumber).terminus = true;
            }
        }
    }

    public void affectPointsToStations(){
        for(String lineNumber : Constants.listOfLinesNames){
            for (Object o : LinesDirectory.getInstance().lines.get(lineNumber).geomVectorField.getGeometries()) {
                MasonGeometry mg = (MasonGeometry) o;
                //Dangerous...
                if(mg.getStringAttribute("type").equals("station")){
                    //We affect a refference to the station (object)
                    Station station = StationsDirectory.getInstance().getStation(mg.getStringAttribute("line"),mg.getStringAttribute("name"));
                    mg.addAttribute("station", station);
                    //We put the location of the station in the station (object)
                    station.location = mg.getGeometry().getCentroid();
                }
            }
        }
    }



    /*
    This method is used to create all geometries used to display Gare rectangle (around stations)
     *
     *  |----------------|
     *  |  x             |
     *  |            x   |   All x are points, and o is the centroid
     *  |                |
     *  |       o        |
     *  |                |
     *  |            x   |
     *  |   x            |
     *  |----------------|
     *
     */
    public void createAllGeomVectorFieldForGares(){
        // looping through each station of superStationsMap
        for (Map.Entry<String, SuperStation> entry : StationsDirectory.getInstance().superStations.entrySet()) {
            String key = entry.getKey();
            SuperStation value = entry.getValue();
            // we're looking only for Gare (SuperStations with more than one station)

            ArrayList<Point> allPoints = new ArrayList<Point>();
            // looping through each station of the Gare
            for (Map.Entry<String, Station> entrySubStation : value.stations.entrySet()) {
                String keySubStation = entrySubStation.getKey();
                Station valueSubStation = entrySubStation.getValue();

                allPoints.add(valueSubStation.location);

            }
            GeometryFactory gf = new GeometryFactory();
            // build a geometry from all substation location (just a list of point)
            Geometry geom = gf.buildGeometry(allPoints);

            // getting the envelope of all points
            Envelope envelope = geom.getEnvelopeInternal();
            envelope.expandBy(0.000009);

            MasonGeometry rectangleAroundSubStation = new MasonGeometry(gf.toGeometry(envelope));

            // adding station name to the geometry
            rectangleAroundSubStation.addStringAttribute(Constants.STATION_NAME_STR, key);
            if (value.stations.size() > 1) {
                rectangleAroundSubStation.addStringAttribute(Constants.IS_MULTIPLE_STATION_STR, Constants.TRUE);
            } else {
                rectangleAroundSubStation.addStringAttribute(Constants.IS_MULTIPLE_STATION_STR, Constants.FALSE);
            }
            // casting to gare
            Gare gare = new Gare(rectangleAroundSubStation);

            geomVectorFieldGare.addGeometry(gare);

        }
        // setting MBR according to line MBR (here we take the first, but it can be anything)
        geomVectorFieldGare.setMBR(LinesDirectory.getInstance().lines.get("1").geomVectorField.getMBR());
    }

    /*
    This method is used to draw rectangle around each super station centroid
     */
    public void setUpGarePortrayal() {
        geomVectorFieldGarePortrayal.setField(geomVectorFieldGare);

        geomVectorFieldGarePortrayal.setPortrayalForAll(
                new LabelledPortrayal2D(
                        new GeomPortrayal() {
                            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                filled = false;
                                Gare gare = (Gare) object;
                                if (gare.getStringAttribute(Constants.IS_MULTIPLE_STATION_STR).equals(Constants.TRUE)) {
                                    paint = Color.WHITE;
                                } else {
                                    // adding translucent color if Gare.nbStation == 1
                                    paint = new Color(128,128,128,0);;
                                }
                                super.draw(object, graphics, info);
                            }
                // "null" indicate that it will use toString of object
                }, 8.0, null , Color.WHITE, true)

        );

    }
    /*On Debug*/
    public static void main(String[] args) {
        StationsDirectory s = StationsDirectory.getInstance();
    }

}
