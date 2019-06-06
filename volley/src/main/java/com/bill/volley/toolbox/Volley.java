package com.bill.volley.toolbox;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.bill.volley.Network;
import com.bill.volley.RequestQueue;

/**
 * Created by Bill on 2019/6/4.
 */

/**
 * 辅助类，创建 {@link RequestQueue}
 */
public class Volley {

    public static RequestQueue newRequestQueue(Context context) {
        return newRequestQueue(context, (HttpStack) null);
    }

    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        BasicNetwork network;
        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                network = new BasicNetwork(new HurlStack());
            } else {
                String userAgent = "volley/0";
                try {
                    String packageName = context.getPackageName();
                    PackageInfo info =
                            context.getPackageManager().getPackageInfo(packageName, /* flags= */ 0);
                    userAgent = packageName + "/" + info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                }

                network =
                        new BasicNetwork(
                                new HttpClientStack(AndroidHttpClient.newInstance(userAgent)));
            }
        } else {
            network = new BasicNetwork(stack);
        }

        return newRequestQueue(context, network);
    }

    private static RequestQueue newRequestQueue(Context context, Network network) {
        RequestQueue queue = new RequestQueue(new MemoryCache(), network);
        queue.start();
        return queue;
    }

}
