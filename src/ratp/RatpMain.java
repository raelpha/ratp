package ratp;

import ratp.directory.LinesDirectory;
import ratp.directory.SchedulesDirectory;
import ratp.directory.StationsDirectory;
import sim.app.geo.masoncsc.util.Pair;
import sim.display.Console;
import station.Station;

import java.util.List;

public class RatpMain {


    private static void initializeDirectories(){
        SchedulesDirectory.initialize();
        StationsDirectory.initialize();
        LinesDirectory.initialize();
        StationsDirectory.getInstance().affectPointsToStations();
        StationsDirectory.getInstance().createAllGeomVectorFieldForGares();
    }

    public static void main(String[] args){
        initializeDirectories();
        List<Station> sfds = StationsDirectory.getInstance().getTowardsStation(StationsDirectory.getInstance().getStation("13","Pernety"), StationsDirectory.getInstance().getStation("13","Liège"));
        List<Pair<Station, List<Station>>> fdsjhvbjd = StationsDirectory.getInstance().getAdjacentStationsWithDestination(StationsDirectory.getInstance().getStation("2","Pigalle"));
        RatpNetwork model = new RatpNetwork(System.currentTimeMillis());
        RatpStateWithUI gui = new RatpStateWithUI(model);

        Console console = new Console(gui);
        console.setVisible(true);

        // console.pressPlay();
    }
}
