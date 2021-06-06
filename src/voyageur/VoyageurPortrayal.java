package voyageur;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import java.awt.*;

public class VoyageurPortrayal extends RectanglePortrayal2D {
    public VoyageurPortrayal(){
        super();
    }
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        AgentVoyageur agent = (AgentVoyageur) object;
        Color color = new Color(agent.colere,0,255 - agent.colere);
        graphics.setPaint(color);
        super.draw(object, graphics, info);
    }
}
