package com.datatub.iresearch.analyz.ner.util;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TreepathReader {

	private static final Logger LOG = Logger.getLogger(TreepathReader.class);

	public static class Treepath {
		public String pathValue;

		public Treepath(String value) {
			this.pathValue = value;
		}

		public String toString() {
			return pathValue;
		}
	}

	private class NodeData {
		private Node node;
		private String nodeValue;

		public NodeData(Node node) {
			this.node = node;
			Element ele = (Element) node;
			nodeValue = ele.getAttribute("name");
			if (nodeValue.isEmpty())
				nodeValue = ele.getAttribute("key");
		}

		public Node getNode() {
			return node;
		}

		public boolean equals(Object obj) { // 在这个应用中只需要判断两个node是否完全一样。
			return obj == this;
		}

		public int hashCode() {
			return nodeValue.hashCode();
		}
	}

	private Document pathDoc;

	public TreepathReader(InputStream treepathXml) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = dbf.newDocumentBuilder();
			pathDoc = dombuilder.parse(treepathXml);
			if (pathDoc == null)
				return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Treepath> getAllPaths() {
		List<Treepath> paths = new LinkedList<Treepath>();
		List<NodeData> candidateNodes = new LinkedList<NodeData>();
		Set<NodeData> nodeSet = new HashSet<NodeData>(); // 包含所有的根节点、中间节点和叶节点
		NodeList propertyNodes = pathDoc.getElementsByTagName("property");
		for (int i = 0, l = propertyNodes.getLength(); i < l; i++) {
			if (propertyNodes.item(i).getNodeType() == Node.ELEMENT_NODE)
				candidateNodes.add(new NodeData(propertyNodes.item(i)));
		}
		getPathsUnderNode(candidateNodes, nodeSet);
		for (NodeData node : nodeSet) {
			paths.add(getPath(node));
		}
		return paths;
	}

	private void getPathsUnderNode(List<NodeData> iteratorNodes,
			Set<NodeData> allNodes) {
		while (!iteratorNodes.isEmpty()) {
			NodeData pnodeSex = iteratorNodes.remove(0);
			Node pnode = pnodeSex.getNode();
			allNodes.add(pnodeSex);
			if (pnode.hasChildNodes()) {
				NodeList cnodes = pnode.getChildNodes();
				for (int i = 0, l = cnodes.getLength(); i < l; i++) {
					if (cnodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
						NodeData cnode = new NodeData(cnodes.item(i));
						iteratorNodes.add(cnode);
						allNodes.add(cnode);
					}
				}
				getPathsUnderNode(iteratorNodes, allNodes);
			}
		}
	}

	/**
	 * 
	 * @param sexNode
	 * @return 如果xml格式错误，返回为null。
	 */
	private Treepath getPath(NodeData sexNode) {
		StringBuffer sb = new StringBuffer();
		Node node = sexNode.getNode();
		while (!node.getNodeName().equals("configuration")) {
			Element ele = (Element) node;
			String value = ele.getAttribute("name");
			if (value.isEmpty())
				value = ele.getAttribute("key");
			if (value.isEmpty()) {
				LOG.error("***error format while reading treepath node value.");
				return null;
			}
			sb.append(value).append("#");
			node = node.getParentNode();
		}
		String pathValue = sb.substring(0, sb.toString().length() - 1);
		String[] segs = pathValue.split("#");
		sb = new StringBuffer().append(segs[segs.length - 1]);
		for (int i = segs.length - 2; i >= 0; i--)
			sb.append("#").append(segs[i]);
		return new Treepath(sb.toString());
	}

}