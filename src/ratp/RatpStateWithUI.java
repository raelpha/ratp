package ratp;

import global.Constants;
import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.geo.GeomVectorFieldPortrayal;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RatpStateWithUI extends GUIState {

    public Display2D display;

    public JFrame displayFrame;

    /**
     * A map storing each line, and station, as a GeomVectorFieldPortrayal
     */
    final private Map<String, GeomVectorFieldPortrayal> linesPortrayals = new HashMap<>();

    public RatpStateWithUI(SimState state) {
        super(state);
    }

    /**
     * Called when the GUI is created
     * Create display and attaching it to the console.
     *
     * @param controller Console Controller
     */
    @Override
    public void init(Controller controller) {
        super.init(controller);
        display = new Display2D(Constants.DISPLAY_SIZE, Constants.DISPLAY_SIZE, this);

        for (String s : Constants.listOfLinesNames) {
            display.attach(LinesDirectory.getInstance().lines.get(s).geomVectorFieldPortrayal, "Ligne " + s);
        }
        display.attach(StationsDirectory.getInstance().geomVectorFieldGarePortrayal, "Gares informations");

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
     * Where we set up the visualization when the user is
     * about to start playing the simulation
     */
    private void setupPortrayals() {
        RatpNetwork ratpNetwork = (RatpNetwork) state;

        for(String lineName : Constants.listOfLinesNames){
            LinesDirectory.getInstance().lines.get(lineName).setupPortrayal();
        }

        StationsDirectory.getInstance().setUpGarePortrayal();

        display.reset();
        display.setBackdrop(Color.BLACK);
        display.repaint();
    }
}
