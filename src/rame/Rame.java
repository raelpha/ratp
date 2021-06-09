package rame;


import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;
import global.Constants;
import sim.engine.SimState;
import sim.engine.Steppable;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import ratp.RatpNetwork;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.*;

import java.util.*;

import ratp.directory.SchedulesDirectory.Schedule;


public class Rame implements Steppable {

    private LineString currentLine;
    private LineString nextLine;
    private MasonGeometry location;
    private List<Schedule> scheduleStation;
    private Iterator itSchedule;
    private String nextStation;
    private String nextnextStation;
    private double basemoveRate = 0.00001D;
    private double moveRate;
    private LengthIndexedLine segment;
    private int attente;
    double startIndex;
    double endIndex;
    double currentIndex;
    double nextStartIndex;
    PointMoveTo pointMoveTo;
    private static GeometryFactory fact = new GeometryFactory();
    private int maxUser = Constants.MAX_USER_RAME;
    private List<Object> users = new ArrayList<Object>();
    private String nameLine;

    public Rame(RatpNetwork state, String nameLine, List<Schedule> schedule, Object ... params) {
        this.scheduleStation = schedule;
        this.itSchedule = scheduleStation.iterator();
        this.nameLine = nameLine;
        this.moveRate = this.basemoveRate;
        this.segment = null;
        this.startIndex = 0.0D;
        this.endIndex = 0.0D;
        this.currentIndex = 0.0D;
        this.nextStartIndex = 0.00D;
        this.pointMoveTo = new PointMoveTo();
        this.location = new MasonGeometry(fact.createPoint(new Coordinate(0.1D, 0.1D)));
        this.location.isMovable = true;
        this.location.addAttribute("type", "rame");
        this.location.addAttribute("color", "#ff0000");
        this.attente = -1;
        setDepart(state, ((Schedule)itSchedule.next()).station.name);
        this.location.addDoubleAttribute("MOVE RATE", this.basemoveRate);
        if(params.length > 0) {
            maxUser = (int) params[0];
        }
    }

    public MasonGeometry getGeometry() {
        return this.location;
    }

    public LineString getCurrentLine() { return this.currentLine;}

    public double getCurrentIndex() {return this.currentIndex;}

    public double getStartIndex() {return this.startIndex;}

    public int getDirection() {return this.scheduleStation.get(0).direction;}

    public Schedule getTerminus() {return this.scheduleStation.get(this.scheduleStation.size()-1);}

    public String getNameLine() {return this.nameLine;}

    public void step(SimState state) {
        RatpNetwork network = (RatpNetwork) state;
        this.move(network);
    }

    private boolean arrived() {
        return this.moveRate > 0.0D && this.currentIndex >= this.endIndex || this.moveRate < 0.0D && this.currentIndex <= this.startIndex;
    }

    private void setDepart(RatpNetwork geo, String stationName){
        nextStation = stationName;
        nextnextStation = ((Schedule)itSchedule.next()).station.name;
        GeomPlanarGraph network = new GeomPlanarGraph();
        network.createFromGeomField(geo.getLine(this.nameLine).getRight());
        Iterator it = network.edgeIterator();
        GeomPlanarGraphEdge originEdge = null;
        while(it.hasNext() && ((originEdge == null) || (originEdge!=null && !originEdge.getStringAttribute("destinatio").equals(stationName) && !originEdge.getStringAttribute("origin").equals(stationName) ))){
            originEdge = (GeomPlanarGraphEdge) it.next();
            //System.out.println("Origine : "+ originEdge.getStringAttribute("origin")+ originEdge.getStringAttribute("destinatio"));
        }
        currentLine = originEdge.getLine();
        it = network.edgeIterator();
        GeomPlanarGraphEdge nextEdge = null;
        while(it.hasNext() && ((nextEdge == null) || (nextEdge!=null && !nextEdge.getStringAttribute("destinatio").equals(nextnextStation) && !nextEdge.getStringAttribute("origin").equals(nextnextStation) ))){
            nextEdge = (GeomPlanarGraphEdge) it.next();
            //System.out.println("Origine : "+ originEdge.getStringAttribute("origin")+ originEdge.getStringAttribute("destinatio"));
        }
        nextLine = nextEdge.getLine();
        if(originEdge.getStringAttribute("origin").equals(stationName)){
            this.setNewRoute(currentLine, true);
        } else {
            this.setNewRoute(currentLine, false);
        }
        this.nextStation = this.nextnextStation;
        this.nextnextStation = ((Schedule)itSchedule.next()).station.name;
    }

    private List<Rame> getRameOnLine(RatpNetwork geo, LineString line){
        List<Rame> rameInLine = new ArrayList<>();
        Bag objectGeo = geo.getLine(nameLine).getRight().getGeometries();
        Iterator objectIt = objectGeo.iterator();
        String direction = Integer.toString(getDirection());
        while(objectIt.hasNext()){
            MasonGeometry mElem = (MasonGeometry)objectIt.next();
            if(mElem.hasAttribute("type") && mElem.getStringAttribute("type").equals("rame") && mElem.getStringAttribute("direction").equals(direction)){
                Rame r = (Rame)(((AttributeValue)mElem.getAttribute("rame")).getValue());
                LineString lineOfRame = r.getCurrentLine();
                if(lineOfRame.equals(line) && !r.getGeometry().equals(this.location)) {
                    rameInLine.add(r);
                }
            }
        }
        return rameInLine;
    }

    private boolean isRameCloseOnLine (RatpNetwork geo, LineString line, double index, double distance){
        List<Rame> rameOnLine = getRameOnLine(geo, line);
        Iterator rameIterator = rameOnLine.iterator();
        while(rameIterator.hasNext()){
            Rame r = (Rame) rameIterator.next();
            if(index==-1 && Math.abs(r.getStartIndex()-r.getCurrentIndex())<=distance){
                return true;
            }
            if (index >=0 && Math.abs(r.getCurrentIndex()-index)<=distance){
                return true;
            }
        }
        return false;
    }

    private boolean isRameClose(RatpNetwork geo, double distance) {

        if(isRameCloseOnLine(geo, this.currentLine, this.currentIndex, distance)){
            return true;
        }
        if(Math.abs(this.currentIndex-this.endIndex)<distance){
            if(isRameCloseOnLine(geo, this.nextLine, -1, distance-Math.abs(this.endIndex-this.currentIndex))){
                return true;
            }
        }
        return false;
    }

    private void findNewPath(RatpNetwork geo) {
        this.currentLine = getLineForStation(geo, this.location.getGeometry().getCoordinate(), this.nextStation);
        Point startPoint = this.currentLine.getStartPoint();
        Point endPoint = this.currentLine.getEndPoint();
        Point nextBegin = null;
        if (startPoint.equals(this.location.geometry)) {
            this.setNewRoute(this.currentLine, true);
            if(!nextStation.equals(nextnextStation)) {
                this.nextLine = getLineForStation(geo, endPoint.getCoordinate(), this.nextnextStation);
                nextBegin = endPoint;
            }
        } else if (endPoint.equals(this.location.geometry)) {
            this.setNewRoute(this.currentLine, false);
            if(!nextStation.equals(nextnextStation)) {
                this.nextLine = getLineForStation(geo, startPoint.getCoordinate(), this.nextnextStation);
                nextBegin = startPoint;
            }
        } else {
            System.err.println("Where am I?");
        }
        LengthIndexedLine l = new LengthIndexedLine(this.nextLine);
        if(nextBegin != null && this.nextLine.getStartPoint().equals(nextBegin)) {
            this.nextStartIndex = l.getStartIndex();
        } else if (nextBegin!=null && this.nextLine.getEndPoint().equals(nextBegin)){
            this.nextStartIndex = l.getEndIndex();
        } else {
            //nothing happen
        }
    }

    private LineString getLineForStation (RatpNetwork geo, Coordinate coord, String station) {
        GeomPlanarGraph network = new GeomPlanarGraph();
        network.createFromGeomField(geo.getLine(this.nameLine).getRight());
        Node currentJunction = network.findNode(coord);
        if (currentJunction != null) {
            DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();
            if (edges.length > 0) {
                GeomPlanarGraphDirectedEdge directedEdge;
                if (edges.length > 1) {
                    int i = 0;
                    while (!((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[i]).getEdge()).getStringAttribute("destinatio").equals(station) && !((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[i]).getEdge()).getStringAttribute("origin").equals(station)) {
                        i++;
                    }
                    directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
                } else {
                   // System.out.println("hello");
                    System.out.println(((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[0]).getEdge()).getStringAttribute("origin") + " +-+ " +((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[0]).getEdge()).getStringAttribute("destinatio"));
                    directedEdge = (GeomPlanarGraphDirectedEdge) edges[0];
                }
                GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge) directedEdge.getEdge();
                return edge.getLine();
            }
        }
        return null;
    }

    private void setNewRoute(LineString line, boolean start) {
        this.segment = new LengthIndexedLine(line);
        this.startIndex = this.segment.getStartIndex();
        this.endIndex = this.segment.getEndIndex();
        Coordinate startCoord = null;
        if (start) {
            startCoord = this.segment.extractPoint(this.startIndex);
            this.currentIndex = this.startIndex;
            this.moveRate = this.basemoveRate;
        } else {
            startCoord = this.segment.extractPoint(this.endIndex);
            this.currentIndex = this.endIndex;
            this.moveRate = -this.basemoveRate;
        }

        this.moveTo(startCoord);
    }

    public void moveTo(Coordinate c) {
        this.pointMoveTo.setCoordinate(c);
        this.location.getGeometry().apply(this.pointMoveTo);
        this.getGeometry().geometry.geometryChanged();
    }

    private void move(RatpNetwork geoTest) {
        if(isRameClose(geoTest,0.0001D)){
            this.moveRate = 0;
            this.basemoveRate = 0;
        }
        if (!this.arrived()) {
            this.moveAlongPath();
        } else {
            if(!this.nextStation.equals(this.nextnextStation) && attente == -1) {
                attente = 100;
            } else if (!this.nextStation.equals(this.nextnextStation) && attente > 0) {
                attente--;
            } else if(!this.nextStation.equals(this.nextnextStation) && attente == 0) {
                attente --;
                nextStation = this.nextnextStation;
                if(itSchedule.hasNext()) {
                    this.nextnextStation = ((Schedule) itSchedule.next()).station.name;
                }
                //System.out.println(nextStation + " " + nextnextStation);
                this.findNewPath(geoTest);
            } else {
                //delete (event ou auto delete)
            }
        }
    }

    private void moveAlongPath() {
        this.currentIndex += this.moveRate;
        if (this.moveRate < 0.0D) {
            if (this.currentIndex < this.startIndex) {
                this.currentIndex = this.startIndex;
            }
        } else if (this.currentIndex > this.endIndex) {
            this.currentIndex = this.endIndex;
        }

        Coordinate currentPos = this.segment.extractPoint(this.currentIndex);
        this.moveTo(currentPos);

    }

    public int numberOfUser(){
        return users.size();
    }

    public int freePlaces(){
        return maxUser-numberOfUser();
    }

    public boolean addUser(Object u){
        if(freePlaces()!=0){
            users.add(u);
            return true;
        } else {
            return false;
        }
    }

    public List<Object> removeUser (String stationName) {
        List<Object> returnList = new ArrayList<Object>();
        ListIterator<Object> it = users.listIterator();
        while(it.hasNext()){
            //precédure de détection et d'ajout
        }
        return returnList;
    }

}
