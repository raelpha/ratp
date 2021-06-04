package ratp;

import ratp.directory.LinesDirectory;
import ratp.directory.SchedulesDirectory;
import ratp.directory.StationsDirectory;
import sim.display.Console;

public class RatpMain {


    private static void initializeDirectories(){
        SchedulesDirectory.initialize();
        StationsDirectory.initialize();
        LinesDirectory.initialize();
        StationsDirectory.getInstance().affectPointsToStations();
        //TODO appeler méthode construct geomvectorfield // équivalent affect point to stations
        StationsDirectory.getInstance().createAllGeomVectorFieldForGares();
        //TODO
    }

    public static void main(String[] args){
        initializeDirectories();
        RatpNetwork model = new RatpNetwork(System.currentTimeMillis());
        RatpStateWithUI gui = new RatpStateWithUI(model);

        System.out.println("Starting mason console");
        Console console = new Console(gui);
        console.setVisible(true);
        console.pressPlay();
    }
}
