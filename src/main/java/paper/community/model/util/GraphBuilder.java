package paper.community.model.util;

import paper.render.NodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/7/10
 */
public class GraphBuilder {
    protected Map<String, NodeType> ware = new HashMap<>();
    protected Map<NodeType, List<NodeType>> graph = new HashMap<>();

    public synchronized void link(String src, String target) {
        if (!ware.containsKey(src)) {
            ware.put(src, new NodeType(src));
            graph.put(ware.get(src), new ArrayList<NodeType>());
        }

        if (!ware.containsKey(target)) {
            ware.put(target, new NodeType(target));
            graph.put(ware.get(target), new ArrayList<NodeType>());
        }

        graph.get(ware.get(src)).add(ware.get(target));
        graph.get(ware.get(target)).add(ware.get(src));
    }

    public Map<NodeType, List<NodeType>> getGraph() {
        return graph;
    }

    public Map<String, NodeType> getWare() {
        return ware;
    }
}
