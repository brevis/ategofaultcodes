package com.craftific.ategofaultcodes;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {


    ListView listView;
    TextView filter;
    CustomAdapter mAdapter;
    HashMap<String, HashMap<String, String>> faultCodesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filter = (TextView)findViewById(R.id.filter);
        if (filter != null) {
            filter.clearFocus();
        }
        filter.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFaultCodes(s.toString());
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadFaultCodes();
        filterFaultCodes("");
    }

    /**
     * Load data from json resource to hashmap
     */
    void loadFaultCodes() {
        faultCodesMap = new HashMap<>();

        InputStream is = this.getResources().openRawResource(R.raw.atego_fault_codes);
        String json = null;
        try {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
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
                    HashMap<String, String> categoriesData = new HashMap<>();

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
                            String faultDescription = null;
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
        listView = (ListView) findViewById(R.id.categories);
        mAdapter = new CustomAdapter(this);

        boolean found = false;

        Iterator<?> categoriesIterator = faultCodesMap.keySet().iterator();
        while (categoriesIterator.hasNext()) {
            String category = (String) categoriesIterator.next();
            boolean isCategoryAdded = false;
            HashMap<String, String> codesMap = faultCodesMap.get(category);
            Iterator<?> codesIterator = codesMap.keySet().iterator();
            while (codesIterator.hasNext()) {
                String code = (String) codesIterator.next();
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
