package lines;

import java.util.List;

public class Service {

    List<Crossable> schedule;

    String service_name;

    Crossable getOrigin(){
        return schedule.get(0);
    }

    Crossable getDestination(){
        return schedule.get(schedule.size()-1);
    }


}
