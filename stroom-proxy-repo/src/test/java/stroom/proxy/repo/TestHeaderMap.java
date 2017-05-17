package stroom.proxy.repo;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class TestHeaderMap {
    @Test
    public void testSimple() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.put("person", "person1");

        Assert.assertEquals("person1", headerMap.get("person"));
        Assert.assertEquals("person1", headerMap.get("PERSON"));

        Assert.assertEquals(new HashSet<>(Arrays.asList("person")), headerMap.keySet());

        headerMap.put("PERSON", "person2");

        Assert.assertEquals("person2", headerMap.get("person"));
        Assert.assertEquals("person2", headerMap.get("PERSON"));

        Assert.assertEquals(new HashSet<>(Arrays.asList("PERSON")), headerMap.keySet());

        HeaderMap headerMap2 = new HeaderMap();
        headerMap2.put("persOn", "person3");
        headerMap2.put("persOn1", "person4");

        headerMap.putAll(headerMap2);

        Assert.assertEquals(new HashSet<>(Arrays.asList("persOn", "persOn1")), headerMap.keySet());

    }

    @Test
    public void testRemove() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.put("a", "a1");
        headerMap.put("B", "b1");

        headerMap.removeAll(Arrays.asList("A", "b"));

        Assert.assertEquals(0, headerMap.size());
    }

    @Test
    public void testReadWrite() throws IOException {
        HeaderMap headerMap = new HeaderMap();
        headerMap.read("b:2\na:1\nz\n".getBytes(CharsetConstants.DEFAULT_CHARSET));
        Assert.assertEquals("1", headerMap.get("a"));
        Assert.assertEquals("2", headerMap.get("b"));
        Assert.assertNull(headerMap.get("z"));

        Assert.assertEquals("a:1\nb:2\nz\n", new String(headerMap.toByteArray(), CharsetConstants.DEFAULT_CHARSET));
    }

    @Test
    public void testtoString() throws IOException {
        HeaderMap headerMap = new HeaderMap();
        headerMap.read("b:2\na:1\nz\n".getBytes(CharsetConstants.DEFAULT_CHARSET));

        // HeaderMap's are used in log output and so check that they do output
        // the map values.
        Assert.assertTrue(headerMap.toString(), headerMap.toString().contains("b=2"));
        Assert.assertTrue(headerMap.toString(), headerMap.toString().contains("a=1"));
    }

    @Test
    public void testTrim() {
        HeaderMap headerMap = new HeaderMap();
        headerMap.put(" person ", "person1");
        headerMap.put("PERSON", "person2");
        headerMap.put("FOOBAR", "1");
        headerMap.put("F OOBAR", "2");
        headerMap.put(" foobar ", " 3 ");

        Assert.assertEquals("person2", headerMap.get("PERSON "));
        Assert.assertEquals("3", headerMap.get("FOOBAR"));
    }
}
