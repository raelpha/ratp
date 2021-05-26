package gui;

import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
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

    private GeomVectorFieldPortrayal portrayal = new GeomVectorFieldPortrayal();

    public RatpStateWithUI(SimState state) {
        super(state);
    }

    @Override
    public void init(Controller controller){
        super.init(controller);

        display = new Display2D(FrameSize, FrameSize, this);

        display.attach(portrayal, "Vector layer");

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

        RatpState world = (RatpState) state;

        portrayal.setField(world.vectorField);


        Bag geometries = world.vectorField.getGeometries();
        MasonGeometry geo = (MasonGeometry) world.vectorField.getGeometries().objs[5];
        for (int i = 0; i < geometries.size(); i++) {
            MasonGeometry geometry = (MasonGeometry) geometries.objs[i];
            System.out.println(geometry);
            System.out.println(geometry.getStringAttribute("line"));
            if (geometry.getStringAttribute("line").equals("1")) {
                System.out.println("c la ligne 1");
                portrayal.setPortrayalForObject(geometry, new GeomPortrayal(Color.RED, true));
            }
        }

        // portrayal.setPortrayalForAll(new GeomPortrayal(Color.CYAN, true));

        display.reset();
        display.setBackdrop(Color.WHITE);

        display.repaint();
    }
}
