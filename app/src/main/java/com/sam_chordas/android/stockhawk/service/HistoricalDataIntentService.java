package com.sam_chordas.android.stockhawk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * An Intent Service for fetching historical stock data.
 */
public class HistoricalDataIntentService extends IntentService {
    private static final String LOG_TAG = HistoricalDataIntentService.class.getName();
    private final OkHttpClient client = new OkHttpClient();

    public HistoricalDataIntentService(){
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //build url
        String startDate = intent.getStringExtra("startDate");
        String endDate = intent.getStringExtra("endDate");
        String symbol = intent.getStringExtra("symbol");
        ResultReceiver resultReceiver = intent.getParcelableExtra("receiver");

        final String YAHOO_BASE_URL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20%3D%20%22";
        final String START_DATE_PARAM = "%22%20and%20startDate%20%3D%20%22";
        final String END_DATE_PARAM = "%22%20and%20endDate%20%3D%20%22";
        final String UNITS_PARAM = "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
        String jsonString;

        String urlString = YAHOO_BASE_URL +
                symbol +
                START_DATE_PARAM +
                startDate +
                END_DATE_PARAM +
                endDate +
                UNITS_PARAM;
        try{
            jsonString = fetchData(urlString);
            JSONObject fullJson = new JSONObject(jsonString);
            JSONObject queryObject = fullJson.getJSONObject("query");
            JSONObject resultsObject = queryObject.getJSONObject("results");
            JSONArray quoteArray = resultsObject.getJSONArray("quote");
            int length = quoteArray.length();
            String[] dateList = new String[length];
            float[] quoteList = new float[length];
            for (int i = length-1; i >= 0 ; i--) {
                JSONObject obj = quoteArray.getJSONObject(i);
                String date = obj.getString("Date");
                Float close = (float) obj.getDouble("Adj_Close");
                dateList[(length-1) - i] = date;
                quoteList[(length-1) - i] = close;
            }

            Bundle bundle = new Bundle();
            bundle.putStringArray("dateList", dateList);
            bundle.putFloatArray("quoteList", quoteList);
            resultReceiver.send(200, bundle);
        } catch (JSONException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Returns a JSON string from a call to yahoo historical data API.
     *
     * @param url a url String constructed to fetch data from the yahoo historical data API.
     */
    private String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
