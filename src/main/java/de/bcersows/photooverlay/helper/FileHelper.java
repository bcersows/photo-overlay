package de.bcersows.photooverlay.helper;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

/**
 * Helper class for file-related stuff.
 * 
 * @author BCE
 */
public final class FileHelper {

    private static final String FILE_TYPE_GIF = "gif";
    private static final String FILE_TYPE_PNG = "png";
    private static final String FILE_TYPE_JPG = "jpg";
    private static final String[] VALID_FILE_TYPES = new String[] { FILE_TYPE_GIF, FILE_TYPE_JPG, FILE_TYPE_PNG };

    private FileHelper() {
        // nothing
    }

    /** Find all images in the given paths. **/
    public static Set<String> findImages(@Nonnull final List<String> folderPaths) {
        final Set<String> images = new HashSet<>();

        for (final String folderPath : folderPaths) {
            final File folder = new File(folderPath);
            // if the folder exists and is readable, get all files (including from sub-folders)
            if (folder.exists() && folder.canRead()) {
                final IOFileFilter extensionFilter = new SuffixFileFilter(VALID_FILE_TYPES, IOCase.INSENSITIVE);
                final Collection<File> fileList = FileUtils.listFiles(folder, extensionFilter, DirectoryFileFilter.DIRECTORY);
                images.addAll(fileList.stream().parallel().map(File::getAbsolutePath).collect(Collectors.toSet()));
            }
        }

        return images;
    }

    /**
     * Check if the given path is a folder and exists.
     */
    public static boolean isValidFolder(@Nullable final String folderPath) {
        // null values are never valid
        if (null == folderPath) {
            return false;
        }

        final File folder = new File(folderPath);
        return folder.exists() && folder.isDirectory();
    }

}
