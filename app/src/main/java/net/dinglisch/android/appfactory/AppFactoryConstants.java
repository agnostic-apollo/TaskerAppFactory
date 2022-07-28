package net.dinglisch.android.appfactory;

import android.content.Context;

import androidx.annotation.NonNull;

public class AppFactoryConstants {

    /** App name */
    public static final String APP_NAME = "AppFactory"; // Default: "AppFactory"
    /** App package name */
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID; // Default: "net.dinglisch.android.appfactory"



    /*
     * Termux app core directory paths.
     */


    /** Termux app Files directory path */
    public static String FILES_DIR_PATH; // Default: "/data/data/net.dinglisch.android.appfactory/files"

    /** Termux app $PREFIX directory path */
    public static String PREFIX_DIR_PATH; // Default: "/data/data/net.dinglisch.android.appfactory/files/usr"


    /** Termux app $PREFIX/bin directory path */
    public static String BIN_PREFIX_DIR_PATH; // Default: "/data/data/net.dinglisch.android.appfactory/files/usr/bin"



    /** Termux app $PREFIX/tmp and $TMPDIR directory path */
    public static String TMP_PREFIX_DIR_PATH; // Default: "/data/data/net.dinglisch.android.appfactory/files/usr/tmp"



    /** Termux app $HOME directory path */
    public static String HOME_DIR_PATH; // Default: "/data/data/net.dinglisch.android.appfactory/files/home"



    public static void init(@NonNull Context context) {
        FILES_DIR_PATH = context.getFilesDir().getAbsolutePath().replace("/data/user/0/", "/data/data/");

        PREFIX_DIR_PATH = FILES_DIR_PATH + "/usr";
        BIN_PREFIX_DIR_PATH = PREFIX_DIR_PATH + "/bin";
        TMP_PREFIX_DIR_PATH = PREFIX_DIR_PATH + "/tmp";

        HOME_DIR_PATH = FILES_DIR_PATH + "/home";
    }

}
