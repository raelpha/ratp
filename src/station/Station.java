package station;

import station.*;
import station.AgentVoyageur;
import ratp.RatpNetwork;
import ratp.RatpStateWithUI;
import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.MasonGeometry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Station implements Steppable {
    public String line;
    public String name;
    public Boolean terminus = false;
    public Color color = new Color(255,255,255);
    private List<AgentVoyageur> listAttenteRame ;
    private int colereStation;
    private int nbVoyageurs;
    private Boolean fermee=false;

    public List<AgentVoyageur> getListAttenteRame() {
        return listAttenteRame;
    }

    public void setListAttenteRame(List<AgentVoyageur> listAttenteRame) {
        this.listAttenteRame = listAttenteRame;
    }

    public int getColereStation() {
        return colereStation;
    }

    public void setColereStation(int colereStation) {
        this.colereStation = colereStation;
    }

    public int getNbVoyageurs() {
        return nbVoyageurs;
    }

    public void setNbVoyageurs(int nbVoyageurs) {
        this.nbVoyageurs = nbVoyageurs;
    }

    public Boolean isFermee() {
        return fermee;
    }

    public void setFermee() {
        this.fermee = true;
    }

    public Station(String line, String name) {
        this.line = line;
        this.name = name;
    }

    public Station(String line, String name,int nbVoyageurs, List<AgentVoyageur> listAttenteRame) {
        this.line = line;
        this.name = name;
        this.nbVoyageurs=nbVoyageurs;
        listAttenteRame=new ArrayList<AgentVoyageur>();
        this.listAttenteRame=listAttenteRame;
    }

    MasonGeometry mg = new MasonGeometry();

    @Override
    public void step(SimState simState) {
        RatpNetwork ratpNetwork =(RatpNetwork) simState;
    }

    public static double fonctionNormale(double d) {
        return Math.exp(-Math.pow(d,2));
    }
    public static double doubleNormale(double x) {
        return fonctionNormale((x-7.5)/3)*15 + fonctionNormale((x-18)/4)*10;
    }

    public List<AgentVoyageur> createVoyageurs(double horaire){
        //TODO something
        listAttenteRame=new ArrayList<AgentVoyageur>();
        int nb=(int)doubleNormale(horaire);
        //System.out.println(nb);
        this.setNbVoyageurs(nb);
        for(int i=0;i<getNbVoyageurs();i++){
            AgentVoyageur nv= new AgentVoyageur(this);
            listAttenteRame.add(nv);
        }
        return listAttenteRame;
    }

    public void colereStationTot(List<AgentVoyageur> listVoyageur){
        int colereTot=0;
        for(AgentVoyageur a : listVoyageur){
            colereTot+=a.colere;
        }
        setColereStation(colereTot);
    }

    /*
    public int demanderNbPlaceRame(Rame rame){
        return nbPlaceRestantes;
    }

    public int demanderNbVoyageurDescendant(Rame rame){
        return nbVoyageursDescandant;
    }
    */

    /*public String toString() {
        return String.valueOf(getNomQuai());
    }*/



    public static void main(String[] args){
        Station station=new Station("4","Châtelet",0, null);
        station.createVoyageurs(7.5);
        System.out.println(station.getColereStation());
    }


}
