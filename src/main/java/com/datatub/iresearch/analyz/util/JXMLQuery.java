package com.datatub.iresearch.analyz.util;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple XML Query object in JQuery-like style.
 * It is very helpful for configuration-xml and other small xml and need not depend on other 3rd package, but did not test its performance.
 * So be cautious when trying to use it in frequent xml parsing.
 *
 * @author lhfcws, runhao
 * @since 15/11/17.
 */
public class JXMLQuery implements Serializable {
    // ============= Members
    private Element element = null;

    // ============= Constructors
    public JXMLQuery() {
    }

    public JXMLQuery(Element ele) {
        this.element = ele;
    }


    // ============= Functions

    /**
     * Load xml file from inputstream
     *
     * @param inputStream
     * @return
     */
    public static JXMLQuery load(InputStream inputStream) throws Exception {
        JXMLQuery jxmlQuery = new JXMLQuery();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        jxmlQuery.element = document.getDocumentElement();
        jxmlQuery.element.normalize();
        return jxmlQuery;
    }

    /**
     * Select to get nodes of given xpath query.
     *
     * @param query
     * @return
     */
    public JXMLQueryList select(String query) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodeList = (NodeList) xPath.evaluate(query, element, XPathConstants.NODESET);
            List<JXMLQuery> res = new ArrayList<JXMLQuery>();
            for (int iter = 0; iter < nodeList.getLength(); iter++) {
                if (nodeList.item(iter).getNodeType() == Node.ELEMENT_NODE) {
                    res.add(new JXMLQuery((Element) nodeList.item(iter)));
                }
            }
            return new JXMLQueryList(res);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Root node of the tree.
     *
     * @return
     */
    public JXMLQuery root() {
        Element now = element;
        while (true) {
            try {
                Node node = now.getParentNode();
                if (node instanceof Element) {
                    Element elem = (Element) now.getParentNode();
                    if (elem.equals(now)) {
                        return new JXMLQuery(now);
                    }
                    now = elem;
                } else
                    return new JXMLQuery(now);
            } catch (ClassCastException e) {
                return new JXMLQuery(now);
            }
        }
    }

    /**
     * Parent node of this node.
     *
     * @return null if it is root node.
     */
    public JXMLQuery parent() {
        Node node = element.getParentNode();
        if (node instanceof Element) {
            return new JXMLQuery((Element) node);
        } else
            return null;
    }

    /**
     * Children list nodes of this node.
     *
     * @return empty list if no children.
     */
    public JXMLQueryList children() {
        List<JXMLQuery> children = new ArrayList<JXMLQuery>();
        NodeList childNodes = element.getChildNodes();
        for (int iter = 0; iter < childNodes.getLength(); iter++) {
            if (childNodes.item(iter).getNodeType() == Node.ELEMENT_NODE) {
                children.add(new JXMLQuery((Element) childNodes.item(iter)));
            }
        }
        return new JXMLQueryList(children);
    }

    /**
     * Children list nodes of this node.
     *
     * @return empty list if no children.
     */
    public JXMLQueryList children(String tagName) {
        List<JXMLQuery> children = new ArrayList<JXMLQuery>();
        NodeList childNodes = element.getChildNodes();
        for (int iter = 0; iter < childNodes.getLength(); iter++) {
            if (childNodes.item(iter).getNodeType() == Node.ELEMENT_NODE) {
                Element node = (Element) childNodes.item(iter);
                if (node.getTagName().equals(tagName)) {
                    children.add(new JXMLQuery(node));
                }
            }
        }
        return new JXMLQueryList(children);
    }

    /**
     * Get the index child of children list nodes of this node.
     *
     * @return empty list if no children.
     */
    protected JXMLQuery child(Integer index) {
        JXMLQuery child = null;
        NodeList childNodes = element.getChildNodes();
        int cnt = 0;
        for (Integer iter = 0; iter < childNodes.getLength(); iter++) {
            if (childNodes.item(iter).getNodeType() == Node.ELEMENT_NODE) {
                if (index.equals(cnt)) {
                    child = new JXMLQuery((Element) childNodes.item(iter));
                    break;
                } else cnt++;
            }
        }
        return child;
    }

    /**
     * Get tag name.
     *
     * @return
     */
    public String tagName() {
        return element.getTagName();
    }

//    public NamedNodeMap attributes(){
//        return element.getAttributes();
//    }

    /**
     * Return inner text of the current node.
     *
     * @return "" if it is an tag without inner text.
     */
    public String text() {
        return element.getTextContent();
    }

    /**
     * Return the xml string belonging to the current node.
     * i.e. <name>NAME</name>
     *
     * @return
     */
    public String xml() {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transFactory.newTransformer();
//            transformer.setOutputProperty("encoding", "utf-8");
            transformer.setOutputProperty("indent", "yes");

            DOMSource source = new DOMSource(this.element);
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Refer to the attribution of the current node by the given attrName.
     *
     * @param attrName
     * @return null if not exist.
     */
    public String attr(String attrName) {
        if (attrName == null || attrName.length() == 0)
            return null;
//            throw new IllegalArgumentException("Attribute name must not be empty");

        String attr = element.getAttribute(attrName.toLowerCase());
        return attr != null ? attr : null;
    }

    @Override
    public String toString() {
        return "JXMLQuery{" +
                "element=" + element +
                '}';
    }

    // ============= Static Functions

    // ============= Inner Class

    /**
     * List class of JXMLQuery
     */
    public static class JXMLQueryList extends JXMLQuery implements Iterable<JXMLQuery> {
        private List<JXMLQuery> jxmlQueries;

        public JXMLQueryList(List<JXMLQuery> jxmlQueries) {
            this.jxmlQueries = jxmlQueries;
        }

        @Override
        public JXMLQueryList select(String query) {
            List<JXMLQuery> list = new ArrayList<JXMLQuery>();
            for (JXMLQuery jxmlQuery : jxmlQueries) {
                if (jxmlQuery != null)
                list.addAll(jxmlQuery.select(query).jxmlQueries);
            }
            return new JXMLQueryList(list);
        }

        @Override
        public JXMLQueryList children() {
            List<JXMLQuery> list = new ArrayList<JXMLQuery>();
            for (JXMLQuery jxmlQuery : jxmlQueries) {
                if (jxmlQuery != null)
                    list.addAll(jxmlQuery.children().jxmlQueries);
            }
            return new JXMLQueryList(list);
        }

        @Override
        public JXMLQueryList children(String tagName) {
            List<JXMLQuery> list = new ArrayList<JXMLQuery>();
            for (JXMLQuery jxmlQuery : jxmlQueries) {
                if (jxmlQuery != null)
                    list.addAll(jxmlQuery.children(tagName).jxmlQueries);
            }
            return new JXMLQueryList(list);
        }

        @Override
        public Iterator<JXMLQuery> iterator() {
            return jxmlQueries.iterator();
        }
        
        public JXMLQuery at(int index) {
            return jxmlQueries.get(index);
        }

        /**
         * Return inner text of the current node.
         *
         * @return "" if it is an tag without inner text.
         */
        @Override
        public String text() {
            return at(0).text();
        }

        /**
         * Root node of the tree.
         *
         * @return
         */
        @Override
        public JXMLQuery root() {
            return at(0).root();
        }

        /**
         * Parent node of this node.
         *
         * @return null if it is root node.
         */
        @Override
        public JXMLQuery parent() {
            return at(0).parent();
        }

        /**
         * Get tag name.
         *
         * @return
         */
        @Override
        public String tagName() {
            return at(0).tagName();
        }

        /**
         * Refer to the attribution of the current node by the given attrName.
         *
         * @param attrName
         * @return null if not exist.
         */
        public String attr(String attrName) {
            return at(0).attr(attrName);
        }

        /**
         * Return the xml string belonging to the current node.
         * i.e. <name>NAME</name>
         *
         * @return
         */
        @Override
        public String xml() {
            StringBuilder sb = new StringBuilder();
            for (JXMLQuery jxmlQuery : jxmlQueries)
                sb.append(jxmlQuery.xml());
            return sb.toString();
        }
    }

    // ============= Test main
//    public static void main(String[] args) throws Exception {
//        MLLibConfiguration conf = MLLibConfiguration.getInstance();
//        InputStream is = conf.getConfResourceAsInputStream("sentiment/serial-classifier-list.xml");
//        JXMLQuery jxmlQuery = JXMLQuery.load(is);
//        for (JXMLQuery classifier : jxmlQuery.select("classifier")) {
//            System.out.println(classifier.children("desc").at(0).text());
//        }
//    }
}
