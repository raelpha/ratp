package rame;

import com.vividsolutions.jts.geom.Point;
import ratp.RatpNetwork;
import ratp.directory.LinesDirectory;
import ratp.directory.SchedulesDirectory;
import global.Constants;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.AttributeValue;
import sim.util.geo.MasonGeometry;

import java.sql.Array;
import java.util.*;

public class RameFactory implements Steppable {

    private static RameFactory INSTANCE = new RameFactory();

    List<Pair<String, Rame>> listOfRame = new ArrayList<>();
    Map<String, Map<String, List<SchedulesDirectory.Schedule>>> s = SchedulesDirectory.getInstance().schedules;
    List<String> listOfLine = Constants.listOfLinesNames;
    List<Pair<String, List<SchedulesDirectory.Schedule>>> bufferRame = new ArrayList<>();
    List<Point> actualStepCreation = new ArrayList<>();
    int bufferCheck = 150;

    public static RameFactory getInstance() {return INSTANCE;}

    private RameFactory() {

    }
    public void setBaseRame (RatpNetwork geo){
        Iterator lineNameIterator = listOfLine.iterator();
        while(lineNameIterator.hasNext()){
            String lineName = (String) lineNameIterator.next();
            Map<String, List<SchedulesDirectory.Schedule>> scheduleOnLine = s.get(lineName);
            Iterator scheduleIterator = scheduleOnLine.values().iterator();
            if(lineName.equals("3b")) {
                while (scheduleIterator.hasNext()) {
                    addRame(geo, lineName, (List<SchedulesDirectory.Schedule>) scheduleIterator.next());
                }
            }
        }
    }

    public List<Pair<String, Rame>> getRame() {
        return listOfRame;
    }

    @Override
    public void step(SimState simState) {
        RatpNetwork geo = (RatpNetwork) simState;
        actualStepCreation.clear();
        checkForTerminus(geo);
        if(bufferCheck!=0){
            bufferCheck--;
        } else {
            //System.out.println(bufferRame.size());
            checkBuffer(geo);
            bufferCheck = 150;
        }
    }

    private void checkBuffer (RatpNetwork geo) {
        Bag test = geo.getLine("3b").getRight().getGeometries();
        Iterator testIt = test.iterator();
        while(testIt.hasNext()){
            System.out.println(((MasonGeometry)testIt.next()).getAttributes());
        }
        List<Pair<String, List<SchedulesDirectory.Schedule>>> toDelete = new ArrayList<>();
        Iterator bufferIt = bufferRame.iterator();
        while(bufferIt.hasNext()){
            Pair<String, List<SchedulesDirectory.Schedule>> elem = (Pair<String, List<SchedulesDirectory.Schedule>>)bufferIt.next();
            if(addRame(geo, elem.getLeft(), elem.getRight())){
                toDelete.add(elem);
            }
        }
        bufferRame.removeAll(toDelete);
    }
    private boolean addRame (RatpNetwork geo , String lineName, List<SchedulesDirectory.Schedule> s){
        Point startPoint = s.get(0).station.location;
        if(actualStepCreation.contains(startPoint)){
            Pair<String, List<SchedulesDirectory.Schedule>> r = new Pair<>(lineName, s);
            bufferRame.add(r);
            return false;
        }
        MasonGeometry startRame = new MasonGeometry(startPoint);
        Bag obj = geo.getLine(lineName).getRight().getTouchingObjects(startRame);
        Iterator objIterator = obj.iterator();
        while (objIterator.hasNext()) {
            MasonGeometry elem = (MasonGeometry) objIterator.next();
            if (elem.hasAttribute("type") && elem.getStringAttribute("type").equals("rame")) {
                Pair<String, List<SchedulesDirectory.Schedule>> r = new Pair<>(lineName, s);
                bufferRame.add(r);
                return false;
            }
        }
        actualStepCreation.add(startPoint);
        createRame(geo, lineName, s);
        return true;
    }

    private void createRame(RatpNetwork geo, String lineName, List<SchedulesDirectory.Schedule> s){
        Rame r = new Rame(geo, lineName, s);
        addRameToGeometry(geo, r);
        geo.schedule.scheduleRepeating(r);
        Pair<String, Rame> newRame = new Pair(lineName, r);
        listOfRame.add(newRame);
    }

    private void addRameToGeometry(RatpNetwork geo, Rame r){
        MasonGeometry rameGeometry = r.getGeometry();
        geo.getLine(r.getNameLine()).getRight().addGeometry(rameGeometry);
    }

    private void checkForTerminus(RatpNetwork geo) {
        List<Pair<String, Rame>> toDelete = new ArrayList<>();
        Iterator rameIterator = listOfRame.iterator();
        while (rameIterator.hasNext()) {
            Pair<String, Rame> elem = (Pair<String, Rame>) rameIterator.next();
            if (elem.getRight().isFinish()) {
                toDelete.add(elem);
            }
        }
        Iterator rameToCreate = toDelete.iterator();
        while (rameToCreate.hasNext()){
            createReverse(geo, ((Pair<String, Rame>)rameToCreate.next()).getRight());
        }
        listOfRame.removeAll(toDelete);
    }

    private void createReverse(RatpNetwork geo, Rame r){
        String stationDebut = r.getOriginStation().station.name;
        String stationEnd = r.getTerminus().station.name;
        List<SchedulesDirectory.Schedule> sch = s.get(r.getNameLine()).get(stationEnd + " -> " + stationDebut);
        createRame(geo, r.getNameLine(), sch);
    }
}
