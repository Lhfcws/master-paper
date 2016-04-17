package paper.render;

import org.apache.hadoop.conf.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import paper.community.model.UserRelations;

import java.util.List;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/4/17
 */
public class GraphmlDrawer extends CommunityGraphDrawer {
    public GraphmlDrawer(Configuration conf) {
        super(conf);
    }

    public GraphmlDrawer(Configuration conf, String fileType) {
        super(conf, fileType);
    }

    public GraphmlDrawer(Configuration conf, String fileType, boolean isDirected) {
        super(conf, fileType, isDirected);
    }

    @Override
    public void buildGraph(Object inputGraph) {
        UserRelations userRelations = (UserRelations) inputGraph;

        for (Map.Entry<String, List<String>> entry : userRelations.entrySet()) {
            Node source = this.getNodeOrNew(entry.getKey());
            for (String targetID : entry.getValue()) {
                Node target = this.getNodeOrNew(targetID);

                Edge edge = this.graphModel.factory().newEdge(source, target);
//                edge.getEdgeData().setR(targetID.getR() * 1.1f);
//                edge.getEdgeData().setG(targetID.getG() * 1.1f);
//                edge.getEdgeData().setB(targetID.getB() * 1.1f);
//                edge.getEdgeData().setR(255);
//                edge.getEdgeData().setG(255);
//                edge.getEdgeData().setB(255);

                if (isDirected)
                    this.directedGraph.addEdge(edge);
                else
                    this.undirectedGraph.addEdge(edge);
            }
        }
    }
}
