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

    /**WARNING: SCHEDULES DIRECTIORY MUST BE INITIALIZED*/

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

    Map<String, SuperStation> superStations;

    private StationsDirectory()
    {
        allStations = allStationsReader(Constants.STATIONS_FILENAME);
        superStations = initializeSuperStations(allStations);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
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

    public void computeStationsAccordingToSchedulesDirectory(List<SchedulesDirectory.Schedule> allSchedules){

        for()

    }

    /*On Debug*/
    public static void main(String[] args) throws IOException {
        StationsDirectory s = StationsDirectory.getInstance();
    }

}
