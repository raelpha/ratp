package ratp;

import global.Constants;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.geo.GeomPortrayal;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;

import javax.swing.*;
import java.awt.*;

public class RatpStateWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;

    private GeomVectorFieldPortrayal networkPortrayal = new GeomVectorFieldPortrayal();

    public RatpStateWithUI(SimState state) {
        super(state);
    }

    @Override
    public void init(Controller controller){
        super.init(controller);
        display = new Display2D(Constants.DISPLAY_SIZE, Constants.DISPLAY_SIZE, this);
        display.attach(networkPortrayal, "Network (all lines) portrayal");
        displayFrame = display.createFrame();
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    @Override
    public void start() {
        super.start();
        setupPortrayals();
    }

    private void setupPortrayals() {
        RatpNetwork ratpNetwork = (RatpNetwork) state;

        this.networkPortrayal.setField(ratpNetwork.linesGeomVectorField);
        this.networkPortrayal.setPortrayalForAll(new GeomPortrayal(){
                public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
                {
                    MasonGeometry geometry  = (MasonGeometry)object;
                    paint = Color.decode(geometry.getStringAttribute("stroke"));
                    super.draw(object, graphics, info);
                }
            }
        );

        display.reset();
        display.setBackdrop(Color.BLACK);
        display.repaint();
    }
}
