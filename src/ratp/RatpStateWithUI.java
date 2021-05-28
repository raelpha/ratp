package ratp;

import global.Constants;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.geo.MasonGeometry;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RatpStateWithUI extends GUIState {

    public Display2D display;

    public JFrame displayFrame;

    /**A map storing each line, and station, as a GeomVectorFieldPortrayal*/
    private Map<String,GeomVectorFieldPortrayal> linesPortrayals = new HashMap<>();

    public RatpStateWithUI(SimState state) {
        super(state);

        //Initialize a GeomVectorFieldPortrayal for each (hardcoded) line
        for(String s : Constants.listOfLinesNames){
            linesPortrayals.put(s, new GeomVectorFieldPortrayal());
        }
    }

    /**
     * Called when the GUI is created
     * Create display and attaching it to the console.
     * @param controller Console Controller
     */
    @Override
    public void init(Controller controller){
        super.init(controller);
        display = new Display2D(Constants.DISPLAY_SIZE, Constants.DISPLAY_SIZE, this);

        for (String s : Constants.listOfLinesNames) {
            display.attach(linesPortrayals.get(s), "Ligne "+s);
        }

        displayFrame = display.createFrame();
        // make the display appears in the "displays" list in Console
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        displayFrame.setTitle("Network");
    }

    @Override
    public void start() {
        super.start();
        setupPortrayals();
    }

    /**
     * @return HTML string parsed for "about" section in MASON window
     */
    public static Object getInfo() {
        return  "<h1 style='color: #5e9ca0;' data-darkreader-inline-color=''>RATP Simulation</h1>"+
                "<h2 style='color: #2e6c80;' data-darkreader-inline-color=''>Purpose of the simulation</h2>"+
                "<p>This simulation simulate the Paris's subway traffic and how passengers " +
                        "reacts to pertubation on the network.</p>" +
                "<h2 style='color: #2e6c80;' data-darkreader-inline-color=''>Authors</h2>"+
                "<ul>"+
                "<li>Yvain</li>"+
                "<li>Rapha&euml;l</li>"+
                "<li>Hugo</li>"+
                "<li>Jimmy</li>" +
                "<li>Cl&eacute;ment</li>" +
                "</ul>";
    }

    /**
     * Where we set up the visualization when the user is
     * about to start playing the simulation
     */
    private void setupPortrayals() {
        RatpNetwork ratpNetwork = (RatpNetwork) state;

        for (Map.Entry<String, GeomVectorFieldPortrayal> l : this.linesPortrayals.entrySet()) {
            l.getValue().setField(ratpNetwork.linesGeomVectorField.get(l.getKey()));
            l.getValue().setPortrayalForAll(new GeomPortrayal(){
                 /** Here, we redraw each LineString and Point according to its line color*/
                 public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
                 {
                     MasonGeometry geometry  = (MasonGeometry)object;
                     paint = Color.decode(geometry.getStringAttribute("stroke"));

                     //If the geometry is a station
                     if(geometry.getStringAttribute("type")!=null && geometry.getStringAttribute("type").equals("station"))
                         filled = true;

                     //If the geometry is a section (line)
                     if(geometry.getStringAttribute("type")!=null && geometry.getStringAttribute("type").equals("section"))
                         filled = false;

                     scale = 0.000003D;
                     super.draw(object, graphics, info);
                 }
                }
            );
        }

        display.reset();
        display.setBackdrop(Color.BLACK);
        display.repaint();
    }
}
