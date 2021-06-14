package rame;

import com.vividsolutions.jts.geom.Point;
import ratp.RatpNetwork;
import ratp.directory.SchedulesDirectory;
import global.Constants;
import sim.app.geo.masoncsc.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.geo.MasonGeometry;
import voyageur.AgentVoyageur;

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
    int creationTicker;

    public static RameFactory getInstance() {return INSTANCE;}

    private RameFactory() {
        this.creationTicker = Constants.generateMode;
    }
    public void setBaseRame (RatpNetwork geo){
        Iterator lineNameIterator = listOfLine.iterator();
        while(lineNameIterator.hasNext()){
            String lineName = (String) lineNameIterator.next();
            Map<String, List<SchedulesDirectory.Schedule>> scheduleOnLine = s.get(lineName);
            Iterator scheduleIterator = scheduleOnLine.values().iterator();
            while (scheduleIterator.hasNext()) {
                addRame(geo, lineName, (List<SchedulesDirectory.Schedule>) scheduleIterator.next());
            }
        }
    }

    public float getNbRameStopped() {
        if (listOfRame.size() == 0)
            return 0.F;
        int count = 0;
        for (Pair<String, Rame> rame: listOfRame) {
            if (rame.getRight().isStopped()) {
                count++;
            }
        }

        return (float) count / (float) listOfRame.size();
    }

    private void createRameForLigne (RatpNetwork geo, String lineName){
        Map<String, List<SchedulesDirectory.Schedule>> scheduleOnLine = s.get(lineName);
        Iterator scheduleIterator = scheduleOnLine.values().iterator();
        while (scheduleIterator.hasNext()) {
            addRame(geo, lineName, (List<SchedulesDirectory.Schedule>) scheduleIterator.next());
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
        checkForNewRame(geo);
        if(bufferCheck!=0){
            bufferCheck--;
        } else {
            //System.out.println(bufferRame.size());
            checkBuffer(geo);
            bufferCheck = 150;
        }
    }

    private void checkForNewRame(RatpNetwork geo){
        Iterator lineit = listOfLine.iterator();
        if(creationTicker < 0) {
            List<String> toAdd = new ArrayList<>();
            while (lineit.hasNext()) {
                String lineName = (String) lineit.next();
                int number = 0;
                int surchargeNumber = 0;
                Iterator rameIt = listOfRame.iterator();
                while (rameIt.hasNext()) {
                    Pair<String, Rame> elem = (Pair<String, Rame>) rameIt.next();
                    if (elem.getLeft().equals(lineName)) {
                        number++;
                        if (elem.getRight().freePlaces() < Constants.listOfCapacity.get(lineName) / 10) {
                            surchargeNumber++;
                        }
                    }
                }
                if ((float) surchargeNumber / (float) number > 0.5) {
                    toAdd.add(lineName);
                }
            }
            Iterator rameToAdd = toAdd.iterator();
            while (rameToAdd.hasNext()) {
                createRameForLigne(geo, (String) rameToAdd.next());
            }
        } else if (creationTicker>0){
            creationTicker--;
        } else {
            creationTicker = Constants.generateMode;
            while(lineit.hasNext()){
                createRameForLigne(geo, (String) lineit.next());
            }
        }
    }

    private void checkBuffer (RatpNetwork geo) {
        /*Bag test = geo.getLine("3b").getRight().getGeometries();
        Iterator testIt = test.iterator();
        while(testIt.hasNext()){
            //System.out.println(((MasonGeometry)testIt.next()).getAttributes());
        }*/
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
        Object[] params = {new ArrayList<AgentVoyageur>(), Constants.listOfCapacity.get(lineName)};
        createRame(geo, lineName, s, params);
        return true;
    }

    private void createRame(RatpNetwork geo, String lineName, List<SchedulesDirectory.Schedule> s, Object ... params){
        Rame r = new Rame(geo, lineName, s, params);
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
        Object[] params = {r.forceRemoveUser(), Constants.listOfCapacity.get(r.getNameLine())};
        createRame(geo, r.getNameLine(), sch, params);
    }

    public void clear(){
        Iterator rame = listOfRame.iterator();
        while(rame.hasNext()){
            ((Pair<String,Rame>)rame.next()).getRight().setFinish();
        }
        listOfRame.clear();
        bufferRame.clear();
        actualStepCreation.clear();
        creationTicker = Constants.generateMode;
        bufferCheck = 150;
    }
}
