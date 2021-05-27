package fakeag;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.planargraph.DirectedEdgeStar;
import com.vividsolutions.jts.planargraph.Node;
import ratp.RatpNetwork;
import sim.app.geo.campusworld.CampusWorld;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.io.geo.ShapeFileImporter;
import sim.util.geo.GeomPlanarGraphDirectedEdge;
import sim.util.geo.GeomPlanarGraphEdge;
import sim.util.geo.MasonGeometry;
import sim.util.geo.PointMoveTo;

public class Agent implements Steppable {
    private static final long serialVersionUID = -1113018274619047013L;
    private MasonGeometry location;
    private double basemoveRate = 500000.0D;
    private double moveRate;
    private LengthIndexedLine segment;
    double startIndex;
    double endIndex;
    double currentIndex;
    PointMoveTo pointMoveTo;
    private static GeometryFactory fact = new GeometryFactory();

    public Agent(RatpNetwork state) {
        this.moveRate = this.basemoveRate;
        this.segment = null;
        this.startIndex = 0.0D;
        this.endIndex = 0.0D;
        this.currentIndex = 0.0D;
        this.pointMoveTo = new PointMoveTo();
        this.location = new MasonGeometry(fact.createPoint(new Coordinate(50.0D, 50.0D)));
        this.location.isMovable = true;
        int walkway = state.random.nextInt(state.vectorField.getGeometries().numObjs);
        MasonGeometry mg = (MasonGeometry)state.vectorField.getGeometries().objs[walkway];
        try {
            this.setNewRoute((LineString)mg.getGeometry(), true);
        } catch (Exception e) {
            System.out.println(e);
        }
        int age;
        if (state.random.nextBoolean()) {
            this.location.addStringAttribute("TYPE", "STUDENT");
            age = (int)(20.0D + 2.0D * state.random.nextGaussian());
            this.location.addIntegerAttribute("AGE", age);
        } else {
            this.location.addStringAttribute("TYPE", "FACULTY");
            age = (int)(40.0D + 9.0D * state.random.nextGaussian());
            this.location.addIntegerAttribute("AGE", age);
        }

        this.basemoveRate *= Math.abs(state.random.nextGaussian());
        this.location.addDoubleAttribute("MOVE RATE", this.basemoveRate);
    }

    public MasonGeometry getGeometry() {
        return this.location;
    }

    private boolean arrived() {
        return this.moveRate > 0.0D && this.currentIndex >= this.endIndex || this.moveRate < 0.0D && this.currentIndex <= this.startIndex;
    }

    public String getType() {
        return this.location.getStringAttribute("TYPE");
    }

    private void findNewPath(RatpNetwork geoTest) {
        Node currentJunction = geoTest.network.findNode(this.location.getGeometry().getCoordinate());
        if (currentJunction != null) {
            DirectedEdgeStar directedEdgeStar = currentJunction.getOutEdges();
            Object[] edges = directedEdgeStar.getEdges().toArray();
            if (edges.length > 0) {
                int i = geoTest.random.nextInt(edges.length);
                GeomPlanarGraphDirectedEdge directedEdge = (GeomPlanarGraphDirectedEdge)edges[i];
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

    public void step(SimState state) {
        RatpNetwork campState = (RatpNetwork)state;
        this.move(campState);
        System.out.println(this.location);
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
}
