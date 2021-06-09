package station;

import com.vividsolutions.jts.geom.Point;
import lines.Line;
import rame.Rame;
import ratp.directory.StationsDirectory;
import sim.util.geo.MasonGeometry;

import java.awt.*;
import voyageur.AgentVoyageur;
import ratp.RatpNetwork;
import ratp.RatpStateWithUI;
import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.MasonGeometry;
import global.Constants;
import java.util.ArrayList;
import java.util.List;

public class Station implements Steppable{
    public Line line;
    public String lineNumber="XXX";
    public String name="XXX";
    public Boolean terminus = false;
    public Color color = new Color(255,255,255);
    public Color legacyColor = new Color(255,255,255);
    public Point location;
    public Boolean spawn=false;
    private List<AgentVoyageur> listAttenteRame ;
    private int colereStation;
    private int nbVoyageurs;
    private Boolean fermee=false;
    public Rame rameSurPlace;
    //Deprecated
    /*
    public Station(String lineId, String name) {
        this.lineNumber = lineId;
        this.name = name;
    }
    */

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


    /*public Station(Line line, String name) {
        this.line = line;
        this.lineNumber = line.number;
        this.name = name;
        this.color = line.color;
        this.legacyColor = line.color;
    }*/

    public Station(Line line, String name) {
        this.line = line;
        this.lineNumber = line.number;
        this.name = name;
        this.color = line.color;
        this.legacyColor = line.color;
        this.colereStation=0;
        this.nbVoyageurs=nbVoyageurs;
        /*List<AgentVoyageur> _listAttenteRame=new ArrayList<AgentVoyageur>();
        for(int i=0;i<nbVoyageurs;i++){
            //AgentVoyageur nv= new AgentVoyageur(this);
            //_listAttenteRame.add(nv);
        }*/
        this.listAttenteRame=new ArrayList<AgentVoyageur>();
    }

    MasonGeometry mg = new MasonGeometry();

    @Override
    public void step(SimState simState) {
        RatpNetwork ratpNetwork =(RatpNetwork) simState;
        //System.out.println("hello");
        if(!spawn){
            System.out.println("hey");
            int nbVoyageurSpawn = (int)doubleNormale(7,5);
            for(int i=0;i<1;i++){
                ratpNetwork.addVoyageur(this);
            }
            this.spawn=true;
        }
    }

    public static double fonctionNormale(double d) {
        return Math.exp(-Math.pow(d,2));
    }
    public static double doubleNormale(int heure, int minutes) {
        int minuteHeure = heure*60;
        int minuteTot=minuteHeure + minutes;
        double x = minuteTot/60;
        return fonctionNormale((x-7.5)/3)*Constants.NB_PIC_MATIN + fonctionNormale((x-18)/4)*Constants.NB_PIC_SOIR;
    }

    /*public List<AgentVoyageur> createVoyageurs(double horaire){
        listAttenteRame=new ArrayList<AgentVoyageur>();
        //System.out.println(nb);
        this.setNbVoyageurs(nb);
        for(int i=0;i<getNbVoyageurs();i++){
            //AgentVoyageur nv= new AgentVoyageur(this);
            //listAttenteRame.add(nv);
        }
        return listAttenteRame;
    }*/

    public void colereStationTot(List<AgentVoyageur> listVoyageur){
        int colereTot=0;
        for(AgentVoyageur a : listVoyageur){
            colereTot+=a.colere;
        }
        setColereStation(colereTot);
    }

    public void chargerRame(Rame rame){
        int nbPlace = demanderNbPlaceRame(rame);
        for(int i =0;i<nbPlace;i++){
            AgentVoyageur a = getListAttenteRame().remove(0);
            rame.addUser(a);
        }

    }

    /*public void dechargerRame(Rame rame,String name){
        List<AgentVoyageur> listDescedants = new ArrayList<AgentVoyageur>();
        listDescedants=rame.removeUser(this.name);
        for(AgentVoyageur a: listDescedants){
            //remove la station de la liste de station du voyageur
            //Si la liste est vide , ça signifie que c'est son terminus donc on le remove
            if(a.chemi.isEmpty()){ // terminus
                //retirer l'agent
            }
            else{ //sinon on le change de quai
                addToMctList(name,a);
            }
        }
    }*/

    public void addToMctList(String name,AgentVoyageur a){
        StationsDirectory.getInstance().gares.get(name).queueMct.add(a);
    }

    public int demanderNbPlaceRame(Rame rame) {
        return rame.freePlaces();
    }

    /*
    public int demanderNbVoyageurDescendant(Rame rame){
        return nbVoyageursDescandant;
    }*/





    public static void main(String[] args){



    }

}
