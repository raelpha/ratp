package ratp;

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
    public static int FrameSize = 600;
    public Display2D display;
    public JFrame displayFrame;
    private GeomVectorFieldPortrayal agentPortrayal = new GeomVectorFieldPortrayal();

    private GeomVectorFieldPortrayal portrayal = new GeomVectorFieldPortrayal();

    public RatpStateWithUI(SimState state) {
        super(state);
    }

    @Override
    public void init(Controller controller){
        super.init(controller);

        display = new Display2D(FrameSize, FrameSize, this);

        display.attach(portrayal, "Vector layer");
        this.display.attach(this.agentPortrayal, "Agents", true);

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

        RatpNetwork world = (RatpNetwork) state;

        portrayal.setField(world.vectorField);
        portrayal.setPortrayalForAll(new GeomPortrayal(Color.RED, true));


        Bag geometries = world.vectorField.getGeometries();
        MasonGeometry geo = (MasonGeometry) world.vectorField.getGeometries().objs[5];

        portrayal.setPortrayalForAll(new GeomPortrayal()
        {
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
            {
                MasonGeometry geometry  = (MasonGeometry)object;
                paint = Color.decode(geometry.getStringAttribute("stroke"));
                super.draw(object, graphics, info);
            }
        });

        this.agentPortrayal.setField(world.agents);
        this.agentPortrayal.setPortrayalForAll(new GeomPortrayal(Color.RED, 0.3D, true));

        display.reset();
        //&display.setBackdrop(Color.BLACK);
        display.repaint();
    }
}
