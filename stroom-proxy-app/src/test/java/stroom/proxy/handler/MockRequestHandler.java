package stroom.proxy.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import stroom.proxy.util.zip.StroomZipEntry;

public class MockRequestHandler implements RequestHandler {
    private List<String> entryNameList = new ArrayList<>();
    private List<byte[]> byteArrayList = new ArrayList<>();
    private ByteArrayOutputStream byteArrayOutputStream = null;

    int handleErrorCount = 0;
    int handleFooterCount = 0;
    int handleHeaderCount = 0;
    int handleEntryCount = 0;

    boolean generateExceptionOnHeader = false;
    boolean generateExceptionOnData = false;

    @Override
    public void handleError() throws IOException {
        handleErrorCount++;
    }

    @Override
    public void handleFooter() throws IOException {
        handleFooterCount++;
    }

    @Override
    public void handleHeader() throws IOException {
        handleHeaderCount++;
        if (generateExceptionOnHeader) {
            throw new IOException("Mock Header Error");
        }
    }

    @Override
    public void validate() {
    }

    @Override
    public void handleEntryData(byte[] data, int off, int len) throws IOException {
        byteArrayOutputStream.write(data, off, len);

        if (generateExceptionOnData) {
            throw new IOException("Mock Data Error");
        }
    }

    @Override
    public void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException {
        handleEntryCount++;
        String fullName = stroomZipEntry.getFullName();
        entryNameList.add(fullName);
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public void handleEntryEnd() throws IOException {
        byteArrayList.add(byteArrayOutputStream.toByteArray());
        byteArrayOutputStream = null;
    }

    public List<String> getEntryNameList() {
        return entryNameList;
    }

    public List<byte[]> getByteArrayList() {
        return byteArrayList;
    }

    public byte[] getByteArray(String entryName) {
        return byteArrayList.get(entryNameList.indexOf(entryName));
    }

    public int getHandleErrorCount() {
        return handleErrorCount;
    }

    public int getHandleFooterCount() {
        return handleFooterCount;
    }

    public int getHandleHeaderCount() {
        return handleHeaderCount;
    }

    public int getHandleEntryCount() {
        return handleEntryCount;
    }

    public void setGenerateExceptionOnData(boolean generateExceptionOnData) {
        this.generateExceptionOnData = generateExceptionOnData;
    }

    public void setGenerateExceptionOnHeader(boolean generateExceptionOnHeader) {
        this.generateExceptionOnHeader = generateExceptionOnHeader;
    }

}
