package com.craftific.ategofaultcodes;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.TreeMap;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    TextView filter;
    CustomAdapter mAdapter;
    TreeMap<String, TreeMap<String, String>> faultCodesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filter = findViewById(R.id.filter);
        if (filter != null) {
            filter.clearFocus();

            filter.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterFaultCodes(s.toString());
                }
            });
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        MobileAds.initialize(this);
        AdView mAdView = findViewById(R.id.adViewMain);
        AdRequest adRequest = new AdRequest.Builder().build();
        if (mAdView != null) {
            mAdView.loadAd(adRequest);
        }

        loadFaultCodes();
        filterFaultCodes("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Load data from json resource to hashmap
     */
    void loadFaultCodes() {
        faultCodesMap = new TreeMap<>();

        InputStream is = this.getResources().openRawResource(R.raw.atego_fault_codes);
        String json;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            json = null;
        }

        if (json != null) {
            JSONObject jsonData;
            try {
                jsonData = new JSONObject(json).getJSONObject("data");
            } catch (JSONException e) {
                jsonData = null;
            }

            if (jsonData != null) {
                Iterator<?> categories = jsonData.keys();
                while (categories.hasNext()) {
                    TreeMap<String, String> categoriesData = new TreeMap<>();

                    String category = (String)categories.next();
                    JSONObject codesData;
                    try {
                        codesData = jsonData.getJSONObject(category);
                    } catch (JSONException e) {
                        codesData = null;
                    }

                    if (codesData != null) {
                        Iterator<?> codes = codesData.keys();
                        while (codes.hasNext()) {
                            String faultCode = (String) codes.next();
                            String faultDescription;
                            try {
                                faultDescription = codesData.getString(faultCode);
                            } catch (JSONException e) {
                                faultDescription = null;
                            }
                            if (faultDescription != null) {
                                categoriesData.put(faultCode, faultDescription);
                            }
                        }
                    }
                    faultCodesMap.put(category, categoriesData);
                }
            }
        }
    }

    /**
     * Filter fault codes and display in List View
     * @param kw keyword
     */
    void filterFaultCodes(String kw) {
        listView = findViewById(R.id.categories);
        mAdapter = new CustomAdapter(this);

        boolean found = false;

        for (String category : faultCodesMap.keySet()) {
            boolean isCategoryAdded = false;
            TreeMap<String, String> codesMap = faultCodesMap.get(category);
            if (codesMap == null) {
                continue;
            }
            for (String code : codesMap.keySet()) {
                String codeDescription = category + " " + code + ": " + codesMap.get(code);
                if (codeDescription.toLowerCase().contains(kw.toLowerCase())) {
                    if (!isCategoryAdded) {
                        mAdapter.addSectionHeaderItem(new TransactionObject(category));
                        isCategoryAdded = true;
                    }
                    mAdapter.addItem(new TransactionObject(codeDescription));
                    found = true;
                }
            }
        }

        if (!found) {
            mAdapter.addItem(new TransactionObject("Результатов нет"));
        }

        listView.setAdapter(mAdapter);
    }
}
