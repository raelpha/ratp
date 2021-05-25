package lines;

import javafx.util.Pair;

import java.util.List;

public class Station extends Crossable {

    List<Pair<Line,Boolean>> connections; // <Ligne,isconnectionOpen>

    public Boolean isTerminus(){
        if(origin==null || destination==null)
            return true;
        return false;
    }

}
