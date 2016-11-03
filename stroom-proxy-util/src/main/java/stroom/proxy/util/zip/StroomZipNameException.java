package stroom.proxy.util.zip;

public class StroomZipNameException extends RuntimeException {
    private static final long serialVersionUID = 6550229574319866082L;

    public StroomZipNameException(String msg) {
        super(msg);
    }

    public static StroomZipNameException createDuplicateFileNameException(String fileName) {
        return new StroomZipNameException("Duplicate File " + fileName);
    }

    public static StroomZipNameException createOutOfOrderException(String fileName) {
        return new StroomZipNameException("File Name is out of order " + fileName);
    }
}
