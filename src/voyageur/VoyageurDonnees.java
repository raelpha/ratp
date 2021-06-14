package voyageur;

import sim.app.geo.masoncsc.util.Pair;
import station.Station;


import java.util.List;
import java.util.Queue;

public class VoyageurDonnees {
    public int colere;
    public Station destination;
    public Station stationCourante;
    Queue<Pair<Station, List<Station>>> cheminEnvisage;

    public VoyageurDonnees(int colere, Station destination, Station stationCourante, Queue<Pair<Station, List<Station>>> cheminEnvisage)
    {
        this.colere = colere;
        this.destination = destination;
        this.stationCourante = stationCourante;
        this.cheminEnvisage = cheminEnvisage;
    }

}
