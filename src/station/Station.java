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
    private double colereStation;
    private int nbVoyageurs;
    private Boolean fermee=false;
    private List<Rame> rameSurPlace = new ArrayList<>();
    private Boolean test=false;
    private Boolean tets2=false;
    private List<AgentVoyageur> voyageurDescendu=new ArrayList<>();
    private int attente=0;
    private Boolean testClear=false;
    private Boolean sizeListTest=false;

    public String getName() {return name;}

    public void addRame(Rame r){rameSurPlace.add(r);}

    public void removeRame(Rame r) {rameSurPlace.remove(r);}

    public List<AgentVoyageur> getListAttenteRame() {
        return listAttenteRame;
    }

    public double getColereStation() {
        return colereStation;
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
        StationsDirectory.getInstance().fermerStation(this);
        /*for(AgentVoyageur a : getListAttenteRame()){
            a.FermetureStation(this);
        }*/
    }

    public void setOuvert(){
        this.fermee=false;
        StationsDirectory.getInstance().ouvrirStation(this);
    }

    public void setColereStation(){
        if (this.getListAttenteRame().size() == 0) {
            colereStation = 0;
        } else {
            double nb = 0;
            for (AgentVoyageur aV : this.getListAttenteRame()) {
                nb += aV.colere;
            }
            nb = nb / 100;
            colereStation = nb / getListAttenteRame().size();
        }
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
        this.listAttenteRame=new ArrayList<AgentVoyageur>();
    }

    MasonGeometry mg = new MasonGeometry();

    @Override
    public void step(SimState simState) {
        RatpNetwork ratpNetwork =(RatpNetwork) simState;
        clearAllStation();
        spawnVoyageur(ratpNetwork);
        arriveeRame(ratpNetwork);
        setColereStation();
        //this.descenteRame();

        /*if (test) {
            if (!spawn) {
                //int nbVoyageurSpawn = (int)doubleNormale(7,5);
                for (int i = 0; i < 2; i++) {
                    AgentVoyageur temp = ratpNetwork.addVoyageur(this);
                }
                for (int i = 0; i < 2; i++) {
                    AgentVoyageur temp1 = ratpNetwork.addVoyageur(this);
                    temp1.destination=StationsDirectory.getInstance().getStation("13","La Fourche");
                    temp1.cheminEnvisage=temp1.trouverChemin(this, temp1.destination);
                }
                for (int i = 0; i < 2; i++) {
                    AgentVoyageur temp2 = ratpNetwork.addVoyageur(this);
                    temp2.destination=StationsDirectory.getInstance().getStation("13","Les Agnettes");
                    temp2.cheminEnvisage=temp2.trouverChemin(this, temp2.destination);
                }
                for (int i = 0; i < 2; i++) {
                    AgentVoyageur temp2 = ratpNetwork.addVoyageur(this);
                    temp2.destination=StationsDirectory.getInstance().getStation("13","Liège");
                    temp2.cheminEnvisage=temp2.trouverChemin(this, temp2.destination);
                }

                this.spawn=true;
            }
            //System.out.println(this.getListAttenteRame());
            //addToMctList(this.name,listAttenteRame.remove(0));
            //System.out.println(StationsDirectory.getInstance().gares.get(this.name).queueMct);
        }*/

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

        /*if (StationsDirectory.getInstance().getStation("13", "Place de Clichy").test == false) {
            StationsDirectory.getInstance().getStation("13", "Place de Clichy").test = true;
            //System.out.println("get toutes les lignes d'une station");
            //System.out.println(StationsDirectory.getInstance().gares.get("Nation").stations.entrySet());
            //System.out.println(" nb : " +StationsDirectory.getInstance().getStation("1", "Nation").getNbVoyageurs());
        }*/

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

//TODO descenteRame a debug
    public void descenteRame(RatpNetwork ratpNetwork){
        if (!this.rameSurPlace.isEmpty() && !this.isFermee()) {
            //System.out.println("rameSurPlace : " + this.rameSurPlace + " station : "+this.name);
            for (int i = 0; i < this.rameSurPlace.size(); i++) {
                Rame rame = this.rameSurPlace.get(0);
                List<AgentVoyageur> removeUserDeLaRame=new ArrayList<AgentVoyageur>();
                removeUserDeLaRame= rame.removeUser();
                if (!removeUserDeLaRame.isEmpty()) {
                    //System.out.println("debug : "+removeUserDeLaRame);
                    for(AgentVoyageur aV : removeUserDeLaRame){
                        //System.out.println(aV.cheminEnvisage.peek().getLeft());
                        //System.out.println(aV.cheminEnvisage.peek().getLeft().name);
                        if(aV.cheminEnvisage.isEmpty()){
                            System.out.println("Je suis arrivé : "+aV);
                        }else{
                            //System.out.println(aV.cheminEnvisage.peek().getLeft().name);
                            /*if (aV.stationCourante.name.equals(aV.cheminEnvisage.peek().getLeft().name)) {
                                //this.addToMctList(a.stationCourante.name, a);
                                Gare g = StationsDirectory.getInstance().gares.get(aV.stationCourante.name);
                                //a.cheminEnvisage.poll();
                                String lineNumber = aV.cheminEnvisage.peek().getLeft().lineNumber;
                                System.out.println("lineNumber : " + lineNumber);
                                String stationName = aV.cheminEnvisage.peek().getLeft().name;
                                Station s = StationsDirectory.getInstance().getStation(lineNumber, stationName);
                                s.getListAttenteRame().add(aV);
                            }*/
                            //a.cheminEnvisage.poll();
                            String lineNumber = aV.cheminEnvisage.peek().getLeft().lineNumber;
                            System.out.println("lineNumber : " + lineNumber);
                            String stationName = aV.cheminEnvisage.peek().getLeft().name;
                            Station s = StationsDirectory.getInstance().getStation(lineNumber, stationName);
                            s.getListAttenteRame().add(aV);

                            //getRemoveForceUserRame(rame).remove(aV);

                            aV.SortirDeRame(ratpNetwork.yard,this);
                            //System.out.println("Liste mct : "+this.getGare(this.name).getListMct());
                        }
                    }
                }
                List<AgentVoyageur> forceRemoveUserDeLaRame=new ArrayList<AgentVoyageur>();
                forceRemoveUserDeLaRame= rame.forceRemoveUser();
                if(!forceRemoveUserDeLaRame.isEmpty()){
                    for(AgentVoyageur aV: forceRemoveUserDeLaRame){
                        aV.cheminEnvisage= aV.trouverChemin(aV.stationCourante,aV.destination);
                        this.getListAttenteRame().add(aV);
                        aV.SortirDeRame(ratpNetwork.yard,this);
                    }
                }

            }
        }
    }

    public void arriveeRame(RatpNetwork ratpNetwork){
        //descenteRame(ratpNetwork);
        monteeRame(ratpNetwork);
    }

    public void monteeRame(RatpNetwork ratpNetwork){
        if (!this.rameSurPlace.isEmpty() && !this.isFermee()) {
            for (int i = 0; i < this.rameSurPlace.size(); i++) {
                Rame rame = this.rameSurPlace.get(i);
                int sizeList;
                if(getListAttenteRame().size()<nbPlaceRame(rame)){
                    sizeList=getListAttenteRame().size();
                    sizeListTest=true;
                }else{
                    sizeList=nbPlaceRame(rame);
                }
                for(int j =0 ;j<sizeList;j++){
                    //System.out.println("sizeList : "+sizeList);
                    //System.out.println(this.getListAttenteRame());
                    //System.out.println("j : "+j);
                    if(checkDestinationVoyageurRame(getListAttenteRame().get(j),rame)){
                        AgentVoyageur a = getListAttenteRame().get(j);
                        rame.addUser(a);
                        //System.out.println("Je monte dans la rame");
                        a.enTrain=true;
                        ratpNetwork.removeVoyageur(a);
                        this.getListAttenteRame().remove(a);
                        //System.out.println("rame user : "+rame.users);
                        //System.out.println(" j : "+j+" size : "+getListAttenteRame().size()+ " voyageur : "+a);
                        j--;
                        sizeList--;
                    }
                }
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

    void clearAllStation(){
        if(!testClear){
            this.rameSurPlace.clear();
            this.getListAttenteRame().clear();
            this.voyageurDescendu.clear();
            testClear=true;
        }
    }

    public void spawnVoyageur(RatpNetwork ratpNetwork){
        if(!spawn) {
            int nombreAleatoire = (int) (Math.random() * 2); //TODO previous 11
            for (int i = 0; i < nombreAleatoire; i++) {
                ratpNetwork.addVoyageur(this);
            }
            this.spawn=true;
        }
    }

    public Boolean checkDestinationVoyageurRame(AgentVoyageur a,Rame rame){
        if(!a.cheminEnvisage.isEmpty()) {
            for (Station s : a.cheminEnvisage.peek().getRight()) {
                //System.out.println("cheminVoyageur : " + s.name);
                //System.out.println("cheminRame : " + getTerminusRame(rame).name);
                if (s.name.equals(this.getTerminusRame(rame).name)) {
                    return true;
                }
            }
        }
        return false;
    }


    public Station getTerminusRame(Rame rame){
        return rame.getTerminus().station;
    }

    public void addToMctList(String name,AgentVoyageur a){
        StationsDirectory.getInstance().gares.get(name).listMct.add(a);
    }

    public int nbPlaceRame(Rame rame) {
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
