package paper.community.fastunfolding;

import java.util.*;

/**
 * 实现FastUnfolding算法（目前只支持无向图）
 *
 * @author liukang
 * @since 2016/1/14
 */
public class FastUnfolding {
    private Map<Node, List<Node>> graph;
    private Map<Long, Vertex> vertexs;
    private List<Community> communities;
    private boolean directed;
    public double m;
    public double q;
    public long cost = 0;

    public FastUnfolding() {
        directed = false;
        m = q = 0;
        vertexs = new HashMap<>();
        communities = new LinkedList<>();
    }

    /**
     * 初始化阶段：为每个node分配vertex，为每个vertex分配community
     */
    private void initAlgo() {
        if (isDirected()) {

        } else {
            m = 0;
            for (Map.Entry<Node, List<Node>> e : graph.entrySet()) {
                Node n1 = e.getKey();
                //  给每个node分配vertex
                Vertex v = new Vertex(n1.getId());
                vertexs.put(v.getId(), v);
                n1.setVertex(v);
                //  给每个vertex分配community
                Community c = new Community(v.getId());
//                c.addVertex(v);
                communities.add(c);
                v.setCommunity(c);

                for (Node n2 : e.getValue()) {
                    n1.getLinkedNodes().put(n2, 1d);
                    n2.getLinkedNodes().put(n1, 1d);
                }
            }

            for (Node n : graph.keySet()) {
                if (n.getDegree() != 0) {
                    Vertex v = n.getVertex();
                    v.addNode(n);
                    v.getCommunity().addVertex(v);
                    m += n.getDegree();
                }
            }
            m /= 2;
        }
    }

    /**
     * detect阶段：遍历vertexs集合，为每个vertex分配最佳社群
     *
     * @return
     */
    private boolean detect() {
        double deltaQ = 0, max = 0;
        boolean isGlobalOptimum = true;  //  是否全局最优
        boolean isLocalOptimum = false;  //  是否局部最优
        Community newComm = null;
        //  满足局部最优则退出当前detect阶段
        while (!isLocalOptimum) {
            isLocalOptimum = true;
            for (Community oldComm : communities) {
                Iterator<Vertex> vIter = oldComm.getVertexs().iterator();
                while (vIter.hasNext()) {
                    newComm = oldComm;
                    Vertex v1 = vIter.next();
                    max = calRemoveDeltaQ(v1);

                    for (Vertex v2 : v1.getLinkedVertexs().keySet()) {
                        if (oldComm != v2.getCommunity()) {
                            deltaQ = calDeltaQ(v1, v2.getCommunity());
                            if (deltaQ > max) {
                                max = deltaQ;
                                newComm = v2.getCommunity();
                            }
                        }
                    }

                    if (max < 0) {
                        isGlobalOptimum = false;
                        isLocalOptimum = false;
                        for (Community c : communities) {
                            if (c.isEmpty()) {
                                v1.changeCommunity(vIter, c);
                                break;
                            }
                        }
                    } else if (max > 0 && oldComm != newComm) {
                        isGlobalOptimum = false;
                        isLocalOptimum = false;
                        v1.changeCommunity(vIter, newComm);
                    }
                }
            }
//            for (Community c : communities) {
//                System.out.println(c);
//            }
//            System.out.println("q : " + calModularity());
        }
        return isGlobalOptimum;
    }

    /**
     * reset阶段：将当前一个社群转换为一个节点
     */
    private void reset() {
        // 生成新的vertexs
        vertexs.clear();
        Iterator<Community> iter = communities.iterator();
        while (iter.hasNext()) {
            Community c = iter.next();
            //  删除空社群
            if (c.getId() == null || c.isEmpty()) {
                iter.remove();
                continue;
            }
            vertexs.put(c.getId(), c.toVertex());
        }

        //  将原邻接vertex链表映射到新vertexs
        for (Map.Entry<Long, Vertex> e1 : vertexs.entrySet()) {
            Map<Vertex, Double> linkedVertexs = new HashMap<>();
            Vertex v1 = e1.getValue();
            for (Map.Entry<Vertex, Double> e2 : v1.getLinkedVertexs().entrySet()) {
                Vertex v2 = vertexs.get(e2.getKey().getCommunity().getId());
                if (v1.getCommunity() != v2.getCommunity()) {
                    if (!linkedVertexs.containsKey(v2)) {
                        linkedVertexs.put(v2, e2.getValue());
                    } else {
                        linkedVertexs.put(v2, linkedVertexs.get(v2) + e2.getValue());
                    }
                }
            }
            v1.setLinkedVertexs(linkedVertexs);
        }
    }

    /**
     * 输出社群探测结果
     */
    public void printResult() {
        int cnt = 0;
        for (Vertex v : vertexs.values()) {
            Collections.sort(v.getNodes(), new Comparator<Node>() {
                @Override
                public int compare(Node n1, Node n2) {
                    Long i1 = Long.parseLong(n1.getName());
                    Long i2 = Long.parseLong(n2.getName());
                    if (i1 > i2) {
                        return 1;
                    } else if (i1 < i2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            System.out.print("[" + cnt++ + "] ");
            for (Node n : v.getNodes()) {
                System.out.print(n.getName() + ", ");
            }
            System.out.println();
        }
        System.out.println("Modularity (Q) is : " + calModularity());
    }

    /**
     * 运行算法
     */
    public void runAlgo() {
        if (graph == null) {
            System.out.println("[ERROR] graph is empty.");
        }
        int cnt = 0;
//        System.out.println("[COMMUNITY] initialize algorithm.");
        initAlgo();
//        System.out.println("[COMMUNITY] run algorithm. ");
        long start = System.currentTimeMillis();
        while (!detect()) {
            reset();
//            System.out.println("[COMMUNITY] round " + ++cnt + " finished.");
        }
        cost += System.currentTimeMillis() - start;
//        printResult();
    }

    /**
     * 计算模块度
     *
     * @return
     */
    private double calModularity() {
        q = 0;
        for (Community c : communities) {
            q += (c.getDegree() - c.getOutPathNum()) / (2 * m) - Math.pow(c.getDegree() / (2 * m), 2);
        }
        return q;
    }

    /**
     * 计算将v加入社群c时Q的增益
     *
     * @param v
     * @param c
     * @return
     */
    private double calDeltaQ(Vertex v, Community c) {
        double deltaQ = 0;
        if (isDirected()) {

        } else {
            deltaQ = v.getLinkNumToCommunity(c) * 2 - c.getDegree() / m * v.getDegree();
        }
        return deltaQ;
    }

    /**
     * 计算从当前所属community去除时Q的损失值
     *
     * @param v
     * @return
     */
    private double calRemoveDeltaQ(Vertex v) {
        double deltaQ = 0;
        if (isDirected()) {

        } else {
            deltaQ = v.getLinkNumToCommunity(v.getCommunity()) * 2 - (v.getCommunity().getDegree() - v.getDegree()) / m * v.getDegree();
        }
        return deltaQ;
    }

    public Map<Node, List<Node>> getGraph() {
        return graph;
    }

    public void setGraph(Map<Node, List<Node>> graph) {
        this.graph = graph;
    }

    public Map<Long, Vertex> getVertexs() {
        return vertexs;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }
}
