package station;

import java.util.HashMap;
import java.util.Map;

public class SuperStation {
    public int id;
    public String name;

    public SuperStation(String name) {
        this.name = name;
    }

    Map<String, Station> stations = new HashMap<>();
}
