package station;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ratp.RatpMain;
import ratp.RatpNetwork;
import sim.app.virus.Agent;
import sim.engine.SimState;
import sim.engine.Steppable;
import voyageur.AgentVoyageur;

public class SuperStation implements Steppable{
    public int id;
    public String name;

    public List<AgentVoyageur> listMct;
    //private List<QuaiAgent> listQuai;

    public List<AgentVoyageur> getListMct() {
        return listMct;
    }

    public void setListMct(List<AgentVoyageur> listMct) {
        this.listMct = listMct;
    }



    /*
    public List<QuaiAgent> getListQuai() {
        return listQuai;
    }

    public void setListQuai(List<QuaiAgent> listQuai) {
        this.listQuai = listQuai;
    }*/


    public SuperStation(int id, String name) {
        this.id = id;
        this.name = name;
        this.listMct=new ArrayList<AgentVoyageur>();
    }


    public Map<String, Station> stations = new HashMap<>();


    public Station getStation(String lineId){
        return stations.get(lineId);
    }

    @Override
    public void step(SimState simState) {
        RatpNetwork ratpNetwork = (RatpNetwork) simState;
    }

}
