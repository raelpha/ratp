package station;

import java.util.HashMap;
import java.util.Map;
import ratp.RatpMain;
import ratp.RatpNetwork;
import sim.engine.SimState;
import sim.engine.Steppable;

public class SuperStation implements Steppable{
    public int id;
    public String name;

    /*private List<AgentVoyageur> listMct;
    private List<QuaiAgent> listQuai;

    public List<AgentVoyageur> getListMct() {
        return listMct;
    }

    public void setListMct(List<AgentVoyageur> listMct) {
        this.listMct = listMct;
    }

    public List<QuaiAgent> getListQuai() {
        return listQuai;
    }

    public void setListQuai(List<QuaiAgent> listQuai) {
        this.listQuai = listQuai;
    }*/

    public SuperStation(int id, String name) {
        this.id = id;
        this.name = name;
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
