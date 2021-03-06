package me.yluo.ruisiapp.activity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.utils.IntentUtils;
import me.yluo.ruisiapp.widget.htmlview.HtmlView;


/**
 * Created by yluo on 2015/10/5 0005.
 * 关于页面
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
        }

        TextView version = findViewById(R.id.version);
        TextView serverVersion = findViewById(R.id.server_version);
        findViewById(R.id.btn_back).setOnClickListener(view -> finish());

        String ss = "<b>达尔问手机客户端</b><br />功能不断完善中，bug较多还请多多反馈......<br />";

        TextView htmlView = findViewById(R.id.html_text);
        HtmlView.parseHtml(ss).into(htmlView);

        PackageInfo info = null;
        PackageManager manager = getPackageManager();
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int versionCode = 0;
        if (info != null) {
            String version_name = info.versionName;
            versionCode = info.versionCode;
            String a = "当前版本:" + version_name;
            version.setText(a);
        }

        findViewById(R.id.fab).setOnClickListener(v -> Snackbar.make(v, "你要提交bug或者建议吗?", Snackbar.LENGTH_LONG)
                .setAction("确定", view -> {
                    String user = App.getName(AboutActivity.this);
                    if (user != null) {
                        user = "by:" + user;
                    }
                    IntentUtils.sendMail(getApplicationContext(), user);
                })
                .show());

        int finalVersionCode = versionCode;

        // 检查更新实现 读取我发帖的标题比较版本号
        // 我会把版本号写在标题上[code:xxx]
        HttpUtil.get(App.CHECK_UPDATE_URL, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                int ih = res.indexOf("keywords");
                int h_start = res.indexOf('\"', ih + 15);
                int h_end = res.indexOf('\"', h_start + 1);
                String title = res.substring(h_start + 1, h_end);
                if (title.contains("code")) {
                    SharedPreferences.Editor editor = getSharedPreferences(App.MY_SHP_NAME, MODE_PRIVATE).edit();
                    editor.putLong(App.CHECK_UPDATE_KEY, System.currentTimeMillis());
                    editor.apply();
                    int st = title.indexOf("code");
                    int code = GetId.getNumber(title.substring(st));
                    if (code > finalVersionCode) {
                        serverVersion.setText("检测到新版本点击查看");
                        serverVersion.setOnClickListener(view -> PostActivity.open(AboutActivity.this, App.CHECK_UPDATE_URL, "谁用了FREEDOM"));
                        return;
                    }
                }

                serverVersion.setText("当前已是最新版本");
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                serverVersion.setText("检测新版本失败...");
            }
        });
    }

}
