package cn.studyjams.s2.sj0191.ruofeng;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends Activity implements MaterialSearchBar.OnSearchActionListener {

    TextView wordView;
    TextView phoneticView;
    TextView translateView;
    public String myQuery;
    public String myPhonetic;
    public String myTranslation;
    public TranslateResult myResult;
    MaterialSearchBar searchBar;
    DrawerLayout drawer;
    FrameLayout frameView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        wordView = (TextView) findViewById(R.id.word_view);
        phoneticView = (TextView) findViewById(R.id.phonetic_view);
        translateView = (TextView) findViewById(R.id.translate_view);
        frameView = (FrameLayout) findViewById(R.id.frame_layout_bg);

        searchBar.setOnSearchActionListener(this);
        searchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (v.getTag() instanceof String) {
//                    EditText searchEdit = searchBar.getSearchEditText();
//                    searchEdit.setText((String) v.getTag());
                    sendRequestWithOkHttp((String) v.getTag());
                }
            }


            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawer.openDrawer(Gravity.START);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
        }
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        //并不知道背景色是不是应该这么改
        int transparentBlack = getResources().getColor(R.color.transparentBlack);
        if (enabled) {
            frameView.setBackgroundColor(transparentBlack);
        } else {
            frameView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        sendRequestWithOkHttp(text.toString());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(Gravity.START)) {
            drawer.closeDrawer(Gravity.START);
        } else {
            super.onBackPressed();
        }
    }


    public String getURL(String word) {
        String appKey = "00e74b045d1c7225";
        String salt = String.valueOf(System.currentTimeMillis());
        String from = "en";
        String to = "zh-CN";
        String sign = md5(appKey + word + salt + "uuXTCBFEW65hYsLHW1RA6OoeVeT5S3Mx");
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", word);
        params.put("from", from);
        params.put("to", to);
        params.put("sign", sign);
        params.put("salt", salt);
        params.put("appKey", appKey);
        return getUrlWithQueryString("https://openapi.youdao.com/api", params);
    }


    public static String md5(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes();
        try {
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 根据api地址和参数生成请求URL
     *
     * @param url
     * @param params
     * @return
     */
    public static String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }

        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }

        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }

            if (i != 0) {
                builder.append('&');
            }

            builder.append(key);
            builder.append('=');
            builder.append(encode(value));

            i++;
        }

        return builder.toString();
    }

    /**
     * 进行URL编码
     *
     * @param input
     * @return
     */
    public static String encode(String input) {
        if (input == null) {
            return "";
        }

        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return input;
    }

    private void sendRequestWithOkHttp(final String word) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(getURL(word))
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d(TAG, "response JSON" + responseData);
                        parseJSON(responseData);
                    } else {
                        ToastUtil.show(MainActivity.this, "查询请求失败");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtil.show(MainActivity.this, "查询请求失败");
                }
            }
        }).start();
    }

    private void parseJSON(String jsonData) {
        Gson gson = new Gson();
        myResult = gson.fromJson(jsonData, TranslateResult.class);

        //查询出错，errorCode为0表示查询成功
        if (!myResult.getErrorCode().equals("0")) {
            ToastUtil.show(MainActivity.this, "查询出错：" + myResult.getErrorCode());
            return;
        }
        //查询成功，但是结果为空
        if (myResult.getBasic() == null || myResult.getTranslation() == null) {
            ToastUtil.show(MainActivity.this, "没有找到结果");
            return;
        }

        for (String explain : myResult.getBasic().getExplains()) {
            Log.d(TAG, "parseJSON: basic.explain" + explain);
        }

        Log.d(TAG, "parseJSON: errorCode: " + myResult.getErrorCode());
        Log.d(TAG, "parseJSON: query: " + myResult.getQuery());
        Log.d(TAG, "parseJSON: translation: " + myResult.getTranslation().get(0));
        Log.d(TAG, "parseJSON: basic.phonetic: " + myResult.getBasic().getPhonetic());

        myQuery = myResult.getQuery();
        myPhonetic = myResult.getBasic().getPhonetic();
        myTranslation = myResult.getTranslation().get(0);

        //render ui
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wordView.setText(myQuery);
                //有可能有的查询结果没有音标
                if (myPhonetic == null)
                    phoneticView.setText("");
                else {
                    StringBuffer afterPhonetic = new StringBuffer(myPhonetic);
                    afterPhonetic.append('/');
                    afterPhonetic.insert(0, '/');
                    phoneticView.setText(afterPhonetic);
                }
                translateView.setText(myTranslation);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        MainActivity.this, R.layout.list_item, myResult.getBasic().getExplains()
                );
                ListView listView = findViewById(R.id.explains_list_view);
                listView.setAdapter(adapter);

                searchBar.disableSearch();
            }
        });

    }

}
