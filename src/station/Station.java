package station;

import lines.Line;
import sim.util.geo.MasonGeometry;

import java.awt.*;

public class Station {
    public Line line;
    public String lineNumber="XXX";
    public String name="XXX";
    public Boolean terminus = false;
    public Color color = new Color(255,255,255);

    public Station(String lineId, String name) {
        this.lineNumber = lineId;
        this.name = name;
    }

    public Station(Line line, String name) {
        this.line = line;
        this.lineNumber = line.number;
        this.name = name;
    }

    MasonGeometry mg = new MasonGeometry();

}
