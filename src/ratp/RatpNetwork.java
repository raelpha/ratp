package ratp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.planargraph.Node;
import fakeag.Agent;
import sim.engine.SimState;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileExporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.Bag;
import sim.util.geo.GeomPlanarGraph;
import sim.util.geo.MasonGeometry;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

public class RatpNetwork extends SimState {
    int WIDTH = 600;
    int HEIGHT = 600;
    public GeomVectorField vectorField = new GeomVectorField(WIDTH, HEIGHT);
    public GeomPlanarGraph network = new GeomPlanarGraph();

    URI absolute_shp = new File(new File(
            "data/ratp_rotated/ratp_pivotated.shp").getCanonicalPath()).toURI();
    URI absolute_db = new File(new File(
            "data/ratp_rotated/ratp_pivotated.dbf").getCanonicalPath()).toURI();

    public GeomVectorField junctions = new GeomVectorField(600, 600);
    //public GeomVectorField walkways = new GeomVectorField(600, 600);
    public RatpNetwork(long seed) throws IOException {
        super(seed);
        Bag attributes = new Bag();
        attributes.add("line");
        attributes.add("stroke"); // TODO: Rename stroke to color
        attributes.add("sectionId");
        attributes.add("origin");
        attributes.add("destination");

        try {
            ShapeFileImporter.read(absolute_shp.toString(), absolute_db.toString(), vectorField, attributes);
            this.network.createFromGeomField(this.vectorField);
            //ShapeFileImporter.read(absolute_shp.toString(), absolute_db.toString(), this.walkways);
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public int numAgents = 500;

    public GeomVectorField agents = new GeomVectorField(600, 600);

    public int getNumAgents() {
        return this.numAgents;
    }

    public void setNumAgents(int n) {
        if (n > 0) {
            this.numAgents = n;
        }

    }

    void addAgents() {
        for(int i = 0; i < this.numAgents; ++i) {
            Agent a = new Agent(this);
            this.agents.addGeometry(a.getGeometry());
            this.schedule.scheduleRepeating(a);
        }

    }

    public void finish() {
        super.finish();
        ShapeFileExporter.write("agents", this.agents);
    }

    public void start() {
        super.start();
        this.agents.clear();
        this.addAgents();
        //this.agents.setMBR(this.buildings.getMBR());
        this.schedule.scheduleRepeating(this.agents.scheduleSpatialIndexUpdater(), 2147483647, 1.0D);
    }


    private void addIntersectionNodes(Iterator nodeIterator, GeomVectorField intersections) {
        GeometryFactory fact = new GeometryFactory();
        Coordinate coord = null;
        Point point = null;

        for(int var6 = 0; nodeIterator.hasNext(); ++var6) {
            Node node = (Node)nodeIterator.next();
            coord = node.getCoordinate();
            point = fact.createPoint(coord);
            this.junctions.addGeometry(new MasonGeometry(point));
        }

    }



}
