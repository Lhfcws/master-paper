package paper.render.force;

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory;
import org.gephi.layout.plugin.forceAtlas2.Region;

/**
 * compute repulsion force with community
 *
 * @author lhfcws
 * @since 2016/1/13
 */
public class CommunityRepulsionForce extends ForceFactory.RepulsionForce {
    private double coefficient;
    private double communityRepulsion;
    public static volatile double maxDis = 0;

    public CommunityRepulsionForce(ForceFactory factory, double coefficient, double communityRepulsion) {
        factory.super();
        this.coefficient = coefficient;
        this.communityRepulsion = communityRepulsion;
    }

    @Override
    public void apply(Node n1, Node n2) {
        ForceAtlas2LayoutData n1Layout = n1.getNodeData().getLayoutData();
        ForceAtlas2LayoutData n2Layout = n2.getNodeData().getLayoutData();

        // Get the distance
        double xDist = n1.getNodeData().x() - n2.getNodeData().x();
        double yDist = n1.getNodeData().y() - n2.getNodeData().y();
        double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);
        if (maxDis < distance) maxDis = distance;

        if (distance > 0) {
            // NB: factor = force / distance
            double factor = coefficient * n1Layout.mass * n2Layout.mass / distance / distance;
            if (n1.getNodeData().getAttributes().getValue("community") == n2.getNodeData().getAttributes().getValue("community"))
                factor /= communityRepulsion;
            else {
                double d = 1;
                if (d < 1) d = 1;
                factor *= communityRepulsion * d;
            }

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }

    @Override
    public void apply(Node n, Region r) {
        ForceAtlas2LayoutData nLayout = n.getNodeData().getLayoutData();

        // Get the distance
        double xDist = n.getNodeData().x() - r.getMassCenterX();
        double yDist = n.getNodeData().y() - r.getMassCenterY();
        double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

        if (distance > 0) {
            // NB: factor = force / distance
            double factor = coefficient * nLayout.mass * r.getMass() / distance / distance;

            nLayout.dx += xDist * factor;
            nLayout.dy += yDist * factor;
        }
    }

    @Override
    public void apply(Node n, double g) {
        ForceAtlas2LayoutData nLayout = n.getNodeData().getLayoutData();

        // Get the distance
        double xDist = n.getNodeData().x();
        double yDist = n.getNodeData().y();
        double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

        if (distance > 0) {
            // NB: factor = force / distance
            double factor = coefficient * nLayout.mass * g / distance;

            nLayout.dx -= xDist * factor;
            nLayout.dy -= yDist * factor;
        }
    }
}
