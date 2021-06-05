package station;

import com.vividsolutions.jts.geom.Point;
import lines.Line;
import sim.util.geo.MasonGeometry;

import java.awt.*;

public class Station {
    public Line line;
    public String lineNumber="XXX";
    public String name="XXX";
    public Boolean terminus = false;
    public Color color = new Color(255,255,255);
    public Color legacyColor = new Color(255,255,255);
    public Point location;
    //Deprecated
    /*
    public Station(String lineId, String name) {
        this.lineNumber = lineId;
        this.name = name;
    }
    */

    public Station(Line line, String name) {
        this.line = line;
        this.lineNumber = line.number;
        this.name = name;
        this.color = line.color;
        this.legacyColor = line.color;
    }

    MasonGeometry mg = new MasonGeometry();

}
