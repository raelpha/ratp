package station;

import com.vividsolutions.jts.geom.Point;
import lines.Line;
import rame.Rame;
import ratp.directory.StationsDirectory;
import sim.util.geo.MasonGeometry;

import java.awt.*;
import voyageur.AgentVoyageur;
import ratp.RatpNetwork;
import sim.engine.SimState;
import sim.engine.Steppable;
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
    private final List<AgentVoyageur> listAttenteRame ;
    private double colereStation;
    private int nbVoyageurs;
    private Boolean fermee=false;
    private final List<Rame> rameSurPlace = new ArrayList<>();
    private Boolean test=false;
    private final Boolean tets2=false;
    private final List<AgentVoyageur> voyageurDescendu=new ArrayList<>();
    private int attente=0;
    private Boolean testClear=false;
    private final Boolean sizeListTest=false;
    private final int attenteMCT=50;

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
        return nbVoyageurs;
    }

    public void setNbVoyageurs() {
        this.nbVoyageurs = this.getListAttenteRame().size();
    }

    public Boolean isFermee() {
        return fermee;
    }

    public void setFermee() {
        this.fermee = true;
        StationsDirectory.getInstance().fermerStation(this);
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

        setColereStation();
        setNbVoyageurs();

        if (test) {

        }

        if (StationsDirectory.getInstance().getStation("8", "Lourmel").test == false) {
            StationsDirectory.getInstance().getStation("8", "Lourmel").test = true;
        }
        arriveeRame(ratpNetwork);
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

    public void descenteRame(RatpNetwork ratpNetwork){
        if (!this.rameSurPlace.isEmpty() && !this.isFermee()) {
            for (int i = 0; i < this.rameSurPlace.size(); i++) {
                Rame rame = this.rameSurPlace.get(0);
                List<AgentVoyageur> removeUserDeLaRame=new ArrayList<AgentVoyageur>();
                removeUserDeLaRame= rame.removeUser();
                if (!removeUserDeLaRame.isEmpty()) {
                    for(AgentVoyageur aV : removeUserDeLaRame){
                        if(aV.cheminEnvisage.isEmpty()){
                            System.out.println("Je suis arrivé : "+aV);
                            aV.SortirDeRame(ratpNetwork.yard,this);
                            rame.users.remove(aV);
                            ratpNetwork.removeVoyageur(aV);
                        }else{
                            String lineNumber = aV.cheminEnvisage.peek().getLeft().lineNumber;
                            String stationName = aV.cheminEnvisage.peek().getLeft().name;
                            Station s = StationsDirectory.getInstance().getStation(lineNumber, stationName);
                            s.getListAttenteRame().add(aV);
                            if(attente==0){
                                aV.SortirDeRame(ratpNetwork.yard,this);
                            }
                            else{
                                attente--;
                            }
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
        descenteRame(ratpNetwork);
        monteeRame(ratpNetwork);
    }

    public void monteeRame(RatpNetwork ratpNetwork){
        if (!this.rameSurPlace.isEmpty() && !this.isFermee()) {
            for (int i = 0; i < this.rameSurPlace.size(); i++) {
                Rame rame = this.rameSurPlace.get(i);
                int sizeList;
                if(getListAttenteRame().size()<nbPlaceRame(rame) ){
                    sizeList=getListAttenteRame().size();
                }else{
                    sizeList=nbPlaceRame(rame);
                }
                for(int j =0 ;j<sizeList;j++){
                    if(checkDestinationVoyageurRame(getListAttenteRame().get(j),rame)){
                        AgentVoyageur a = getListAttenteRame().get(j);
                        rame.addUser(a);
                        a.enTrain=true;
                        ratpNetwork.removeVoyageur(a);
                        this.getListAttenteRame().remove(a);
                        j--;
                        sizeList--;
                    }
                }
            }
        }
    }

    void clearAllStation(){
        if(!this.testClear){
            this.rameSurPlace.clear();
            this.getListAttenteRame().clear();
            this.voyageurDescendu.clear();
            this.testClear=true;
        }
    }

    public void spawnVoyageur(RatpNetwork ratpNetwork){
        if(!spawn) {
            int nombreAleatoire = (int) (Math.random() * Constants.facteurVoyageurMax + 1); //TODO previous 11
            for (int i = 0; i < nombreAleatoire; i++) {
                ratpNetwork.addVoyageur(this);
            }
            this.spawn=true;
        }
    }

    public Boolean checkDestinationVoyageurRame(AgentVoyageur a,Rame rame){
        if(!a.cheminEnvisage.isEmpty()) {
            for (Station s : a.cheminEnvisage.peek().getRight()) {
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


    public static void main(String[] args){

    }

}
