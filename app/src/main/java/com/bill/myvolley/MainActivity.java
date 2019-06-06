package com.bill.myvolley;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.bill.volley.Request;
import com.bill.volley.RequestQueue;
import com.bill.volley.Response;
import com.bill.volley.VolleyError;
import com.bill.volley.toolbox.StringRequest;
import com.bill.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

    }

    public void handleClick(View view) {
        String url = "http://www.mocky.io/v2/5ce7a43d3500008d00cf6014";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Bill", response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Bill", error == null ? "error is null" : ":" + error.getMessage());
                    }
                });
        requestQueue.add(request);

        StringRequest postRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.e("Bill", response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Bill", error == null ? "error is null" : ":" + error.getMessage());
                    }
                });
        requestQueue.add(postRequest);

        GsonRequest gsonRequest = new GsonRequest<>(Request.Method.GET,
                url, MyBean.class,
                new Response.Listener<MyBean>() {
                    @Override
                    public void onResponse(MyBean response) {
                        Log.i("Bill", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Bill", error == null ? "error is null" : ":" + error.getMessage());
                    }
                });
        requestQueue.add(gsonRequest);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestQueue.stop();
    }
}
