package ratp.directory;

import global.Constants;
import lines.Line;
import sim.util.geo.MasonGeometry;
import station.Station;
import station.SuperStation;

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

    public List<SuperStation> allSuperStations;

    public Map<String, SuperStation> superStations;

    public StationsDirectory()
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

    /*On Debug*/
    public static void main(String[] args) {
        StationsDirectory s = StationsDirectory.getInstance();
    }

}
