package station;

import com.vividsolutions.jts.geom.Envelope;
import global.Constants;
import sim.field.continuous.Continuous2D;
import sim.field.geo.GeomVectorField;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.portrayal.grid.HexaValueGridPortrayal2D;
import sim.portrayal.simple.HexagonalPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.util.geo.MasonGeometry;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SuperStation {
    public int id;
    public String name;

    public GeomVectorField yard = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    public GeomVectorFieldPortrayal yardPortrayal =  new GeomVectorFieldPortrayal();

    public SuperStation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Map<String, Station> stations = new HashMap<>();

    public Station getStation(String lineId){
        return stations.get(lineId);
    }

    public void setupPortrayal() {

        yardPortrayal.setField(yard);
        yardPortrayal.setPortrayalForAll(
                new LabelledPortrayal2D(
                        new HexagonalPortrayal2D() {}, 5.0, null, Color.WHITE, false) {
        }
        );

    }

    public void buildFieldFromStation() {
        Envelope MBR = new Envelope();

        for (Station s: stations.values()) {
            MasonGeometry geom = new MasonGeometry(s.location);
            yard.addGeometry(geom);
            MBR.expandToInclude(s.location.getX(), s.location.getY());

        }
        MBR.expandBy(MBR.getHeight()*0.1, MBR.getWidth()*0.5*0.1);

        yard.setMBR(MBR);

    }
}
