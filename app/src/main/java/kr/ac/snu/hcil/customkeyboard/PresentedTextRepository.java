package kr.ac.snu.hcil.customkeyboard;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by min90 on 07/02/2017.
 */

class PresentedTextRepository {
    private static final String TAG = PresentedTextRepository.class.getName();
    private static PresentedTextRepository mInstance = null;
    private ArrayList<Phrase> mPhraseList = new ArrayList<>();
    private Context mContext = null;

    enum PhraseSetType { TWEET, MACKENZIE100, MACKENZIE2, ENRON_MOBILE, PREVIEW }

    private class Phrase {
        PhraseSetType mType;
        String mPhrase;
        Phrase(String phrase, PhraseSetType type)
        {
            mPhrase = phrase;
            mType = type;
        }
    }

    private static String[] mTweetPhraseSet = {
            "the best way to get in contact with you",
            "getting more handsome day by day",
            "or expect me to do anything for you",
            "what do you possess if you possess not god",
            "my stomach is empty as my wallet",
            "my mom stopped you one time",
            "always look at what you have left",
            "this reminded me of my speech",
            "i am so done with assignments",
            "they obviously learned from a few mistake",
            "it takes a special breed of batsmen",
            "any chance of a signed hat",
            "its gonna be on a slow pace",
            "i love you so much",
            "you guys are very important to me",
            "an airliner shot down",
            "she makes me so happy",
            "her name was in it",
            "ask her to follow me please",
            "fuck them with extreme prejudice",
            "you will be on it next time",
            "it would probably be illegal",
            "i am just ready to be happy",
            "we were in stopped traffic for an hour",
            "i feel like the ugly duckling out of my friends",
            "you make me smile so much",
            "go to new york for your own health",
            "big mistake to vote a man into office because of skin color",
            "ready for it to be tomorrow already lol",
            "if i could just get some sleep right about now",
            "my mom is making a twitter",
            "when you ask your parents for money",
            "keep calm this too shall pass",
            "just come down and spend a day in the city",
            "i found a coke bottle with your name on it"
    };

    private static String[] mPreviewPhraseSet = {
            "please call me tomorrow if possible",
            "do you need it today",
            "this looks fine"
    };

    private PresentedTextRepository(Context context)
    {
        mContext = context;

        for(String phrase : mTweetPhraseSet ){
            mPhraseList.add(new Phrase(phrase.toLowerCase(), PhraseSetType.TWEET));
        }

        String mackenzie_phrases100[] = context.getResources().getStringArray(R.array.phrases100);
        for(String phrase : mackenzie_phrases100){
            mPhraseList.add(new Phrase(phrase.toLowerCase(), PhraseSetType.MACKENZIE100));
        }

        String mackenzie_phrases2[] = context.getResources().getStringArray(R.array.phrases2);
        for(String phrase : mackenzie_phrases2){
            mPhraseList.add(new Phrase(phrase.toLowerCase(), PhraseSetType.MACKENZIE2));
        }

        String jsonString = null;
        try {
            InputStream is = context.getResources().openRawResource(R.raw.enron_mobile);
            BufferedReader jsonFileBufferedReader = new BufferedReader(new InputStreamReader(is));
            jsonString = jsonFileBufferedReader.readLine();
            jsonFileBufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonString != null) {
            JSONArray phrases = null;
            try {
                phrases = new JSONArray(jsonString);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
            if (phrases != null) {
                for(int i=0; i<phrases.length(); i++) {
                    try {
                        mPhraseList.add(new Phrase(phrases.getString(i), PhraseSetType.ENRON_MOBILE));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for(String phrase : mPreviewPhraseSet ){
            mPhraseList.add(new Phrase(phrase.toLowerCase(), PhraseSetType.PREVIEW));
        }
    }

    public static synchronized PresentedTextRepository getInstance(Context context) {
        if(mInstance == null){
            mInstance = new PresentedTextRepository(context);
        }
        return mInstance;
    }

    List<String> getPhraseList(PhraseSetType type, int number, boolean isRandom) {
        ArrayList<String> retList = new ArrayList<>();
        for(Phrase phrase : mPhraseList){
            if (phrase.mType == type) {
                retList.add(phrase.mPhrase);
            }
        }

        if (isRandom == true) {
            long seed =System.nanoTime();
            Collections.shuffle(retList, new Random(seed));
        }
        if( number == -1)
            return retList;
        return retList.subList(0, Math.min(number,retList.size()));
    }
}
