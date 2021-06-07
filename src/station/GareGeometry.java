package station;

import global.Constants;
import sim.util.geo.MasonGeometry;

public class GareGeometry extends MasonGeometry {
    String name;
    public GareGeometry() {
        super();
        this.name = this.getStringAttribute(Constants.STATION_NAME_STR);
    }

    public GareGeometry(MasonGeometry gem) {
        this.addAttribute(Constants.IS_MULTIPLE_STATION_STR, gem.getStringAttribute(Constants.IS_MULTIPLE_STATION_STR));
        this.addAttribute(Constants.STATION_NAME_STR, gem.getStringAttribute(Constants.STATION_NAME_STR));
        this.geometry = gem.getGeometry();
        this.name = this.getStringAttribute(Constants.STATION_NAME_STR);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
