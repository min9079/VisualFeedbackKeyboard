package kr.ac.snu.hcil.customkeyboard;

import android.support.annotation.RawRes;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by min90 on 29/11/2017.
 */

public class SystemUtils {
    private static final String TAG = MainActivity.class.getName();

    static boolean saveFile(InputStream in, File directory, String file_name) {
        boolean ret = true;
        FileOutputStream out;
        try {
            Log.d(TAG, directory.getAbsolutePath() + file_name);
            out = new FileOutputStream(directory.getAbsolutePath() + file_name);
            byte[] buff = new byte[1024];
            int read = 0;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
                ret = false;
            } finally {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }
}
