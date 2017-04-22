package com.example.roma.exchangerateapplication.network;

import com.example.roma.exchangerateapplication.helper.CurrencyNBU;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface ExchangeRateDateServiceNBU {
    @GET("exchange")
    Call<List<CurrencyNBU>> getAllCurrencyDate(
            @Query("valcode")  String valcode,
            @Query("date")  String date,
            @Query("json")  String json
    );
}
