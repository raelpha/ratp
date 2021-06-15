package ratp.directory;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Point;
import global.Constants;
import lines.Line;
import sim.app.geo.masoncsc.util.Pair;
import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.field.geo.GeomVectorField;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.util.geo.MasonGeometry;
import station.Station;
import station.Gare;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StationsDirectory {

    /**WARNING: SCHEDULES DIRECTIORY MUST BE INITIALIZED (DATA LOADED)*/

    public static StationsDirectory INSTANCE = new StationsDirectory();

    public static StationsDirectory getInstance()
    {
        return INSTANCE;
    }

    public static void initialize()
    {
        StationsDirectory s = getInstance();
    }

    public List<Gare> getAllGares() {
        return allGares;
    }

    public List<Gare> allGares;

    public Map<String, Gare> gares;

    //geomVectorField used to store all Gare geometries
    public GeomVectorField geomVectorFieldGare = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    // Field portrayal in which we're drawing
    public GeomVectorFieldPortrayal geomVectorFieldGarePortrayal =  new GeomVectorFieldPortrayal();

    public List<Station> stationsOuvertes = new ArrayList<>();

    /**
     * Constructor: fill allGares from external file then create associated map
     */
    private StationsDirectory()
    {
        allGares = allGaresReader(Constants.STATIONS_FILENAME);
        gares = fillGaresMap(allGares);
    }
    public void OuvrirStations(){
        for(Gare g : allGares) stationsOuvertes.addAll(g.stations.values());
    }
    public void fermerStation(Station s) { stationsOuvertes.remove(s); }
    public void ouvrirStation(Station s) { stationsOuvertes.add(s); }

    public Station getStation(String lineId, String stationName){
        return gares.get(stationName).getStation(lineId);
    }

    public void instantiateStation(Line line, String stationName){
        gares.get(stationName).stations.put(line.number, new Station(line, stationName));
    }

    public List<Station> getAdjacentStations(Station station){
        List<Station> adjacentStations = new ArrayList<>();
        //Get stations of the belonging Gares
        for (Map.Entry<String, Station> entry : gares.get(station.name).stations.entrySet()) {
            if(entry.getValue()!=station)
                adjacentStations.add(entry.getValue());
        }
        //Get one or two neighbours from line
        for(Map.Entry<String, List<SchedulesDirectory.Schedule>> entry : SchedulesDirectory.getInstance().schedules.get(station.lineNumber).entrySet()){
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

    public List<Station> getTowardsStation(Station origin, Station objective){
        List<Station> towards = new ArrayList<>();
        //Si on est sur la même ligne, on retourne les vraies destinations
        if(origin.lineNumber.equals(objective.lineNumber)) {
            //Pour chaque service de la ligne sur laquelle se situe la station
            for (Map.Entry<String, List<SchedulesDirectory.Schedule>> schedules : SchedulesDirectory.getInstance().schedules.get(origin.lineNumber).entrySet()) {
                //Pour chaque
                boolean originDetected = false;
                boolean destinationDetected = false;
                Station destination = null;
                for (SchedulesDirectory.Schedule s : schedules.getValue()) {
                    destination = s.destination;
                    if (s.station.equals(origin))
                        originDetected = true;
                    if (originDetected)
                        if (s.station.equals(objective))
                            destinationDetected = true;
                }
                if (destinationDetected && destination != null) {
                    towards.add(destination);
                }
            }
        }else{ // Si on n'est pas sur la même ligne, on retourne toutes les destinations possibles, il faudra mettre un coup de ponceuse après (destination=destination n+1)

        }

        return towards;
    }

    public List<Pair<Station, List<Station>>> getAdjacentStationsWithDestination(Station station){
        List<Pair<Station,List<Station>>> adjacentStations = new ArrayList<>();
        //Get stations of the belonging Gares
        for (Map.Entry<String, Station> entry : gares.get(station.name).stations.entrySet()) {
            if(entry.getValue()!=station){
                //Warning ! Nullptr warning !
                Pair<Station,List<Station>> s = new Pair<Station,List<Station>>(entry.getValue(),getTowardsStation(station,entry.getValue()));
                adjacentStations.add(s);
            }
        }
        //Get one or two neighbours from line
        for(Map.Entry<String, List<SchedulesDirectory.Schedule>> entry : SchedulesDirectory.getInstance().schedules.get(station.lineNumber).entrySet()){
            for(int i=0;i<entry.getValue().size();i++){
                List<SchedulesDirectory.Schedule> listOfStations = entry.getValue();
                if(listOfStations.get(i).station==station){
                    //On se met sur la station étudiée
                    //On ne regarde que la +1 car la -1 sera traitée dans l'autre direction
                    //Corner cases: 7b
                    if(i<entry.getValue().size()-1){
                        if(!adjacentStations.contains(listOfStations.get(i+1).station)){
                            Pair<Station,List<Station>> s = new Pair<Station,List<Station>>(listOfStations.get(i+1).station,getTowardsStation(station, listOfStations.get(i+1).station));
                            adjacentStations.add(s);
                        }
                    }
                }
            }
        }
        return adjacentStations;
    }

    public static List<Gare> allGaresReader(String name){
        List<Gare> ss = new ArrayList<>();
        try {
            ss = Files.readAllLines(Paths.get(name))
                    .stream()
                    .skip(1) // to skip the header
                    .map(line -> new Gare(
                            Integer.parseInt(line.split(";")[0]),
                            line.split(";")[1]))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ss;
    }

    Map<String, Gare> fillGaresMap(List<Gare> allStations){

        Map<String, Gare> gares = new HashMap<>();

        for(Gare gare : allStations){
            //If the map does not contains (yet)
            if(!gares.containsKey(gare.name)){
                gares.put(gare.name, gare);
            }
        }
        return gares;
    }

    public void addStationsToGares(List<SchedulesDirectory.Schedule> allSchedules){
        for(SchedulesDirectory.Schedule schedule : allSchedules){
            gares.get(schedule.station.name).stations.put(schedule.lineNumber,schedule.station);

            //We label the station as a terminus if the Gares <-station equals the schedule destination or origin
            if(schedule.destination == gares.get(schedule.station.name).stations.get(schedule.lineNumber)
            || schedule.origin == gares.get(schedule.station.name).stations.get(schedule.lineNumber)){
                gares.get(schedule.station.name).stations.get(schedule.lineNumber).terminus = true;
            }
        }
        StationsDirectory.getInstance().OuvrirStations();
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
        // looping through each station of Gares
        for (Map.Entry<String, Gare> entry : StationsDirectory.getInstance().gares.entrySet()) {
            String key = entry.getKey();
            Gare value = entry.getValue();
            // we're looking only for Gare

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
            envelope.expandBy(0.000008);

            MasonGeometry rectangleAroundSubStation = new MasonGeometry(gf.toGeometry(envelope));

            // adding station name to the geometry
            rectangleAroundSubStation.addStringAttribute(Constants.STATION_NAME_STR, key);
            if (value.stations.size() > 1) {
                rectangleAroundSubStation.addStringAttribute(Constants.IS_MULTIPLE_STATION_STR, Constants.TRUE);
            } else {
                rectangleAroundSubStation.addStringAttribute(Constants.IS_MULTIPLE_STATION_STR, Constants.FALSE);
            }
            // casting to gare
            Gare gareGeometry = new Gare(rectangleAroundSubStation);

            geomVectorFieldGare.addGeometry(gareGeometry);

        }
        // setting MBR according to line MBR (here we take the first, but it can be anything)
        geomVectorFieldGare.setMBR(LinesDirectory.getInstance().lines.get("1").geomVectorField.getMBR());
    }

    /*
    This method is used to draw rectangle around each Gare centroid
     */
    public void setUpGarePortrayal() {
        geomVectorFieldGarePortrayal.setField(geomVectorFieldGare);

        geomVectorFieldGarePortrayal.setPortrayalForAll(
                new LabelledPortrayal2D(
                        new GeomPortrayal() {
                            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                filled = false;
                                Gare gareGeometry = (Gare) object;
                                if (gareGeometry.getStringAttribute(Constants.IS_MULTIPLE_STATION_STR).equals(Constants.TRUE)) {
                                    if (gareGeometry.isFermee()) {
                                        paint = Color.RED;

                                    } else {
                                        paint = Color.WHITE;
                                    }
                                } else {
                                    if (gareGeometry.isFermee()) {
                                        paint = Color.RED;

                                    } else {
                                        paint = new Color(128,128,128,0);
                                    }
                                    // adding translucent color if Gare.nbStation == 1
                                }
                                super.draw(object, graphics, info);
                            }

                            @Override
                            public boolean handleMouseEvent(GUIState guistate, Manipulating2D manipulating, LocationWrapper wrapper, MouseEvent event, DrawInfo2D fieldPortrayalDrawInfo, int type) {
                                if (SwingUtilities.isRightMouseButton(event) && event.getClickCount() == 1) {
                                    Gare gare = (Gare) wrapper.getObject();
                                    if (!gare.isFermee()) {
                                        gare.setFermee();
                                    } else {
                                        gare.setOuvert();
                                    }
                                }
                                return super.handleMouseEvent(guistate, manipulating, wrapper, event, fieldPortrayalDrawInfo, type);
                            }

                            // "null" indicate that it will use toString of object
                        }, 4.0, null , Color.WHITE, true)

        );

    }

    public double getColereAllStations() {
        double colere = 0;
        for (Map.Entry<String, Gare> entry : StationsDirectory.getInstance().gares.entrySet()) {
            for (Map.Entry<String, Station> entryStation :  entry.getValue().stations.entrySet()) {
                colere += entryStation.getValue().getColereStation();
            }
        }
        return colere;
    }

    /*On Debug*/
    public static void main(String[] args) {
        StationsDirectory s = StationsDirectory.getInstance();
    }

}
