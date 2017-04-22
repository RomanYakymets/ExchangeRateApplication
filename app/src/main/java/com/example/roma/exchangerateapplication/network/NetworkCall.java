package com.example.roma.exchangerateapplication.network;


import android.os.AsyncTask;

import com.example.roma.exchangerateapplication.helper.CurrencyNBU;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class NetworkCall extends AsyncTask<Call, Void, Boolean> {
    ResultListener listener;
    Date dateFrom, dateTo;

    private List<CurrencyNBU> listCurrencyNBU;

    public interface ResultListener {
        void handleAsyncResult(Boolean success);
    }

    public NetworkCall(ResultListener listener, List<CurrencyNBU> listCurrencyNBU,
                       Date dateFrom, Date dateTo) {
        this.listener = listener;
        this.listCurrencyNBU = listCurrencyNBU;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    @Override
    protected Boolean doInBackground(Call... calls) {
        try {
            Call call = calls[0];
                Response<List<CurrencyNBU>> response = call.execute();
                List<CurrencyNBU> currencyList = response.body();
                listCurrencyNBU.add(currencyList.get(0));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        listener.handleAsyncResult(success);
    }

}
