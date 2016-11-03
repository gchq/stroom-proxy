package stroom.proxy.util.zip;

public enum StroomZipFileType {
    Data(".dat"), Context(".ctx"), Meta(".meta"), Manifest(".mf");

    private final String extValue;

    private StroomZipFileType(final String extValue) {
        this.extValue = extValue;
    }

    public String getExtension() {
        return extValue;
    }

}
