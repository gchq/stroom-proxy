package stroom.proxy.repo;

import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.test.StroomUnitTest;
import stroom.proxy.util.zip.StroomZipRepository;
import stroom.proxy.util.zip.HeaderMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPathCreator extends StroomUnitTest {

    private static final StroomLogger LOGGER = StroomLogger.getLogger(TestPathCreator.class);

    final HeaderMap headerMap = new HeaderMap();

    @Before
    public void setup() {

        headerMap.put("feed", "myFeed");
        headerMap.put("type1", "mytype1");
        headerMap.put("type2", "mytype2");
    }

    @Test
    public void testFindVars() {
        final String[] vars = PathCreator.findVars("/temp/${feed}-FLAT/${pipe}_less-${uuid}/${searchId}");
        Assert.assertEquals(4, vars.length);
        Assert.assertEquals("feed", vars[0]);
        Assert.assertEquals("pipe", vars[1]);
        Assert.assertEquals("uuid", vars[2]);
        Assert.assertEquals("searchId", vars[3]);
    }


    @Test
    public void testReplace() {

        String template = "someText_${type1}_someText_${feed}_someText_${type2}_someText";

        String result = PathCreator.replace(template, headerMap, StroomZipRepository.MAX_FILENAME_LENGTH);

        LOGGER.info("result: %s", result);
        Assert.assertEquals("someText_mytype1_someText_myFeed_someText_mytype2_someText", result);
    }


    @Test
    public void testReplaceTooLong() {

        String template = "someText_${type1}_someText_${feed}_someText_${type2}_someText";

        String result = PathCreator.replace(template, headerMap, 55);

        //expanded text is a bit longer than 55 so redact the last var
        LOGGER.info("result: %s", result);
        Assert.assertEquals("someText_mytype1_someText_myFeed_someText__someText", result);
    }

    @Test
    public void testReplaceTooLong2() {

        String template = "someText_${type1}_someText_${feed}_someText_${type2}_someText";

        String result = PathCreator.replace(template, headerMap, 40);

        //expanded text is a longer than 40 so redact all the vars to fit inside 40
        LOGGER.info("result: %s", result);
        Assert.assertEquals("someText__someText__someText__someText", result);
    }

    @Test
    public void testReplaceTooLong3() {

        String template = "someText_${type1}_someText_${feed}_someText_${type2}_someText";

        String result = PathCreator.replace(template, headerMap, 10);

        //event after redacting all vars it is still too long so will return an empty string
        LOGGER.info("result: %s", result);
        Assert.assertEquals("", result);
    }
}
