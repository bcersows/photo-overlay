package de.bcersows.photooverlay.helper;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for file-related stuff.
 * 
 * @author BCE
 */
public final class FileHelper {
    private static final Logger LOG = LoggerFactory.getLogger(FileHelper.class);

    private static final String FILE_TYPE_GIF = "gif";
    private static final String FILE_TYPE_PNG = "png";
    private static final String FILE_TYPE_JPG = "jpg";
    private static final String[] VALID_FILE_TYPES = new String[] { FILE_TYPE_GIF, FILE_TYPE_JPG, FILE_TYPE_PNG };

    private FileHelper() {
        // nothing
    }

    public static Set<String> findImages(@Nonnull final File sourceFolder) {
        final Set<String> images = new HashSet<>();

        // if the folder exists and is readable, get all files (including from sub-folders)
        if (sourceFolder.exists() && sourceFolder.canRead()) {
            final IOFileFilter extensionFilter = new SuffixFileFilter(VALID_FILE_TYPES, IOCase.INSENSITIVE);
            final Collection<File> fileList = FileUtils.listFiles(sourceFolder, extensionFilter, DirectoryFileFilter.DIRECTORY);
            images.addAll(fileList.stream().parallel().map(File::getAbsolutePath).collect(Collectors.toSet()));
        }

        return images;
    }

}
