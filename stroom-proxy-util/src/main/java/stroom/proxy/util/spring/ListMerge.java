package stroom.proxy.util.spring;

import java.util.List;

/**
 * <p>
 * Class used to merge lists in our spring configuration.
 * </p>
 */
public class ListMerge {
    private List<Object> source;
    private List<Object> target;

    public List<Object> getTarget() {
        return target;
    }

    public void setTarget(final List<Object> target) {
        this.target = target;
        peformMerge();
    }

    public List<Object> getSource() {
        return source;
    }

    public void setSource(final List<Object> list) {
        this.source = list;
        peformMerge();
    }

    private void peformMerge() {
        if (source != null && target != null) {
            target.addAll(source);
        }
    }
}
