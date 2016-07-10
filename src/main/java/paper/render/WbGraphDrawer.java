package paper.render;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.google.common.reflect.TypeToken;
import com.yeezhao.commons.util.AdvFile;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.StringUtil;
import com.yeezhao.commons.util.serialize.GsonSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import paper.community.model.util.GraphBuilder;
import paper.community.model.util.IntMap;

import java.awt.*;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static void main(String[] args) throws Exception {
        String clusterFN = "community_1945133444_clusters.txt";
        String relFN = "community_1945133444_relations.txt";

        Configuration conf = MLLibConfiguration.getInstance();
        InputStream clusterIn = conf.getConfResourceAsInputStream(clusterFN);
        InputStream relIn = conf.getConfResourceAsInputStream(relFN);

        String json = IOUtils.toString(clusterIn);
        Map<String, Integer> clusters = GsonSerializer.deserialize(json, new TypeToken<Map<String, Integer>>() {
        }.getType());
        final GraphBuilder graphBuilder = new GraphBuilder();
        final AtomicInteger limit = new AtomicInteger(0);
        AdvFile.loadFileInRawLines(relIn, new ILineParser() {
            @Override
            public void parseLine(String s) {
                limit.incrementAndGet();
                if (limit.get() > 5000) return;
                String[] arr = s.trim().split("\t");
                if (arr.length > 1) {
                    String[] ts = arr[1].split(StringUtil.STR_DELIMIT_1ST);
                    for (String t : ts) {
                        graphBuilder.link(arr[0], t);
                    }
                }
            }
        });

        System.out.println("Total nodes: " + graphBuilder.getWare().size());

        Set<Integer> set = new HashSet<>(clusters.values());
        List<Color> colorList = ColorBuilder.generateGapColors(set.size());
        IntMap intMap = new IntMap();
        for (int cid : set)
            intMap.get(cid);

        for (NodeType nodeType : graphBuilder.getWare().values()) {
            nodeType.communityID = clusters.get(nodeType.getId());
            nodeType.setColor(
                    colorList.get(
                            intMap.get(nodeType.communityID)
                    )
            );
        }

        int a = conf.getInt("fa2c.community.attraction", 100);
        int r = conf.getInt("fa2c.community.repulsion", 1);

        WbGraphDrawer drawer = new WbGraphDrawer(conf);
        drawer.buildGraph(graphBuilder.getGraph());
        drawer.startLayout();
        drawer.export("/Users/lhfcws/coding/workspace/branches/lhfcws-paper1/src/main/resources/layout-" + a + "-" + r + ".gexf");
        System.out.println("Export done.");
        drawer.stopLayout();
        System.exit(0);
    }
}
