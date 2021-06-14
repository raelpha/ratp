package ratp;

import global.Constants;
import ratp.directory.LinesDirectory;
import ratp.directory.StationsDirectory;
import sim.display.ChartUtilities;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;

import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.geo.GeomVectorFieldPortrayal;
import sim.util.Valuable;
import sim.util.media.chart.ChartGenerator;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;
import voyageur.AgentVoyageur;
import voyageur.VoyageurPortrayal;


import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RatpStateWithUI extends GUIState {

    ContinuousPortrayal2D yardPortrayal = new ContinuousPortrayal2D();
    public Display2D display;

    public JFrame displayFrame;

    public TimeSeriesAttributes attributesColere;
    public TimeSeriesChartGenerator chartColere;

    public TimeSeriesAttributes attributesRameStopped;
    public TimeSeriesChartGenerator chartRameStopped;

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

        chartColere = ChartUtilities.buildTimeSeriesChartGenerator(
                this,
                "Evolution de la colère avec le temps",
                "Temps");
        chartColere.setYAxisLabel("Colère");
        attributesColere = ChartUtilities.addSeries(chartColere, "Colère sur l'ensemble du réseau");

        chartRameStopped = ChartUtilities.buildTimeSeriesChartGenerator(
                this,
                "Evolution du nombre de rames à l'arrêt",
                "Temps");
        chartRameStopped.setYAxisLabel("Nombre de rames à l'arrêt (%)");
        attributesRameStopped = ChartUtilities.addSeries(
                chartRameStopped,
                "Pourcentage de rames à l'arrêt l'ensemble du réseau");
        attributesRameStopped.setStrokeColor(Color.BLUE);

    }

    @Override
    public void start() {
        super.start();
        setupPortrayals();

        chartColere.clearAllSeries();

        ChartUtilities.scheduleSeries(this, attributesColere, new Valuable() {
            @Override
            public double doubleValue() {
                return ((RatpNetwork) state).getAllColere() * 100;
            }
        });

        chartRameStopped.clearAllSeries();

        ChartUtilities.scheduleSeries(this, attributesRameStopped, new Valuable() {
            @Override
            public double doubleValue() {
                return ((RatpNetwork) state).getAllRameStopped() * 100;
            }
        });
    }

    @Override
    public void load(final SimState state) {
        super.start();
        ChartUtilities.scheduleSeries(this, attributesColere, new Valuable() {
            @Override
            public double doubleValue() {
                return ((RatpNetwork) state).getAllColere() * 100;
            }
        });

        ChartUtilities.scheduleSeries(this, attributesRameStopped, new Valuable() {
            @Override
            public double doubleValue() {
                return ((RatpNetwork) state).getAllRameStopped() * 100;
            }
        });
    }

    @Override
    public Object getSimulationInspectedObject() { return state; } // return the model

    /**
     * Where we set up the visualization when the user is
     * about to start playing the simulation
     */
    private void setupPortrayals() {
        RatpNetwork ratpNetwork = (RatpNetwork) state;

        for(String lineName : Constants.listOfLinesNames){
            LinesDirectory.getInstance().lines.get(lineName).setupPortrayal();
        }

        display.attach(yardPortrayal, "Voyageurs");
        yardPortrayal.setField(ratpNetwork.yard);
        yardPortrayal.setPortrayalForClass(AgentVoyageur.class, getVoyageurPortrayal());

        StationsDirectory.getInstance().setUpGarePortrayal();

        display.reset();
        display.setBackdrop(Color.BLACK);
        display.repaint();
    }

    private VoyageurPortrayal getVoyageurPortrayal(){
        VoyageurPortrayal vp = new VoyageurPortrayal();
        return vp;
    }

    @Override
    public boolean step() {
        display.updateUI();
        display.repaint();
        return super.step();
    }
}
