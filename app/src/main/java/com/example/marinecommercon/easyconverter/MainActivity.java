package com.example.marinecommercon.easyconverter;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private NetworkService.NetworkAPI service;
    private Map<String, Float> rates = new HashMap<>();
    private List<String> keys = new ArrayList<>();
    private SharedPreferences ratesPreference;
    private SharedPreferences ratesUpdatePreference;

    private RecyclerView currenciesRecycler;
    private CurrenciesAdapter adapter;
    private int currentActionLine = 0;

    private EditText editText1;
    private EditText editText2;
    private Button button1;
    private Button button2;
    private FloatingActionButton fab;
    private Dialog currenciesDialog;
    private Dialog addCurrencyDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        editText1 = findViewById(R.id.editText1);
        editText2 = findViewById(R.id.editText2);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        fab = findViewById(R.id.fab);

        new NetworkService();
        service = NetworkService.getClient();
        ratesPreference = getApplicationContext().getSharedPreferences("rates", 0);
        ratesUpdatePreference = getApplicationContext().getSharedPreferences("ratesUpdate", 0);

        editText1.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    currentActionLine = 1;
                    updateValue();
                }
                return false;
            }
        });

        editText2.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(keyCode == KeyEvent.KEYCODE_ENTER){
                    currentActionLine = 2;
                    updateValue();
                }
                return false;
            }
        });

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        fab.setOnClickListener(this);
        editText1.requestFocus();

        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1:
                currentActionLine = 1;
                displayCurrenciesDialog();
                break;
            case R.id.button2:
                currentActionLine = 2;
                displayCurrenciesDialog();
                break;
            case R.id.fab:
                displayAddCurrencyDialog();
                break;
        }
    }

    private void init() {
        initRates();
        initKeys();
        initButtons();
    }

    private void initRates() {
        long timestamp = ratesUpdatePreference.getLong("lastUpdate", 0);
        long diff = new Date().getTime() - timestamp;
        int numOfDays = (int) (diff / (1000 * 60 * 60 * 24));
        if (numOfDays > 1 || timestamp == 0) {
            getRates();
        }
        else {
            Map<String, ?> allRates = ratesPreference.getAll();
            for (Map.Entry<String, ?> rate : allRates.entrySet()) {
                rates.put(rate.getKey(), (Float) rate.getValue());
            }
            rates = sortByKeys(rates);
        }
    }

    private void updateValue() {
        float valueEntered;

        if (currentActionLine == 1 && editText1.getText().toString().length() > 0 && Float.valueOf(editText1.getText().toString()) > 0) {
            valueEntered = Float.valueOf(editText1.getText().toString());
            float rateEntered = rates.get(button1.getText().toString());
            float rateBis = rates.get(button2.getText().toString());
            float valueBis = valueEntered*rateBis/rateEntered;
            if (!Objects.equals(editText2.getText().toString(),String.valueOf(valueBis)))
                editText2.setText(String.valueOf(valueBis));
        }
        else if (currentActionLine == 2 && editText2.getText().toString().length() > 0 && Float.valueOf(editText2.getText().toString()) > 0) {
            valueEntered = Float.valueOf(editText2.getText().toString());
            float rateEntered = rates.get(button2.getText().toString());
            float rateBis = rates.get(button1.getText().toString());
            float valueBis = valueEntered*rateBis/rateEntered;
            if (!Objects.equals(editText1.getText().toString(),String.valueOf(valueBis)))
                editText1.setText(String.valueOf(valueBis));
        }
    }

    public static <K extends Comparable, V> Map<K,V> sortByKeys(Map<K,V> map) {
        return new TreeMap<>(map);
    }

    private void initKeys() {
        if (rates.size() > 0)
            keys = new ArrayList<>(rates.keySet());
    }

    private void initButtons() {
        if (keys.size() > 0) {
            button1.setText(keys.get(0));
            button2.setText(keys.get(0));
        }
    }

    private void getRates() {
        if (App.isMock()) {
            String response = FileUtils.loadFromAsset(this, "rates.json");
            Gson gson = new GsonBuilder().create();
            RateResponse rateResponse = gson.fromJson(response, RateResponse.class);
            rates = rateResponse.getRates();

            if (rates.size() > 0)
                save(rates);
        }
        else {
            Call<RateResponse> call = service.getRates();
            call.enqueue(new Callback<RateResponse>() {
                @Override
                public void onResponse(Call<RateResponse> call, Response<RateResponse> response) {
                    Log.d("MainActivity", "Status Code = " + response.code());

                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            RateResponse rateResponse = response.body();

                            if (rateResponse.isSuccess()) {
                                rates = rateResponse.getRates();
                                save(rates);
                                init();
                            }
                        }
                    } else {
                        //
                    }
                }

                @Override
                public void onFailure(Call<RateResponse> call, Throwable t) {
                    //
                }
            });
        }
    }

    private void save(Map<String, Float> rates) {
        ratesUpdatePreference.edit().putLong("lastUpdate", new Date().getTime()).apply();
        List<String> keys = new ArrayList<>(rates.keySet());
        for (String key : keys) {
            ratesPreference.edit().putFloat(key,rates.get(key)).apply();
        }
    }

    private void displayCurrenciesDialog() {
        if (!isFinishing()) {
            if (currenciesDialog == null) {
                currenciesDialog = new Dialog(this);
                currenciesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                currenciesDialog.setContentView(R.layout.dialog_currencies);
                currenciesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                currenciesDialog.setCanceledOnTouchOutside(true);
                Window window = currenciesDialog.getWindow();

                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                wlp.gravity = Gravity.CENTER;
                window.setAttributes(wlp);

                currenciesDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                this.currenciesRecycler = currenciesDialog.findViewById(R.id.currencies_recycler);

                LinearLayoutManager llm = new LinearLayoutManager(this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                this.currenciesRecycler.setLayoutManager(llm);

                if (rates != null) {
                    adapter = new CurrenciesAdapter(this, new ArrayList<>(rates.keySet()));
                    adapter.listener = new CurrenciesDialogListener() {
                        @Override
                        public void didSelect(String codeName) {
                            getButton(currentActionLine).setText(codeName);
                            updateValue();
                            currenciesDialog.dismiss();
                        }
                    };
                    this.currenciesRecycler.setAdapter(adapter);
                }

                RelativeLayout closeButton = currenciesDialog.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isFinishing()) {
                            currenciesDialog.dismiss();
                        }
                    }
                });
            }
            else {
                // Update position
                String currentRate = getButton(currentActionLine).getText().toString();
                int position = keys.indexOf(currentRate);
                currenciesRecycler.scrollToPosition(position);
                adapter.highlightItem(position);
            }
            currenciesDialog.show();
        }
    }

    private void displayAddCurrencyDialog() {
        if (!isFinishing()) {
            if (addCurrencyDialog == null) {
                addCurrencyDialog = new Dialog(this);
                addCurrencyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                addCurrencyDialog.setContentView(R.layout.dialog_add_currency);
                currenciesDialog.setCanceledOnTouchOutside(true);
                addCurrencyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Window window = addCurrencyDialog.getWindow();

                WindowManager.LayoutParams wlp = window.getAttributes();
                wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
                wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                wlp.gravity = Gravity.CENTER;
                window.setAttributes(wlp);

                addCurrencyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                final EditText codeEditText = addCurrencyDialog.findViewById(R.id.editText1);
                final EditText rateEditText = addCurrencyDialog.findViewById(R.id.editText2);
                editText1.requestFocus();
                addCurrencyDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


                Button addButton = addCurrencyDialog.findViewById(R.id.add_button);
                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String code = codeEditText.getText().toString();
                        String currencyRate = rateEditText.getText().toString();
                        if (checkCodeAndRate(code, currencyRate)) {

                            ratesPreference.edit().putFloat(code,Float.valueOf(currencyRate)).apply();
                            ratesUpdatePreference.edit().putLong("ratesUpdate", 0).apply();
                            init();
                            addCurrencyDialog.dismiss();
                        }
                    }
                });

                RelativeLayout closeButton = addCurrencyDialog.findViewById(R.id.closeButton);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isFinishing()) {
                            addCurrencyDialog.dismiss();
                        }
                    }
                });
            }
            else {
                ((EditText) addCurrencyDialog.findViewById(R.id.editText1)).setText("");
                addCurrencyDialog.findViewById(R.id.editText1).requestFocus();
                ((EditText) addCurrencyDialog.findViewById(R.id.editText2)).setText("");

            }
            addCurrencyDialog.show();
        }
    }

    private Button getButton(int currentActionLine) {
        if (currentActionLine == 1)
            return (Button) findViewById(R.id.button1);
        else if (currentActionLine == 2)
            return (Button) findViewById(R.id.button2);

        return (Button) findViewById(R.id.button1);
    }

    private boolean checkCodeAndRate(String code, String currencyRate) {
        if (code.length() == 3 && currencyRate.length() > 0) {
            // Verify if the code already exists
            return Objects.equals(rates.get(code), null);
        }
        return false;
    }
}
