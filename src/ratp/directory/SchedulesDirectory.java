package ratp.directory;

import global.Constants;
import station.Station;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchedulesDirectory {

    private static SchedulesDirectory INSTANCE = new SchedulesDirectory();

    public static SchedulesDirectory getInstance()
    {   
        return INSTANCE;
    }

    public static void initialize()
    {
        SchedulesDirectory s = getInstance();
    }
    
    List<Schedule> allSchedules;

    Map<String, List<Schedule>> schedulesByLine = new HashMap<>();

    Map<String, Map<String, List<Schedule>>> schedules = new HashMap<>();

    private void computeSchedulesByLine(){
        //Initialize each list for each line
        for(String line : Constants.listOfLinesNames){
            schedulesByLine.put(line, new ArrayList<>());
        }
        //TODO: move this elsewhere
        for(Schedule s : allSchedules){
            if(schedulesByLine.containsKey(s.line))
                schedulesByLine.get(s.line).add(s);
        }
    }

    private void computeSchedules(){
        for(String l : Constants.listOfLinesNames){
            schedules.put(l, new HashMap<>());
        }

        //Initialize each list for each line
        for(String line : Constants.listOfLinesNames){
            List<String> servicesNames = new ArrayList<>();
            //Yeah, fuck the complexity...
            for(Schedule s : schedulesByLine.get(line)){
                servicesNames.add(s.serviceName);
            }
            servicesNames = servicesNames.stream()
                    .distinct()
                    .collect(Collectors.toList());

            for(String service : servicesNames){
                schedules.get(line).put(service, new ArrayList<>());
            }

            for(Schedule s : schedulesByLine.get(line)){
                schedules.get(line).get(s.serviceName).add(s);
            }
        }
    int i =0;
    }

    private SchedulesDirectory()
    {
        allSchedules = scheduleReader(Constants.SCHEDULES_FILENAME);
        //The dirty way... Initialize this before schedules
        computeSchedulesByLine();
        computeSchedules();
        StationsDirectory.getInstance().addStationsToSuperStations(allSchedules);
    }

    public static class Schedule {

        int entry_id;
        int order;
        //String station_name;
        String line;
        int branch;
        int split;
        Station station;
        Station origin;
        Station destination;
        int direction;
        String serviceName;

        StationsDirectory s = StationsDirectory.getInstance();
        public Schedule(int entry_id, int order, String station_name, String line, int branch, int split, int direction, String stationOriginName, String stationDestinationName, String serviceName) {
            this.entry_id = entry_id;
            this.order = order;
            this.line = line;
            this.branch = branch;
            this.split = split;
            this.direction  = direction;
            this.station = StationsDirectory.getInstance().stations.get(station_name);
            this.origin = StationsDirectory.getInstance().stations.get(stationOriginName);
            this.destination = StationsDirectory.getInstance().stations.get(stationDestinationName);
            this.serviceName = serviceName;
        }
    }

    public static List<Schedule> scheduleReader(String name){

        List<Schedule> schedules = new ArrayList<>();

        try {
            schedules = Files.readAllLines(Paths.get(name))
                    .stream()
                    .skip(1) // to skip the header
                    .map(line -> new Schedule(
                            Integer.parseInt(line.split(";")[0]),
                            Integer.parseInt(line.split(";")[1]),
                            line.split(";")[2],
                            line.split(";")[3],
                            Integer.parseInt(line.split(";")[4]),
                            Integer.parseInt(line.split(";")[5]),
                            Integer.parseInt(line.split(";")[6]),
                            line.split(";")[7],
                            line.split(";")[8],
                            line.split(";")[9]
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return schedules;
    }

    /*On Debug*/
    public static void main(String[] args) throws IOException {
        SchedulesDirectory s = SchedulesDirectory.getInstance();
    }


}
