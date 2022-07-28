package net.dinglisch.android.appfactory.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.system.Os;

import androidx.annotation.Nullable;

import net.dinglisch.android.appfactory.utils.errors.Error;
import net.dinglisch.android.appfactory.utils.file.FileUtils;
import net.dinglisch.android.appfactory.utils.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BootstrapInstaller {

    private static final String LOG_TAG = "BootstrapInstaller";

    private static final String FILES_MAPPING_FILE = "libfiles.so";
    private static final String SYMLINKS_MAPPING_FILE = "libsymlinks.so";

    public static boolean installBootstrap(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();

            String prefixDirectoryPath = context.getFilesDir() + "/usr";

            Error error = FileUtils.deleteDirectoryFile("prefix directory", prefixDirectoryPath, true);
            if (error != null) {
                Logger.logError(LOG_TAG, error.getErrorLogString());
                return false;
            }

            File filesMappingFile = new File(applicationInfo.nativeLibraryDir, FILES_MAPPING_FILE);
            if (!filesMappingFile.exists()) {
                Logger.logError(LOG_TAG, "No FILES_MAPPING_FILE found at \"" + filesMappingFile.getAbsolutePath() +"\"");
                return false;
            }

            Logger.logError(LOG_TAG, "Installing bootstrap");
            BufferedReader reader = new BufferedReader(new FileReader(filesMappingFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("←");
                if (parts.length != 2) {
                    Logger.logError(LOG_TAG, "Malformed line " + line + " in FILES_MAPPING_FILE \"" + filesMappingFile.getAbsolutePath() +  "\"");
                    continue;
                }

                String oldPath = applicationInfo.nativeLibraryDir + "/" + parts[0];
                String newPath = prefixDirectoryPath + "/" + parts[1];

                if (!ensureDirectoryExists(new File(newPath).getParentFile()))
                    return false;

                Logger.logVerbose(LOG_TAG, "About to setup link: \"" + oldPath + "\" ← \"" + newPath + "\"");
                error = FileUtils.deleteFile("link destination", newPath, true);
                if (error != null) {
                    Logger.logError(LOG_TAG, error.getErrorLogString());
                    return false;
                }

                Os.symlink(oldPath, newPath);
            }

            File symlinksMappingFile = new File(applicationInfo.nativeLibraryDir, SYMLINKS_MAPPING_FILE);
            if (!symlinksMappingFile.exists()) {
                Logger.logError(LOG_TAG, "No SYMLINKS_MAPPING_FILE found at \"" + symlinksMappingFile.getAbsolutePath() + "\"");
            }

            reader = new BufferedReader(new FileReader(symlinksMappingFile));
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("←");
                if (parts.length != 2) {
                    Logger.logError(LOG_TAG, "Malformed line " + line + " in SYMLINKS_MAPPING_FILE \"" + symlinksMappingFile.getAbsolutePath() + "\"");
                    continue;
                }

                String oldPath = parts[0];
                String newPath = prefixDirectoryPath + "/" + parts[1];

                if (!ensureDirectoryExists(new File(newPath).getParentFile()))
                    return false;

                Logger.logVerbose(LOG_TAG, "About to setup link: \"" + oldPath + "\" ← \"" + newPath + "\"");
                error = FileUtils.deleteFile("link destination", newPath, true);
                if (error != null) {
                    Logger.logError(LOG_TAG, error.getErrorLogString());
                    return false;
                }
                Os.symlink(oldPath, newPath);
            }
        } catch (Throwable t) {
            Logger.logStackTraceWithMessage(LOG_TAG, "Failed to setup bootstrap", t);
            return false;
        }

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean ensureDirectoryExists(@Nullable File directory) {
        /*
        Error error = FileUtils.createDirectoryFile(directory != null ? directory.getAbsolutePath() : null);
        if (error != null) {
            Logger.logError(LOG_TAG, error.getErrorLogString());
            return false;
        }
        */

        if (directory == null)
            return true;
        if (!directory.isDirectory() && !directory.mkdirs()) {
            Logger.logError(LOG_TAG, "Unable to create directory at \"" + directory.getAbsolutePath() + "\"");
            return false;
        }
        return true;
    }

}
