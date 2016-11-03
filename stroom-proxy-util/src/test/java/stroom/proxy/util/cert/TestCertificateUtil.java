package stroom.proxy.util.cert;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomUnitTest;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestCertificateUtil extends StroomUnitTest {
    @Test
    public void testSpaceInCN() {
        final String dn = "CN=John Smith (johnsmith), OU=ouCode1, OU=ouCode2, O=oValue, C=GB";

        Assert.assertEquals("CN=John Smith (johnsmith),OU=ouCode1,OU=ouCode2,O=oValue,C=GB",
                CertificateUtil.dnToRfc2253(dn));
        Assert.assertEquals("John Smith (johnsmith)", CertificateUtil.extractCNFromDN(dn));
        Assert.assertEquals("johnsmith", CertificateUtil.extractUserIdFromCN(CertificateUtil.extractCNFromDN(dn)));

        final Pattern pattern = Pattern.compile("CN=[^ ]+ [^ ]+ \\(?([a-zA-Z0-9]+)\\)?");
        Assert.assertEquals("johnsmith", CertificateUtil.extractUserIdFromDN(dn, pattern));
    }
}
