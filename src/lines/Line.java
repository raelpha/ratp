package lines;

import global.Constants;
import sim.field.geo.GeomVectorField;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.geo.MasonGeometry;
import station.Station;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Line {

    public Line(String number) {
        this.number = number;
    }

    String number;
    Color lineColor;
    public GeomVectorField geomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    public GeomVectorFieldPortrayal geomVectorFieldPortrayal =  new GeomVectorFieldPortrayal();
    public Map<String, Station> stations = new HashMap<>();

    public void setupPortrayal() {

        geomVectorFieldPortrayal.setField(geomVectorField);

        geomVectorFieldPortrayal.setPortrayalForAll(new GeomPortrayal() {
                                            /** Here, we redraw each LineString and Point according to its line color*/
                                            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                                MasonGeometry geometry = (MasonGeometry) object;
                                                paint = Color.decode(geometry.getStringAttribute("color"));

                                                //If the geometry is a station
                                                if (geometry.getStringAttribute("type") != null && geometry.getStringAttribute("type").equals("station"))
                                                    filled = true;

                                                //If the geometry is a section (line)
                                                if (geometry.getStringAttribute("type") != null && geometry.getStringAttribute("type").equals("section"))
                                                    filled = false;

                                                scale = 0.000003D;
                                                super.draw(object, graphics, info);
                                            }
                                        }
        );

    }
}