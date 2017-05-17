package stroom.proxy.repo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Map that does not care about key case.
 */
public class HeaderMap implements Serializable, Map<String, String> {
    private static final long serialVersionUID = 4877407570072403322L;

    public final static String NAME = "headerMap";

    private static class CIString implements Comparable<CIString>, Serializable {
        private static final long serialVersionUID = 550532045010691235L;

        private String key;
        private String lowerKey;

        CIString(final String key) {
            this.key = key.trim();
            this.lowerKey = this.key.toLowerCase(CharsetConstants.DEFAULT_LOCALE);
        }

        @Override
        public int hashCode() {
            return lowerKey.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CIString)) {
                return false;
            }
            return key.equalsIgnoreCase(((CIString) obj).key);
        }

        @Override
        public int compareTo(final CIString o) {
            return lowerKey.compareTo(o.lowerKey);
        }

        @Override
        public String toString() {
            return key;
        }
    }

    private static class CIEntryAdaptor implements Entry<String, String> {
        private Entry<CIString, String> realEntry;

        private CIEntryAdaptor(final Entry<CIString, String> realEntry) {
            this.realEntry = realEntry;
        }

        @Override
        public String getKey() {
            return realEntry.getKey().key;
        }

        @Override
        public String getValue() {
            return realEntry.getValue();
        }

        @Override
        public String setValue(final String value) {
            return realEntry.setValue(value);
        }
    }

    private HashMap<CIString, String> realMap = new HashMap<>();

    private final static String HEADER_DELIMITER = ":";

    public void read(final InputStream inputStream, final boolean close) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CharsetConstants.DEFAULT_CHARSET));

        String line;
        while ((line = reader.readLine()) != null) {
            final int splitPos = line.indexOf(HEADER_DELIMITER);
            if (splitPos != -1) {
                final String key = line.substring(0, splitPos);
                final String value = line.substring(splitPos + 1);
                put(key, value);
            } else {
                put(line.trim(), null);
            }
        }

        if (close) {
            inputStream.close();
        }
    }

    public void read(final byte[] data) throws IOException {
        read(new ByteArrayInputStream(data), true);
    }

    public void write(final OutputStream outputStream, final boolean close) throws IOException {
        write(new OutputStreamWriter(outputStream, CharsetConstants.DEFAULT_CHARSET), close);
    }

    public void write(final Writer writer, final boolean close) throws IOException {
        try {
            final List<CIString> sortedKeys = new ArrayList<>(realMap.keySet());
            Collections.sort(sortedKeys);
            for (final CIString key : sortedKeys) {
                writer.write(key.key);
                final String value = realMap.get(key);
                if (value != null) {
                    writer.write(":");
                    writer.write(value);
                }
                writer.write("\n");
            }
        } finally {
            if (close) {
                writer.close();
            } else {
                writer.flush();
            }
        }
    }

    public byte[] toByteArray() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        write(byteArrayOutputStream, true);
        return byteArrayOutputStream.toByteArray();
    }

    public void removeAll(final Collection<String> keySet) {
        for (final String key : keySet) {
            remove(key);
        }
    }

    @Override
    public void clear() {
        realMap.clear();
    }

    @Override
    public boolean containsKey(final Object key) {
        return realMap.containsKey(new CIString((String) key));
    }

    @Override
    public boolean containsValue(final Object value) {
        return realMap.containsValue(value);
    }

    @Override
    public String get(final Object key) {
        return realMap.get(new CIString((String) key));
    }

    @Override
    public String getOrDefault(Object key, String defaultVal) {
        String val = realMap.get(new CIString((String) key));
        return val == null ? defaultVal : val;
    }

    @Override
    public boolean isEmpty() {
        return realMap.isEmpty();
    }

    public String computeIfAbsent(final String key, final Function<String, String> mappingFunction) {
        return realMap.computeIfAbsent(new CIString(key), k -> mappingFunction.apply(k.key));
    }

    @Override
    public String put(final String key, String value) {
        if (value != null) {
            value = value.trim();
        }
        final CIString newKey = new CIString(key);
        final String oldValue = realMap.remove(newKey);
        realMap.put(newKey, value);
        return oldValue;
    }

    @Override
    public String remove(final Object key) {
        return realMap.remove(new CIString((String) key));
    }

    @Override
    public int size() {
        return realMap.size();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<Entry<String, String>> entrySet() {
        final Set<Entry<String, String>> rtnSet = new HashSet<>();
        for (final Entry<CIString, String> entry : realMap.entrySet()) {
            rtnSet.add(new CIEntryAdaptor(entry));
        }
        return rtnSet;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Set<String> keySet() {
        final Set<String> rtnSet = new HashSet<>();
        for (final CIString entry : realMap.keySet()) {
            rtnSet.add(entry.key);
        }
        return rtnSet;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void putAll(final Map<? extends String, ? extends String> m) {
        for (final Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Collection<String> values() {
        return realMap.values();
    }

    @Override
    public String toString() {
        return realMap.toString();
    }

}
