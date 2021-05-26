package ratp;

import gui.RatpState;
import gui.RatpStateWithUI;
import sim.display.Console;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        RatpState model = new RatpState(System.currentTimeMillis());
        RatpStateWithUI gui = new RatpStateWithUI(model);
        Console console = new Console(gui);
        console.setVisible(true);
        console.pressPlay();
    }
}
