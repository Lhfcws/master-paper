package paper.community.fastunfolding;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liukang
 * @since 2016/1/14
 */
public class Node {
    private static volatile long INCREMENT = 0l;
    private long id = -1;
    private String name = "";
    private Vertex vertex = null;
    private Map<Node, Double> linkedNodes = new HashMap<>();
    public int in = 0;
    public int out = 0;

    public Node(){
        this.id = INCREMENT++;
    }

    public Node(String name){
        this();
        this.name = name;
        try {
            int i = Integer.valueOf(name);
            this.id = i;
        } catch (Exception e) {
        }
    }

    public int getDegree(){
        return linkedNodes.size();
    }

//    public String toString(){
//        return "id=" + id + " name=" + name + " vertex=" + vertex.getId() + " degree=" + getDegree() + " in=" + in + " out=" + out;
//    }


    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", linkedNodesSize=" + linkedNodes.size() +
                ", in=" + in +
                ", out=" + out +
                '}';
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public Community getCommunity() {
        return vertex.getCommunity();
    }

    public Map<Node, Double> getLinkedNodes() {
        return linkedNodes;
    }
}
