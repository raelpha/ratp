package ratp;

import ratp.directory.SchedulesDirectory;
import ratp.directory.StationsDirectory;
import sim.display.Console;

import java.io.IOException;

public class RatpMain {


    private static void initializeDirectories(){
        SchedulesDirectory.initialize();
        StationsDirectory.initialize();
    }

    public static void main(String[] args) throws IOException {
        initializeDirectories();
        RatpNetwork model = new RatpNetwork(System.currentTimeMillis());
        RatpStateWithUI gui = new RatpStateWithUI(model);
        Console console = new Console(gui);
        console.setVisible(true);
        console.pressPlay();
    }
}