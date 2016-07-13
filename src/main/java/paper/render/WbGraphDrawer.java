package paper.render;

import com.datatub.iresearch.analyz.base.MLLibConfiguration;
import com.google.common.reflect.TypeToken;
import com.yeezhao.commons.util.AdvFile;
import com.yeezhao.commons.util.FreqDist;
import com.yeezhao.commons.util.ILineParser;
import com.yeezhao.commons.util.StringUtil;
import com.yeezhao.commons.util.serialize.GsonSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import paper.community.model.Community;
import paper.community.model.util.GraphBuilder;
import paper.community.model.util.GraphFilter;
import paper.community.model.util.IntMap;

import java.awt.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class WbGraphDrawer extends CommunityGraphDrawer {
    public WbGraphDrawer(Configuration conf) {
        super(conf);
    }

    @Override
    public void buildGraph(Object inputGraph) {
        Map<NodeType, List<NodeType>> links = (Map<NodeType, List<NodeType>>) inputGraph;

        int edges = 0;
        for (Map.Entry<NodeType, List<NodeType>> entry : links.entrySet()) {
            Node source = this.getNodeOrNew(entry.getKey());
            for (NodeType targetID : entry.getValue()) {
                edges++;
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
        System.out.println("Total links : " + edges);
    }

    public static void main(String[] args) throws Exception {
        int topNComm = 20;
        String clusterFN = "community_1945133444_clusters.txt";
        String relFN = "community_1945133444_relations.txt";

        Configuration conf = MLLibConfiguration.getInstance();
        InputStream clusterIn = conf.getConfResourceAsInputStream(clusterFN);
        InputStream relIn = conf.getConfResourceAsInputStream(relFN);

        String json = IOUtils.toString(clusterIn);
        final Map<String, Integer> clusters = GsonSerializer.deserialize(json, new TypeToken<Map<String, Integer>>() {
        }.getType());

        // trunccommunity
        FreqDist<Integer> counter = new FreqDist<>();
        for (Map.Entry<String, Integer> e : clusters.entrySet())
            counter.inc(e.getValue());
        List<Map.Entry<Integer, Integer>> sortList = counter.sortValues(false);
        if (sortList.size() > topNComm)
            sortList = sortList.subList(0, topNComm);
        counter.clear();
        for (Map.Entry<Integer, Integer> e : sortList)
            counter.put(e.getKey(), e.getValue());

        Set<Map.Entry<String, Integer>> clustersSet = new HashSet<>(clusters.entrySet());
        for (Map.Entry<String, Integer> e : clustersSet) {
            if (!counter.containsKey(e.getValue()))
                clusters.remove(e.getKey());
        }


        GraphFilter graphFilter = new GraphFilter();
        final GraphBuilder graphBuilder = new GraphBuilder();
        AdvFile.loadFileInRawLines(relIn, new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] arr = s.trim().split("\t");
                String src = arr[0];
                if (clusters.containsKey(src) && arr.length > 1) {
                    String[] ts = arr[1].split(StringUtil.STR_DELIMIT_1ST);
                    for (String t : ts) {
                        if (clusters.containsKey(t))
                            graphBuilder.link(src, t);
                    }
                }
            }
        });

        Map<NodeType, List<NodeType>> graph = graphFilter.filter(graphBuilder.getGraph(), topNComm * 1000);
        System.out.println("Total nodes: " + graphFilter.size());

        Set<Integer> clusterValueSet = new HashSet<>(clusters.values());
        List<Color> colorList = ColorBuilder.generateGapColors(clusterValueSet.size());
        IntMap intMap = new IntMap();
        for (int cid : clusterValueSet)
            intMap.get(cid);

        for (String uid : graphFilter.existUsers) {
            NodeType nodeType = graphBuilder.getWare().get(uid);
            nodeType.communityID = clusters.get(uid);
            nodeType.setColor(
                    colorList.get(
                            intMap.get(nodeType.communityID)
                    )
            );
        }

        int[] as = new int[] {1,};
        int[] rs = new int[] {1, };

        int a = conf.getInt("fa2c.community.attraction", 50);
        int r = conf.getInt("fa2c.community.repulsion", 1);

        for (int i : as) {
            a = i;
            for (int j : rs) {
                r = j;

                Configuration configuration = new Configuration(conf);
                configuration.setInt("fa2c.community.attraction", a);
                configuration.setInt("fa2c.community.repulsion", r);

                System.out.println("Attraction: " + a + ", Repulsion: " + r);
                WbGraphDrawer drawer = new WbGraphDrawer(configuration);
                drawer.buildGraph(graph);
                drawer.startLayout();
                drawer.export("/Users/lhfcws/coding/workspace/branches/lhfcws-paper1/src/main/resources/layout-" + a + "-" + r + ".gexf");
                System.out.println("Export done.");
                drawer.stopLayout();
            }
        }
        System.exit(0);
    }
}
