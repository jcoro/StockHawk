package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility functions for Stock Hawk.
 */
public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();
    public static boolean showPercent = true;
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat(DATE_FORMAT, new Locale("en", "US"));

    public static String getDateString( long dateInMillis ) {
        return FORMAT.format(dateInMillis);
    }

    /**
     * Gets a date string that can translate to different locales.
     * @param dateInMillis The date in milliseconds since epoch.
     * @return The friendly date String
     */
    public static String getFriendlyDateString(long dateInMillis ) {
        DateFormat stringDateFormatter = DateFormat.getDateInstance();
        return stringDateFormatter.format(dateInMillis);
    }

    /**
     * Returns the chart's date labels for the time period selected.
     * @param labels the master date label list (six months).
     * @param startDate The start date for the time period selected.
     * @return A StringArray with three "visible" dates and the rest empty strings to make sure the
     * StringArray length equals the number of data points of the quote array.
     */
    public static String[] getDateLabels(String[] labels, String startDate) {
        Date parsedStartDate = new Date();
        Date parsedCurrentDate = new Date();
        try {
            parsedStartDate = FORMAT.parse(startDate);
        } catch (Exception e){
            e.printStackTrace();
        }
        int index = 0;
        for (int i = labels.length-1; i >= 0 ; i--) {
            try {
                parsedCurrentDate = FORMAT.parse(labels[i]);
            } catch (Exception e){
                e.printStackTrace();
            }
            if (parsedStartDate.before( parsedCurrentDate )){
                index = i;
            }
        }

        String[] truncatedLabels = Arrays.copyOfRange(labels, index, labels.length);
        int length = truncatedLabels.length;
        String[] returnArray = new String[length];
        int middle = length/2;
        for (int i = 0; i < length; i++) {
            if(i == 0){
                returnArray[i] = truncatedLabels[i].substring(5, truncatedLabels[i].length());
            } else if (i == middle) {
                returnArray[i] = truncatedLabels[i].substring(5, truncatedLabels[i].length());
            } else if (i == truncatedLabels.length - 1) {
                returnArray[i] = truncatedLabels[truncatedLabels.length-1].substring(5, truncatedLabels[i].length());
            } else
                returnArray[i] = "";
        }
        return returnArray;
    }

    /**
     * Returns the FloatArray of quote values for the time period selected.
     * @param values The master list of quote values (six months).
     * @param numOfEntries The number of entries to return (calculated from the construction of the date list.
     * @return The FloatArray of quote values for the time period selected.
     */
    public static float[] getValuesLabels(float[] values, int numOfEntries){
        int index = values.length - numOfEntries;
        return Arrays.copyOfRange(values, index, values.length);
    }

    /**
     * Returns an ArrayList of values for storage in the database.
     * @param JSON the JSON String.
     * @return An ArrayList of values for storage in the database.
     */
    public static ArrayList quoteJsonToContentVals(String JSON){
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject;
        JSONArray resultsArray;
        try{
          jsonObject = new JSONObject(JSON);
          if (jsonObject != null && jsonObject.length() != 0){
            jsonObject = jsonObject.getJSONObject("query");
            int count = Integer.parseInt(jsonObject.getString("count"));
            if (count == 1){
              jsonObject = jsonObject.getJSONObject("results")
                  .getJSONObject("quote");
                ContentProviderOperation testNullValues =  buildBatchOperation(jsonObject);
                if(testNullValues!=null)
              batchOperations.add(testNullValues);
            } else{
              resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

              if (resultsArray != null && resultsArray.length() != 0){
                for (int i = 0; i < resultsArray.length(); i++){
                  jsonObject = resultsArray.getJSONObject(i);
                  batchOperations.add(buildBatchOperation(jsonObject));
                }
              }
            }
          }
        } catch (JSONException e){
            e.printStackTrace();
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice){
        bidPrice = String.format(new Locale("en", "US"), "%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    /**
     * Truncates the change in stock prices.
     * @param change the change value from the yahoo data.
     * @param isPercentChange A boolean indicating if the preference is to show change as a percentage.
     * @return The truncated change in stock prices or null if the value in the yahoo data is null.
     */
    public static String truncateChange(String change, boolean isPercentChange){
        if(!change.equals("null")) {
            String weight = change.substring(0, 1);
            String ampersand = "";
            if (isPercentChange) {
                ampersand = change.substring(change.length() - 1, change.length());
                change = change.substring(0, change.length() - 1);
            }
            change = change.substring(1, change.length());
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format(new Locale("en", "US"), "%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
            return change;
        } else
            return "not found";
    }

    /**
     * Returns a ContentProviderOperation necessary to store the quote info in the database.
     * @param jsonObject the JSONObject from which the ContentProviderOperation is derived.
     * @return A ContentProviderOperation necessary to store the quote info in the database
     */
    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        String ask = null;
        try {
            ask = jsonObject.getString("Ask");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (ask != null && !ask.equals("null") ) {
            try {
                String change = jsonObject.getString("Change");
                builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
                builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
                builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                        jsonObject.getString("ChangeinPercent"), true));
                builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
                builder.withValue(QuoteColumns.ISCURRENT, 1);
                if (change.charAt(0) == '-') {
                    builder.withValue(QuoteColumns.ISUP, 0);
                } else {
                    builder.withValue(QuoteColumns.ISUP, 1);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return builder.build();
        }
        return null;
    }

    public static String getDateOneWeekAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return getDateString(cal.getTimeInMillis());
    }

    public static String getFriendlyDateOneWeekAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        return getFriendlyDateString(cal.getTimeInMillis());
    }

    public static String getDateOneMonthAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return getDateString(cal.getTimeInMillis());
    }

    public static String getFriendlyDateOneMonthAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return getFriendlyDateString(cal.getTimeInMillis());
    }

    public static String getDateThreeMonthsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -3);
        return getDateString(cal.getTimeInMillis());
    }

    public static String getFriendlyDateThreeMonthsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -3);
        return getFriendlyDateString(cal.getTimeInMillis());
    }

    public static String getDateSixMonthsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        return getDateString(cal.getTimeInMillis());
    }

    public static String getFriendlyDateSixMonthsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -6);
        return getFriendlyDateString(cal.getTimeInMillis());
    }

    /**
     * Returns the upper limit for the X axis label.
     * @param values the FloatArray of values.
     * @return An int representing the highest X axis label.
     */
    public static int getXUpperLimit(float[] values){
        Float high = 0f;
        for (Float i : values){
            if (i > high) {
                high = i;
            }
        }
        return Math.round(high + 1);
    }

    /**
     * Returns the lower limit for the X axis label.
     * @param values the FloatArray of values.
     * @return An int representing the lowest X axis label.
     */
    public static int getXLowerLimit(float[] values){
        Float low = values[0];
        for (Float i : values){
            if (i < low) {
                low = i;
            }
        }
        return Math.round(low - 1);
    }

}
