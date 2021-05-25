package lines;

public class Crossable {

    String name;

    public Crossable origin;
    public Crossable destination;

    double length;
    double max_speed;
    double minimum_stop_time = 0;

    Boolean isCrossable = true;
    Boolean isOpen = true;

    // TODO: Not a string ! Replace by a GeoJSON
    String shape;

    @Override
    public String toString() {
        return name;
    }

    public String toFullString() {
        return "Crossable{" +
                "origin=" + origin +
                ", destination=" + destination +
                ", name='" + name + '\'' +
                '}';
    }

    public double getCrossingETA(){
        if(!isOpen)
            return Double.MAX_VALUE;

        if(max_speed==0)
            return Double.MAX_VALUE;

        return length/max_speed + minimum_stop_time;
    }
}
