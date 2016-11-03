package stroom.proxy.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipException;

import stroom.proxy.util.zip.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import stroom.proxy.repo.ProxyRepositoryManager;
import stroom.proxy.util.io.CloseableUtil;
import stroom.proxy.util.io.FileUtil;
import stroom.proxy.util.io.StreamUtil;
import stroom.proxy.util.task.MonitorImpl;
import stroom.proxy.util.test.StroomTestUtil;
import stroom.proxy.util.test.StroomUnitTest;
import stroom.proxy.util.test.StroomExpectedException;
import stroom.proxy.util.thread.ThreadLocalBuffer;

public class TestProxyRepositoryReader extends StroomUnitTest {
    private ProxyRepositoryManager proxyRepositoryManager;
    private MockRequestHandler mockRequestHandler;
    private ProxyRepositoryReader proxyRepositoryReader;

    @Before
    public void setup(){
        clearTestDir();
    }

    private void init() throws IOException {
        proxyRepositoryManager = new ProxyRepositoryManager();
        proxyRepositoryManager.setRepoDir(StroomTestUtil.createUniqueTestDir(getCurrentTestDir()).getCanonicalPath());

        mockRequestHandler = new MockRequestHandler();
        proxyRepositoryReader = new ProxyRepositoryReader(new MonitorImpl()) {
            @Override
            public List<RequestHandler> createOutgoingRequestHandlerList() {
                final List<RequestHandler> list = new ArrayList<RequestHandler>();
                list.add(mockRequestHandler);
                return list;
            }

        };
        proxyRepositoryReader.setProxyRequestThreadLocalBuffer(new ThreadLocalBuffer());
        proxyRepositoryReader.setProxyRepositoryManager(proxyRepositoryManager);
    }

    @Test
    public void testSimpleNothingTodo() throws IOException {
        init();
        proxyRepositoryReader.doRunWork();
    }

    @Test
    public void testSimpleOneFile() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream = proxyRepository.getStroomZipOutputStream();

        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_META_ENTRY,
                "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_DATA_ENTRY,
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));

        Assert.assertEquals(1, proxyRepository.getFileCount());
        stroomZipOutputStream.close();

        Assert.assertTrue(proxyRepository.isFile(1));

        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(2, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleErrorCount());
        Assert.assertEquals(2, mockRequestHandler.getEntryNameList().size());

        final String sendHeader1 = new String(mockRequestHandler.getByteArray("001.meta"), StreamUtil.DEFAULT_CHARSET);
        Assert.assertTrue(sendHeader1.contains("Feed:TEST"));
        Assert.assertTrue(sendHeader1.contains("GUID:Z1"));

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));

    }

    @Test
    @StroomExpectedException(exception = IOException.class)
    public void testSimpleOneFileWithHeaderError() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream = proxyRepository.getStroomZipOutputStream();

        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_META_ENTRY,
                "GUID:Z1\nFeed:TEST".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_DATA_ENTRY,
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream.close();

        mockRequestHandler.setGenerateExceptionOnHeader(true);

        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleErrorCount());

        proxyRepository.clean();

        Assert.assertTrue("Expecting file to still be there", proxyRepository.isFile(1));

    }

    @Test
    @StroomExpectedException(exception = IOException.class)
    public void testSimpleOneFileWithDataError() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream = proxyRepository.getStroomZipOutputStream();

        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_META_ENTRY,
                "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_DATA_ENTRY,
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream.close();

        mockRequestHandler.setGenerateExceptionOnData(true);

        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleErrorCount());

        proxyRepository.clean();

        Assert.assertTrue("Expecting file to still be there", proxyRepository.isFile(1));
    }

    @Test
    @StroomExpectedException(exception = { IOException.class, ZipException.class })
    public void testSimpleOneFileWithBadZip() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream = proxyRepository.getStroomZipOutputStream();

        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_META_ENTRY,
                "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_DATA_ENTRY,
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream.close();

        final File zipFile = stroomZipOutputStream.getFinalFile();

        FileUtil.deleteFile(zipFile);

        FileOutputStream testStream = null;
        try {
            testStream = new FileOutputStream(zipFile);
            testStream.write("not a zip file".getBytes(StreamUtil.DEFAULT_CHARSET));
        } finally {
            CloseableUtil.close(testStream);
        }

        proxyRepositoryReader.doRunWork();

        // As the zip is corrupt we can't even read the header
        Assert.assertEquals(0, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleErrorCount());

        proxyRepository.clean();

        Assert.assertTrue("Expecting bad file", proxyRepository.isBad(1));
    }

    @Test
    public void testMultipleFilesWithContextSameFeed() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream1 = proxyRepository.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                new StroomZipEntry("req.header", "req", StroomZipFileType.Meta),
                "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                new StroomZipEntry("req.data", "req", StroomZipFileType.Data),
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                new StroomZipEntry("req.context", "req", StroomZipFileType.Context),
                "Test Context".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream1.close();

        final StroomZipOutputStream stroomZipOutputStream2 = proxyRepository.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream2,
                new StroomZipEntry("req.header", "req", StroomZipFileType.Meta),
                "Feed:TEST\nGUID:Z2\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream2,
                new StroomZipEntry("req.data", "req", StroomZipFileType.Data),
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream2,
                new StroomZipEntry("req.context", "req", StroomZipFileType.Context),
                "Test Context".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream2.close();

        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(6, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleErrorCount());

        Assert.assertEquals(Arrays.asList("001.meta", "001.ctx", "001.dat", "002.meta", "002.ctx", "002.dat"),
                mockRequestHandler.getEntryNameList());

        proxyRepository.clean();

        final String sendHeader2 = new String(mockRequestHandler.getByteArray("002.meta"), StreamUtil.DEFAULT_CHARSET);
        Assert.assertTrue(sendHeader2.contains("Feed:TEST"));
        Assert.assertTrue(sendHeader2.contains("GUID:Z2"));

        Assert.assertEquals("Test Data",
                new String(mockRequestHandler.getByteArray("001.dat"), StreamUtil.DEFAULT_CHARSET));
        Assert.assertEquals("Test Data",
                new String(mockRequestHandler.getByteArray("002.dat"), StreamUtil.DEFAULT_CHARSET));

        Assert.assertFalse(proxyRepository.isFile(1));
        Assert.assertFalse(proxyRepository.isFile(2));
    }

    @Test
    public void testMultipleFilesAtLimitWithContextSameFeed() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream1 = proxyRepository.getStroomZipOutputStream();
        for (int i = 0; i < 10; i++) {
            StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                    new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Meta),
                    "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
            StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                    new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Data),
                    "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
            StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                    new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Context),
                    "Test Context".getBytes(StreamUtil.DEFAULT_CHARSET));
        }
        stroomZipOutputStream1.close();

        final StroomZipOutputStream stroomZipOutputStream2 = proxyRepository.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream2,
                new StroomZipEntry("req.header", "req", StroomZipFileType.Meta),
                "Feed:TEST\nGUID:Z2\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream2,
                new StroomZipEntry("req.data", "req", StroomZipFileType.Data),
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream2,
                new StroomZipEntry("req.context", "req", StroomZipFileType.Context),
                "Test Context".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream2.close();

        final StroomZipOutputStream stroomZipOutputStream3 = proxyRepository.getStroomZipOutputStream();
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream3,
                new StroomZipEntry("req.header", "req", StroomZipFileType.Meta),
                "Feed:TEST\nGUID:Z2\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream3,
                new StroomZipEntry("req.data", "req", StroomZipFileType.Data),
                "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream3,
                new StroomZipEntry("req.context", "req", StroomZipFileType.Context),
                "Test Context".getBytes(StreamUtil.DEFAULT_CHARSET));
        stroomZipOutputStream3.close();

        proxyRepositoryReader.setMaxAggregation(10);
        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(2, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(2, mockRequestHandler.getHandleFooterCount());

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));
        Assert.assertFalse(proxyRepository.isFile(2));
    }

    @Test
    public void testMultipleFilesAtSizeLimitSameFeed() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        for (int a = 0; a < 5; a++) {
            final StroomZipOutputStream stroomZipOutputStream1 = proxyRepository.getStroomZipOutputStream();
            for (int i = 0; i < 10; i++) {
                StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                        new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Meta),
                        "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
                StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                        new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Data),
                        "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
            }
            stroomZipOutputStream1.close();
        }

        proxyRepositoryReader.setMaxAggregation(10000);
        proxyRepositoryReader.setMaxStreamSize(10L);
        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(5, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(5, mockRequestHandler.getHandleFooterCount());

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));
        Assert.assertFalse(proxyRepository.isFile(2));
    }

    @Test
    public void testMultipleFilesAtSizeLimitSameFeed2() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        for (int a = 0; a < 5; a++) {
            final StroomZipOutputStream stroomZipOutputStream1 = proxyRepository.getStroomZipOutputStream();
            for (int i = 0; i < 10; i++) {
                StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                        new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Meta),
                        "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
                StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                        new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Data),
                        "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
            }
            stroomZipOutputStream1.close();
        }

        proxyRepositoryReader.setMaxAggregation(10000);
        proxyRepositoryReader.setMaxStreamSize(100000L);
        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleFooterCount());

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));
        Assert.assertFalse(proxyRepository.isFile(2));
    }

    @Test
    public void testMultipleFilesAtCountLimitSameFeed() throws IOException {
        init();

        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        for (int a = 0; a < 5; a++) {
            final StroomZipOutputStream stroomZipOutputStream1 = proxyRepository.getStroomZipOutputStream();
            for (int i = 0; i < 10; i++) {
                StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                        new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Meta),
                        "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
                StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream1,
                        new StroomZipEntry(null, String.valueOf(i), StroomZipFileType.Data),
                        "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));
            }
            stroomZipOutputStream1.close();
        }

        proxyRepositoryReader.setMaxAggregation(20);
        proxyRepositoryReader.setMaxStreamSize(null);
        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(3, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(3, mockRequestHandler.getHandleFooterCount());

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));
        Assert.assertFalse(proxyRepository.isFile(2));
    }

    @Test
    public void testSimpleOneFileTemplated() throws IOException {
        init();

        HeaderMap headerMap = new HeaderMap();
        headerMap.put("feed", "myFeed");
        headerMap.put("key1", "myKey1");
        headerMap.put("key2", "myKey2");
        headerMap.put("key3", "myKey3");

        //template should be case insensitive as far as key names go as the headermap is case insensitive
        final String zipFilenameTemplate = "${FEED}_${key2}_${kEy1}_${keyNotInMeta}_${Key3}";
        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream = proxyRepository.getStroomZipOutputStream(headerMap, zipFilenameTemplate);

        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_META_ENTRY,
                                              "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_DATA_ENTRY,
                                              "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));

        Assert.assertEquals(1, proxyRepository.getFileCount());
        stroomZipOutputStream.close();

        String filename = proxyRepository.getZipFiles().iterator().next().getName();
        final String delim = StroomZipRepository.DEFAULT_ZIP_FILENAME_DELIMITER;

        Assert.assertEquals("001" + delim + "myFeed_myKey2_myKey1__myKey3.zip", filename);

        Assert.assertTrue(proxyRepository.isFile(1));

        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(2, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleErrorCount());
        Assert.assertEquals(2, mockRequestHandler.getEntryNameList().size());

        final String sendHeader1 = new String(mockRequestHandler.getByteArray("001.meta"), StreamUtil.DEFAULT_CHARSET);
        Assert.assertTrue(sendHeader1.contains("Feed:TEST"));
        Assert.assertTrue(sendHeader1.contains("GUID:Z1"));

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));

    }

    @Test
    public void testSimpleOneFileEmptyTemplate() throws IOException {
        init();

        HeaderMap headerMap = new HeaderMap();
        headerMap.put("feed", "myFeed");
        headerMap.put("key1", "myKey1");
        headerMap.put("key2", "myKey2");
        headerMap.put("key3", "myKey3");

        //template should be case insensitive as far as key names go as the headermap is case insensitive
        final String zipFilenameTemplate = "";
        final StroomZipRepository proxyRepository = proxyRepositoryManager.getActiveRepository();

        final StroomZipOutputStream stroomZipOutputStream = proxyRepository.getStroomZipOutputStream(headerMap, zipFilenameTemplate);

        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_META_ENTRY,
                                              "Feed:TEST\nGUID:Z1\n".getBytes(StreamUtil.DEFAULT_CHARSET));
        StroomZipOutputStreamUtil.addSimpleEntry(stroomZipOutputStream, StroomZipFile.SINGLE_DATA_ENTRY,
                                              "Test Data".getBytes(StreamUtil.DEFAULT_CHARSET));

        Assert.assertEquals(1, proxyRepository.getFileCount());
        stroomZipOutputStream.close();

        String filename = proxyRepository.getZipFiles().iterator().next().getName();

        Assert.assertEquals("001.zip", filename);

        Assert.assertTrue(proxyRepository.isFile(1));

        proxyRepositoryReader.doRunWork();

        Assert.assertEquals(1, mockRequestHandler.getHandleHeaderCount());
        Assert.assertEquals(1, mockRequestHandler.getHandleFooterCount());
        Assert.assertEquals(2, mockRequestHandler.getHandleEntryCount());
        Assert.assertEquals(0, mockRequestHandler.getHandleErrorCount());
        Assert.assertEquals(2, mockRequestHandler.getEntryNameList().size());

        final String sendHeader1 = new String(mockRequestHandler.getByteArray("001.meta"), StreamUtil.DEFAULT_CHARSET);
        Assert.assertTrue(sendHeader1.contains("Feed:TEST"));
        Assert.assertTrue(sendHeader1.contains("GUID:Z1"));

        proxyRepository.clean();

        Assert.assertFalse(proxyRepository.isFile(1));

    }
}
