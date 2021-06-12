package station;

import com.vividsolutions.jts.geom.Point;
import lines.Line;
import rame.Rame;
import ratp.directory.StationsDirectory;
import sim.app.geo.masoncsc.util.Pair;
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
    private List<Rame> rameSurPlace = new ArrayList<>();
    public Boolean test=false;
    public Boolean tets2=false;
    private List<AgentVoyageur> voyageurDescendu=new ArrayList<>();
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
        /*/if(!spawn){
            int nbVoyageurSpawn = (int)doubleNormale(7,5);
            for(int i=0;i<1;i++){
                ratpNetwork.addVoyageur(this);
            }
            this.spawn=true;
        }*/
        if(test){
            //System.out.println("yo");
            //System.out.println(this.name);
            if(!spawn){
                //int nbVoyageurSpawn = (int)doubleNormale(7,5);
                for(int i=0;i<1;i++){
                    AgentVoyageur temp = ratpNetwork.addVoyageur(this);
                    this.voyageurDescendu.add(temp);
                }
                this.spawn=true;
            }
            //addToMctList(this.name,listAttenteRame.remove(0));
            //System.out.println(StationsDirectory.getInstance().gares.get(this.name).queueMct);
        }

        if(!this.voyageurDescendu.isEmpty()){
            for(AgentVoyageur a : this.voyageurDescendu){
                if(this.tets2==false){
                    System.out.println(a.stationCourante.name);
                    System.out.println(a.cheminEnvisage.peek().getLeft().name);
                    if(a.stationCourante.name.equals(a.cheminEnvisage.peek().getLeft().name)){
                        this.addToMctList(a.stationCourante.name,a);
                        Gare g=StationsDirectory.getInstance().gares.get(a.stationCourante.name);
                        a.cheminEnvisage.poll();
                        String lineNumber = a.cheminEnvisage.peek().getLeft().lineNumber;
                        String stationName = a.cheminEnvisage.peek().getLeft().name;
                        Station s = StationsDirectory.getInstance().getStation(lineNumber,stationName);
                        System.out.println("avant :"+s.getListAttenteRame());
                        s.getListAttenteRame().add(g.getQueueMct().poll());
                        System.out.println("apres: "+s.getListAttenteRame());
                        //System.out.println("tiens :"+StationsDirectory.getInstance().gares.get("Nation"));
                        //Gare g = StationsDirectory.getInstance().gares.get("Nation");
                        //System.out.println(g.name);
                    }

                    this.tets2=true;
                }
            }
        }

        if(StationsDirectory.getInstance().getStation("8","Reuilly - Diderot").test==false){
            StationsDirectory.getInstance().getStation("8","Reuilly - Diderot").test=true;
        }

        if(!rameSurPlace.isEmpty()){
            Rame rame = rameSurPlace.remove(0);
            if(!getListAttenteRame().isEmpty()){
                AgentVoyageur a =getListAttenteRame().remove(0);
                System.out.println(rame.freePlaces());
                rame.addUser(a);
                System.out.println(rame.freePlaces());
                ratpNetwork.removeVoyageur(a);
            }
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
