package station;

import global.Constants;
import sim.util.geo.MasonGeometry;

import java.util.HashMap;
import java.util.Map;

public class Gare extends MasonGeometry {
    public int id;
    public String name;


    public Gare(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Gare(MasonGeometry gem) {
        this.addAttribute(Constants.IS_MULTIPLE_STATION_STR, gem.getStringAttribute(Constants.IS_MULTIPLE_STATION_STR));
        this.addAttribute(Constants.STATION_NAME_STR, gem.getStringAttribute(Constants.STATION_NAME_STR));
        this.geometry = gem.getGeometry();
        this.name = this.getStringAttribute(Constants.STATION_NAME_STR);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public Map<String, Station> stations = new HashMap<>();

    public Station getStation(String lineId){
        return stations.get(lineId);
    }



}
