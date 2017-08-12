package com.jascaffe.jaslocation;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jascaffe.jaslocation.net.OkHttpClientManager;
import com.jascaffe.jaslocation.service.LocationService;
import com.jascaffe.jaslocation.utils.AbAppConfig;
import com.jascaffe.jaslocation.utils.AbAppUtil;
import com.jascaffe.jaslocation.utils.AbLogUtil;
import com.jascaffe.jaslocation.utils.AbSharedUtil;
import com.jascaffe.jaslocation.utils.AbToastUtil;
import com.jascaffe.jaslocation.utils.SecurityUtil;
import com.squareup.okhttp.Request;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION = 887;
    // UI references.
    private AutoCompleteTextView mEmailView;
    //    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLocation;
    private Button mEmailSignInButton;
    private View container;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        container.setVisibility(View.INVISIBLE);
        if (mayRequestLocation()) {
            container.setVisibility(View.VISIBLE);
            startLocation();
        } else {
            container.setVisibility(View.INVISIBLE);
        }
    }

    private void initViews() {
        // Set up the login form.
        container = findViewById(R.id.container);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        tvLocation = (TextView) findViewById(R.id.id_location_tv);
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        if (!TextUtils.isEmpty(AbSharedUtil.getString(this, AbAppConfig.USERNAME))) {
            if (mEmailView != null) {
                mEmailView.setEnabled(false);
                mEmailView.setText(AbSharedUtil.getString(this, AbAppConfig.NAME));
            }
            if (mEmailSignInButton != null)
                mEmailSignInButton.setText(getString(R.string.action_sign_out_short));

        }
    }

    /**
     * login...
     */
    private void doLogin() {
        String engineerId = AbSharedUtil.getString(this, AbAppConfig.USERNAME);
        if (!TextUtils.isEmpty(engineerId)) {
            new AlertDialog.Builder(LoginActivity.this)
                    .setMessage(getString(R.string.signouthint))
                    .setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            stopLocation();
                        }
                    }).create().show();
        } else {
            login();
        }
    }

    private void login() {
        final String name = mEmailView.getText().toString();
        if (TextUtils.isEmpty(name)) {
            showErroHint(getString(R.string.error_invalid_email), mEmailView);
            return;
        }
        showLoadingDialog(getString(R.string.loading));
        Map<String, String> params = new HashMap<String, String>();
        String times = "" + System.currentTimeMillis();
        String sign = SecurityUtil.md5(name + SecurityUtil.APP_SIGTURE + times);
        params.put("code", name);
        params.put("sign", sign);
        params.put("times", times);
        OkHttpClientManager.postAsyn(AbAppConfig.LOGIN_URL, new OkHttpClientManager.ResultCallback<String>() {

            @Override
            public void onError(Request request, Exception e) {
                dissDialog();
                showErroDialog();
            }

            @Override
            public void onResponse(String response) {
                dissDialog();
                AbLogUtil.e("TAG", "result--->" + response);
                try {
                    JSONObject obj = new JSONObject(response);
                    String message = "";
                    if (obj.has("status")) {
                        int status = obj.getInt("status");
                        String engineerId = obj.has("engineerId") ? obj.get("engineerId").toString() : "";
                        message = obj.has("message") ? obj.get("message").toString() : "";
                        if (status == 0 && !TextUtils.isEmpty(engineerId)) {
                            AbSharedUtil.putString(getApplicationContext(), AbAppConfig.USERNAME, engineerId);
                            AbSharedUtil.putString(getApplicationContext(), AbAppConfig.NAME, name);
                            AbToastUtil.showToast(LoginActivity.this, getString(R.string.str_success));
                            startLocation();
                            finish();
                            return;
                        }
                    }
                    AbToastUtil.showToast(LoginActivity.this, TextUtils.isEmpty(message) ? getString(R.string.erro) : message);
                } catch (Exception e) {
                    AbToastUtil.showToast(LoginActivity.this, getString(R.string.erro));
                }
            }
        }, params);
    }


    /**
     * start location service...
     */
    private void startLocation() {
        Intent locIntent = new Intent(this, LocationService.class);
        if (AbAppUtil.isServiceRunning(this, LocationService.class.getName())) {
            stopService(locIntent);
        }
        startService(locIntent);
    }

    /**
     * stop location service...
     */
    private void stopLocation() {
        if (mEmailView != null) {
            mEmailView.setEnabled(true);
            mEmailView.setText("");
        }
        if (mEmailSignInButton != null)
            mEmailSignInButton.setText(getString(R.string.action_sign_in_short));
        AbSharedUtil.putString(this, AbAppConfig.USERNAME, "");
        Intent locIntent = new Intent(this, LocationService.class);
        stopService(locIntent);
    }

    private boolean mayRequestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showRequestPermissionDialog();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE}, REQUEST_LOCATION);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                container.setVisibility(View.VISIBLE);
                startLocation();
            } else {
                showRequestPermissionDialog();
            }
        }
    }

    private void showRequestPermissionDialog() {
        Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(View v) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                    }
                }).show();
    }

    private void showErroHint(String msg, EditText editText) {
        // 将提示文字改为红色
        ForegroundColorSpan fgcspan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(msg);
        ssbuilder.setSpan(fgcspan, 0, msg.length(), 0);
        editText.setError(ssbuilder);
    }

    private void showLoadingDialog(String msg) {
        dissDialog();
        mDialog = ProgressDialog.show(this, "", msg);
    }

    private void dissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void showErroDialog() {
        new AlertDialog.Builder(LoginActivity.this)
                .setMessage(getString(R.string.tryagain))
                .setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        doLogin();
                    }
                }).create().show();
    }
}

