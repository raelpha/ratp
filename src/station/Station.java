package station;

import com.vividsolutions.jts.geom.Point;
import lines.Line;
import rame.Rame;
import ratp.directory.StationsDirectory;
import sim.app.geo.masoncsc.util.Pair;
import sim.app.virus.Agent;
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
import voyageur.VoyageurDonnees;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
    private List<Rame> rameSurPlace = new ArrayList<>();
    public Boolean test=false;
    public Boolean tets2=false;
    private List<AgentVoyageur> voyageurDescendu=new ArrayList<>();
    public int attente=0;
    private Boolean testClear=false;
    //Deprecated
    /*
    public Station(String lineId, String name) {
        this.lineNumber = lineId;
        this.name = name;
    }
    */

    public String getName() {return name;}

    public void addRame(Rame r){rameSurPlace.add(r);}

    public void removeRame(Rame r) {rameSurPlace.remove(r);}

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
        nbVoyageurs=getListAttenteRame().size();
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
        if(!testClear){
            //System.out.println("je clear");
            this.rameSurPlace.clear();
            //System.out.println("listeAttenteRame : "+this.rameSurPlace);
            this.getListAttenteRame().clear();
            testClear=true;
        }
        this.descenteRame();

        if (test) {
            //System.out.println("yo");
            //System.out.println(this.name);
            if (!spawn) {
                //int nbVoyageurSpawn = (int)doubleNormale(7,5);
                for (int i = 0; i < 1; i++) {
                    AgentVoyageur temp = ratpNetwork.addVoyageur(this);
                    if(!this.getListAttenteRame().contains(temp)) {
                        this.getListAttenteRame().add(temp);
                    }
                }
                AgentVoyageur temp = ratpNetwork.addVoyageur(this);
                temp.destination=StationsDirectory.getInstance().getStation("1","George V");
                if(!this.getListAttenteRame().contains(temp)) {
                    this.getListAttenteRame().add(temp);
                }
                //System.out.println("nv : "+this.getNbVoyageurs()+ " get : "+this.getListAttenteRame());
                this.spawn=true;
            }
            //System.out.println(this.getListAttenteRame());
            //addToMctList(this.name,listAttenteRame.remove(0));
            //System.out.println(StationsDirectory.getInstance().gares.get(this.name).queueMct);
        }

            /*if (!this.voyageurDescendu.isEmpty()) {
                for (AgentVoyageur a : this.voyageurDescendu) {
                    if (this.tets2 == false) {
                        //System.out.println(a.stationCourante.name);
                        //System.out.println(a.cheminEnvisage.peek().getLeft().name);
                        if (a.stationCourante.name.equals(a.cheminEnvisage.peek().getLeft().name)) {
                            this.addToMctList(a.stationCourante.name, a);
                            Gare g = StationsDirectory.getInstance().gares.get(a.stationCourante.name);
                            a.cheminEnvisage.poll();
                            String lineNumber = a.cheminEnvisage.peek().getLeft().lineNumber;
                            System.out.println("lineNumber : " + lineNumber);
                            String stationName = a.cheminEnvisage.peek().getLeft().name;
                            Station s = StationsDirectory.getInstance().getStation(lineNumber, stationName);
                            System.out.println("avant :" + s.getListAttenteRame());
                            s.getListAttenteRame().add(g.getQueueMct().take());

                            System.out.println("apres: " + s.getListAttenteRame());
                            //System.out.println("tiens :"+StationsDirectory.getInstance().gares.get("Nation"));
                            //Gare g = StationsDirectory.getInstance().gares.get("Nation");
                            //System.out.println(g.name);
                        }

                        //this.tets2=true;
                    }
                }
                //Station s1 = StationsDirectory.getInstance().getStation("1", "Nation");
                //System.out.println("après :"+s1.getListAttenteRame());
            }*/

        if (StationsDirectory.getInstance().getStation("1", "Nation").test == false) {
            StationsDirectory.getInstance().getStation("1", "Nation").test = true;
            //System.out.println("get toutes les lignes d'une station");
            //System.out.println(StationsDirectory.getInstance().gares.get("Nation").stations.entrySet());
            //System.out.println(" nb : " +StationsDirectory.getInstance().getStation("1", "Nation").getNbVoyageurs());
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


    public void descenteRame(){
        if (!this.rameSurPlace.isEmpty()) {
            System.out.println("rameSurPlace : " + this.rameSurPlace + " station : "+this.name);
            for (int i = 0; i < this.rameSurPlace.size(); i++) {
                Rame rame = this.rameSurPlace.remove(0);
                if (!this.getRemoveUserRame(rame).isEmpty()) {
                    for(AgentVoyageur aV : this.getRemoveForceUserRame(rame)){
                        if(aV.cheminEnvisage.isEmpty()){
                            System.out.println("Je suis arrivé : "+aV);
                        }else{
                            this.addToMctList(this.name,aV);
                            //getRemoveForceUserRame(rame).remove(aV);
                            System.out.println("Liste mct : "+this.getGare(this.name).getListMct());
                        }
                    }
                }
                if(!this.getRemoveForceUserRame(rame).isEmpty()){
                    System.out.println("cc");
                    //TODO recalcul a*
                }
                /*
                for (AgentVoyageur aV : getListAttenteRame()) {
                    if (rame.freePlaces() > 0) {
                        System.out.println("chemin Destination voyageur : " + aV.cheminEnvisage.peek().getRight().get(0).name);
                        System.out.println("chemin destination rame : " + rame.getTerminus().station.name);
                        if (aV.cheminEnvisage.peek().getRight().get(0).name.equals(rame.getTerminus().station.name)) {
                            System.out.println("salut");
                        }
                    }
                }*/

            }
        }
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
            //rame.addUser(a);
        }

    }

    /*public void checkDestinationVoyageur(List<AgentVoyageur> listAgent,List<Rame> listRame){
        for(AgentVoyageur a : listAgent){
            for(Rame r : listRame){
                if(r.)
            }
        }
    }*/

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
        StationsDirectory.getInstance().gares.get(name).listMct.add(a);
    }

    public int demanderNbPlaceRame(Rame rame) {
        return rame.freePlaces();
    }

    public List<AgentVoyageur> getRemoveUserRame(Rame rame){
        return rame.forceRemoveUser();
    }

    public List<AgentVoyageur> getRemoveForceUserRame(Rame rame){
        return rame.forceRemoveUser();
    }

    public Gare getGare(String gareName){
        return StationsDirectory.getInstance().gares.get(gareName);
    }

    /*
    public int demanderNbVoyageurDescendant(Rame rame){
        return nbVoyageursDescandant;
    }*/





    public static void main(String[] args){

    }

}
