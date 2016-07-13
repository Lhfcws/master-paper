package paper.test;

import com.yeezhao.hornbill.analyz.algo.text.keyword.KeywordExtractionClassifier;
import org.junit.Test;

/**
 * @author lhfcws
 * @since 16/7/13
 */
public class TestAny {
    @Test
    public void testKeyword() {
        KeywordExtractionClassifier keywordExtractionClassifier = new KeywordExtractionClassifier();
        String res = keywordExtractionClassifier.extract("画像标签的质量评估一直是个难题。社会化大众人工标注的标签可以借助一些机器提取关键词（即类似本文的内容标签抽取方法）或专家标注或主题词进行对比匹配来估算标签质量[]。因此，在没有业界公认较好的评估手段，以及缺乏专家资源和人力标注资源的情况下，本文采取人工后验的方式来评估社群标签质量。即社群标签抽取算法得到前五个标签后，人工取部分度数权值较大的用户去观察其账号信息和微博内容，判断标签是否一定程度上能代表这一部分用户。", 20);
        System.out.println(res);
    }
}
