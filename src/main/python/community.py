print('Start python')
import sys
from igraph import *

print('Import done')


def getNodeList(fn):
    ret = []
    with open(fn, "r") as fp:
        for line in fp.readlines():
            if line.count("<node id=\"") > 0:
                nodeid = line.strip().replace("<node id=\"", "").replace("\">", "")
                ret.append(nodeid)
    return ret


def community(fn, output):
    g = Graph.Read_GraphML(fn)
    nodes = getNodeList(fn)
    # cl = g.community_walktrap(steps=5)
    cl = g.community_multilevel()
    # cl = g.community_fastgreedy()
    # clusters = list(cl.as_clustering().membership)
    clusters = list(cl.membership)

    ret = {}
    for i, node in enumerate(nodes):
        ret[node] = clusters[i]

    # print(ret)
    fw = open(output, "w")
    fw.write(str(ret))
    fw.flush()
    fw.close()

    print("python done")


community(sys.argv[1], sys.argv[2])
