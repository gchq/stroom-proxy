package stroom.proxy.util.shared;

public class TaskIdImpl implements TaskId {
    private static final long serialVersionUID = -8404944210149631124L;

    private String id;
    private TaskId parentId;

    /**
     * Do not use this constructor directly, instead please use TaskIdFactory.
     */
    public TaskIdImpl() {
        // Default constructor necessary for GWT serialisation.
    }

    /**
     * Do not use this constructor directly, instead please use TaskIdFactory.
     */
    public TaskIdImpl(final String id, final TaskId parentId) {
        this.id = id;
        this.parentId = parentId;
    }

    @Override
    public TaskId getParentId() {
        return parentId;
    }

    @Override
    public boolean hasAncestor(final TaskId id) {
        return recursiveEquals(id, parentId);
    }

    @Override
    public boolean isOrHasAncestor(final TaskId id) {
        return recursiveEquals(id, this);
    }

    private boolean recursiveEquals(final TaskId id, final TaskId ancestorId) {
        if (id == null || ancestorId == null) {
            return false;
        } else if (id.equals(ancestorId)) {
            return true;
        }

        return recursiveEquals(id, ancestorId.getParentId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TaskIdImpl)) {
            return false;
        }

        final TaskIdImpl taskId = (TaskIdImpl) o;
        return id.equals(taskId.id);
    }

    @Override
    public String toString() {
        return "{" + id + "}";
    }
}
