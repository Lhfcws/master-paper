package paper.community.model;

import com.yeezhao.commons.util.AdvHashMap;

import java.util.*;

/**
 * @author lhfcws
 * @since 16/4/10
 */
public class Communities {
    protected AdvHashMap<Integer, Community> communityList;
    protected AdvHashMap<String, Integer> revIndex;

    public Communities() {
        this.communityList = new AdvHashMap<>();
        this.revIndex = new AdvHashMap<>();
    }

    public AdvHashMap<Integer, Community> getCommunityList() {
        return communityList;
    }

    public void setCommunityList(AdvHashMap<Integer, Community> communityList) {
        this.communityList = communityList;
    }

    public AdvHashMap<String, Integer> getRevIndex() {
        return revIndex;
    }

    public void setRevIndex(AdvHashMap<String, Integer> revIndex) {
        this.revIndex = revIndex;
    }

    public Communities addCommunity(Community community) {
        this.communityList.put(community.id, community);
        for (String uid : community.users.keySet()) {
            this.revIndex.put(uid, community.id);
        }
        return this;
    }

    public Collection<Community> getAllCommunities() {
        return communityList.values();
    }

    public Community getCommunity(int cid) {
        return communityList.get(cid);
    }

    public Communities setDefault(int cid) {
        if (!this.communityList.containsKey(cid))
            this.communityList.put(cid, new Community(cid));
        return this;
    }

    public Integer getCommIDByUser(String uid) {
        Integer cid = this.revIndex.get(uid);
        if (cid == null) cid = -1;
        return cid;
    }

    public Community getCommByUser(String uid) {
        Integer cid = this.revIndex.get(uid);
        if (cid != null)
            return this.getCommunity(cid);
        else
            return null;
    }

    public boolean usersInSameComm(String uid1, String uid2) {
        int cid = getCommIDByUser(uid1);
        return (cid != -1) && (cid == getCommIDByUser(uid2));
    }
}
