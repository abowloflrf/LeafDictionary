package cn.studyjams.s2.sj0191.ruofeng;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    TextView wordView;
    TextView phoneticView;
    TextView translateView;
    public String myQuery;
    public String myPhonetic;
    public String myTranslation;
    public TranslateResult myResult;
    FloatingSearchView floatingSearchView;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        floatingSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        wordView = (TextView) findViewById(R.id.word_view);
        phoneticView = (TextView) findViewById(R.id.phonetic_view);
        translateView = (TextView) findViewById(R.id.translate_view);

        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                sendRequestWithOkHttp(currentQuery);
            }
        });

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
                    String responseData = response.body().string();
                    Log.d(TAG, "++++++ " + responseData);
                    parseJSON(responseData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSON(String jsonData) {
        Gson gson = new Gson();
        myResult = gson.fromJson(jsonData, TranslateResult.class);
        Log.d(TAG, "parseJSON: errorCode: " + myResult.getErrorCode());
        //查询出错，errorCode为0表示查询成功
        if (!myResult.getErrorCode().equals("0"))
            return;
        //查询成功，但是结果为空
        if (myResult.getBasic() == null || myResult.getTranslation() == null)
            return;
        Log.d(TAG, "parseJSON: " + myResult.getErrorCode());
        Log.d(TAG, "parseJSON: " + myResult.getQuery());
        Log.d(TAG, "parseJSON: " + myResult.getBasic().getPhonetic());
        Log.d(TAG, "parseJSON: " + myResult.getTranslation().get(0));
        for (String explain : myResult.getBasic().getExplains()) {
            Log.d(TAG, "parseJSON: " + explain);
        }

        myQuery = myResult.getQuery();
        myPhonetic = myResult.getBasic().getPhonetic();
        myTranslation = myResult.getTranslation().get(0);

        //render ui
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wordView.setText(myQuery);
                StringBuffer afterPhonetic = new StringBuffer(myPhonetic);
                afterPhonetic.append('/');
                afterPhonetic.insert(0, '/');
                phoneticView.setText(afterPhonetic);
                translateView.setText(myTranslation);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        MainActivity.this, R.layout.list_item, myResult.getBasic().getExplains()
                );
                ListView listView = (ListView) findViewById(R.id.explains_list_view);
                listView.setAdapter(adapter);
            }
        });

    }

}
