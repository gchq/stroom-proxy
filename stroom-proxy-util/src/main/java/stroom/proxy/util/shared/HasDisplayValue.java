package stroom.proxy.util.shared;

/**
 * <p>
 * Used by classes that have a string key and value (e.g. used to populate drop
 * downs etc).
 * </p>
 */
public interface HasDisplayValue {
    /**
     * The string label/description of this object.
     */
    String getDisplayValue();
}
