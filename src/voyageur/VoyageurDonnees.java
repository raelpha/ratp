package voyageur;

<<<<<<< Updated upstream
import station.Station;
=======
import com.vividsolutions.jts.planargraph.Node;
>>>>>>> Stashed changes

import java.util.List;
import java.util.Queue;

public class VoyageurDonnees
{
    public int colere;
<<<<<<< Updated upstream
    public Station destination;
    public Station stationCourante;
    public Queue<Station> cheminEnvisage;

    public VoyageurDonnees(int colere, Station destination, Station stationCourante, Queue<Station> cheminEnvisage)
=======
    public Node destination;
    public Node stationCourante;
    public List<Node> cheminEnvisage;

    public VoyageurDonnees(int colere, Node destination, Node stationCourante, List<Node> cheminEnvisage)
>>>>>>> Stashed changes
    {
        this.colere = colere;
        this.destination = destination;
        this.stationCourante = stationCourante;
        this.cheminEnvisage = cheminEnvisage;
    }
<<<<<<< Updated upstream
}
=======
}
>>>>>>> Stashed changes
