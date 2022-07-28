package net.dinglisch.android.appfactory;

import android.app.Application;
import android.content.Context;

import net.dinglisch.android.appfactory.utils.logger.Logger;

public class AppFactoryApplication extends Application {

    private static final String LOG_TAG = "TermuxApplication";

    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        // Set log config for the app
        setLogConfig(context);

        Logger.logDebug("Starting Application");

        AppFactoryConstants.init(this);
    }

    public static void setLogConfig(Context context) {
        Logger.setDefaultLogTag(AppFactoryConstants.APP_NAME);

        Logger.setLogLevel(null, Logger.LOG_LEVEL_VERBOSE);
    }

}
