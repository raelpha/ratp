package ratp.lines;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import global.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Schedules {


    /** Instance unique pré-initialisée */
    private static Schedules INSTANCE = new Schedules();

    /** Point d'accès pour l'instance unique du singleton */
    public static Schedules getInstance()
    {   
        return INSTANCE;
    }

    public static void initialize()
    {
        Schedules s = getInstance();
    }
    
    List<Schedule> schedules;

    Map<String, List<Schedule>> schedules_by_line = new HashMap<>();

    Map<String, List<Schedule>> schedulesbyLineAnd = new HashMap<>();

    private Schedules()
    {
        schedules = scheduleReader(Constants.SCHEDULES_FILENAME);

        //Initialize each list for each line
        for(String line : Constants.listOfLinesNames){
            schedules_by_line.put(line, new ArrayList<>());
        }

        //TODO: move this elsewhere
        for(Schedule s : schedules){
            if(schedules_by_line.containsKey(s.line))
                schedules_by_line.get(s.line).add(s);
        }
        
    }

    public static class Schedule {

        public int route_id;
        public String route_long_name;

        public Schedule(int route_id, String route_long_name, int service_id, long trip_id, int trip_headsign, int trip_short_name, int direction_id, int stop_id, String stop_name, String stop_sequence, String line) {
            this.route_id = route_id;
            this.route_long_name = route_long_name;
            this.service_id = service_id;
            this.trip_id = trip_id;
            this.trip_headsign = trip_headsign;
            this.trip_short_name = trip_short_name;
            this.direction_id = direction_id;
            this.stop_id = stop_id;
            this.stop_name = stop_name;
            this.stop_sequence = stop_sequence;
            this.line = line;
        }

        public int service_id;
        public long trip_id;
        public int trip_headsign;
        public int trip_short_name;
        public int direction_id;
        public int stop_id;
        public String stop_name;
        public String stop_sequence;
        public String line;

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<Schedule> scheduleReader(String name){

        List<Schedule> schedules = new ArrayList<>();

        try {
            //String filename = new File(new File(name).getCanonicalPath()).toURI().getPath();

            schedules = Files.readAllLines(Paths.get(name))
                    .stream()
                    .skip(1)
                    .map(line -> new Schedule(
                            Integer.parseInt(line.split(";")[0]),
                            line.split(";")[1],
                            Integer.parseInt(line.split(";")[2]),
                            Long.parseLong(line.split(";")[3]),
                            Integer.parseInt(line.split(";")[4]),
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
