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

    private List<SuperStation> allSuperStations;

    //Map<String, Station> stations;

    Map<String, SuperStation> superStations;

    private StationsDirectory()
    {
        allSuperStations = allSuperStationsReader(Constants.STATIONS_FILENAME);
        //stations = computeStationsMap(allSuperStations);
        superStations = fillSuperStationsMap(allSuperStations);
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

    public static Map<String, Station> computeStationsMap(List<Station> stations){
        Map<String, Station> s = new HashMap<>();
        for(Station station : stations){
            s.put(station.name+" "+station.name, station);
        }
        return s;
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
            superStations.get(schedule.station.name).stations.put(schedule.line,schedule.station);

            //We label the station as a terminus if the superStation<-station equals the schedule destination or origin
            if(schedule.destination == superStations.get(schedule.station.name).stations.get(schedule.line)
            || schedule.origin == superStations.get(schedule.station.name).stations.get(schedule.line)){
                superStations.get(schedule.station.name).stations.get(schedule.line).terminus = true;
            }
        }
        int i=0;
    }

    /*On Debug*/
    public static void main(String[] args) throws IOException {
        StationsDirectory s = StationsDirectory.getInstance();
        int i=0;
    }

}
