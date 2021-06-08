package station;

import global.Constants;
import sim.util.geo.MasonGeometry;

import java.util.HashMap;
import java.util.Map;

/**
 * A gare is made of one or more Station.
 * This class is also used to display associated MasonGeometry with a portrayal
 */
public class Gare extends MasonGeometry {
    public int id;
    public String name;
    public Map<String, Station> stations = new HashMap<>();

    /**
     * Main constructor, used when instantiating new gare (without associated MasonGeometry)
     * @param id debug value - can be null
     * @param name
     */
    public Gare(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * This constructor is used to create a Gare from an existing geometry
     * It is especially used when you want to create a Gare from many Stations
     * @param gem
     */
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

    public Station getStation(String lineId){
        return stations.get(lineId);
    }



}
