package ratp.directory;

import global.Constants;
import station.Station;
import station.SuperStation;

import java.io.IOException;
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

    List<Station> allStations;

    Map<String, Station> stations;

    Map<String, SuperStation> superStations;

    private StationsDirectory()
    {
        allStations = allStationsReader(Constants.STATIONS_FILENAME);
        stations = computeStationsMap(allStations);
        superStations = initializeSuperStations(allStations);
    }


    public static List<Station> allStationsReader(String name){
        List<Station> s = new ArrayList<>();
        try {
            s = Files.readAllLines(Paths.get(name))
                    .stream()
                    .skip(1) // to skip the header
                    .map(line -> new Station(
                            Integer.parseInt(line.split(";")[0]),
                            line.split(";")[1]))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    public static Map<String, Station> computeStationsMap(List<Station> stations){
        Map<String, Station> s = new HashMap<>();
        for(Station station : stations){
            s.put(station.name, station);
        }
        return s;
    }

    Map<String, SuperStation> initializeSuperStations(List<Station> allStations){

        Map<String, SuperStation> superStations = new HashMap<>();

        for(Station station : allStations){
            //If the map does not contains (yet)
            if(!superStations.containsKey(station.name)){
                superStations.put(station.name, new SuperStation(station.name));
            }
        }
        return superStations;
    }

    public void addStationsToSuperStations(List<SchedulesDirectory.Schedule> allSchedules){
        for(SchedulesDirectory.Schedule schedule : allSchedules){
            superStations.get(schedule.station.name).stations.put(schedule.line,schedule.station);
            //If the superStation<-station equals the schedule destination or origin, we label the station as a terminus
            if(schedule.destination == superStations.get(schedule.station.name).stations.get(schedule.line)
            || schedule.origin == superStations.get(schedule.station.name).stations.get(schedule.line)){
                superStations.get(schedule.station.name).stations.get(schedule.line).terminus = true;
            }
        }
    }

    /*On Debug*/
    public static void main(String[] args) throws IOException {
        StationsDirectory s = StationsDirectory.getInstance();
    }

}
