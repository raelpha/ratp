package voyageur;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class VoyageurPortrayal extends OvalPortrayal2D
{
    public VoyageurPortrayal(){
        super();
        filled = true;
        scale = VoyageurConstants.voyageurScale;
    }
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        AgentVoyageur agent = (AgentVoyageur) object;
        Color color = new Color(agent.colere,0,255 - agent.colere);
        paint = color;
        super.draw(object, graphics, info);
    }
}
