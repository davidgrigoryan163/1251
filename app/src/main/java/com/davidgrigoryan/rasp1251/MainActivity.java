package com.davidgrigoryan.rasp1251;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.marcoscg.licenser.Library;
import com.marcoscg.licenser.License;
import com.marcoscg.licenser.LicenserDialog;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import de.cketti.library.changelog.ChangeLog;



public class MainActivity extends AppCompatActivity {
    private static WebView web;
    private WebView mWebView;
    private java.lang.String url;
    Boolean isInternetPresent = false;
    private int REQUEST_CODE_WRITE_EXTERNAL_STORAGE;
    private String currentVersion;
    private LicenserDialog licenserDialog;
    private RelativeLayout rlRate;
    private Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        licenserDialog = new LicenserDialog(this, R.style.DialogStyle)
                .setTitle("Лицензия ПО")
                .setCancelable(true)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setCustomNoticeTitle("Файлы:")
                .setLibrary(new Library("Android Support Libraries",
                        "https://developer.android.com/topic/libraries/support-library/index.html",
                        License.APACHE))
                .setLibrary(new Library("Apache 2.0",
                        "https://www.apache.org/licenses/LICENSE-2.0",
                        License.APACHE))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: 11/02/2018
                    }
                });
        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
        Timer repeatTask = new Timer();
        repeatTask.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("http://www.smedk.ru/wp-content/uploads/files/education/rasp/1251.htm");
                    }
                });
            }
        }, 0, 60000);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                1);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_image();
            }
        });
        mWebView = (WebView) findViewById(R.id.view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("http://www.smedk.ru/wp-content/uploads/files/education/rasp/1251.htm");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        String newUA = "User Agent";
        newUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        mWebView.getSettings().setUserAgentString(newUA);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.clearCache(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo i = manager.getActiveNetworkInfo();
        boolean hasConnect = (i != null && i.isConnected() && i.isAvailable());

        if (hasConnect) {
        } else {
        }
        final ProgressDialog pd = ProgressDialog.show(MainActivity.this, "Загрузка расписания...", "Обновление данных...", true);
        mWebView.setWebViewClient(new MyWebViewClient());
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
                try {
                    webView.stopLoading();
                } catch (Exception e) {
                }

                if (webView.canGoBack()) {
                    webView.goBack();
                }

                webView.loadUrl("about:blank");
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Нет интернет подключения!");
                alertDialog.setMessage("Пожайлуйста убедитесть включен ли " +
                        "Wi-Fi или мобильные данные и повторите попытку. ");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                alertDialog.show();
                super.onReceivedError(webView, errorCode, description, failingUrl);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                pd.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                pd.dismiss();
                Toast.makeText(MainActivity.this, "Расписание загружено", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Обновление данных завершено", Toast.LENGTH_SHORT).show();
                String webUrl = mWebView.getUrl();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rate:
                showDialog();
                break;
            case R.id.changelog: {
                new DarkThemeChangeLog(this).getFullLogDialog().show();
                break;
            }
            case R.id.about:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.license:
                licenserDialog.show();
                return true;
        }
        return true;
    }

    public static class DarkThemeChangeLog extends ChangeLog {
        public static final String DARK_THEME_CSS =
                "body { color: #212121; background-color: #ffffff; }" + "\n" + DEFAULT_CSS;

        public DarkThemeChangeLog(Context context) {
            super(new ContextThemeWrapper(context, R.style.DarkTheme), DARK_THEME_CSS);
        }
    }

    //Обновление приложения
    private class GetVersionCode extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String newVersion = null;
            try {
                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName() + "&hl=it")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                return newVersion;
            } catch (Exception e) {
                return newVersion;
            }
        }
        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
                }
            }
            Log.d("Обновить", "Текущая версия " + currentVersion + "Версия приложения из PlayMarket" + onlineVersion);
        }
    }
        private void save_image () {
            Picture picture = mWebView.capturePicture();
            Bitmap b = Bitmap.createBitmap(picture.getWidth(),
                    picture.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            Toast.makeText(MainActivity.this, "Изображения сохранено в формате JPG. Файл находится:" + "/sdcard/Pictures/image.jpg", Toast.LENGTH_SHORT).show();
            picture.draw(c);
            FileOutputStream fos = null;
            try {

                fos = new FileOutputStream("mnt/sdcard/Pictures/image.jpg");
                if (fos != null) {
                    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                    fos.close();
                }
            } catch (Exception e) {
            }
        }
    private void showDialog() {
        final RatingDialog ratingDialog = new RatingDialog.Builder(this)
                .icon(drawable)
                .session(1)
                .threshold(1)
                .title("Понравилось мое приложение?")
                .titleTextColor(R.color.black)
                .positiveButtonText("Не сейчас")
                .negativeButtonText("Никогда")
                .positiveButtonTextColor(R.color.black)
                .negativeButtonTextColor(R.color.grey_500)
                .formTitle("Оставить отзыв")
                .formHint("Напишите мне отзыв это поможет улучшить приложение")
                .formSubmitText("Отправить")
                .formCancelText("Отмена")
                .ratingBarColor(R.color.yellow)
                .playstoreUrl("https://play.google.com/store/apps/details?id=com.davidgrigoryan.rasp1251")
                .onThresholdCleared(new RatingDialog.Builder.RatingThresholdClearedListener() {
                    @Override
                    public void onThresholdCleared(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                        ratingDialog.dismiss();
                    }
                })
                .onThresholdFailed(new RatingDialog.Builder.RatingThresholdFailedListener() {
                    @Override
                    public void onThresholdFailed(RatingDialog ratingDialog, float rating, boolean thresholdCleared) {
                        ratingDialog.dismiss();
                    }
                })
                .onRatingChanged(new RatingDialog.Builder.RatingDialogListener() {
                    @Override
                    public void onRatingSelected(float rating, boolean thresholdCleared) {
                        Toast.makeText(MainActivity.this, "Спасибо за отзыв!", Toast.LENGTH_SHORT).show();
                    }
                })
                .onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() {
                    @Override
                    public void onFormSubmitted(String feedback) {

                    }
                }).build();
        ratingDialog.show();
    }
}
