package at.rihnet.rihnetlogistikmde.utils;

import android.os.Build;

/**
 * Utility class to check for Datalogic-specific features.
 */
public class DatalogicUtils {

    private static final String DATALOGIC_MANUFACTURER = "Datalogic";

    /**
     * Checks if the app is running on a Datalogic device by comparing
     * the device manufacturer's name.
     *
     * @return true if the device manufacturer is Datalogic, false otherwise.
     */
    public static boolean isDatalogicDevice() {
        return Build.MANUFACTURER.equalsIgnoreCase(DATALOGIC_MANUFACTURER);
    }
}
