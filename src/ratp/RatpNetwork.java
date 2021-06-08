package ratp;

import lines.Line;
import rame.RameFactory;
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

import java.util.*;

public class RatpNetwork extends SimState {

    /**linesGeomVectorField contains all the LineString of the network*/
    //Map<String,GeomVectorField> linesGeomVectorField = new HashMap<>();

    /**We WILL make another Graph here for passenger interconnection*/
    //public GeomPlanarGraph passengerNetwork = new GeomPlanarGraph();

    Map<String, Line> lines = LinesDirectory.getInstance().lines;
    RameFactory factory = RameFactory.getInstance();

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
        rameGeometry.addAttribute("type", "rame");
        rameGeometry.addAttribute("direction", Integer.toString(schedules.get(0).direction));
        rameGeometry.addAttribute("rame", r);
        getLine(lineName).getRight().addGeometry(r.getGeometry());
        this.schedule.scheduleRepeating(r);

    }

    public void start() {
        super.start();
        // ce code est la fameuse factory qui crée un rame pour chaque schedule (attention les rame qui arrive en face s'arrêteront)
        /*factory.setBaseRame(this);
        List<Pair<String, Rame>> listeRame =  factory.getRame();
        Iterator rameIterator = listeRame.iterator();
        while(rameIterator.hasNext()){
            Rame r = ((Pair<String, Rame>)rameIterator.next()).getRight();
            this.schedule.scheduleRepeating(r);
        }*/

        //ce code permet de tester en ajouter des rames à l'unité, il suffit de préciser la ligne et la liste de schedule de la rame
        SchedulesDirectory sd = SchedulesDirectory.getInstance();
        //List<Schedule> schedules = sd.schedules.get("1").get("La Défense -> Château de Vincennes");
        //addAgent("1", schedules);
        addAgent("14", sd.schedules.get("14").get("Olympiades -> Mairie de Saint-Ouen"));
        //addAgent("3", sd.schedules.get("3").get("Pont de Levallois - Bécon -> Gallieni"));
        //addAgent("6");
    }

}
