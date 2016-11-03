package stroom.proxy.util.zip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomZipFile_RealExample {
    @Test
    public void testRealZip1() throws IOException {
        File sourceFile = new File("./src/test/resources/stroom/util/zip/BlankZip.zip");
        StroomZipFile stroomZipFile = new StroomZipFile(sourceFile);

        ArrayList<String> list = new ArrayList<>(stroomZipFile.getStroomZipNameSet().getBaseNameList());
        Collections.sort(list);

        stroomZipFile.close();
    }

}
