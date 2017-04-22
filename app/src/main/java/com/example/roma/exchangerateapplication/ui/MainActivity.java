package com.example.roma.exchangerateapplication.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.roma.exchangerateapplication.helper.CurrencyNBU;
import com.example.roma.exchangerateapplication.R;
import com.example.roma.exchangerateapplication.network.RestManager;
import com.example.roma.exchangerateapplication.helper.DateUtil;
import com.example.roma.exchangerateapplication.network.NetworkCall;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements NetworkCall.ResultListener{

    public final static String SERIALIZABLE_ARRAY_KEY = "serializable_array_key";
    private static final String TAG = "MainActivity111111";

    private Toolbar mToolbar;
    private RestManager mManager;
    private EditText txtDateFrom;
    private EditText txtDateTo;
    private DatePickerDialog exchangeRateDatePickerFrom, exchangeRateDatePickerTo;
    Spinner currencySpinner;

    private LineChart chart;
    ArrayList<String> xValues = new ArrayList<>();
    final SimpleDateFormat mainDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);

    private List<CurrencyNBU> currencyNBUList;
    private List<NetworkCall> networkCallList = new ArrayList<>();
    Calendar newCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mManager = new RestManager();
        initExchangeRateDatePicker();
        configViews();
        configToolbar();

        currencyNBUList = new ArrayList<>();
        chart.setNoDataText("Waiting data");
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        CurrencyNBU[] serializableObjectArray;
        serializableObjectArray = (CurrencyNBU[]) savedInstanceState
                .getSerializable(SERIALIZABLE_ARRAY_KEY);
        for (int i = 0; i < serializableObjectArray.length; i++) {
            currencyNBUList.add(serializableObjectArray[i]);
        }
        initChart();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CurrencyNBU[] serializableObjectArray = new CurrencyNBU[currencyNBUList.size()];
        currencyNBUList.toArray(serializableObjectArray);
        outState.putSerializable(SERIALIZABLE_ARRAY_KEY,
                serializableObjectArray);
    }

    private void initChart() {
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xValues.get((int) value % xValues.size());
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });
        updateChart();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }

    public  void updateChart() {

        if(isNetworkAvailable()) {
            List<Entry> entries = new ArrayList<>();
            xValues.clear();
            for (int i = 0; i < currencyNBUList.size(); i++) {
                entries.add(new Entry(i, currencyNBUList.get(i).getRate().floatValue()));
                xValues.add(currencyNBUList.get(i).getExchangedate());
            }
            LineDataSet dataSet = new LineDataSet(entries, "");
            configureDataSet(dataSet);
            dataSet.setColor(Color.WHITE);
            dataSet.setValueTextColor(R.color.colorPrimaryDark);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            if(!currencyNBUList.isEmpty()) {
                chart.setDescription(currencyNBUList.get(0).getTxt()
                        +"("+currencyNBUList.get(0).getCc()+")");
            }
            chart.invalidate();
        } else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public  void onClick(View w){
        switch (w.getId()){
            case R.id.txtDateFrom:
                exchangeRateDatePickerFrom.show();
                break;
            case R.id.txtDateTo:
                exchangeRateDatePickerTo.show();
                break;
        }
    }


    public void onShowExchangeRate(View view) {

        if(networkCallList != null){
            for( int i = 0; i < networkCallList.size(); i++ ){
                networkCallList.get(i).cancel(false);
            }
            networkCallList.clear();
        }


        final String dateFrom = txtDateFrom.getText().toString();
        final String dateTo = txtDateTo.getText().toString();
        Date dateFromD = new Date();
        Date dateToD = new Date();
        Date dateCurrent = newCalendar.getTime();

        try {
            dateFromD = dateFormat.parse(dateFrom);
            dateToD = dateFormat.parse(dateTo);
        } catch (ParseException e) {
            Log.d(TAG, "ParseException" + e);
        }

        Log.d(TAG, " dateFrom "+ dateFromD.toString());
        Log.d(TAG, " dateTo "+ dateToD.toString());
        dateToD.setTime(dateToD.getTime()+1);
        currencyNBUList.clear();

        if (!dateFromD.before(dateCurrent) &&  !dateToD.before(dateCurrent)){
            Toast.makeText(this, "Wrong date", Toast.LENGTH_SHORT).show();
        } else if (!isNetworkAvailable()){
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }
        else {
            while (dateFromD.before(dateToD)) {
                Call<List<CurrencyNBU>> call = mManager.exchangeRateDateServiceNBU()
                        .getAllCurrencyDate(currencySpinner.getSelectedItem().toString(),
                                mainDateFormat.format(dateFromD), "");
                networkCallList.add((NetworkCall)new NetworkCall(this, currencyNBUList, dateFromD, dateToD)
                        .execute(call));
                dateFromD = DateUtil.addDays(dateFromD, 1);
            }
        }
    }

    private void initExchangeRateDatePicker(){

        Calendar newCalendar = Calendar.getInstance();
        exchangeRateDatePickerFrom = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, monthOfYear, dayOfMonth);
                txtDateFrom.setText(dateFormat.format(newCal.getTime()));
            }
        },newCalendar.get(Calendar.YEAR)
                ,newCalendar.get(Calendar.MONTH)
                ,newCalendar.get(Calendar.DAY_OF_MONTH));
        exchangeRateDatePickerTo = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, monthOfYear, dayOfMonth);
                txtDateTo.setText(dateFormat.format(newCal.getTime()));
            }
        },newCalendar.get(Calendar.YEAR)
                ,newCalendar.get(Calendar.MONTH)
                ,newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void configViews() {

        txtDateFrom = (EditText)findViewById(R.id.txtDateFrom);
        txtDateTo = (EditText)findViewById(R.id.txtDateTo);

        currencySpinner = (Spinner) findViewById(R.id.currencySpinner);

        chart = (LineChart) findViewById(R.id.chart);
    }

    private void configureDataSet(LineDataSet dataSet) {
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);
    }

    @Override
    public void handleAsyncResult(Boolean success) {
        if(success){
            updateChart();
        }
    }

    private void configToolbar() {
        mToolbar = (Toolbar) this.findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_reload:
                initChart();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
