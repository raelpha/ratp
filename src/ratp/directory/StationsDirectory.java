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
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.geo.MasonGeometry;
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



    /**
     *     Help function used to compute centroid
     * @param allPoints
     * @return centroid
     */
    private Coordinate centroid(ArrayList<Point> allPoints)  {
        double centroidX = 0, centroidY = 0;

        for(Point point : allPoints) {
            centroidX += point.getX();
            centroidY += point.getY();
        }
        Coordinate centroid = new Coordinate(centroidX / allPoints.size(), centroidY / allPoints.size());
        return centroid;
    }

    /*
    This method is used to create all geometries used to display Gare rectangle (around stations)
     */
    public void createAllGeomVectorFieldForGares(){
        GeometryFactory gf = new GeometryFactory();
        // looping through each station of superStationsMap
        for (Map.Entry<String, SuperStation> entry : StationsDirectory.getInstance().superStations.entrySet()) {
            String key = entry.getKey();
            SuperStation value = entry.getValue();
            // we're looking only for Gare (SuperStations with more than one station)
            if (value.stations.size() > 1) {
                ArrayList<Point> allPoints = new ArrayList<Point>();
                // looping through each station of the Gare
                for (Map.Entry<String, Station> entrySubStation : value.stations.entrySet()) {
                    String keySubStation = entrySubStation.getKey();
                    Station valueSubStation = entrySubStation.getValue();
                    // create Geometry from point
                    allPoints.add(valueSubStation.location);
                }
                // computing centroid from all point (all sub station location)
                Point centroid = gf.createPoint(centroid(allPoints));
                MasonGeometry pointSubStation = new MasonGeometry(centroid);

                // adding station name to the geometry
                pointSubStation.addStringAttribute(Constants.STATION_NAME_STR, key);

                geomVectorFieldGare.addGeometry(pointSubStation);
            }
        }
    }

    /*
    This method is used to draw rectangle around each super station centroid
     */
    public void setUpGarePortrayal() {
        System.out.println("Setup portrayal for gares...");
        geomVectorFieldGarePortrayal.setField(geomVectorFieldGare);



        geomVectorFieldGarePortrayal.setPortrayalForAll(
                new LabelledPortrayal2D(
                        new GeomPortrayal() {
                            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                System.out.println("draw " + object);
                                MasonGeometry geometry = (MasonGeometry) object;
                                scale = 0.000013D;
                                super.draw(object, graphics, info);
                            }
                }, 5.0, null, Color.WHITE, false)
        );

    }
    /*On Debug*/
    public static void main(String[] args) {
        StationsDirectory s = StationsDirectory.getInstance();
    }

}
