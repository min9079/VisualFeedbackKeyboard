package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by min90 on 09/05/2017.
 */

public class ReportManager {
    private static final String TAG = ReportManager.class.getName();
    private static final int MIN_NETWORK_BANDWIDTH_KBPS = 300;
    // Message to notify the network request timout handler that too much time has passed.
    private static final int MESSAGE_CONNECTIVITY_TIMEOUT = 1;
    // How long the app should wait trying to connect to a sufficient high-bandwidth network before
    // asking the user to add a new Wi-Fi network.
    private static final long NETWORK_CONNECTIVITY_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(30);

    private static ReportManager mInstance = null;

    private Context mContext = null;
    private File mPath = null;
    private File mLogFile = null;
    private FileWriter mWriter = null;
    private JSONObject mBlocksJSONObject = null;
    private JSONObject mBlockJSONObject = null;
    private JSONArray mTestResultsJSONObject = null;
    private NetworkManager mNetworkManager = null;

    private ReportManager(Context context) {
        mPath = new File(Environment.getExternalStorageDirectory(), "CustomKeyboard");
        mPath.mkdirs();
        mContext = context;
        mNetworkManager = NetworkManager.getInstance(context);
   }

    public static synchronized ReportManager getInstance(Context context) {
        if(mInstance == null){
            mInstance = new ReportManager(context);
        }
        return mInstance;
    }

    public boolean initialize(boolean isNewLog) {
        JSONArray experimentsJSONArrary = null;

        //mLogFile = new File(mPath, DateFormat.getDateTimeInstance().format(new Date()));
        mLogFile = new File(Environment.getExternalStorageDirectory(), "log.json");
        if (mLogFile.exists()) {
            File backFile = new File(Environment.getExternalStorageDirectory(), "old_log.json");
            mLogFile.renameTo(backFile);

            if (isNewLog) {
                mBlocksJSONObject = null; // reset json log file.
            } else{
                String jsonString;
                try {
                    BufferedReader jsonFileBufferedReader;
                    jsonFileBufferedReader = new BufferedReader(new FileReader(backFile));
                    jsonString = jsonFileBufferedReader.readLine();
                    jsonFileBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    if (jsonString != null) {
                        mBlocksJSONObject = new JSONObject(jsonString);
                        experimentsJSONArrary = mBlocksJSONObject.getJSONArray("Blocks");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        try {
            mWriter = new FileWriter(mLogFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        mBlockJSONObject = new JSONObject();
        try {
            mBlockJSONObject.put("Block", DateFormat.getDateTimeInstance().format(new Date()));
            mBlockJSONObject.put("uuid", UUID.randomUUID().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        mTestResultsJSONObject = new JSONArray();
        try {
            mBlockJSONObject.put("Test Results", mTestResultsJSONObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        if (experimentsJSONArrary == null) {
            experimentsJSONArrary = new JSONArray();
        }
        experimentsJSONArrary.put(mBlockJSONObject);

        if(mBlocksJSONObject == null) {

            Log.d(TAG, "Initialize empty json file..");

            mBlocksJSONObject = new JSONObject();
            try {
                mBlocksJSONObject.put("Blocks", experimentsJSONArrary);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        Log.d(TAG, "initialize done.");

        return true;
    }

    public boolean setTestInfo(SharedPreferences sharedPreferences) {
        if (mBlockJSONObject == null) {
            Log.e(TAG, "mJSONObject is null");
            return false;
        }
        // Report Block Information
        JSONObject experimentJson = new JSONObject();
        try {
            experimentJson.put("Participant Code", sharedPreferences.getString("participant_list", "P0"));
            experimentJson.put("Session Code", sharedPreferences.getString("session_list", "S0"));
            experimentJson.put("Group Code", sharedPreferences.getString("group_list", "G0"));
            experimentJson.put("Condition Code", sharedPreferences.getString("condition_list", "C0"));
            experimentJson.put("Phrases per Block", sharedPreferences.getString("num_phrases_list", "5"));
            experimentJson.put("Phrases File", sharedPreferences.getString("phrases_file_list", "phrases2"));

            mBlockJSONObject.put("Block Info.", experimentJson);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        // Report Method Configurations
        JSONObject methodJson = new JSONObject();
        try {
            methodJson.put("IsBold", sharedPreferences.getBoolean("method_conf_char_bold", true));
            methodJson.put("IsItalic", sharedPreferences.getBoolean("method_conf_char_italic", false));
            methodJson.put("IsSize", sharedPreferences.getBoolean("method_conf_char_size", false));
            methodJson.put("IsColor", sharedPreferences.getBoolean("method_conf_char_color", true));
            methodJson.put("MaxNofChars", sharedPreferences.getString("method_conf_n_of_chars", "5"));
            methodJson.put("Char PredictThreshold", sharedPreferences.getString("method_conf_predict_threshold", "0.15"));
            methodJson.put("Word PredictThreshold", sharedPreferences.getString("method_conf_word_predict_threshold", "0.15"));
            methodJson.put("Char Display", sharedPreferences.getString("method_conf_char_display", "DECODED"));
            methodJson.put("Word Prediction", sharedPreferences.getBoolean("method_conf_word_prediction", true));

            mBlockJSONObject.put("Method Info.", methodJson);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }


        Log.d(TAG, "set test info.");
        return true;
    }

    public boolean setTestResult(TextEntryTest textEntryTest) {
        if (mTestResultsJSONObject == null) {
            Log.e(TAG, "mTestResultsJSONObject is null");
            return false;
        }
        // Report Block Result
        JSONObject resultJson = new JSONObject();
        try {
            resultJson.put("WPM", Float.toString(textEntryTest.getWPM()));
            resultJson.put("Error Rate", Float.toString(textEntryTest.getErrRate()));
            resultJson.put("KSPC", Float.toString(textEntryTest.getKSPC()));
            resultJson.put("Presented Phrase", textEntryTest.getPresentedPhrase());
            resultJson.put("Transcribed Phrase", textEntryTest.getTranscribedPhrase());
            resultJson.put("Start Time", textEntryTest.getStartTime());
            resultJson.put("End Time", textEntryTest.getEndTime());
            resultJson.put("Touches", textEntryTest.getTouches());

            mTestResultsJSONObject.put(resultJson);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        Log.d(TAG, "set test result.");
        return true;
    }

    public boolean dumpTestInfo(){
        if (mBlocksJSONObject == null) {
            Log.e(TAG, "mJSONObject is null");
            return false;
        }
        try {
            if(mWriter != null) {
                mWriter.append(mBlocksJSONObject.toString());
                mWriter.append('\n');
                mWriter.flush();
                mWriter.close();
            }

            sendJSONToServerAsync(mBlocksJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
            mWriter = null;
            mPath = null;
            return false;
        }
        mWriter = null;
        mPath = null;

        Log.d(TAG, "dump test result.");
        return true;
    }

    private void sendJSONToServerAsync(final String json_log) {

        Log.d(TAG, "send test result async.");
        if(mNetworkManager.isNetworkHighBandwidth()) {
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    return sendJSONToServer(json_log);
                }
            }.execute();
        } else {
            mNetworkManager.sendWhenReady(new NetworkManager.SendCallback() {
                @Override
                public void send() {
                    sendJSONToServer(json_log);
                }
            });
       }
    }

    private String sendJSONToServer(String json_log) {

        Log.d(TAG, "send test result.");
        StringBuilder sb = new StringBuilder();

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://min90.koreasouth.cloudapp.azure.com:3000/log");
            urlConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "urlConnection");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setUseCaches(false);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            urlConnection.connect();

            Log.d(TAG, "connect urlConnection");

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(json_log);
            Log.d(TAG, json_log);
            out.close();

            Log.d(TAG, "write output json");

            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + '\n');
                }
                br.close();

                Log.d(TAG, sb.toString());
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "malformed exception.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "io exception.");
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(TAG, "other exception.");
            Log.d(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
                Log.d(TAG, "disconnect");
            }
            Log.d(TAG, sb.toString());
            return sb.toString();
        }
    }
}
