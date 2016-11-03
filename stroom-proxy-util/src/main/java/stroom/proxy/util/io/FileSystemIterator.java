package stroom.proxy.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Iterator to recurse around a file tree. It will pick out lowest level files
 * first (like stored in a repository).
 */
public class FileSystemIterator implements Iterator<File>, Iterable<File> {
    private final File root;
    private final Pattern fileMatch;
    private final List<File> dirList;
    private final List<File> fileList;
    private Iterator<File> dirIterator = null;
    private Iterator<File> fileIterator = null;
    private FileSystemIterator subDirIterator = null;

    /**
     * Build a pattern for an extension like "zip". to match anything ending
     * with *.zip
     */
    public static Pattern buildSimpleExtensionPattern(String suffix) {
        return Pattern.compile(".*\\." + suffix);
    }

    public FileSystemIterator(File dir, Pattern afileMatch) {
        this.root = dir;
        this.fileMatch = afileMatch;
        dirList = new ArrayList<>();
        fileList = new ArrayList<>();

        if (root != null && root.isDirectory()) {
            String kids[] = root.list();
            if (kids != null) {
                for (String kid : kids) {
                    File kidFile = new File(root, kid);
                    if (kidFile.isDirectory()) {
                        dirList.add(kidFile);
                    } else {
                        if (fileMatch.matcher(kid).matches()) {
                            fileList.add(kidFile);
                        }
                    }
                }
            }
        }

        Collections.sort(dirList);
        Collections.sort(fileList);
    }

    private Iterator<File> getDirIterator() {
        if (dirIterator == null) {
            dirIterator = dirList.iterator();
        }
        return dirIterator;
    }

    private Iterator<File> getFileIterator() {
        if (fileIterator == null) {
            fileIterator = fileList.iterator();
        }
        return fileIterator;
    }

    @Override
    public boolean hasNext() {
        if (getFileIterator().hasNext()) {
            return true;
        }
        if (subDirIterator != null && subDirIterator.hasNext()) {
            return true;
        }
        subDirIterator = null;
        while (getDirIterator().hasNext()) {
            File subDir = getDirIterator().next();
            subDirIterator = new FileSystemIterator(subDir, fileMatch);
            if (subDirIterator.hasNext()) {
                return true;
            }
            subDirIterator = null;
        }
        // Run out of dirs
        return false;
    }

    @Override
    public File next() {
        if (getFileIterator().hasNext()) {
            return getFileIterator().next();
        }
        if (subDirIterator != null) {
            return subDirIterator.next();
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<File> iterator() {
        return this;
    }

}
