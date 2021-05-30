package station;

import sim.util.geo.MasonGeometry;

import java.awt.*;

public class Station {
    public String line;
    public String name;
    public Boolean terminus = false;
    public Color color = new Color(255,255,255);
    public Station(String line, String name) {
        this.line = line;
        this.name = name;
    }

    MasonGeometry mg = new MasonGeometry();

}
