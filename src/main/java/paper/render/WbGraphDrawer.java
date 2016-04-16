package paper.render;

import org.apache.hadoop.conf.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

import java.util.List;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class WbGraphDrawer extends CommunityGraphDrawer {
    private static final int SHOW_COMMUNITY_NUM = 50;

    public WbGraphDrawer(Configuration conf) {
        super(conf);
    }

    @Override
    public void buildGraph(Object inputGraph) {
        Map<NodeType, List<NodeType>> links = (Map<NodeType, List<NodeType>>) inputGraph;

        String community = null;
        for (Map.Entry<NodeType, List<NodeType>> entry : links.entrySet()) {
            Node source = this.getNodeOrNew(entry.getKey());
            for (NodeType targetID : entry.getValue()) {
                Node target = this.getNodeOrNew(targetID);

                Edge edge = this.graphModel.factory().newEdge(source, target);
//                edge.getEdgeData().setR(targetID.getR() * 1.1f);
//                edge.getEdgeData().setG(targetID.getG() * 1.1f);
//                edge.getEdgeData().setB(targetID.getB() * 1.1f);
                edge.getEdgeData().setR(255);
                edge.getEdgeData().setG(255);
                edge.getEdgeData().setB(255);

                if (isDirected)
                    this.directedGraph.addEdge(edge);
                else
                    this.undirectedGraph.addEdge(edge);
            }
        }
    }
}
