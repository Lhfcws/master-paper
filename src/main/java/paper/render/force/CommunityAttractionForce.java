package paper.render.force;

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory;

/**
 * compute repulsion force with community
 *
 * @author lhfcws
 * @since 2016/1/13
 */
public class CommunityAttractionForce extends ForceFactory.AttractionForce {
    private double coefficient;
    private double communityAttraction;

    public CommunityAttractionForce(ForceFactory factory, double coefficient, double communityAttraction) {
        factory.super();
        this.coefficient = coefficient;
        this.communityAttraction = communityAttraction;
    }

    @Override
    public void apply(Node n1, Node n2, double w) {
        ForceAtlas2LayoutData n1Layout = n1.getNodeData().getLayoutData();
        ForceAtlas2LayoutData n2Layout = n2.getNodeData().getLayoutData();

        // Get the distance
        double xDist = n1.getNodeData().x() - n2.getNodeData().x();
        double yDist = n1.getNodeData().y() - n2.getNodeData().y();
        double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist) * w;

        if (distance > 0) {
            // NB: factor = force / distance
//            double factor = -coefficient * n1Layout.mass * n2Layout.mass / distance / distance;
            double factor = -coefficient * w;

            if (n1.getNodeData().getAttributes().getValue("community").equals(n2.getNodeData().getAttributes().getValue("community"))) {
                double d = Math.log(distance)
//                        / Math.log(1.4)
                        ;
//                double d = 1;
                if (d < 1) d = 1;
                factor *= communityAttraction * d;
            } else {
                factor /= communityAttraction;
            }

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }
}
