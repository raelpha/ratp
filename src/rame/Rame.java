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
import sim.util.geo.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Rame implements Steppable {

    private MasonGeometry location;
    private double basemoveRate = 0.0000001D;
    private double moveRate;
    private LengthIndexedLine segment;
    private LineString line;
    double startIndex;
    double endIndex;
    double currentIndex;
    PointMoveTo pointMoveTo;
    private static GeometryFactory fact = new GeometryFactory();
    private MasonGeometry mg;
    private int maxUser = Constants.MAX_USER_RAME;
    private List<Object> users = new ArrayList<Object>();

    public Rame(RatpNetwork state, String nameLine, Object ... params) {
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
        mg = getLine(state, nameLine);
        this.setNewRoute((LineString)mg.getGeometry(), true);
        this.location.addDoubleAttribute("MOVE RATE", this.basemoveRate);
        if(params.length > 0) {
            maxUser = (int) params[0];
        }
    }

    MasonGeometry getLine(RatpNetwork s,String name){
        return (MasonGeometry) s.getLine(name).getRight().getGeometries().objs[0];
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

    private void findNewPath(RatpNetwork geoTest) {
        GeomPlanarGraph network = new GeomPlanarGraph();
        network.createFromGeomField(geoTest.getLine("1").getRight());
        Node currentJunction = network.findNode(this.location.getGeometry().getCoordinate());
        if (currentJunction != null) {
            DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();
            if (edges.length > 0) {
                GeomPlanarGraphDirectedEdge directedEdge;
                if(edges.length>1) {
                    int i = 0;
                    while (((GeomPlanarGraphEdge)((GeomPlanarGraphDirectedEdge)edges[i]).getEdge()).getLine().equals(line)){
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
        this.line = line;
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
        if (!this.arrived()) {
            this.moveAlongPath();
        } else {
            this.findNewPath(geoTest);
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

    public List<Object> removeUser (String statioName) {
        List<Object> returnList = new ArrayList<Object>();
        ListIterator<Object> it = users.listIterator();
        while(it.hasNext()){
            //precédure de détection et d'ajout
        }
        return returnList;
    }



}
