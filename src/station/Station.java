package station;

import sim.util.geo.MasonGeometry;

public class Station {
    public int id;
    public String name;
    public Boolean terminus = false;
    public Station(int id, String name) {
        this.id = id;
        this.name = name;
    }

    MasonGeometry mg = new MasonGeometry();

}
