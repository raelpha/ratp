package ratp;

import lines.Line;
import ratp.directory.LinesDirectory;
import ratp.directory.SchedulesDirectory;
import ratp.directory.SchedulesDirectory.*;
import sim.engine.SimState;


import global.Constants;
import rame.Rame;
import sim.app.geo.campusworld.Agent;
import sim.app.geo.masoncsc.util.Pair;
import sim.field.geo.GeomVectorField;
import rame.Rame;
import sim.util.geo.MasonGeometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

public class RatpNetwork extends SimState {

    /**linesGeomVectorField contains all the LineString of the network*/
    //Map<String,GeomVectorField> linesGeomVectorField = new HashMap<>();

    /**We WILL make another Graph here for passenger interconnection*/
    //public GeomPlanarGraph passengerNetwork = new GeomPlanarGraph();

    Map<String, Line> lines = LinesDirectory.getInstance().lines;

    public RatpNetwork(long seed){
        super(seed);

        /*for (String s : Constants.listOfLinesNames){
            linesGeomVectorField.put(s, new GeomVectorField(Constants.FIELD_SIZE, Constants.FIELD_SIZE));
        }*/
        //FileImporter.shapeFileImporterByLine("lines/lines", linesGeomVectorField);


    }

    public Pair<String, GeomVectorField> getLine(String name){
        GeomVectorField l = lines.get(name).geomVectorField;
        Pair returnValue = new Pair <String, GeomVectorField>(name,l);
        return returnValue;
    }

    private void addAgent(String lineName, List<Schedule> schedules){
        Rame r = new Rame(this, lineName, schedules);
        MasonGeometry rameGeometry = r.getGeometry();
        rameGeometry.addAttribute("direction", Integer.toString(schedules.get(0).direction));
        getLine(lineName).getRight().addGeometry(r.getGeometry());
        this.schedule.scheduleRepeating(r);
    }

    public void start() {
        super.start();
        SchedulesDirectory sd = SchedulesDirectory.getInstance();
        List<Schedule> schedules = sd.schedules.get("1").get("La Défense -> Château de Vincennes");
        addAgent("1", schedules);
        addAgent("1", sd.schedules.get("1").get("Château de Vincennes -> La Défense"));
        //addAgent("3");
        //addAgent("6");
    }

}
