package com.tedkim.android.tutils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

import java.util.Random;

/**
 * Class collection of String
 * Created by Ted
 */
public class StringUtils {

    /**
     * Get random code (english and number)
     *
     * @param length random code length
     * @return random code
     */
    public static String getRandomCode(int length) {
        Random rnd = new Random();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < length; i++) {
            if (rnd.nextBoolean()) {
                buf.append((char) (rnd.nextInt(26) + 97));
            } else {
                buf.append((rnd.nextInt(10)));
            }
        }

        return String.valueOf(buf);
    }

    /**
     * Check the PATH to the URI
     *
     * @param context context
     * @param uri     url
     * @return path
     */
    public static String getPathFromURI(Context context, Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        } else {
            return uri.getPath();               // FOR OI/ASTRO/Dropbox etc
        }
    }

    /**
     * Cut phrase
     *
     * @param phrase original phrase
     * @param start  string index
     * @return cut phrase
     */
    public static String cutPhrase(String phrase, int start) {
        return phrase.substring(start);
    }

    /**
     * Cut phrase
     *
     * @param phrase original phrase
     * @param start  start index
     * @param end    end index
     * @return cut phrase
     */
    public static String cutPhrase(String phrase, int start, int end) {
        return phrase.substring(start, end);
    }

    /**
     * split phrase
     *
     * @param phrase  original phrase
     * @param divider divider
     * @return split phrase string array
     */
    public static String[] splitPhrase(String phrase, String divider) {
        return phrase.split(divider);
    }

    /**
     * Remove gap phrase
     *
     * @param phrase original phrase
     * @return remove gap phrase
     */
    public static String removeGapPhrase(String phrase) {
        return phrase.trim();
    }

    /**
     * Get New-line character remove
     *
     * @param message changed message
     * @return Removed New-line character
     */
    public static String removeNewLineCharacter(String message) {
        return message.replace(System.getProperty("line.separator"), "");
    }

    /**
     * set TextView under line
     *
     * @param textView textview
     * @param message  under line message
     */
    public static void setUnderLine(TextView textView, String message) {
        SpannableString content = new SpannableString(message);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        textView.setText(content);
    }
}
