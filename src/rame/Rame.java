package rame;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
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

    private MasonGeometry location;
    private List<Schedule> scheduleStation;
    private Iterator itSchedule;
    private String nextStation;
    private double basemoveRate = 0.000001D;
    private double moveRate;
    private int detect = 10;
    private LengthIndexedLine segment;
    private int attente;
    double startIndex;
    double endIndex;
    double currentIndex;
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

    public void step(SimState state) {
        RatpNetwork network = (RatpNetwork) state;
        this.move(network);
    }

    private boolean arrived() {
        return this.moveRate > 0.0D && this.currentIndex >= this.endIndex || this.moveRate < 0.0D && this.currentIndex <= this.startIndex;
    }

    private void setDepart(RatpNetwork geo, String stationName){
        GeomPlanarGraph network = new GeomPlanarGraph();
        network.createFromGeomField(geo.getLine(this.nameLine).getRight());
        Iterator it = network.edgeIterator();
        GeomPlanarGraphEdge originEdge = null;
        while(it.hasNext() && ((originEdge == null) || (originEdge!=null && !originEdge.getStringAttribute("destinatio").equals(stationName) && !originEdge.getStringAttribute("origin").equals(stationName) ))){
            originEdge = (GeomPlanarGraphEdge) it.next();
            //System.out.println("Origine : "+ originEdge.getStringAttribute("origin")+ originEdge.getStringAttribute("destinatio"));
        }
        LineString originLine = originEdge.getLine();
        if(originEdge.getStringAttribute("origin").equals(stationName)){
            this.setNewRoute(originLine, true);
        } else {
            this.setNewRoute(originLine, false);
        }
        nextStation = ((Schedule)itSchedule.next()).station.name;
    }

    private void findNewPath(RatpNetwork geoTest) {
        GeomPlanarGraph network = new GeomPlanarGraph();
        network.createFromGeomField(geoTest.getLine(this.nameLine).getRight());
        Node currentJunction = network.findNode(this.location.getGeometry().getCoordinate());
        if (currentJunction != null) {
            DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();
            if (edges.length > 0) {
                GeomPlanarGraphDirectedEdge directedEdge;
                if(edges.length>1) {
                    int i = 0;
                    while (!((GeomPlanarGraphEdge)((GeomPlanarGraphDirectedEdge)edges[i]).getEdge()).getStringAttribute("destinatio").equals(this.nextStation) && !((GeomPlanarGraphEdge)((GeomPlanarGraphDirectedEdge)edges[i]).getEdge()).getStringAttribute("origin").equals(this.nextStation)) {
                        i++;
                    }
                    directedEdge = (GeomPlanarGraphDirectedEdge) edges[i];
                } else {
                    directedEdge = (GeomPlanarGraphDirectedEdge) edges[0];
                }
                GeomPlanarGraphEdge edge = (GeomPlanarGraphEdge)directedEdge.getEdge();
                LineString newRoute = edge.getLine();
                Point startPoint = newRoute.getStartPoint();
                Point endPoint = newRoute.getEndPoint();
                if (startPoint.equals(this.location.geometry)) {
                    this.setNewRoute(newRoute, true);
                } else if (endPoint.equals(this.location.geometry)) {
                    this.setNewRoute(newRoute, false);
                } else {
                    System.err.println("Where am I?");
                }
            }
        }

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

        otherTrainDetected(geoTest);
        if (!this.arrived()) {
            this.moveAlongPath();
        } else {
            if(itSchedule.hasNext() && attente == -1) {
                attente = 100;
            } else if (itSchedule.hasNext() && attente > 0) {
                attente--;
            } else if(itSchedule.hasNext() && attente == 0) {
                attente --;
                nextStation = ((Schedule) itSchedule.next()).station.name;
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
        //System.out.println("Current : " + this.currentIndex + " Start : " + this.startIndex + " End : " + this.endIndex);

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

    private boolean otherTrainDetected (RatpNetwork geo){
        GeomVectorField geoLine = geo.getLine(this.nameLine).getRight();
        Bag nearestObject = geoLine.getObjectsWithinDistance(this.location.getGeometry(), 0.0009D);
        if (!nearestObject.isEmpty()){
            Iterator i = nearestObject.iterator();
            while(i.hasNext()){
                MasonGeometry element = (MasonGeometry) i.next();
                if(element.getStringAttribute("type").equals("rame")){
                    //System.out.println(element.getGeometry().toString());
                    //System.out.println("End");
                    System.out.println("hello");
                    if(!this.location.getGeometry().equals(element.getGeometry())) {
                        //System.out.println("Begin");
                        //System.out.println("Us" + this.location.getGeometry().toString());
                        //System.out.println("End");
                        //System.out.println(element.getGeometry().toString());
                        this.moveRate = 0;
                        this.basemoveRate = 0;
                    }
                }
            }
        }
        return true;
    }



}
