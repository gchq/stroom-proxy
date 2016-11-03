package stroom.proxy.util.zip;

import stroom.proxy.util.logging.StroomLogger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomFileNameUtil {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(TestStroomFileNameUtil.class);

    @Test
    public void testPad() {
        Assert.assertEquals("001", StroomFileNameUtil.getFilePathForId(1));
        Assert.assertEquals("999", StroomFileNameUtil.getFilePathForId(999));
        Assert.assertEquals("001/001000", StroomFileNameUtil.getFilePathForId(1000));
        Assert.assertEquals("001/001999", StroomFileNameUtil.getFilePathForId(1999));
        Assert.assertEquals("009/111/009111999", StroomFileNameUtil.getFilePathForId(9111999));

    }

    @Test
    public void testConstructFilename(){
        HeaderMap headerMap = new HeaderMap();
        headerMap.put("feed", "myFeed");
        headerMap.put("var1", "myVar1");

        final String[] delimiters = {"%", "¬", "|", "~", ":"};
        final String emptyTemplate = "";
        final String staticTemplate = "someStaticText";

        for (String delimiter : delimiters) {
            LOGGER.info("Using delimiter [%s]", delimiter);
            final String dynamicTemplate = "${var1}" + delimiter + "${feed}";

            final String extension1 = ".zip";
            final String extension2 = ".bad";

            Assert.assertEquals("001.zip.bad",
                                StroomFileNameUtil.constructFilename(delimiter, 1,
                                                                  emptyTemplate, headerMap, extension1, extension2));
            Assert.assertEquals("003/003000" + delimiter + "myVar1" + delimiter + "myFeed.zip",
                                StroomFileNameUtil.constructFilename(delimiter, 3000,
                                                                  dynamicTemplate, headerMap, extension1));
            Assert.assertEquals("003/003000.zip",
                                StroomFileNameUtil.constructFilename(delimiter, 3000,
                                                                  null, headerMap, extension1));
            Assert.assertEquals("003/003000" + delimiter + "someStaticText.zip",
                                StroomFileNameUtil.constructFilename(delimiter, 3000,
                                                                  staticTemplate, headerMap, extension1));
            Assert.assertEquals("003/003000" + delimiter + "someStaticText",
                                StroomFileNameUtil.constructFilename(delimiter, 3000,
                                                                  staticTemplate, headerMap));
        }
    }

}
