package rame;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;
import global.Constants;
import station.Station;
import voyageur.AgentVoyageur;
import sim.engine.SimState;
import sim.engine.Steppable;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import ratp.RatpNetwork;
import sim.util.Bag;
import sim.util.geo.*;

import java.util.*;
import java.math.BigDecimal;

import ratp.directory.SchedulesDirectory.Schedule;


public class Rame implements Steppable {

    private Station currentStation = null;
    private LineString currentLine;
    private int lineDirection;
    private LineString nextLine;
    private final MasonGeometry location;
    private final List<Schedule> scheduleStation;
    private final Iterator itSchedule;
    private String nextStation;
    private String nextnextStation;
    private final double maxSpeed = Constants.rameMaxSpeed;
    private double currentSpeed = 0.0000000000D;
    private final double acceleration = Constants.rameAcceleration;
    private final double braking = Constants.rameBraking;
    private LengthIndexedLine segment;
    private int attente;
    private boolean finish = false;
    private int panne = 0;
    double startIndex;
    double endIndex;
    double currentIndex;
    double nextStartIndex;
    public PointMoveTo pointMoveTo;
    private static final GeometryFactory fact = new GeometryFactory();
    private int maxUser = Constants.MAX_USER_RAME;
    //TODO change public to private
    public List<AgentVoyageur> users = new ArrayList<>();
    private List<AgentVoyageur> forceUser = new ArrayList<>();
    private final String nameLine;
    private int colereCooldown = 50;

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
        this.attente = Constants.attenteRame;
        setDepart(state, ((Schedule)itSchedule.next()).station.name);
        this.location.addDoubleAttribute("MOVE RATE", this.maxSpeed);
        if(params.length > 0) {
            if(params.length>=1) {
                forceUser = (List<AgentVoyageur>) params[0];
            }
            if(params.length>=2) {
                maxUser = (int) params[1];
            }
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

    public void setPanne(int p){
        this.panne = p;
        addColereToAllVoyageur();
    }

    public boolean isPanne(){return panne!=0;}

    public boolean isStopped(){return isPanne()||(currentSpeed==0 && currentStation==null);}

    private void addColereToAllVoyageur() {
        Iterator voyageur = users.iterator();
        while(voyageur.hasNext()){
            ((AgentVoyageur)voyageur.next()).addToColere(2);
        }
        Iterator forceVoyageur = forceUser.iterator();
        while(forceVoyageur.hasNext()){
            ((AgentVoyageur)forceVoyageur.next()).addToColere(15);
        }
    }

    private void addColereToAllForceUser(){
        Iterator voyageur = forceUser.iterator();
        while(voyageur.hasNext()){
            ((AgentVoyageur)voyageur.next()).addToColere(10);
        }
    }

    public void setFinish(){finish=true;}

    public void step(SimState state) {
        RatpNetwork network = (RatpNetwork) state;
        this.move(network);
    }

    private boolean arrived() {
        return this.currentSpeed > 0.0D && this.currentIndex >= this.endIndex || this.currentSpeed < 0.0D && this.currentIndex <= this.startIndex;
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
        }
        currentLine = originEdge.getLine();
        it = network.edgeIterator();
        GeomPlanarGraphEdge nextEdge = null;
        while(it.hasNext() && ((nextEdge == null) || (nextEdge!=null && !nextEdge.getStringAttribute("destinatio").equals(nextnextStation) && !nextEdge.getStringAttribute("origin").equals(nextnextStation) ))){
            nextEdge = (GeomPlanarGraphEdge) it.next();
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
        if(colereCooldown>0){
            colereCooldown--;
        }
        if(panne!=0){
            panne--;
            currentSpeed=0.0D;
        } else if (!this.arrived() && currentStation == null) {
            setNewVitesse(geo);
            if(this.currentSpeed == 0 && colereCooldown==0){
                addColereToAllVoyageur();
                colereCooldown = 50;
            }
            this.moveAlongPath();
        } else {
            if(this.nextStation.equals(this.nextnextStation) && attente == 0) {
                leaveStation();
                geo.getLine(this.nameLine).getRight().removeGeometry(this.location);
                finish = true;
                attente=-2;
            } else if(attente == -1) {
                if(enterInStation(geo)) {
                    attente = Constants.attenteRame;
                } else {
                    if(Constants.stationPassante) {
                        removeUser();
                        leaveStation();
                        addColereToAllForceUser();
                        nextStation = this.nextnextStation;
                        if (itSchedule.hasNext()) {
                            this.nextnextStation = ((Schedule) itSchedule.next()).station.name;
                        }
                        this.findNewPath(geo);
                        return;
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
                this.findNewPath(geo);
                setNewVitesse(geo);
                this.moveAlongPath();

            } else {
                //delete (event ou auto delete)
            }
        }
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
        Bag object = geo.getLine(this.nameLine).getRight().getGeometries();

        if(object.isEmpty()){
            return false;
        } else {
            Iterator itObject = object.iterator();
            while(itObject.hasNext()){
                MasonGeometry mg = (MasonGeometry) itObject.next();
                if (mg.hasAttribute("type") && mg.getStringAttribute("type").equals("station")){
                    currentStation = (Station)((AttributeValue)mg.getAttribute("station")).getValue();
                    if (currentStation.getName().equals(this.nextStation)){
                        if(currentStation.isFermee()){
                            return false;
                        } else {
                            currentStation.addRame(this);
                            this.currentSpeed = 0.0D;
                            return true;
                        }
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
                if(!isClosed) {
                    returnList.add(a);
                    toDelete.add(a);
                } else {
                    forceUser.add(a);
                    toDelete.add(a);
                }
            }
            if (!a.cheminEnvisage.isEmpty() && !a.cheminEnvisage.peek().getLeft().lineNumber.equals(this.nameLine)){
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
