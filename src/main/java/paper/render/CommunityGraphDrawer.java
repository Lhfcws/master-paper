package paper.render;

import com.yeezhao.commons.util.FileSystemHelper;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.plugin.ExporterGML;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlas;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingoldBuilder;
import org.gephi.layout.spi.Layout;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import paper.render.force.CommunityForce;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author lhfcws
 * @since 16/1/5.
 */
public abstract class CommunityGraphDrawer {

    public static final String GEXF_FILE = "gexf";  // default
    public static final String GML_FILE = "gml";
    public static final String GRAPHML_FILE = "graphml";

    protected ProjectController projectController = null;
    protected Workspace workspace = null;
    protected GraphModel graphModel = null;
    protected DirectedGraph directedGraph = null;
    protected UndirectedGraph undirectedGraph = null;
    protected ExportController exportController = null;
    protected GraphExporter graphExporter = null;
    protected AutoLayout autoLayout;
    protected String fileType;
    protected boolean isDirected = true;
    protected long LAYOUT_TIME = 15;   // 15 sec

    protected Configuration conf;


    public CommunityGraphDrawer(Configuration conf) {
        this.conf = conf;
        this.fileType = GEXF_FILE;
        init();
    }

    public CommunityGraphDrawer(Configuration conf, String fileType) {
        this.conf = conf;
        this.fileType = fileType;
        init();
    }

    public CommunityGraphDrawer(Configuration conf, String fileType, boolean isDirected) {
        this.conf = conf;
        this.fileType = fileType;
        this.isDirected = isDirected;
        init();
    }

    public void init() {
        //Lookup不是gephi的类，是另一个jar包openide的类，用来寻找来加载类。Lookup.getDefault() 拿到单例。
        projectController = Lookup.getDefault().lookup(ProjectController.class);

        workspace = projectController.getCurrentWorkspace();

        if (workspace == null) {
            projectController.newProject();
        } else {
            projectController.cleanWorkspace(workspace);
        }
        workspace = projectController.getCurrentWorkspace();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        if (isDirected)
            directedGraph = graphModel.getDirectedGraph();
        else
            undirectedGraph = graphModel.getUndirectedGraph();

        exportController = Lookup.getDefault().lookup(ExportController.class);
        switch (fileType) {
            case GML_FILE:
                graphExporter = (GraphExporter) exportController.getExporter("gml exporter");
                ExporterGML exporterGML = (ExporterGML) graphExporter;
                exporterGML.setProgressTicket(new DefaultProgressTicket());
                break;
            case GEXF_FILE:
                graphExporter = (GraphExporter) exportController.getExporter(GEXF_FILE);
                break;
            case GRAPHML_FILE:
                graphExporter = (GraphExporter) exportController.getExporter(GRAPHML_FILE);
                break;
        }
        graphExporter.setWorkspace(workspace);
    }

    protected Layout buildLayout() {
//        Layout layout = buildFruchtermanReingoldLayout();
//        Layout layout = buildForceAtlasLayout();
        Layout layout = buildForceAtlas2Layout();
//        Layout layout = buildForceAtlas2cLayout();
        return layout;
    }

    protected FruchtermanReingold buildFruchtermanReingoldLayout() {
        FruchtermanReingold layout = new FruchtermanReingold(new FruchtermanReingoldBuilder());
        layout.setGraphModel(this.graphModel);
        return layout;
    }

    protected ForceAtlasLayout buildForceAtlasLayout() {
        ForceAtlasLayout layout = new ForceAtlasLayout(new ForceAtlas());
        layout.setGraphModel(this.graphModel);
        layout.setGravity(5.0);
        return layout;
    }

    protected ForceAtlas2 buildForceAtlas2Layout() {
        ForceAtlas2 layout = new ForceAtlas2(new ForceAtlas2Builder());
        layout.setGraphModel(this.graphModel);
        layout.setEdgeWeightInfluence(0.0);
        layout.setBarnesHutOptimize(true);
        layout.setJitterTolerance(1.0);
        layout.setGravity(1.0);
        return layout;
    }

    protected CommunityForce buildForceAtlas2cLayout() {
        CommunityForce layout = new CommunityForce(new ForceAtlas2Builder());
//        layout.setEdgeWeightInfluence(0.0);
        layout.setBarnesHutOptimize(true);
        layout.setJitterTolerance(1.0);
//        layout.setOutboundAttractionDistribution(true);
//        layout.setAdjustSizes(true);
//        layout.setLinLogMode(true);
        layout.setCommunityAttraction(conf.getDouble("fa2c.community.attraction", 100.0f));
        layout.setCommunityRepulsion(conf.getDouble("fa2c.community.repulsion", 1.0f));
        layout.setGravity(1.0);
        return layout;
    }

    public void startLayout() {
        this.autoLayout = new AutoLayout(LAYOUT_TIME, TimeUnit.SECONDS);
        this.autoLayout.setGraphModel(this.graphModel);
        Layout layout = buildLayout();
        this.autoLayout.addLayout(layout, 1);
        this.autoLayout.execute();
    }

    public void export(String localFile) throws IOException {
        this.exportController.exportFile(
                new File(localFile), this.graphExporter
        );
    }

    public void exportHDFS(String hdfsFile) throws IOException {
        OutputStream outputStream = FileSystemHelper.getInstance(conf).getHDFSFileOutputStream(hdfsFile);
        String localFile = "/tmp/" + this.getClass().getSimpleName() + this.fileType;
        this.exportController.exportFile(
                new File(localFile), this.graphExporter
        );
        IOUtils.copy(new FileInputStream(localFile), outputStream);
        outputStream.flush();
        outputStream.close();
    }

    public void stopLayout() {
        if (this.autoLayout != null)
            this.autoLayout.cancel();
    }

    public void clear() {
        this.directedGraph.clear();
    }

    protected Node newNode(String id) {
        NodeType nodeType = new NodeType(id);
        return this.newNode(nodeType);
    }

    protected Node newNode(NodeType nodeType) {
        Node node = graphModel.factory().newNode(nodeType.getId());
        if (nodeType.getName() != null)
            node.getNodeData().setLabel(nodeType.getName());
        node.getNodeData().setSize(nodeType.getSize());
        node.getNodeData().getAttributes().setValue("community", String.valueOf(nodeType.communityID));
        node.getNodeData().setColor(nodeType.getR() , nodeType.getG() , nodeType.getB());

        if (isDirected)
            directedGraph.addNode(node);
        else
            undirectedGraph.addNode(node);
        return node;
    }

    protected Node getNodeOrNew(String id) {
        if (isDirected) {
            Node node = directedGraph.getNode(id);
            if (node == null)
                node = this.newNode(id);
            return node;
        } else {
            Node node = undirectedGraph.getNode(id);
            if (node == null)
                node = this.newNode(id);
            return node;
        }
    }

    protected Node getNodeOrNew(NodeType nodeType) {
        if (isDirected) {
            Node node = directedGraph.getNode(nodeType.getId());
            if (node == null)
                node = this.newNode(nodeType);
            return node;
        } else {
            Node node = undirectedGraph.getNode(nodeType.getId());
            if (node == null)
                node = this.newNode(nodeType);
            return node;
        }
    }

    public abstract void buildGraph(Object inputGraph);

}
