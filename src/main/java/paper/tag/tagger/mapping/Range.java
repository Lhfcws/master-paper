package paper.tag.tagger.mapping;

/**
 * @author lhfcws
 * @since 16/4/27
 */
public class Range {
    protected int left;
    protected boolean leftInclude = true;
    protected int right;
    protected boolean rightInclude = true;
    protected String raw;

    public static Range parse(String s) throws Exception {
        s = s.trim();
        Range rm = new Range();
        rm.raw = s;
        if (s.charAt(0) == '(')
            rm.leftInclude = false;
        if (s.charAt(s.length() - 1) == ')')
            rm.rightInclude = false;

        s = s.substring(1, s.length() - 1);
        String[] arr = s.split(",");
        rm.left = Integer.parseInt(arr[0].trim());
        rm.right = Integer.parseInt(arr[1].trim());

        return rm;
    }

    @Override
    public String toString() {
        return raw;
    }

    public int getLeft() {
        return left;
    }

    public boolean isLeftInclude() {
        return leftInclude;
    }

    public int getRight() {
        return right;
    }

    public boolean isRightInclude() {
        return rightInclude;
    }

    public boolean in(int n) {
        if (n < left || n > right)
            return false;
        else if (n > left && n < right)
            return true;
        else if (n == left && leftInclude)
            return true;
        else if (n == right && rightInclude)
            return true;
        else
            return false;
    }


}
