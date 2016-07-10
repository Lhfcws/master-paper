package paper.community.fastunfolding;

import java.util.*;

/**
 * @author liukang
 * @since 2016/1/15
 */
public class Vertex {
    private long id = 0l;
    private List<Node> nodes = new LinkedList<>();
    private Community community = null;
    private int degree = 0;
    private Map<Vertex, Double> linkedVertexs = new HashMap<>();
    private long timestamp = System.currentTimeMillis();

    public Vertex() {
    }

    public Vertex(long id) {
        this();
        this.id = id;
    }

    public Vertex(Community community) {
        this();
        this.community = community;
    }

    public int getLinkNumToCommunity(Community c) {
        int weight = 0;
        for (Map.Entry<Vertex, Double> e : linkedVertexs.entrySet()) {
            if (e.getKey().getCommunity() == c) {
                weight += e.getValue();
            }
        }
        return weight;
    }

    /**
     * 添加单个Node节点，仅在初始化时调用
     *
     * @param n
     */
    public void addNode(Node n) {
        if (nodes.add(n)) {
            degree += n.getDegree();
            for (Map.Entry<Node, Double> e : n.getLinkedNodes().entrySet()) {
                Vertex v = e.getKey().getVertex();
                if (v != null)
                    if (linkedVertexs.containsKey(v)) {
                        linkedVertexs.put(v, linkedVertexs.get(v) + e.getValue());
                    } else {
                        linkedVertexs.put(v, e.getValue());
                    }
            }
        }
    }

    /**
     * 将另一个vertex合并到当前vertex中
     *
     * @param v 需要合并的vertex
     */
    public void addVertex(Vertex v) {
        degree += v.getDegree();
        nodes.addAll(v.nodes);
        for (Map.Entry<Vertex, Double> e : v.linkedVertexs.entrySet()) {
            if (linkedVertexs.containsKey(e.getKey())) {
                linkedVertexs.put(e.getKey(), linkedVertexs.get(e.getKey()) + e.getValue());
            } else {
                linkedVertexs.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 改变所属社群
     *
     * @param iter
     * @param target
     */
    public void changeCommunity(Iterator<Vertex> iter, Community target) {
        community.removeVertex(this, iter);
        setCommunity(target);
        community.addVertex(this);
    }

//    public String toString() {
//        return "id=" + id + " nodes=" + nodes.size() + " community=" + community.getId() + " degree=" + degree + " linkedVertexs=" + linkedVertexs.size();
//    }


    @Override
    public String toString() {
        return "Vertex{" +
                "id=" + id +
                ", nodes=" + nodes +
                ", degree=" + degree +
                '}';
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public int getDegree() {
        return degree;
    }

    public void setLinkedVertexs(Map<Vertex, Double> linkedVertexs) {
        this.linkedVertexs = linkedVertexs;
    }

    public Map<Vertex, Double> getLinkedVertexs() {
        return linkedVertexs;
    }
}
