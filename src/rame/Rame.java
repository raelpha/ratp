package rame;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;
import global.Constants;
import sim.app.virus.Agent;
import station.Station;
import voyageur.AgentVoyageur;
import sim.engine.SimState;
import sim.engine.Steppable;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import ratp.RatpNetwork;
import sim.field.geo.GeomVectorField;
import sim.util.Bag;
import sim.util.geo.*;

import java.util.*;
import java.math.BigDecimal;

import ratp.directory.SchedulesDirectory.Schedule;
import voyageur.VoyageurDonnees;


public class Rame implements Steppable {

    private Station currentStation = null;
    private LineString currentLine;
    private int lineDirection;
    private LineString nextLine;
    private MasonGeometry location;
    private List<Schedule> scheduleStation;
    private Iterator itSchedule;
    private String nextStation;
    private String nextnextStation;
    private double maxSpeed = 0.000001D;
    private double currentSpeed = 0.0000000000D;
    private double acceleration = 0.00000001D;
    private double braking = -0.000000008D;
    private LengthIndexedLine segment;
    private int attente;
    private boolean finish = false;
    private int panne = 0;
    double startIndex;
    double endIndex;
    double currentIndex;
    double nextStartIndex;
    public PointMoveTo pointMoveTo;
    private static GeometryFactory fact = new GeometryFactory();
    private int maxUser = Constants.MAX_USER_RAME;
    private List<AgentVoyageur> users = new ArrayList<>();
    private List<AgentVoyageur> forceUser = new ArrayList<>();
    private String nameLine;

    public Rame(RatpNetwork state, String nameLine, List<Schedule> schedule, Object ... params) {
        this.scheduleStation = schedule;
        this.itSchedule = scheduleStation.iterator();
        this.nameLine = nameLine;
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
        this.location.addAttribute("direction", Integer.toString(this.getDirection()));
        this.location.addAttribute("rame", this);
        this.attente = 100;
        setDepart(state, ((Schedule)itSchedule.next()).station.name);
        this.location.addDoubleAttribute("MOVE RATE", this.maxSpeed);
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

    public double getEndIndex() {return this.endIndex;}

    public int getDirection() {return this.scheduleStation.get(0).direction;}

    public Schedule getTerminus() {return this.scheduleStation.get(this.scheduleStation.size()-1);}

    public Schedule getOriginStation() {return this.scheduleStation.get(0);}

    public String getNameLine() {return this.nameLine;}

    public Boolean isFinish() {return this.finish;}

    public void setPanne(int p){this.panne = p;}

    public boolean isPanne(){return panne!=0;}

    public boolean isStopped(){return isPanne()||(currentSpeed==0 && currentStation==null);}

    public void step(SimState state) {
        //System.out.println("++++++++++++++++++++++++++++++");
        RatpNetwork network = (RatpNetwork) state;
        this.move(network);
    }

    private boolean arrived() {
        return this.currentSpeed > 0.0D && this.currentIndex >= this.endIndex || this.currentSpeed < 0.0D && this.currentIndex <= this.startIndex;
    }

    private void setDepart(RatpNetwork geo, String stationName){
        //System.out.println(stationName);
        nextStation = stationName;
        nextnextStation = ((Schedule)itSchedule.next()).station.name;
        //System.out.println(nextnextStation);
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
        enterInStation(geo);
    }

    private List<Rame> getRameOnLine(RatpNetwork geo, LineString line){
        List<Rame> rameInLine = new ArrayList<>();
        Bag objectGeo = geo.getLine(nameLine).getRight().getGeometries();
        Iterator objectIt = objectGeo.iterator();
        while(objectIt.hasNext()){
            MasonGeometry mElem = (MasonGeometry)objectIt.next();
            if(mElem.hasAttribute("type") && mElem.getStringAttribute("type").equals("rame")){
                Rame r = (Rame)(((AttributeValue)mElem.getAttribute("rame")).getValue());
                if(r.getDirection()==this.getDirection()) {
                    LineString lineOfRame = r.getCurrentLine();
                    if (lineOfRame.equals(line) && !r.getGeometry().equals(this.location)) {
                        rameInLine.add(r);
                    }
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
            if(index==-1 && line.getStartPoint().equals(nextStartIndex)) {
                if(Math.abs(r.getStartIndex()-r.getCurrentIndex())<=distance) {
                    return true;
                }
            } else if (index==-1 && line.getStartPoint().equals(nextStartIndex)){
                if(Math.abs(r.getEndIndex()-r.getCurrentIndex())<=distance){
                    return true;
                }
            } else if (index >=0 && Math.abs(r.getCurrentIndex()-index)<=distance){
                if((lineDirection==1 && r.getCurrentIndex()>index) || (lineDirection==-1 && r.getCurrentIndex()<index))
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
        if (this.location.getGeometry().getCoordinate().equals(startPoint.getCoordinate())) {
            this.setNewRoute(this.currentLine, true);
            if(!nextStation.equals(nextnextStation)) {
                this.nextLine = getLineForStation(geo, endPoint.getCoordinate(), this.nextnextStation);
                nextBegin = endPoint;
            }
        } else if(this.location.getGeometry().getCoordinate().equals(endPoint.getCoordinate())) {
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
            System.out.println("nothing happen");
            //nothing happen
        }
    }

    private LineString getLineForStation (RatpNetwork geo, Coordinate coord, String station) {
       // System.out.println(station);
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
                        //System.out.println(((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[i]).getEdge()).getStringAttribute("destinatio") + "+-+" + ((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[i]).getEdge()).getStringAttribute("origin"));
                        i++;
                    }
                    directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
                } else {
                    //System.out.println(((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[0]).getEdge()).getStringAttribute("origin") + " +-+ " +((GeomPlanarGraphEdge) ((GeomPlanarGraphDirectedEdge) edges[0]).getEdge()).getStringAttribute("destinatio"));
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
            this.lineDirection = 1;
        } else {
            startCoord = this.segment.extractPoint(this.endIndex);
            this.currentIndex = this.endIndex;
            this.lineDirection = -1;
        }

        this.moveTo(startCoord);
    }

    public void moveTo(Coordinate c) {
        this.pointMoveTo.setCoordinate(c);
        this.location.getGeometry().apply(this.pointMoveTo);
        this.getGeometry().geometry.geometryChanged();
    }

    private void setNewVitesse(RatpNetwork geo){
        BigDecimal cs = new BigDecimal(Double.toString(this.currentSpeed));
        BigDecimal ms = new BigDecimal(Double.toString(this.maxSpeed));
        BigDecimal div = new BigDecimal(20);
        BigDecimal mult = new BigDecimal(75);
        BigDecimal resSpeed = ms.divide(div);
        BigDecimal resBrak = cs.multiply(mult);
        double maxSpeedStation = resSpeed.doubleValue();
        double brakingLimitDistance = resBrak.doubleValue();

        if(isRameClose(geo, Constants.DETECTION_DISTANCE)){
            if(this.lineDirection==-1) {
                this.currentSpeed -= braking;
                if (this.currentSpeed > 0) {
                    this.currentSpeed = 0;
                }
            } else {
                this.currentSpeed += braking;
                if (this.currentSpeed < 0) {
                    this.currentSpeed = 0;
                }
            }
        }
        else if(((this.lineDirection == 1 && Math.abs(this.currentIndex-this.endIndex)<brakingLimitDistance) || (this.lineDirection == -1 && Math.abs(this.currentIndex-this.startIndex)<brakingLimitDistance)) && Math.abs(this.currentSpeed)>maxSpeedStation){
            if(this.lineDirection==-1) {
                this.currentSpeed -= braking;
                return;
            } else if (this.lineDirection==1) {
                this.currentSpeed += braking;
                return;
            }
        }
        else if (Math.abs(this.currentSpeed)<this.maxSpeed) {
            if(this.lineDirection==-1) {
                this.currentSpeed -= this.acceleration;
                if (this.currentSpeed < -this.maxSpeed) {
                    this.currentSpeed = -this.maxSpeed;
                }
            } else {
                this.currentSpeed += this.acceleration;
                if (this.currentSpeed > this.maxSpeed) {
                    this.currentSpeed = this.maxSpeed;
                }
            }
        }
    }

    private void move(RatpNetwork geo) {
        if(panne!=0){
            panne--;
            currentSpeed=0.0D;
        } else if (!this.arrived() && currentStation == null) {
            setNewVitesse(geo);
            this.moveAlongPath();
        } else {
            if(this.nextStation.equals(this.nextnextStation) && attente == 0) {
                leaveStation();
                geo.getLine(this.nameLine).getRight().removeGeometry(this.location);
                finish = true;
                attente=-2;
            } else if(attente == -1) {
                if(enterInStation(geo)) {
                    attente = 100;
                } else {
                    if(Constants.stationPassante) {
                        removeUser();
                        nextStation = this.nextnextStation;
                        if (itSchedule.hasNext()) {
                            this.nextnextStation = ((Schedule) itSchedule.next()).station.name;
                        }
                        this.findNewPath(geo);
                    }
                }
            } else if (attente > 0) {
                attente--;
            } else if(!this.nextStation.equals(this.nextnextStation) && attente == 0) {
                leaveStation();
                attente --;
                nextStation = this.nextnextStation;
                if(itSchedule.hasNext()) {
                    this.nextnextStation = ((Schedule) itSchedule.next()).station.name;
                }
                //System.out.println(nextStation + " " + nextnextStation);
                this.findNewPath(geo);

            } else {
                //delete (event ou auto delete)
            }
            //System.out.println(nextStation + "+-+" + nextnextStation);
            //System.out.println("------------------------------------");
        }
        //System.out.println(this.currentSpeed);
    }

    private void moveAlongPath() {
        this.currentIndex += this.currentSpeed;
        if (this.currentSpeed < 0.0D) {
            if (this.currentIndex < this.startIndex) {
                this.currentIndex = this.startIndex;
            }
        } else if (this.currentIndex > this.endIndex) {
            this.currentIndex = this.endIndex;
        }
        Coordinate currentPos = this.segment.extractPoint(this.currentIndex);
        this.moveTo(currentPos);
    }

    private boolean enterInStation (RatpNetwork geo) {
        this.currentSpeed = 0.0D;
        Bag object = geo.getLine(this.nameLine).getRight().getGeometries();
        if(object.isEmpty()){
            return false;
        } else {
            Iterator itObject = object.iterator();
            while(itObject.hasNext()){
                MasonGeometry mg = (MasonGeometry) itObject.next();
                if (mg.hasAttribute("type") && mg.getStringAttribute("type").equals("station")){
                    currentStation = (Station)((AttributeValue)mg.getAttribute("station")).getValue();
                    if (currentStation.getName().equals(this.nextStation) && !currentStation.isFermee()){
                        currentStation.addRame(this);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void leaveStation() {
        currentStation.removeRame(this);
        currentStation = null;
    }

    public int numberOfUser(){
        return users.size()+forceUser.size();
    }

    public int freePlaces(){
        return maxUser-numberOfUser();
    }

    public boolean addUser(AgentVoyageur u){
        if(freePlaces()!=0){
            users.add(u);
            return true;
        } else {
            return false;
        }
    }

    public List<AgentVoyageur> removeUser () {
        String stationName = currentStation.getName();
        Boolean isClosed = currentStation.isFermee();
        List<AgentVoyageur> returnList = new ArrayList<>();
        ListIterator<AgentVoyageur> it = users.listIterator();
        List<AgentVoyageur> toDelete = new ArrayList<>();
        while(it.hasNext()){
            AgentVoyageur a = it.next();
            if(!a.cheminEnvisage.isEmpty() && a.cheminEnvisage.peek().getLeft().name.equals(stationName)){
                a.cheminEnvisage.poll();
            }
            if (a.cheminEnvisage.isEmpty()){
                forceUser.add(a);
                toDelete.add(a);
            }
            if (!a.cheminEnvisage.peek().getLeft().line.equals(this.nameLine)){
                if(!isClosed) {
                    returnList.add(a);
                    toDelete.add(a);
                } else {
                    forceUser.add(a);
                    toDelete.add(a);
                }
            }
        }
        users.removeAll(toDelete);
        return returnList;
    }

    public List<AgentVoyageur> forceRemoveUser() {
        return forceUser;
    }

}
