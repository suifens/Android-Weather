package com.goodtech.tq.httpClient;

import android.content.Context;

import com.goodtech.tq.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class ApiResponseHandler implements Callback {

    private static final String TAG = "ApiResponseHandler";
    private Context mContext;

    public ApiResponseHandler(Context context) {
        mContext = context;
    }

    public ApiResponseHandler() { }

    public abstract void onResponse(boolean success, JSONObject jsonObject, ErrorCode errCode);

    @Override
    public void onFailure(Call call, IOException e) {
        onResponse(false, null, ErrorCode.UNKNOWN);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {

        int statusCode = response.code();
        ResponseBody body = response.body();

        if (response.isSuccessful()) {
            parseSuccess(statusCode, body.string());
        } else {
            parseFailure(statusCode, body.string());
        }

        call.cancel();
    }

    private void parseFailure(int statusCode, String bodyString) {

        if (bodyString == null) {
            if (statusCode == 408) { // XXX 实际超时返回的是0
                onResponse(false, null, ErrorCode.TIMEOUT);
            } else if (statusCode == 0 || statusCode == 502) {
                onResponse(false, null, ErrorCode.SVRFAIL);
            } else {
                onResponse(false, null, ErrorCode.UNKNOWN);
            }
            return;
        }

        try {
            JSONObject jsonRsp = new JSONObject(bodyString);
            if (jsonRsp == null) {
                onResponse(false, null, ErrorCode.ERRJSON);
                return;
            }

            ErrorCode errCode = ErrorCode.UNKNOWN;
            if (jsonRsp.has("message")) {
                JSONObject joMsg = jsonRsp.optJSONObject("message");
                String errorCodeStr = joMsg.optString("code");
                errCode = Utils.resolveErrorCode(errorCodeStr);
            }

            if (errCode == ErrorCode.ERR401) {
                //  用户信息过时
//                WeatherApp.getInstance().logoutRepeat();
            }
            onResponse(false, jsonRsp, errCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void parseSuccess(int statusCode, String bodyString) {

        try {
            JSONObject jsonObject = new JSONObject(bodyString);
            if (jsonObject == null) {

                onResponse(false, null, ErrorCode.UNKNOWN);
                return;
            }

            ErrorCode errCode = ErrorCode.UNKNOWN;
            if (jsonObject.has("message")) {
                JSONObject joMsg = jsonObject.optJSONObject("message");
                String errorCodeStr = joMsg.optString("code");
                errCode = Utils.resolveErrorCode(errorCodeStr);
            }

            boolean success = true;
            if (jsonObject.has("success")) {
                success = jsonObject.optBoolean("success");
            }


            if (errCode == ErrorCode.ERR401) {
                //  用户信息过时
//                WeatherApp.getInstance().logoutRepeat();
            }

            onResponse(success, jsonObject, errCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
