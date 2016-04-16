package paper.render;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lhfcws
 * @since 16/1/5.
 */
public class NodeType {
    protected String id = null;
    protected String name = " ";
    protected float size = 1.0f;
    protected float alpha = 0.7f;
    protected float r = 233f;
    protected float g = 23f;
    protected float b = 0f;
    protected int communityID = 0;
    protected Map<String, Object> attrs;

    public NodeType() {
        this.attrs = new HashMap<>();
    }

    public NodeType(String id) {
        this();
        this.id = id;
    }

    public NodeType(String id, Color color) {
        this(id);
        this.setColor(color);
    }

    public NodeType(String id, Color color, float size) {
        this(id, color);
        this.size = size;
    }

    public NodeType(String id, Color color, float size, int cid) {
        this(id, color, size);
        this.communityID = cid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public void setColor(Color c) {
        setR(c.getRed());
        setG(c.getGreen());
        setB(c.getBlue());
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, Object> attrs) {
        this.attrs = attrs;
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
