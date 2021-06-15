package voyageur;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;

import java.awt.*;

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
        Color color = new Color(agent.colere*(255/100),0,255 - agent.colere*(255/100));
        paint = color;
        super.draw(object, graphics, info);
    }
}
