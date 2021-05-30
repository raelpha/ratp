package ratp;

import ratp.lines.Schedules;
import sim.display.Console;

import java.io.IOException;

public class RatpMain {

    public static void main(String[] args) throws IOException {
        Schedules.initialize();
        RatpNetwork model = new RatpNetwork(System.currentTimeMillis());
        RatpStateWithUI gui = new RatpStateWithUI(model);
        Console console = new Console(gui);
        console.setVisible(true);
        console.pressPlay();
    }
}
