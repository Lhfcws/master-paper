package com.datatub.iresearch.analyz.text.sentiment.shorttext.utils;

import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ISerialSntClassifier;
import com.datatub.iresearch.analyz.text.sentiment.shorttext.classfiers.ASntILClassifier;
import com.datatub.iresearch.analyz.util.JXMLQuery;
import com.datatub.iresearch.analyz.util.load.Dicts;
import com.datatub.iresearch.analyz.util.load.DictsLoader;
import com.datatub.iresearch.analyz.util.load.DualFileLoader;
import org.apache.commons.configuration.ConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Temp for mllib train and test.
 * Hornbill has a more universal way to load classifiers.
 *
 * @author lhfcws
 * @since 15/11/20.
 */
public class SntClassfierLoader {
    private static Map<String, ASntILClassifier> sntILClassifiers = null;
    private static List<ISerialSntClassifier> aSerialSntClassifierList = null;

    @Deprecated
    /**
     * TODO user sentiment/serial-classifier-list.xml config
     *
     * @return
     * @throws ConfigurationException
     */
    private static synchronized Map<String, ASntILClassifier> loadSntILClassifiers(boolean force) {
        if (sntILClassifiers == null || force) {
            sntILClassifiers = new ConcurrentHashMap<String, ASntILClassifier>();

//            Configuration conf = MLLibConfiguration.getInstance();

            DualFileLoader.loadDualXMLFile("sentiment/serial-classifier-list.xml", new DualFileLoader.IJXMLQueryParser() {
                @Override
                public void parse(JXMLQuery jxmlQuery) {
                    DictsLoader.loadSntAccModels(false);
                    for (JXMLQuery q : jxmlQuery.select("classifier")) {
                        try {
                            ASntILClassifier sntILClassifier = reflectSntILCLassifier(
                                    q.children("class").text()
                            );
                            sntILClassifier.update(Dicts.sntAccurateModelMap);
                            sntILClassifiers.put(
                                    q.children("name").text(),
                                    sntILClassifier
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("[DEBUG] " + Dicts.sntAccurateModelMap);
                    System.out.println("[DEBUG] " + sntILClassifiers);
                }
            });
//            InputStream is = conf.getConfResourceAsInputStream("sentiment/serial-classifier-list.xml");
//            JXMLQuery jxmlQuery = JXMLQuery.load(is);
//            sntILClassifiers.put(RandomForestClassifier.class.getSimpleName(), new RandomForestClassifier());
//            sntILClassifiers.put(LinearRegressionClassifier.class.getSimpleName(), new LinearRegressionClassifier());
        }
        return sntILClassifiers;
    }

    public static synchronized List<ISerialSntClassifier> loadSerialSntClassifiers(boolean force) {
        if (aSerialSntClassifierList == null || force) {
            aSerialSntClassifierList = new ArrayList<ISerialSntClassifier>();
//            Configuration conf = MLLibConfiguration.getInstance();
//            InputStream is = conf.getConfResourceAsInputStream("sentiment/serial-classifier-list.xml");
//            JXMLQuery jxmlQuery = JXMLQuery.load(is);

            DualFileLoader.loadDualXMLFile("sentiment/serial-classifier-list.xml", new DualFileLoader.IJXMLQueryParser() {
                @Override
                public void parse(JXMLQuery jxmlQuery) {
                    DictsLoader.loadSntAccModels(false);
                    for (JXMLQuery q : jxmlQuery.select("classifier")) {
                        try {
                            ISerialSntClassifier iSerialSntClassifier = reflectSerialSntClassifier(
                                    q.children("class").text()
                            );
                            aSerialSntClassifierList.add(iSerialSntClassifier);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }
        return aSerialSntClassifierList;
    }

    private static ASntILClassifier reflectSntILCLassifier(String classname) throws Exception {
        return (ASntILClassifier) reflectClass(classname);
    }

    private static ISerialSntClassifier reflectSerialSntClassifier(String classname) throws Exception {
        return (ISerialSntClassifier) reflectClass(classname);
    }

    private static Object reflectClass(String classname) throws Exception {
        return  Class.forName(classname).getConstructor().newInstance();
    }

    /*************
     * TEST
     */
    public static void main(String[] args) throws Exception {
        System.out.println(
//                loadSntILClassifiers(false)
                loadSerialSntClassifiers(false)
        );
    }
}
