package station;

import global.Constants;
import ratp.RatpNetwork;
import ratp.directory.StationsDirectory;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.MasonGeometry;
import voyageur.AgentVoyageur;

import java.util.*;

/**
 * A gare is made of one or more Station.
 * This class is also used to display associated MasonGeometry with a portrayal
 */
public class Gare extends MasonGeometry implements Steppable {
    public int id;
    public String name;
    public Map<String, Station> stations = new HashMap<>();
    public List<AgentVoyageur> listMct = new ArrayList<AgentVoyageur>();
    public List<AgentVoyageur> listVoyageurGare;

    private final Boolean test=false;
    private Boolean fermee=false;
    private int nbVoyageurs;

    /**
     * Main constructor, used when instantiating new gare (without associated MasonGeometry)
     * @param id debug value - can be null
     */
    public Gare(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * This constructor is used to create a Gare from an existing geometry
     * It is especially used when you want to create a Gare from many Stations
     * @param gem geometry used for the simulation
     */
    public Gare(MasonGeometry gem) {
        this.addAttribute(Constants.IS_MULTIPLE_STATION_STR, gem.getStringAttribute(Constants.IS_MULTIPLE_STATION_STR));
        this.addAttribute(Constants.STATION_NAME_STR, gem.getStringAttribute(Constants.STATION_NAME_STR));
        this.geometry = gem.getGeometry();
        this.name = this.getStringAttribute(Constants.STATION_NAME_STR);
    }

    public List<AgentVoyageur> getListGare(){
        this.listVoyageurGare =new ArrayList<AgentVoyageur>();
        List<List<AgentVoyageur>> listListTot =new ArrayList<List<AgentVoyageur>>();
        for (Map.Entry<String, Station> entry : StationsDirectory.getInstance().gares.get(this.name).stations.entrySet()) {
            listListTot.add(StationsDirectory.getInstance().getStation(entry.getKey(),this.name).getListAttenteRame());
        }
        for (List<AgentVoyageur> l: listListTot) {
            listVoyageurGare.addAll(l);
        }
        return listVoyageurGare;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public Station getStation(String lineId){
        return stations.get(lineId);
    }

    public List<AgentVoyageur> getListMct() {
        return listMct;
    }


    public Boolean isFermee() {
        return fermee;
    }

    public void setFermee() {
        this.fermee = true;
        //On ferme toutes les stations dans la Gare
        for (Map.Entry<String, Station> entry : StationsDirectory.getInstance().gares.get(this.name).stations.entrySet()) {
            StationsDirectory.getInstance().getStation(entry.getKey(),this.name).setFermee();
        }
    }

    public void setOuvert(){
        this.fermee=false;
        for (Map.Entry<String, Station> entry : StationsDirectory.getInstance().gares.get(this.name).stations.entrySet()) {
            StationsDirectory.getInstance().getStation(entry.getKey(),this.name).setOuvert();
        }
    }

    public int getNbVoyageurs(){
        return nbVoyageurs;
    }

    public void setNbVoyageurs() {
        int nbVoya=0;
        for (Map.Entry<String, Station> entry : StationsDirectory.getInstance().gares.get(this.name).stations.entrySet()) {
            nbVoya+=StationsDirectory.getInstance().getStation(entry.getKey(),this.name).getNbVoyageurs();
        }
        this.nbVoyageurs=nbVoya;
    }



    @Override
    public void step(SimState simState) {
        RatpNetwork ratpNetwork = (RatpNetwork) simState;
        setNbVoyageurs();
    }

}
