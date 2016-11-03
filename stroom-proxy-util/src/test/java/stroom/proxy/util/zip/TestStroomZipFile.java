package stroom.proxy.util.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.io.CloseableUtil;
import stroom.proxy.util.io.StreamUtil;
import stroom.proxy.util.test.StroomTestUtil;
import stroom.proxy.util.test.StroomUnitTest;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomZipFile extends StroomUnitTest {
    @Test
    public void testRealZip1() throws IOException {
        File testDir = getCurrentTestDir();
        Assert.assertTrue(testDir.exists());
        final File uniqueTestDir = StroomTestUtil.createUniqueTestDir(testDir);
        Assert.assertTrue(uniqueTestDir.exists());
        final File file = File.createTempFile("TestStroomZipFile", ".zip", uniqueTestDir );
        System.out.println(file.getAbsolutePath());
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));

            zipOutputStream.putNextEntry(new ZipEntry("test/test.dat"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        } finally {
            CloseableUtil.close(zipOutputStream);
        }

        StroomZipFile stroomZipFile = null;
        try {
            stroomZipFile = new StroomZipFile(file);

            Assert.assertEquals(stroomZipFile.getStroomZipNameSet().getBaseNameSet(),
                    new HashSet<>(Arrays.asList("test/test.dat")));

            Assert.assertNotNull(stroomZipFile.getInputStream("test/test.dat", StroomZipFileType.Data));
            Assert.assertNull(stroomZipFile.getInputStream("test/test.dat", StroomZipFileType.Context));

        } finally {
            CloseableUtil.close(stroomZipFile);

            Assert.assertTrue(file.delete());
        }
    }

    @Test
    public void testRealZip2() throws IOException {
        final File file = File.createTempFile("TestStroomZipFile", ".zip",
                StroomTestUtil.createUniqueTestDir(getCurrentTestDir()));
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));

            zipOutputStream.putNextEntry(new ZipEntry("request.hdr"));
            zipOutputStream.write("header".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("request.dat"));
            zipOutputStream.write("data".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
            zipOutputStream.putNextEntry(new ZipEntry("request.ctx"));
            zipOutputStream.write("context".getBytes(StreamUtil.DEFAULT_CHARSET));
            zipOutputStream.closeEntry();
        } finally {
            CloseableUtil.close(zipOutputStream);
        }

        StroomZipFile stroomZipFile = null;
        try {
            stroomZipFile = new StroomZipFile(file);

            Assert.assertEquals(stroomZipFile.getStroomZipNameSet().getBaseNameSet(),
                    new HashSet<>(Arrays.asList("request")));

            Assert.assertNotNull(stroomZipFile.getInputStream("request", StroomZipFileType.Data));
            Assert.assertNotNull(stroomZipFile.getInputStream("request", StroomZipFileType.Meta));
            Assert.assertNotNull(stroomZipFile.getInputStream("request", StroomZipFileType.Context));

        } finally {
            CloseableUtil.close(stroomZipFile);
            Assert.assertTrue(file.delete());
        }
    }
}
