package paper.community.model;

import com.yeezhao.commons.util.DoubleDist;
import paper.render.ColorBuilder;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class Community {
    public int id;
    public Color color = ColorBuilder.DEFAULT_COLOR;
    public Map<String, WeiboUser> users = new HashMap<>();
    public List<String> kols = new LinkedList<>();
    public DoubleDist<String> commTags = new DoubleDist<>();

    public Community(int id) {
        this.id = id;
    }

    public Community() {
    }

    public Community addUser(String uid, WeiboUser user) {
        this.users.put(uid, user);
        return this;
    }

    public Community setUserWeight(String uid, double weight) {
        WeiboUser weiboUser = users.get(uid);
        if (weiboUser != null) {
            weiboUser.weight = weight;
        }
        return this;
    }

    public Community incUserWeight(String uid, double weight) {
        WeiboUser weiboUser = users.get(uid);
        if (weiboUser != null) {
            weiboUser.weight += weight;
        }
        return this;
    }

    public double getUserWeight(String uid) {
        WeiboUser weiboUser = users.get(uid);
        if (weiboUser != null) {
            return weiboUser.weight;
        } else
            return 1.0;
    }
}
