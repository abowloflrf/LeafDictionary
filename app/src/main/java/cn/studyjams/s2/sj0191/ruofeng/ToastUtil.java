package cn.studyjams.s2.sj0191.ruofeng;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {
    static Toast toast = null;

    public static void show(Context context, String text) {
        try {
            if (toast != null) {
                toast.setText(text);
                toast.show();
            } else {
                toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (Exception e) {
            //解决在子线程中调用Toast的异常情况处理
            Looper.prepare();
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }
}
