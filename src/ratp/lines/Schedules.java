package ratp.lines;

import global.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Schedules {

    private static Schedules INSTANCE = new Schedules();

    public static Schedules getInstance()
    {   
        return INSTANCE;
    }

    public static void initialize()
    {
        Schedules s = getInstance();
    }
    
    List<Schedule> allSchedules;

    Map<String, List<Schedule>> schedules_by_line = new HashMap<>();

    Map<String, Map<String, List<Schedule>>> schedules = new HashMap<>();

    private Schedules()
    {
        allSchedules = scheduleReader(Constants.SCHEDULES_FILENAME);

        //Initialize each list for each line
        for(String line : Constants.listOfLinesNames){
            schedules_by_line.put(line, new ArrayList<>());
        }
        //TODO: move this elsewhere
        for(Schedule s : allSchedules){
            if(schedules_by_line.containsKey(s.line))
                schedules_by_line.get(s.line).add(s);
        }

        for(String l : Constants.listOfLinesNames){
            schedules.put(l, new HashMap<>());
        }

        //Initialize each list for each line
        for(String line : Constants.listOfLinesNames){
            List<String> servicesNames = new ArrayList<>();
            //Yeah, fuck the complexity...
            for(Schedule s : schedules_by_line.get(line)){
                servicesNames.add(s.service);
            }
            servicesNames = servicesNames.stream()
                            .distinct()
                            .collect(Collectors.toList());

            for(String service : servicesNames){
                schedules.get(line).put(service, new ArrayList<>());
            }

            for(Schedule s : schedules_by_line.get(line)){
                schedules.get(line).get(s.service).add(s);
            }
        }
    }

    public static class Schedule {

        int entry_id;
        int order;
        //TODO: Replace station by station_legacyname -- please use station for now
        String station_legacyname;
        String station;
        String line;
        int branch;
        int split;

        public Schedule(int entry_id, int order, String station_legacyname, String station, String line, int branch, int split, int direction, String station_origin, String station_destination, String service) {
            this.entry_id = entry_id;
            this.order = order;
            this.station_legacyname = station_legacyname;
            this.station = station;
            this.line = line;
            this.branch = branch;
            this.split = split;
            this.direction = direction;
            this.station_origin = station_origin;
            this.station_destination = station_destination;
            this.service = service;
        }

        int direction;
        String station_origin;
        String station_destination;
        String service;

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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
                            line.split(";")[4],
                            Integer.parseInt(line.split(";")[5]),
                            Integer.parseInt(line.split(";")[6]),
                            Integer.parseInt(line.split(";")[7]),
                            line.split(";")[8],
                            line.split(";")[9],
                            line.split(";")[10]
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return schedules;
    }

    /*On Debug*/
    public static void main(String[] args) throws IOException {
        Schedules s = Schedules.getInstance();
    }


}
