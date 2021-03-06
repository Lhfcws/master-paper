package paper;

import com.google.common.reflect.TypeToken;
import com.yeezhao.commons.util.*;
import com.yeezhao.commons.util.serialize.GsonSerializer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.conf.Configuration;
import paper.community.*;
import paper.community.model.*;
import paper.community.model.util.GraphFilter;
import paper.query.WeiboContentScanSpark;
import paper.query.WeiboUserScanSpark;
import paper.render.*;
import paper.tag.CommFilterTag;
import paper.tag.Evaluator;
import paper.tag.TfIdfCalculator;
import paper.tag.TfWdCalculator;
import paper.tag.tagger.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class CommunityRunner implements CliRunner {
    static final Type freqDistStrType = new TypeToken<FreqDist<String>>() {
    }.getType();

    // CLI PARAMS
    public static final String PARAM_CLI_UID = "uid";
    public static final String PARAM_CLI_TOPNCOMM = "topNComm";
    public static final String PARAM_CLI_TOPNDOTS = "topNDots";
    public static final String PARAM_CLI_TOPNKOL = "topNKol";

    public static final String PARAM_NOSCANUSER = "noscanuser";
    public static final String PARAM_NOSCANCONTENT = "nocontent";
    public static final String PARAM_NODETECTION = "nodetect";
    public static final String PARAM_NOTAG = "notag";
    public static final String PARAM_NOCOMMTAG = "nocommtag";
    public static final String PARAM_NORENDER = "norender";

    public static List<String> NOT_OPT_PARAMS = Arrays.asList(new String[]{
            PARAM_CLI_TOPNCOMM, PARAM_CLI_TOPNKOL, PARAM_CLI_UID, PARAM_CLI_TOPNDOTS
    });

    // FILE PARAMS
    public static final String ROOT = "/tmp/community/";
    public static final String USER_FILE = ROOT + "%s-userinfo.txt";
    public static final String REL_FILE = ROOT + "%s-relation.txt";
    public static final String CONTENTUID_FILE = ROOT + "%s-contentuid.txt";
    public static final String CONTENT_FILE = ROOT + "%s-content.txt";
    public static final String GRAPHML_FILE = ROOT + "%s-relation.graphml";
    public static final String PYRESULT_FILE = ROOT + "%s-pyresult.txt";
    public static final String GEXF_FILE = ROOT + "%s-relation.gexf";
    public static final String TAG_FILE = ROOT + "%s-tags.txt";
    public static final String CONTENTTAG_FILE = ROOT + "%s-contenttags.txt";
    public static final String ATTRTAG_FILE = ROOT + "%s-attrtags.txt";
    public static final String COMMTAG_FILE = ROOT + "%s-commtags.txt";

    // UTIL PARAMS
    protected Configuration conf = MLLibConfiguration.getInstance();
    protected FileSystemHelper fs = FileSystemHelper.getInstance(conf);

    protected static List<Tagger> taggers;

    // MEMBERs
    protected Communities communities;
    protected int topNComm = 15;
    protected int topNKol = 10;
    protected int topTag = 15;
    protected int topNDots = 1000;
    protected List<String> opts;
    protected String theUserID;
    protected AdvHashMap<String, WeiboUser> allUsers;
    protected UserRelations userRelations;
    protected UserContent userContent;

    public CommunityRunner() {
        clear();
        taggers = Arrays.asList(new Tagger[]{
                ContentTagger.getInstance()
        });
    }

    public CommunityRunner clear() {
        this.communities = new Communities();
        this.opts = new ArrayList<>();
        this.allUsers = new AdvHashMap<>();
        this.userRelations = new UserRelations();
        this.userContent = new UserContent();
        System.gc();
        return this;
    }

    public CommunityRunner setOpts(List<String> opts) {
        this.opts = opts;
        return this;
    }

    public CommunityRunner setTheUserID(String theUserID) {
        this.theUserID = theUserID;
        return this;
    }

    public void setTopNKol(int topNKol) {
        this.topNKol = topNKol;
    }

    public void setTopNComm(int topNComm) {
        this.topNComm = topNComm;
    }

    public void setTopNDots(int topNDots) {
        this.topNDots = topNDots;
    }

    public void run() throws Exception {
        if (!fs.existFile(ROOT)) {
            fs.mkdirs(ROOT);
            fs.mkdirsLocal(ROOT);
            System.out.println("[RUN] mkdir root: " + ROOT);
        }

        scanUser();
        communityDetect();
        calcCommUserWeight();
        truncCommunities();
        scanContent();
        tagging();
        render();

        System.out.println("CommunityRunner done.");
    }

    /**
     * Scan ES to get the followees.
     *
     * @return
     * @throws IOException
     */
    protected CommunityRunner scanUser() throws IOException {
        System.out.println("[INFO] Scan users.");
        if (!opts.contains(PARAM_NOSCANUSER)) {
            WeiboUserScanSpark scanner = new WeiboUserScanSpark();
            scanner.run(
                    theUserID,
                    String.format(USER_FILE, theUserID),
                    String.format(REL_FILE, theUserID)
            );
        }

        // Load relations
        AdvFile.loadFileInDelimitLine(fs.getHDFSFileInputStream(String.format(REL_FILE, theUserID)), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] sarr = s.split("\t");
                if (sarr.length >= 2) {
                    String source = sarr[0];
                    String[] targets = sarr[1].split(StringUtil.STR_DELIMIT_1ST);
                    userRelations.put(source, Arrays.asList(targets));
                }
            }
        });

        final GraphFilter graphFilter = new GraphFilter();
        userRelations = graphFilter.filter(userRelations, topNDots * topNComm);

        // Load userinfo
        AdvFile.loadFileInDelimitLine(fs.getHDFSFileInputStream(String.format(USER_FILE, theUserID)), new ILineParser() {
            @Override
            public void parseLine(String s) {
                WeiboUser weiboUser = GsonSerializer.fromJson(s, WeiboUser.class);
                if (graphFilter.existUsers.contains(weiboUser.id)) {
                    weiboUser.weight = 1.0 * graphFilter.weights.get(weiboUser.id);
                    allUsers.put(weiboUser.id, weiboUser);
                }
            }
        });

        System.out.println("[LOAD] user size: " + allUsers.size());
        System.out.println("[LOAD] Load userRelations : " + userRelations.totalSize());

        return this;
    }

    /**
     * Invoke python igraph to run community detection
     *
     * @return
     * @throws Exception
     */
    protected CommunityRunner communityDetect() throws Exception {
        System.out.println("[INFO] Detect communities.");

        String pyresFile = String.format(PYRESULT_FILE, theUserID);
        String graphmlFile = String.format(GRAPHML_FILE, theUserID);

        if (!opts.contains(PARAM_NODETECTION)) {
            System.out.println("[COMMUNITY] generate local tmp graphml file");
            GraphmlDrawer graphmlDrawer = new GraphmlDrawer(conf, CommunityGraphDrawer.GRAPHML_FILE, false);
            graphmlDrawer.buildGraph(userRelations);
            graphmlDrawer.startLayout();
            graphmlDrawer.export(graphmlFile);
            graphmlDrawer.stopLayout();

            CommDetectPythonInvoker invoker = new CommDetectPythonInvoker();
            invoker.run(
                    graphmlFile,
                    pyresFile
            );
        }

        List<String> list = AdvFile.readLines(new FileInputStream(pyresFile));
        String json = list.get(0);
        Map<String, Integer> clusterMap = GsonSerializer.fromJson(json, new TypeToken<Map<String, Integer>>() {
        }.getType());
        for (Map.Entry<String, Integer> entry : clusterMap.entrySet()) {
            int cid = entry.getValue();
            String uid = entry.getKey();

            // build community
            this.communities.setDefault(cid).getCommunity(cid)
                    .addUser(uid, allUsers.get(uid));
        }

        return this;
    }

    /**
     * Trunc, lookup kol, filter rubbish
     *
     * @return
     */
    protected CommunityRunner truncCommunities() {
        System.out.println("[INFO] trunc communities");
        RubbishCommunityRecognizer rubbishCommunityRecognizer = new RubbishCommunityRecognizer();
        KOLLookup kolLookup = new KOLLookup();

        List<Community> list = new ArrayList<>(this.communities.getAllCommunities());
        Collections.sort(list, new Comparator<Community>() {
            @Override
            public int compare(Community o1, Community o2) {
                Integer i1 = o1.users.size();
                Integer i2 = o2.users.size();

                return i2.compareTo(i1);
            }
        });

        AdvHashMap<Integer, Community> cmap = new AdvHashMap<>();
        AdvHashMap<String, Integer> rmap = new AdvHashMap<>();
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            Community community = list.get(i);
            // lookup kols
            kolLookup.lookup(community, topNKol);
            System.out.println("[INFO] Looked up " + community.id + " kol size: " + community.kols.size() + ",  " + community.kols);
            // filter rubbish community
            if (rubbishCommunityRecognizer.isRubbish(community)) {
//                System.out.println("[RUBBISH] " + community.id + ", kols: " + community.kols);
                continue;
            }

            List<WeiboUser> weiboUsers = new ArrayList<>(community.users.values());
            Collections.sort(weiboUsers, new Comparator<WeiboUser>() {
                @Override
                public int compare(WeiboUser o1, WeiboUser o2) {
                    return o2.weight.compareTo(o1.weight);
                }
            });
            if (weiboUsers.size() > topNDots) {
                weiboUsers = weiboUsers.subList(0, topNDots);
                community.users = new HashMap<>();
                for (WeiboUser weiboUser : weiboUsers)
                    community.users.put(weiboUser.id, weiboUser);
            }

            cmap.put(community.id, community);
            for (WeiboUser weiboUser : community.users.values()) {
                rmap.put(weiboUser.id, community.id);
            }

            // set color
            community.color = ColorBuilder.colorPool[count];
            if (count++ == topNComm) break;

        }
        this.communities.setCommunityList(cmap);
        this.communities.setRevIndex(rmap);

        System.gc();

        return this;
    }

    /**
     * Calculate community user weight
     *
     * @return
     */
    protected CommunityRunner calcCommUserWeight() {
        for (Map.Entry<String, List<String>> entry : userRelations.entrySet()) {
            int srcCommID = this.communities.getCommIDByUser(entry.getKey());
            if (srcCommID != -1 && entry.getValue() != null)
                for (String dest : entry.getValue()) {
                    int cid = this.communities.getCommIDByUser(dest);
                    if (srcCommID == cid) {
                        this.communities.getCommunity(cid).incUserWeight(dest, 1);
                    }
                }
        }

        return this;
    }

    /**
     * Scan HBase to get user contents.
     *
     * @return
     */
    protected CommunityRunner scanContent() throws IOException {
        System.out.println("[INFO] scan content");
        if (!opts.contains(PARAM_NOSCANCONTENT)) {
            String contentUIDFile = String.format(CONTENTUID_FILE, theUserID);
            System.out.println("[RUN] write tmp content uid file : " + contentUIDFile);

            fs.deleteFile(contentUIDFile);
            BatchWriter batchWriter = new BatchWriter(fs.getHDFSFileOutputStream(contentUIDFile));
            for (Community community : this.communities.getAllCommunities()) {
                for (WeiboUser weiboUser : community.users.values()) {
                    batchWriter.writeWithCache(weiboUser.id + "\n");
                }
            }
            batchWriter.flushNClose();

            System.out.println("[RUN] weiboContentScanSpark");
            WeiboContentScanSpark weiboContentScanSpark = new WeiboContentScanSpark();
            weiboContentScanSpark.run(
                    contentUIDFile,
                    String.format(CONTENT_FILE, theUserID)
            );
        }

//        try {
//            System.out.println("[RUN] read contents.");
//            AdvFile.loadFileInRawLines(fs.getHDFSFileInputStream(String.format(CONTENT_FILE, theUserID)), new ILineParser() {
//                @Override
//                public void parseLine(String s) {
//                    String[] arr = s.split("\t");
//                    if (arr.length == 2) {
//                        userContent.add(arr[0], arr[1]);
//                    }
//                }
//            });
//        } catch (Exception e) {
//            System.err.println("[ERROR] No content available.");
//        }

        return this;
    }

    protected CommunityRunner tagging() throws IOException {
        System.out.println("[INFO] tagging");

        if (!opts.contains(PARAM_NOTAG)) {
            System.out.println("[RUN] ContentTaggerSpark");
            ContentTaggerSpark contentTaggerSpark = new ContentTaggerSpark();
            contentTaggerSpark.run(
                    String.format(CONTENT_FILE, theUserID),
                    String.format(CONTENTTAG_FILE, theUserID)
            );

            System.out.println("[RUN] AttrTaggerSpark");
            AttrTaggerSpark attrTaggerSpark = new AttrTaggerSpark();
            attrTaggerSpark.run(
                    String.format(USER_FILE, theUserID),
                    String.format(ATTRTAG_FILE, theUserID)
            );

            System.out.println("[RUN] Merge tags");
        }

        if (!opts.contains(PARAM_NOCOMMTAG)) {
            contentTags(String.format(CONTENTTAG_FILE, theUserID));
            attrTags(String.format(ATTRTAG_FILE, theUserID));
        }

        return this;
    }

    /**
     * @param output
     * @throws IOException
     */
    public void attrTags(String output) throws IOException {
        AdvFile.loadFileInRawLines(fs.getHDFSFileInputStream(String.format(ATTRTAG_FILE, theUserID)), new ILineParser() {
            @Override
            public void parseLine(String s) {
                String[] sarr = s.split("\t");
                if (sarr.length == 2) {
                    FreqDist<String> freqDist = GsonSerializer.deserialize(sarr[1], freqDistStrType);
                    Community community = communities.getCommByUser(sarr[0]);
                    if (community != null) {
                        // TODO 这里有个坑,我把user的tags覆盖掉了,因为懒,只是用来跑demo.
                        community.users.get(sarr[0]).tags = freqDist;
                    }
                }
            }
        });

        if (!opts.contains(PARAM_NOCOMMTAG)) {
            System.out.println("[RUN] Calculate comm attr tags and write to file.");
            String outputFile = output;
//            String outputFile = String.format(COMMTAG_FILE, theUserID);

            BatchWriter batchWriter = new BatchWriter(new FileOutputStream(outputFile));
            TfIdfCalculator tfIdfCalculator = new TfIdfCalculator();
            for (Community community : this.communities.getAllCommunities()) {
                TfWdCalculator tfWdCalculator = new TfWdCalculator();
                tfWdCalculator.setWeiboUsers(community.users.values());
                community.attrTags = tfWdCalculator.calc(topTag);
                tfIdfCalculator.addDoc(community.id, community.attrTags);
            }
            tfIdfCalculator.calc();
            List<Community> communities = new ArrayList<>(this.communities.getAllCommunities());
            for (Community community : communities) {
                TfIdfCalculator.Doc doc = tfIdfCalculator.getDoc(community.id);
                community.attrTags = doc.getTfIdf();
            }

            for (int i = 0; i < communities.size(); i++) {
                Community community = communities.get(i);
                List<Map.Entry<String, Double>> entries = community.attrTags.sortValues(false);
                entries = CollectionUtil.subList(entries, topTag);
                System.out.println("[DEBUG] " + community.id + " " + community.attrTags);

                StringBuilder sb = new StringBuilder().append(community.id).append("\t")
                        .append("#" + ColorBuilder.toRGBStr(community.color)).append("\t");
                if (entries != null && !entries.isEmpty()) {
                    for (Map.Entry<String, Double> entry : entries)
                        sb.append(entry.getKey()).append("#").append(entry.getValue()).append("$");
                    sb.setLength(sb.length() - 1);
                }

                batchWriter.writeWithCache(sb.append("\n").toString());
            }
            batchWriter.flushNClose();
        }
    }

    public void contentTags(String output) throws IOException {
        int ct = 20;
        if (!opts.contains(PARAM_NOCOMMTAG)) {

            System.out.println("[RUN] Calculate comm content tags and write to file.");
            String outputFile = output;
//            String outputFile = String.format(COMMTAG_FILE, theUserID);

            try {
                for (Community community : this.communities.getAllCommunities()) {
                    for (WeiboUser u : community.users.values()) {
//                        u.tags.clear();
                        community.evaluator.addSimple(u.tags);
                    }
                }
                AdvFile.loadFileInRawLines(fs.getHDFSFileInputStream(String.format(CONTENTTAG_FILE, theUserID)), new ILineParser() {
                    @Override
                    public void parseLine(String s) {
                        String[] sarr = s.split("\t");
                        if (sarr.length == 2) {
                            FreqDist<String> freqDist = GsonSerializer.deserialize(sarr[1], freqDistStrType);
                            Community community = communities.getCommByUser(sarr[0]);
                            if (community != null) {
                                community.users.get(sarr[0]).tags.merge(freqDist);
                                community.evaluator.addSimple(freqDist);
                            }
                        }
                    }
                }, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<DoubleDist<String>> communityTags = new ArrayList<>();
            TfIdfCalculator tfIdfCalculator = new TfIdfCalculator();
            BatchWriter batchWriter = new BatchWriter(new FileOutputStream(outputFile));
            for (Community community : this.communities.getAllCommunities()) {
                TfWdCalculator tfWdCalculator = new TfWdCalculator();
                tfWdCalculator.setWeiboUsers(community.users.values());
                community.contentTags = tfWdCalculator.calc(ct);
                tfIdfCalculator.addDoc(community.id, community.contentTags);
            }
            tfIdfCalculator.calc();
            List<Community> communities = new ArrayList<>(this.communities.getAllCommunities());
            for (Community community : communities) {
                TfIdfCalculator.Doc doc = tfIdfCalculator.getDoc(community.id);
                community.contentTags = doc.getTfIdf();
                communityTags.add(community.contentTags);
            }
            List<List<Map.Entry<String, Double>>> res = CommFilterTag.filter(communityTags, ct);

            // output community tags
            for (int i = 0; i < communities.size(); i++) {
                List<Map.Entry<String, Double>> entries = res.get(i);
                Community community = communities.get(i);
                community.evaluator.tfwd = community.contentTags;
                community.evaluator.evaluate(ct);
                community.evaluator.printSimple(ct);
                System.out.println("[DEBUG] " + community.id + " " + community.contentTags);

                StringBuilder sb = new StringBuilder().append(community.id).append("\t")
                        .append("#" + ColorBuilder.toRGBStr(community.color)).append("\t");
                if (entries != null && !entries.isEmpty()) {
                    for (Map.Entry<String, Double> entry : entries)
                        sb.append(entry.getKey()).append("#").append(entry.getValue()).append("$");
                    sb.setLength(sb.length() - 1);
                }

                batchWriter.writeWithCache(sb.append("\n").toString());
            }
            System.out.println("[Variance] " + Evaluator.variances);

            batchWriter.flushNClose();
        }
    }

    protected CommunityRunner render() throws IOException {
        System.out.println("[INFO] render");

        System.out.println("[RUN] building graph for draw");

        CommunityGraphDrawer drawer = new WbGraphDrawer(conf);
        drawer.buildGraph(this.buildGraph());

        drawer.startLayout();
        System.out.println("[RUN] rendering into gexf");
        drawer.export(String.format(GEXF_FILE, theUserID));
        drawer.stopLayout();
        return this;
    }

    protected Map<NodeType, List<NodeType>> buildGraph() {
        AdvHashMap<NodeType, List<NodeType>> graph = new AdvHashMap<>();
        AdvHashMap<String, NodeType> nodeTypes = new AdvHashMap<>();
        int edge = 0;

        for (Map.Entry<String, List<String>> entry : userRelations.entrySet()) {
            if (nodeTypes.get(entry.getKey()) == null) {
                Community community = this.communities.getCommByUser(entry.getKey());
                if (community == null) continue;

                float w = (float) community.getUserWeight(entry.getKey());
                nodeTypes.put(entry.getKey(), new NodeType(
                        entry.getKey(),
                        community.color,
                        w
                ));
            }
            NodeType src = nodeTypes.get(entry.getKey());

            if (entry.getValue() != null)
                for (String dest : entry.getValue()) {
                    if (nodeTypes.get(dest) == null) {
                        Community community = this.communities.getCommByUser(dest);
                        if (community == null) continue;

                        nodeTypes.put(dest, new NodeType(
                                dest,
                                community.color,
//                                1
                                (float) community.getUserWeight(dest)
                        ));
                    }
                    graph.setDefault(src, new LinkedList<NodeType>());
                    NodeType dn = nodeTypes.get(dest);
                    graph.get(src).add(dn);
//                    src.setSize(src.getSize() + 1);
//                    dn.setSize(dn.getSize() + 1);
                    edge++;
                }
        }


        System.out.println("Total nodes : " + nodeTypes.size() + ", total edges: " + edge);
        System.out.println("Graph size: " + graph.size());

        return graph;
    }

    // ================= CliRunner
    @Override
    public Options initOptions() {
        Options options = new Options();
        options.addOption(PARAM_CLI_UID, true, "the user uid");
        options.addOption(PARAM_CLI_TOPNCOMM, true, "top N community to process");
        options.addOption(PARAM_CLI_TOPNKOL, true, "top N kol to present tags");
        options.addOption(PARAM_CLI_TOPNDOTS, true, "top N dots to render");

        options.addOption(PARAM_NOSCANUSER, false, "no scan user");
        options.addOption(PARAM_NODETECTION, false, "no community detection");
        options.addOption(PARAM_NOSCANCONTENT, false, "no scan content");
        options.addOption(PARAM_NOTAG, false, "dont tag users");
        options.addOption(PARAM_NOCOMMTAG, false, "dont tag community");
        options.addOption(PARAM_NORENDER, false, "no render");

        return options;
    }

    @Override
    public boolean validateOptions(CommandLine cmdl) {
        return cmdl.hasOption(PARAM_CLI_UID);
    }

    @Override
    public void start(CommandLine cmdl) {
        new File(ROOT).mkdirs();

        this.theUserID = cmdl.getOptionValue(PARAM_CLI_UID);
        if (cmdl.hasOption(PARAM_CLI_TOPNCOMM))
            this.topNComm = Integer.valueOf(cmdl.getOptionValue(PARAM_CLI_TOPNCOMM));
        if (cmdl.hasOption(PARAM_CLI_TOPNKOL))
            this.topNKol = Integer.valueOf(cmdl.getOptionValue(PARAM_CLI_TOPNKOL));

        try {
            this.opts = new ArrayList<>();

            for (Option option : cmdl.getOptions()) {
                String optStr = option.getOpt();
                if (!NOT_OPT_PARAMS.contains(optStr))
                    opts.add(optStr);
            }

            this.run();
            System.out.println("[RUN] DONE. Program exited.");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * CliRunner main
     */
    public static void main(String[] args) {
        AdvCli.initRunner(args, CommunityRunner.class.getSimpleName(), new CommunityRunner());
    }
}
