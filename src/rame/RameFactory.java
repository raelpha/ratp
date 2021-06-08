package rame;

import ratp.RatpNetwork;
import ratp.directory.SchedulesDirectory;
import global.Constants;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.MasonGeometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RameFactory implements Steppable {

    private static RameFactory INSTANCE = new RameFactory();

    List<Pair<String, Rame>> listOfRame = new ArrayList<>();
    Map<String, Map<String, List<SchedulesDirectory.Schedule>>> s = SchedulesDirectory.getInstance().schedules;
    List<String> listOfLine = Constants.listOfLinesNames;

    public static RameFactory getInstance() {return INSTANCE;}

    private RameFactory() {

    }
    public void setBaseRame (RatpNetwork geo){
        Iterator lineNameIterator = listOfLine.iterator();
        while(lineNameIterator.hasNext()){
            String lineName = (String) lineNameIterator.next();
            Map<String, List<SchedulesDirectory.Schedule>> scheduleOnLine = s.get(lineName);
            Iterator scheduleIterator = scheduleOnLine.values().iterator();
            while(scheduleIterator.hasNext()){
                Rame r = new Rame(geo, lineName, (List<SchedulesDirectory.Schedule>) scheduleIterator.next());
                addRameToGeometry(geo, r);
                Pair<String, Rame> newRame = new Pair(lineName, r);
                listOfRame.add(newRame);
            }
        }
    }

    public List<Pair<String, Rame>> getRame() {
        return listOfRame;
    }

    @Override
    public void step(SimState simState) {

    }

    private void addRameToGeometry(RatpNetwork geo, Rame r){
        MasonGeometry rameGeometry = r.getGeometry();
        rameGeometry.addAttribute("type", "rame");
        rameGeometry.addAttribute("rame", r);
        rameGeometry.addAttribute("direction", r.getDirection());
        geo.getLine(r.getNameLine()).getRight().addGeometry(rameGeometry);
    }
}
