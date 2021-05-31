package station;

import com.vividsolutions.jts.planargraph.Node;

import java.util.List;
import java.util.Queue;

public class VoyageurDonnees
{
    public int colere;
    public Node destination;
    public Node stationCourante;
    public List<Node> cheminEnvisage;

    public VoyageurDonnees(int colere, Node destination, Node stationCourante, List<Node> cheminEnvisage)
    {
        this.colere = colere;
        this.destination = destination;
        this.stationCourante = stationCourante;
        this.cheminEnvisage = cheminEnvisage;
    }
}