package com.example.marinecommercon.easyconverter;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;


public class NetworkService {

    private static NetworkAPI networkApi ;
    private static String baseUrl = "https://s3-eu-west-1.amazonaws.com/spx-development/contents/" ;

    public static NetworkAPI getClient() {
        if (networkApi == null) {

            OkHttpClient okClient = new OkHttpClient();
            okClient.newBuilder().interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    return response;
                }
            });

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            networkApi = client.create(NetworkAPI.class);
        }
        return networkApi ;
    }

    public interface NetworkAPI {

        @GET("rates.json")
        Call<RateResponse> getRates();
    }

}