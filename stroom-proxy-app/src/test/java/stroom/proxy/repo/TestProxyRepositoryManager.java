package stroom.proxy.repo;

import stroom.proxy.util.io.StreamUtil;
import stroom.proxy.util.scheduler.Scheduler;
import stroom.proxy.util.test.StroomUnitTest;
import stroom.proxy.util.zip.StroomZipFile;
import stroom.proxy.util.zip.StroomZipOutputStream;
import stroom.proxy.util.zip.StroomZipOutputStreamUtil;
import stroom.proxy.util.zip.StroomZipRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestProxyRepositoryManager extends StroomUnitTest {

    @Before
    public void setup() {
        clearTestDir();
    }

    @Test
    public void testRolling() throws IOException {
        final ProxyRepositoryManager proxyRepositoryManager = new ProxyRepositoryManager();
        proxyRepositoryManager.setRepoDir(getCurrentTestDir().getCanonicalPath());
        proxyRepositoryManager.setScheduler(new Scheduler() {
            @Override
            public boolean execute() {
                // Always run
                return true;
            }

            @Override
            public Long getScheduleReferenceTime() {
                return null;
            }
        });

        final StroomZipRepository proxyRepository1 = proxyRepositoryManager.getActiveRepository();
        final StroomZipOutputStream stream1 = proxyRepository1.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stream1, StroomZipFile.SINGLE_DATA_ENTRY,
                "dummy".getBytes(StreamUtil.DEFAULT_CHARSET));
        stream1.close();

        // Roll this REPO
        proxyRepositoryManager.doRunWork();

        try {
            Thread.sleep(10L);
        } catch (InterruptedException e) {
            //ignore it as only 10ms
        }

        final StroomZipRepository proxyRepository2 = proxyRepositoryManager.getActiveRepository();
        final StroomZipOutputStream stream2 = proxyRepository2.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stream2, StroomZipFile.SINGLE_DATA_ENTRY,
                "dummy".getBytes(StreamUtil.DEFAULT_CHARSET));
        stream2.close();

        // Roll this REPO
        proxyRepositoryManager.doRunWork();

        Assert.assertEquals(2, proxyRepositoryManager.getReadableRepository().size());
    }

    @Test
    public void testNonRolling() throws IOException {
        final ProxyRepositoryManager proxyRepositoryManager = new ProxyRepositoryManager();
        proxyRepositoryManager.setRepoDir(getCurrentTestDir().getCanonicalPath());
        proxyRepositoryManager.setScheduler(null);

        final StroomZipRepository proxyRepository1 = proxyRepositoryManager.getActiveRepository();
        final StroomZipOutputStream stream1 = proxyRepository1.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stream1, StroomZipFile.SINGLE_DATA_ENTRY,
                "dummy".getBytes(StreamUtil.DEFAULT_CHARSET));
        stream1.close();

        // Nothing happens
        proxyRepositoryManager.doRunWork();

        // Same Repo
        final StroomZipRepository proxyRepository2 = proxyRepositoryManager.getActiveRepository();
        final StroomZipOutputStream stream2 = proxyRepository2.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stream2, StroomZipFile.SINGLE_DATA_ENTRY,
                "dummy".getBytes(StreamUtil.DEFAULT_CHARSET));
        stream2.close();

        // Nothing happens
        proxyRepositoryManager.doRunWork();

        Assert.assertEquals(1, proxyRepositoryManager.getReadableRepository().size());

    }
}
