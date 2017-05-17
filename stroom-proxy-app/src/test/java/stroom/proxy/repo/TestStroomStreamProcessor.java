package stroom.proxy.repo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import stroom.proxy.StroomStatusCode;
import stroom.proxy.handler.StroomStreamException;
import stroom.proxy.handler.StroomStreamHandler;
import stroom.proxy.handler.StroomStreamProcessor;
import stroom.proxy.util.io.StreamUtil;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;
import stroom.proxy.util.test.StroomUnitTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomStreamProcessor extends StroomUnitTest {
    @Test
    public void testSimple() throws Exception {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                "Sample Data".getBytes(StreamUtil.DEFAULT_CHARSET));

        final HeaderMap headerMap = new HeaderMap();
        headerMap.put("TEST", "VALUE");

        final byte[] buffer = new byte[1000];

        final Path zipFile = getCurrentTestPath().resolve("test.zip");

        final StroomZipOutputStream stroomZipOutputStream = new StroomZipOutputStreamImpl(zipFile);
        final List<StroomStreamHandler> list = new ArrayList<StroomStreamHandler>();
        list.add(StroomStreamHandlerUtil.createStroomStreamHandler(stroomZipOutputStream));
        final StroomStreamProcessor stroomStreamProcessor = new StroomStreamProcessor(headerMap, list, buffer, "test");

        stroomStreamProcessor.process(byteArrayInputStream, "");

        stroomZipOutputStream.close();

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("StreamSize:11\nTEST:VALUE\n",
                StreamUtil.streamToString(stroomZipFile.getInputStream("001", StroomZipFileType.Meta)));
        Assert.assertEquals("Sample Data",
                StreamUtil.streamToString(stroomZipFile.getInputStream("001", StroomZipFileType.Data)));
        stroomZipFile.close();
    }

    @Test
    public void testGZIPErrorSimple() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        gzipOutputStream.write("Sample Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        gzipOutputStream.close();
        final byte[] fullData = byteArrayOutputStream.toByteArray();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullData, 0, fullData.length - 10);

        final HeaderMap headerMap = new HeaderMap();
        headerMap.put("TEST", "VALUE");
        headerMap.put("Compression", "GZIP");

        final byte[] buffer = new byte[1000];

        final Path zipFile = getCurrentTestPath().resolve("test.zip");

        final StroomZipOutputStream stroomZipOutputStream = new StroomZipOutputStreamImpl(zipFile);
        final List<StroomStreamHandler> list = new ArrayList<StroomStreamHandler>();
        list.add(StroomStreamHandlerUtil.createStroomStreamHandler(stroomZipOutputStream));
        final StroomStreamProcessor stroomStreamProcessor = new StroomStreamProcessor(headerMap, list, buffer, "test");

        try {
            stroomStreamProcessor.process(byteArrayInputStream, "");
            stroomZipOutputStream.close();
            final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
            final String msg = StreamUtil.streamToString(stroomZipFile.getInputStream("001", StroomZipFileType.Meta));

            stroomZipFile.close();
            Assert.fail("expecting error but wrote - " + msg);
        } catch (final StroomStreamException ex) {
            Assert.assertEquals(StroomStatusCode.COMPRESSED_STREAM_INVALID, ex.getStroomStatusCode());
        }
    }

    @Test
    public void testZIPErrorSimple() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        zipOutputStream.putNextEntry(new ZipEntry("001.hdr"));
        zipOutputStream.write("Feed:FEED".getBytes(StreamUtil.DEFAULT_CHARSET));
        zipOutputStream.closeEntry();
        zipOutputStream.putNextEntry(new ZipEntry("001.dat"));
        zipOutputStream.write("Sample Data".getBytes(StreamUtil.DEFAULT_CHARSET));
        zipOutputStream.closeEntry();
        zipOutputStream.close();
        final byte[] fullData = byteArrayOutputStream.toByteArray();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullData, 0, fullData.length / 2);

        final HeaderMap headerMap = new HeaderMap();
        headerMap.put("TEST", "VALUE");
        headerMap.put("Compression", "ZIP");

        final byte[] buffer = new byte[1000];

        final Path zipFile = getCurrentTestPath().resolve("test.zip");

        final StroomZipOutputStream stroomZipOutputStream = new StroomZipOutputStreamImpl(zipFile);
        final List<StroomStreamHandler> list = new ArrayList<StroomStreamHandler>();
        list.add(StroomStreamHandlerUtil.createStroomStreamHandler(stroomZipOutputStream));
        final StroomStreamProcessor stroomStreamProcessor = new StroomStreamProcessor(headerMap, list, buffer, "test");

        try {
            stroomStreamProcessor.process(byteArrayInputStream, "");
            stroomZipOutputStream.close();

            final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
            final String msg = StreamUtil.streamToString(stroomZipFile.getInputStream("001", StroomZipFileType.Data));

            stroomZipFile.close();
            Assert.fail("expecting error but wrote - " + msg);
        } catch (final StroomStreamException ex) {
            Assert.assertEquals(StroomStatusCode.COMPRESSED_STREAM_INVALID, ex.getStroomStatusCode());
        }
    }

    @Test
    public void testZIPNoEntries() throws Exception {
        final byte[] fullData = StreamUtil
                .streamToBuffer(
                        getClass().getClassLoader().getResourceAsStream("stroom/proxy/repo/BlankZip.zip"))
                .toByteArray();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(fullData, 0, fullData.length / 2);

        final HeaderMap headerMap = new HeaderMap();
        headerMap.put("TEST", "VALUE");
        headerMap.put("Compression", "ZIP");

        final byte[] buffer = new byte[1000];

        final Path zipFile = getCurrentTestPath().resolve("test.zip");

        final StroomZipOutputStream stroomZipOutputStream = new StroomZipOutputStreamImpl(zipFile);
        final List<StroomStreamHandler> list = new ArrayList<>();
        list.add(StroomStreamHandlerUtil.createStroomStreamHandler(stroomZipOutputStream));
        final StroomStreamProcessor stroomStreamProcessor = new StroomStreamProcessor(headerMap, list, buffer, "test");

        stroomStreamProcessor.process(byteArrayInputStream, "");
        stroomZipOutputStream.close();

        Assert.assertFalse("Blank zips should get ignored", Files.isRegularFile(zipFile));
    }

    @Test
    public void testOrder1() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 1; i <= 10; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");
        doCheckOrder(byteArrayOutputStream, zipFile);

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("1.txt", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "1.txt", "TEST:VALUE");
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("2.txt", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "2.txt", "TEST:VALUE");

        assertMeta(stroomZipFile, "2.txt", "TEST:VALUE");

        stroomZipFile.close();
    }

    @Test
    public void testOrder2() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 1; i <= 10; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry(i + ".meta"));
            zipOutputStream.write(("META:VALUE" + i).getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");
        doCheckOrder(byteArrayOutputStream, zipFile);

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("1", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "1", "META:VALUE1");
        assertMeta(stroomZipFile, "1", "TEST:VALUE");
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("2", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "2", "META:VALUE2");
        assertMeta(stroomZipFile, "2", "TEST:VALUE");

        stroomZipFile.close();
    }

    @Test
    public void testOrder3() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 1; i <= 10; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry(i + ".hdr"));
            zipOutputStream.write(("META:VALUE" + i).getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");
        doCheckOrder(byteArrayOutputStream, zipFile);

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("1", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "1", "META:VALUE1");
        assertMeta(stroomZipFile, "1", "TEST:VALUE");
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("2", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "2", "META:VALUE2");
        assertMeta(stroomZipFile, "2", "TEST:VALUE");

        stroomZipFile.close();
    }

    @Test
    public void testOrder4() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 10; i > 0; i--) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".hdr"));
            zipOutputStream.write(("META:VALUE" + i).getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");
        doCheckOrder(byteArrayOutputStream, zipFile);

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("1", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "1", "META:VALUE1");
        assertMeta(stroomZipFile, "1", "TEST:VALUE");
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("2", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "2", "META:VALUE2");
        assertMeta(stroomZipFile, "2", "TEST:VALUE");

        stroomZipFile.close();
    }

    @Test
    public void testOrder5_Pass() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 10; i > 0; i--) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".hdr"));
            zipOutputStream.write(("META:VALUE" + i).getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        for (int i = 10; i > 0; i--) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();

        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");
        doCheckOrder(byteArrayOutputStream, zipFile);

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("1", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "1", "META:VALUE1");
        assertMeta(stroomZipFile, "1", "TEST:VALUE");
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("2", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "2", "META:VALUE2");
        assertMeta(stroomZipFile, "2", "TEST:VALUE");

        stroomZipFile.close();
    }

    @Test
    public void testOrder5_PassDueToHeaderBuffer() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 10; i > 0; i--) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".hdr"));
            zipOutputStream.write(("META:VALUE" + i).getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        for (int i = 1; i <= 10; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();

        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");

        doCheckOrder(byteArrayOutputStream, zipFile);

        final StroomZipFile stroomZipFile = new StroomZipFile(zipFile);
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("1", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "1", "META:VALUE1");
        assertMeta(stroomZipFile, "1", "TEST:VALUE");
        Assert.assertEquals("data", StreamUtil.streamToString(stroomZipFile.getInputStream("2", StroomZipFileType.Data)));
        assertMeta(stroomZipFile, "2", "META:VALUE2");
        assertMeta(stroomZipFile, "2", "TEST:VALUE");

        stroomZipFile.close();
    }

    @Test
    public void testOrder5_FailDueToHeaderBufferNotUsed() throws Exception {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        for (int i = 10; i > 0; i--) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".hdr"));
            zipOutputStream.write(("streamSize:1\nMETA:VALUE" + i).getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        }
        for (int i = 1; i <= 10; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(i + ".txt"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();

        }
        zipOutputStream.close();

        final Path zipFile = getCurrentTestPath().resolve("test.zip");

        doCheckOrder(byteArrayOutputStream, zipFile, true);
    }

    void assertMeta(final StroomZipFile stroomZipFile, final String baseName, final String expectedMeta) throws IOException {
        final String fullMeta = StreamUtil.streamToString(stroomZipFile.getInputStream(baseName, StroomZipFileType.Meta));
        Assert.assertTrue("Expecting " + expectedMeta + " in " + fullMeta, fullMeta.contains(expectedMeta));

    }

    private void doCheckOrder(final ByteArrayOutputStream byteArrayOutputStream, final Path zipFile)
            throws IOException {
        doCheckOrder(byteArrayOutputStream, zipFile, false);
    }

    private void doCheckOrder(final ByteArrayOutputStream byteArrayOutputStream, final Path zipFile, final boolean fail)
            throws IOException {
        final HeaderMap headerMap = new HeaderMap();
        headerMap.put("TEST", "VALUE");
        headerMap.put("Compression", "ZIP");
        final byte[] buffer = new byte[1000];

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        final StroomZipOutputStream stroomZipOutputStream = new StroomZipOutputStreamImpl(zipFile);
        final List<StroomStreamHandler> list = new ArrayList<>();
        list.add(StroomStreamHandlerUtil.createStroomStreamHandler(stroomZipOutputStream));
        list.add(StroomStreamHandlerUtil.createStroomStreamOrderCheck());

        try {
            final StroomStreamProcessor stroomStreamProcessor = new StroomStreamProcessor(headerMap, list, buffer, "test");

            stroomStreamProcessor.process(byteArrayInputStream, "");
            if (fail) {
                Assert.fail("Expecting a fail");
            }
        } catch (final Exception ex) {
            if (!fail) {
                throw ex;
            }
        }
        stroomZipOutputStream.close();
    }
}
