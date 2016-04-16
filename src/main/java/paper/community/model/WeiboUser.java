package paper.community.model;

import com.yeezhao.commons.util.FreqDist;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/1/4.
 */
public class WeiboUser implements Serializable {
    public String id;
    public String city;
    public String area;
    public String vType;
    public List<String> school;
    public List<String> company;
    public String sex;
    public Integer birthYear;
    public Double weight = 1.0;
    public List<String> follows;
    public FreqDist<String> tags;

    public WeiboUser() {
        this.follows = new LinkedList<>();
        this.tags = new FreqDist<>();
    }

    @Override
    public String toString() {
        return "WeiboUser{" +
                "id='" + id + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", vType='" + vType + '\'' +
                ", school=" + school +
                ", company=" + company +
                ", sex='" + sex + '\'' +
                ", birthYear=" + birthYear +
                ", weight=" + weight +
                ", follows=" + follows +
                ", tags=" + tags +
                '}';
    }

    // ============= MAIN ===============

    /**************************************
     * Test main
     */
    public static void main(String[] args) {
    }
}
