package net.dinglisch.android.appfactory.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import net.dinglisch.android.appfactory.AppFactoryConstants;
import net.dinglisch.android.appfactory.utils.errors.Error;
import net.dinglisch.android.appfactory.utils.file.FileUtils;
import net.dinglisch.android.appfactory.utils.logger.Logger;
import net.dinglisch.android.appfactory.utils.markdown.MarkdownUtils;
import net.dinglisch.android.appfactory.utils.shell.ExecutionCommand;
import net.dinglisch.android.appfactory.utils.shell.environment.AppShellEnvironment;
import net.dinglisch.android.appfactory.utils.shell.runner.app.AppShell;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ApkTools {

    private static final String LOG_TAG = "ApkTools";

    public static boolean processApk(@NonNull final Context context,
                                     @NonNull String apkInputFilePath,
                                     @NonNull String apkOutputFilePath,
                                     boolean verbose,
                                     @NonNull StringBuilder commandMarkdownOutput) {
        File apkInputFile = new File(apkInputFilePath);
        String processingDirectoryPath = AppFactoryConstants.TMP_PREFIX_DIR_PATH + "/" + "apk-" + getCurrentMilliSecondLocalTimeStamp();
        File processingDirectory = new File(processingDirectoryPath);
        String verboseFlags;

        String error = null;
        apkInputFilePath = FileUtils.getCanonicalPath(apkInputFilePath, null);
        if (!apkInputFilePath.endsWith(".apk")) {
            error = "The APK input file path \"" + apkInputFilePath + "\" does not end with \".apk\" extension";
        } else if (apkInputFilePath.contains("'")) {
            error = "The APK input file path \"" + apkInputFilePath + "\" contains single quotes";
        } else if (apkOutputFilePath.contains("'")) {
            error = "The APK output file path \"" + apkOutputFilePath + "\" contains single quotes";
        } else if (!apkInputFile.exists() || !apkInputFile.isFile()) {
            error = "An APK file not found at \"" + apkInputFilePath + "\"";
        } else if (!processingDirectory.mkdirs() || !processingDirectory.isDirectory()) {
            error = "Failed to create APK processing directory \"" + processingDirectoryPath + "\"";
        }

        if (error != null) {
            Logger.logErrorExtended(LOG_TAG, error);
            commandMarkdownOutput.append(getMarkdownCommandOutput("Process APK", error));
            return false;
        }

        String apkFileBasenameWithoutExtension = FileUtils.getFileBasenameWithoutExtension(apkInputFilePath);

        // Decompress resources.arcs in APK
        String apkDecompressedFilePath = processingDirectoryPath + "/" + apkFileBasenameWithoutExtension + "-uncompressed.apk";
        verboseFlags = verbose ? "" : " -q";
        String script = "{ " + "cd '" + processingDirectoryPath + "' && unzip" + verboseFlags + " -o '" +  apkInputFilePath + "' -d unzip && cd unzip && zip -n 'resources.arsc'" + verboseFlags + " -r '" + apkDecompressedFilePath + "' * ; ret=$?; [ $ret -eq 0 ] && echo success || echo failed; exit $ret; } 2>&1";
        if (!runAppShellCommand(context, "Decompress resource.arsc in APK", script, commandMarkdownOutput)) {
            deleteDirectory(processingDirectoryPath);
            return false;
        }

        // Zipalign APK
        verboseFlags = verbose ? " -v" : "";
        String apkZipalignedFilePath = processingDirectoryPath + "/" + apkFileBasenameWithoutExtension + "-zipaligned.apk";
        script = "{ " + "cd '" + processingDirectoryPath + "' && zipalign" + verboseFlags + " 4 '" +  apkDecompressedFilePath + "' '" + apkZipalignedFilePath + "'; ret=$?; [ $ret -eq 0 ] && echo success || echo failed; exit $ret; } 2>&1";
        if (!runAppShellCommand(context, "Zipalign APK", script, commandMarkdownOutput)) {
            deleteDirectory(processingDirectoryPath);
            return false;
        }

        // Sign APK
        String apkSignedFilePath = processingDirectoryPath + "/" + apkFileBasenameWithoutExtension + "-signed.apk";
        apkSignedFilePath = apkZipalignedFilePath;

        script = "{ " + "/system/bin/mv '" + apkSignedFilePath + "' '" +  apkOutputFilePath + "'; ret=$?; [ $ret -eq 0 ] && echo success || echo failed; exit $ret; } 2>&1";
        if (!runAppShellCommand(context, "Move APK to output path", script, commandMarkdownOutput)) {
            deleteDirectory(processingDirectoryPath);
            return false;
        }

        // Cleanup processing directory
        deleteDirectory(processingDirectoryPath);

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean runAppShellCommand(@NonNull final Context context,
                                             @NonNull String label,
                                             @NonNull String script,
                                             @NonNull StringBuilder commandMarkdownOutput) {
        // Run script
        ExecutionCommand executionCommand = new ExecutionCommand(-1, "/system/bin/sh",
                null, script + "\n", null, ExecutionCommand.Runner.APP_SHELL.getName(), false);
        executionCommand.commandLabel = label;
        AppShell appShell = AppShell.execute(context, executionCommand, null,
                new AppShellEnvironment(context), null, true);
        if (appShell == null || !executionCommand.isSuccessful()) {
            Logger.logErrorExtended(LOG_TAG, executionCommand.toString());
            commandMarkdownOutput.append(getMarkdownCommandOutput(label, executionCommand.toString()));
            return false;
        }

        // Build script output
        StringBuilder commandOutput = new StringBuilder();
        commandOutput.append("$ ").append(script);
        commandOutput.append("\n").append(executionCommand.resultData.stdout);

        boolean stderrSet = !executionCommand.resultData.stderr.toString().isEmpty();
        if (executionCommand.resultData.exitCode != 0 || stderrSet) {
            Logger.logErrorExtended(LOG_TAG, executionCommand.toString());
            if (stderrSet)
                commandOutput.append("\n").append(executionCommand.resultData.stderr);
            commandOutput.append("\n").append("exit code: ").append(executionCommand.resultData.exitCode.toString());
        }

        commandMarkdownOutput.append(getMarkdownCommandOutput(label, commandOutput.toString()));

        return executionCommand.resultData.exitCode == 0;
    }

    private static void deleteDirectory(@NonNull String directoryPath) {
        // Use your own function if you don't want to use FileUtils, although it is safe and tested.
        Error error = FileUtils.deleteDirectoryFile("APK processing directory", directoryPath, true);
        if (error != null) {
            Logger.logError(LOG_TAG, error.getErrorLogString());
        }
    }

    private static String getMarkdownCommandOutput(@NonNull String label, String commandOutput) {
        StringBuilder commandMarkdownOutput = new StringBuilder();
        // Build markdown output
        commandMarkdownOutput.append("## ").append(label).append("\n\n");
        commandMarkdownOutput.append(MarkdownUtils.getMarkdownCodeForString(commandOutput, true));
        commandMarkdownOutput.append("\n##\n\n\n");
        return commandMarkdownOutput.toString();
    }


    public static String getCurrentMilliSecondLocalTimeStamp() {
        @SuppressLint("SimpleDateFormat")
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");
        df.setTimeZone(TimeZone.getDefault());
        return df.format(new Date());
    }

}
