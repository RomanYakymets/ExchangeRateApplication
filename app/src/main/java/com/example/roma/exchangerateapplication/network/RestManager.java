package com.example.roma.exchangerateapplication.network;

import com.example.roma.exchangerateapplication.helper.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestManager {

    private ExchangeRateDateServiceNBU exchangeRateDateServiceNBU;

    public ExchangeRateDateServiceNBU exchangeRateDateServiceNBU(){

        if (exchangeRateDateServiceNBU == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.HTTP.BASE_URL_NBU)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            exchangeRateDateServiceNBU = retrofit.create(ExchangeRateDateServiceNBU.class);
        }
        return exchangeRateDateServiceNBU;
    }

}

