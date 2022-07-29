package net.dinglisch.android.appfactory.utils.android;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class PackageUtils {

    private static final String LOG_TAG = "PackageUtils";

    /**
     * Get the uid for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the uid.
     */
    public static int getUidForPackage(@NonNull final Context context) {
        return getUidForPackage(context.getApplicationInfo());
    }

    /**
     * Get the uid for the package associated with the {@code applicationInfo}.
     *
     * @param applicationInfo The {@link ApplicationInfo} for the package.
     * @return Returns the uid.
     */
    public static int getUidForPackage(@NonNull final ApplicationInfo applicationInfo) {
        return applicationInfo.uid;
    }

    /**
     * Get the serial number for the user for the package associated with the {@code context}.
     *
     * @param context The {@link Context} for the package.
     * @return Returns the serial number. This will be {@code null} if failed to get it.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    public static Long getUserIdForPackage(@NonNull Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        if (userManager == null) return null;
        return userManager.getSerialNumberForUser(UserHandle.getUserHandleForUid(getUidForPackage(context)));
    }

    /**
     * Check if the current user is the primary user. This is done by checking if the the serial
     * number for the current user equals 0.
     *
     * @param context The {@link Context} for operations.
     * @return Returns {@code true} if the current user is the primary user, otherwise [@code false}.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static boolean isCurrentUserThePrimaryUser(@NonNull Context context) {
        Long userId = getUserIdForPackage(context);
        return userId != null && userId == 0;
    }

}
