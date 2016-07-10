package paper.community.fastunfolding;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author liukang
 * @since 2016/1/14
 */
public class Community {
    private long id;
    private List<Vertex> vertexs;
    private int degree;
    private long timestamp = System.currentTimeMillis();

    public Community(){
        degree = 0;
        vertexs = new LinkedList<>();
    }

    public Community(long id){
        this();
        this.id = id;
    }

    public Vertex toVertex(){
        Iterator<Vertex> it = vertexs.iterator();
        Vertex v = it.next();
        while (it.hasNext()){
            v.addVertex(it.next());
        }
        emptyVertexs();
        addVertex(v);
        return  v;
    }

    public void addVertex(Vertex v){
        if(vertexs.add(v)){
            degree += v.getDegree();
        }
    }

    public void removeVertex(Vertex v, Iterator<Vertex> iter){
        degree -= v.getDegree();
        iter.remove();
    }

    public void emptyVertexs(){
        degree = 0;
        vertexs.clear();
    }

    public boolean isEmpty(){
        if (vertexs.size() == 0){
            return true;
        }else{
            return false;
        }
    }

    public double getOutPathNum(){
        double weight = 0;
        for (Vertex v1 : vertexs){
            for (Map.Entry<Vertex, Double> e : v1.getLinkedVertexs().entrySet()){
                Vertex v2 = e.getKey();
                if (v2.getCommunity() != this){
                    weight += e.getValue();
                }
            }
        }
        return weight;
    }

    @Override
    public String toString() {
        return "Community{" +
                "id=" + id +
                ", vertexsSize=" + vertexs.size() +
                ", degree=" + degree +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Vertex> getVertexs() {
        return vertexs;
    }

    public int getDegree(){
        return degree;
    }
}
