package lines;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;
import global.Constants;
import rame.Rame;
import sim.display.GUIState;
import sim.display.Manipulating2D;
import sim.field.geo.GeomVectorField;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.LocationWrapper;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.Bag;
import sim.util.geo.AttributeValue;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;
import station.Station;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Line {

    public Line(String number) {
        this.number = number;
    }

    public String number;
    public Color color = Color.RED; // To spot errors
    public Color legacyColor = Color.RED; // To spot errors
    public GeomVectorField geomVectorField = new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE);
    public GeomVectorFieldPortrayal geomVectorFieldPortrayal =  new GeomVectorFieldPortrayal();
    public Map<String, Station> stations = new HashMap<>();

    public void setupPortrayal() {

        geomVectorFieldPortrayal.setField(geomVectorField);

        geomVectorFieldPortrayal.setPortrayalForAll(new GeomPortrayal() {
            /** Here, we redraw each LineString and Point according to its line color*/
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                MasonGeometry geometry = (MasonGeometry) object;
                paint = color;

                //TODO: The station should set its own portrayal
                //If the geometry is a station
                if (geometry.getStringAttribute("type") != null && geometry.getStringAttribute("type").equals("station"))
                    filled = true;

                //If the geometry is a section (line)
                if (geometry.getStringAttribute("type") != null && geometry.getStringAttribute("type").equals("section"))
                    filled = false;

                scale = 0.000003D;
                if (geometry.getStringAttribute("type") != null && geometry.getStringAttribute("type").equals("rame")){
                    filled = true;
                    scale = 0.00001D;
                    if(((Rame)((AttributeValue)geometry.getAttribute("rame")).getValue()).isPanne()){
                        paint = Color.RED.darker();
                    } else {
                        paint = color.darker().darker();
                    }
                }
                super.draw(object, graphics, info);
            }

            @Override
            public boolean handleMouseEvent(GUIState guistate, Manipulating2D manipulating, LocationWrapper wrapper, MouseEvent event, DrawInfo2D fieldPortrayalDrawInfo, int type) {
                if (SwingUtilities.isLeftMouseButton(event) && event.getClickCount() == 1) {
                    Point clickLocation = (Point) wrapper.getLocation();
                    Bag obj = geomVectorField.getGeometries();
                    Iterator objIt = obj.iterator();
                    while (objIt.hasNext()) {
                        MasonGeometry elem = (MasonGeometry) objIt.next();
                        if (elem.hasAttribute("type") && elem.getStringAttribute("type").equals("rame")) {
                            Rame r = ((Rame) (((AttributeValue) elem.getAttribute("rame")).getValue()));
                            if (r.getGeometry().getGeometry().getCoordinate().distance(clickLocation.getCoordinate()) < 0.00001) {
                                r.setPanne(200);
                            }
                        }
                    }
                }
                return super.handleMouseEvent(guistate, manipulating, wrapper, event, fieldPortrayalDrawInfo, type);
            }
        }
        );

    }
}
