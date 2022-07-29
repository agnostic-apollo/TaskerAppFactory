package net.dinglisch.android.appfactory.utils.shell.environment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import net.dinglisch.android.appfactory.AppFactoryConstants;
import net.dinglisch.android.appfactory.utils.android.PackageUtils;

import java.util.HashMap;

/**
 * Environment for the app.
 */
public class AppShellEnvironment extends AndroidShellEnvironment {

    /** Environment variable for the app prefix path. */
    public static final String ENV_PREFIX = "PREFIX"; // Default: "PREFIX"



    @SuppressLint("SdCardPath")
    public AppShellEnvironment(@NonNull Context currentPackageContext) {
        super(currentPackageContext);
    }

    /** Get shell environment for the app. */
    @NonNull
    @Override
    public HashMap<String, String> getEnvironment(boolean isFailSafe) {

        // App environment builds upon the Android environment
        HashMap<String, String> environment = super.getEnvironment(isFailSafe);

        environment.put(ENV_HOME, AppFactoryConstants.HOME_DIR_PATH);
        environment.put(ENV_PREFIX, AppFactoryConstants.PREFIX_DIR_PATH);

        // If failsafe is not enabled, then we keep default PATH and TMPDIR so that system binaries can be used
        if (!isFailSafe) {
            environment.put(ENV_TMPDIR, AppFactoryConstants.TMP_PREFIX_DIR_PATH);
            environment.put(ENV_PATH, AppFactoryConstants.BIN_PREFIX_DIR_PATH);

            // App binaries on Android 7+ rely on DT_RUNPATH, so LD_LIBRARY_PATH should be unset by default
            // Secondary user will have different prefix than /data/data/<package_name>/files/usr
            // and so requires LD_LIBRARY_PATH, otherwise will get "CANNOT LINK EXECUTABLE... library not found" errors
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !PackageUtils.isCurrentUserThePrimaryUser(mCurrentPackageContext)) {
                environment.put(ENV_LD_LIBRARY_PATH, AppFactoryConstants.LIB_PREFIX_DIR_PATH);
            } else {
                environment.remove(ENV_LD_LIBRARY_PATH);
            }
        }

        return environment;
    }





    @NonNull
    @Override
    public String getDefaultWorkingDirectoryPath() {
        return AppFactoryConstants.HOME_DIR_PATH;
    }

    @NonNull
    @Override
    public String getDefaultBinPath() {
        return AppFactoryConstants.BIN_PREFIX_DIR_PATH;
    }

}
