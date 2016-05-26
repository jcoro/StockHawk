package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.CustomResultReceiver;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.HistoricalDataIntentService;

/**
 * Shows historical stock data in a chart.
 * uses https://github.com/diogobernardino/WilliamChart
 */
public class DetailActivity extends AppCompatActivity implements CustomResultReceiver.Receiver {

    private LineChartView mChart;
    private float[] mMasterValues;
    private String[] mMasterLabels;
    private String[] mLabels;
    private float[] mValues;
    private String mStartDate;
    private int mSpinnerPos = -1;
    private String mFriendlyStartDate;
    private CustomResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new CustomResultReceiver(new Handler());
        String symbol = getIntent().getStringExtra("symbol");
        setContentView(R.layout.activity_detail);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(symbol);
        }
        mChart = (LineChartView)findViewById(R.id.linechart);
        TextView symbolTextView = (TextView) findViewById(R.id.detail_stock_symbol_label_textview);
        if (symbolTextView != null) {
            symbolTextView.setText(symbol);
        }
        TextView endDateTextView = (TextView) findViewById(R.id.detail_end_date_label_textview);
        String friendlyDate = Utils.getFriendlyDateString(System.currentTimeMillis());
        if (endDateTextView != null) {
            endDateTextView.setText(friendlyDate);
        }
        if(savedInstanceState == null || savedInstanceState.getFloatArray("masterValues") == null){
            mStartDate = Utils.getDateSixMonthsAgo();
            fetchHistoricalData(symbol, mStartDate);
        }else {
            mSpinnerPos = savedInstanceState.getInt("spinnerPosition");
            mMasterLabels = savedInstanceState.getStringArray("masterLabels");
            mMasterValues = savedInstanceState.getFloatArray("masterValues");
            updateChart(mSpinnerPos);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver.setReceiver(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mReceiver.setReceiver(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.historical_data_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if (mSpinnerPos > -1) {
            spinner.setSelection(mSpinnerPos);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,
                                       int position, long id) {

                if(mSpinnerPos != -1) {
                    switch (position) {
                        case 0:
                            mStartDate = Utils.getDateOneWeekAgo();
                            updateChart(0);
                            mSpinnerPos = 0;
                            break;
                        case 1:
                            mStartDate = Utils.getDateOneMonthAgo();
                            updateChart(1);
                            mSpinnerPos = 1;
                            break;
                        case 2:
                            mStartDate = Utils.getDateThreeMonthsAgo();
                            updateChart(2);
                            mSpinnerPos = 2;
                            break;
                        case 3:
                            mStartDate = Utils.getDateSixMonthsAgo();
                            updateChart(3);
                            mSpinnerPos = 3;
                            break;
                    }
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("spinnerPosition", mSpinnerPos);
        outState.putStringArray("masterLabels", mMasterLabels);
        outState.putFloatArray("masterValues", mMasterValues);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData.containsKey("quoteList")) {
            mMasterValues = resultData.getFloatArray("quoteList");
        }
        if ( resultData.containsKey("dateList") ){
            mMasterLabels = resultData.getStringArray("dateList");
        }
        if (mSpinnerPos == -1){
            mSpinnerPos = 0;
        }
        updateChart(mSpinnerPos);
    }

    /**
     * Launches the HistoricalDataIntentService to fetch historical data.
     * @param symbol the stock symbol.
     * @param startDate the start date requested.
     */
    public void fetchHistoricalData(String symbol, String startDate){
        String date = Utils.getDateString(System.currentTimeMillis());
        Intent histDataIntent = new Intent(this, HistoricalDataIntentService.class);
        histDataIntent.putExtra("startDate", startDate);
        histDataIntent.putExtra("endDate", date);
        histDataIntent.putExtra("symbol", symbol);
        histDataIntent.putExtra("receiver", mReceiver);
        startService(histDataIntent);
    }

    /**
     * Updates the chart
     * @param position The current position of the spinner which dictates the data that is shown.
     */
    public void updateChart(int position){
        int numberOfEntries;

        switch (position) {
            case 0:
                mFriendlyStartDate = Utils.getFriendlyDateOneWeekAgo();
                mLabels = Utils.getDateLabels(mMasterLabels, Utils.getDateOneWeekAgo());
                numberOfEntries = mLabels.length;
                mValues = Utils.getValuesLabels(mMasterValues, numberOfEntries);
                break;
            case 1:
                mFriendlyStartDate = Utils.getFriendlyDateOneMonthAgo();
                mLabels = Utils.getDateLabels(mMasterLabels, Utils.getDateOneMonthAgo());
                numberOfEntries = mLabels.length;
                mValues = Utils.getValuesLabels(mMasterValues, numberOfEntries);
                break;
            case 2:
                mFriendlyStartDate = Utils.getFriendlyDateThreeMonthsAgo();
                mLabels = Utils.getDateLabels(mMasterLabels, Utils.getDateThreeMonthsAgo());
                numberOfEntries = mLabels.length;
                mValues = Utils.getValuesLabels(mMasterValues, numberOfEntries);
                break;
            case 3:
                mFriendlyStartDate = Utils.getFriendlyDateSixMonthsAgo();
                mLabels = Utils.getDateLabels(mMasterLabels, Utils.getDateSixMonthsAgo());
                numberOfEntries = mLabels.length;
                mValues = Utils.getValuesLabels(mMasterValues, numberOfEntries);
                break;
        }
        TextView startDateTextView = (TextView) findViewById(R.id.detail_start_date_label_textview);
        if (startDateTextView != null) {
            startDateTextView.setText(mFriendlyStartDate);
        }

        if(mChart != null) {
            mChart.dismiss();
        }

        if (mValues != null) {
            int xUpperLimit = Utils.getXUpperLimit(mValues);
            int xLowerLimit = Utils.getXLowerLimit(mValues);
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.parseColor("#ffffff"));
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

            // Data
            LineSet dataset = new LineSet(mLabels, mValues);
            dataset.setColor(Color.parseColor("#758cbb"))
                    .setFill(Color.parseColor("#2d374c"))
                    .setDotsColor(Color.parseColor("#758cbb"))
                    .setThickness(4);
            mChart.addData(dataset);

            // Chart
            mChart.setBorderSpacing(Tools.fromDpToPx(30))
                    .setAxisBorderValues(xLowerLimit, xUpperLimit)
                    .setYLabels(AxisController.LabelPosition.OUTSIDE)
                    .setXLabels(AxisController.LabelPosition.INSIDE)
                    .setLabelsColor(Color.parseColor("#ffffff"))
                    .setGrid(ChartView.GridType.HORIZONTAL, gridPaint)
                    .setFontSize(12)
                    .setXAxis(false)
                    .setYAxis(false);

            mChart.show();
        }
        else{
            Toast toast =
                    Toast.makeText(DetailActivity.this, getResources().getString(R.string.detail_error),
                            Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
            toast.show();
        }

    }

}
