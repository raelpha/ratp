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
    }

    public static void main(String[] args){
        initializeDirectories();
        RatpNetwork model = new RatpNetwork(System.currentTimeMillis());
        RatpStateWithUI gui = new RatpStateWithUI(model);

        Console console = new Console(gui);
        console.setVisible(true);
        console.pressPlay();
    }
}
