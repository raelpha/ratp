package station;

import java.util.HashMap;
import java.util.Map;

public class Gare {
    public int id;
    public String name;


    public Gare(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Map<String, Station> stations = new HashMap<>();

    public Station getStation(String lineId){
        return stations.get(lineId);
    }



}
