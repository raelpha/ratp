package station;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

import ratp.RatpMain;
import ratp.RatpNetwork;
import ratp.directory.StationsDirectory;
import sim.app.virus.Agent;
import sim.engine.SimState;
import sim.engine.Steppable;
import voyageur.AgentVoyageur;

public class SuperStation implements Steppable{
    public int id;
    public String name;

    public DelayQueue<AgentVoyageur> queueMct=new DelayQueue<AgentVoyageur>();
    //private List<QuaiAgent> listQuai;



    /*
    public List<QuaiAgent> getListQuai() {
        return listQuai;
    }

    public void setListQuai(List<QuaiAgent> listQuai) {
        this.listQuai = listQuai;
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
