package ratp;

import lines.Line;
import ratp.directory.LinesDirectory;
import sim.engine.SimState;

import java.util.Map;

public class RatpNetwork extends SimState {

    Map<String, Line> lines = LinesDirectory.getInstance().lines;

    public RatpNetwork(long seed){
        super(seed);
    }

}
