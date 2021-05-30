package station;

import sim.util.geo.MasonGeometry;

public class Station {
    public String line;
    public String name;
    public Boolean terminus = false;
    public Station(String line, String name) {
        this.line = line;
        this.name = name;
    }

    MasonGeometry mg = new MasonGeometry();

}
