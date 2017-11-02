package com.gun.mert.yemeklist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    public ArrayList<String> mylist = new ArrayList<>();
    public ArrayList<String> liste2 = new ArrayList<>();
    public ArrayAdapter<String> adapter;
    ListView list;
    public int TOTAL_LIST_ITEMS = 1000;
    public int NUM_ITEMS_PAGE   = 6;
    private int pageCount;
    public int gunIndex;
    private int increment = 0;
    private Button btnileri;
    private Button btngeri;
    private Button btntoday;
    private TextView textView;
    private int bugun;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-1016425908924617~6563484486");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1016425908924617/4150387210");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        btnileri = (Button) findViewById(R.id.btn_ileri);
        btngeri = (Button) findViewById(R.id.btn_geri);
        btntoday = (Button) findViewById(R.id.btn_today);
        textView = (TextView) findViewById(R.id.textView);
        if (isNetworkConnected()){
            try {
                new linkcek().execute();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                btnileri.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        increment++;
                        loadList(increment);
                        CheckEnable();
                        showInterstitial();
                    }
                });

                btngeri.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View v) {
                        increment--;
                        loadList(increment);
                        CheckEnable();
                        showInterstitial();
                    }
                });

                btntoday.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        list.setAdapter(null);
                        linkcek gunobject = new linkcek();
                        gunobject.gunubul();
                        loadList(gunIndex);
                        CheckEnable();
                    }
                });
            }catch (Exception ignored){}

        }else {
            btntoday.setEnabled(false);
            btnileri.setEnabled(false);
            btngeri.setEnabled(false);
            Toast toast = Toast.makeText(getApplicationContext(),"Uygulamayı kullanbilmek için internet bağlantısı gerekmektedir...",Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,Gravity.CENTER,Gravity.CENTER);
            toast.show();
        }

    }
    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this,Hakkimizda.class);
            startActivity(intent);
        }
        else if (id == R.id.action_share) {
            final String appPackageName = getPackageName();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Uygulamayı adresten indirebilirsiniz: https://play.google.com/store/apps/details?id="+ appPackageName);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void CheckEnable()
    {

        if(increment+1 == pageCount)
        {
            btnileri.setEnabled(false);
        }
        else if(increment == 0)
        {
            btngeri.setEnabled(false);
        }
        else
        {
            btngeri.setEnabled(true);
            btnileri.setEnabled(true);
        }
    }


    /*
    /**
     * Method for loading data in listview
     * @param number
     */
    private void loadList(int number)
    {
        ArrayList<String> sort = new ArrayList<String>();
        int start = number * NUM_ITEMS_PAGE;
        for(int i=start;i<(start)+NUM_ITEMS_PAGE;i++)
        {
            if(i<liste2.size())
            {
                sort.add(liste2.get(i));
            }
            else
            {
                break;
            }
        }
        adapter = new ArrayAdapter<String>(this, R.layout.textcenter, sort);
        list.setAdapter(adapter);
    }

    private class linkcek extends AsyncTask<Void,Void,Void> {
        String link1;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Yemek Listesi");
            progressDialog.setMessage("İndiriliyor ve İşleniyor...");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... params) {

            try {
                Document doc = Jsoup.connect("http://sks.karabuk.edu.tr/index.aspx").get();
                Elements info = doc.select("li[id=23XxX");
                Elements info2 = info.select("a[class=menuStil1");
                link1 = info2.attr("href");
                System.out.println("link:"+link1);
                URL url = new URL("http://sks.karabuk.edu.tr"+link1);

                InputStream inputStream = url.openStream();
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                for (int i = 0; i<5;i++){ //gezilecek sütun sayısı
                    for (int j=1; j< sheet.getLastRowNum() + 1; j++) {
                        Row row = sheet.getRow(j);
                        Cell cell = row.getCell(i);
                        try {
                            if (cell.toString()!=""){
                                if (!cell.toString().equals(" "))
                                    mylist.add(cell.getStringCellValue());
                            }
                        }catch (NullPointerException ignored){

                        }
                    }
                }
                listeduzenle(mylist);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            list = (ListView) findViewById(R.id.listview);
            gunIndex = gunubul()/6;
            loadList(gunIndex);
            progressDialog.dismiss();
            textView.setVisibility(View.INVISIBLE);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        void listeduzenle(ArrayList<String> mylist) {
            String Gunler[] = {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
            for (String aGunler : Gunler) {
                for (int j = 0; j <= mylist.size(); j++) {
                    try {
                        String[] parcala = mylist.get(j).split(" ");
                        if (Objects.equals(aGunler, parcala[0])) {
                            for (int k = 0; k < 6; k++) {
                                liste2.add(mylist.get(j + k)); //Bulunan gündeki yemekler sıralanıyor
                                System.out.println("liste Hali: " + mylist.get(j + k) + ": ");
                            }
                            break;
                        }
                    } catch (RuntimeException ignored) {

                    }
                }
            }

            TOTAL_LIST_ITEMS = liste2.size();
            pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE;
        }

        private int gunubul(){
            String aylar[]={"Ocak","Şubat","Mart","Nisan","Mayıs","Haziran","Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık"};
            Calendar simdi=Calendar.getInstance();
            System.out.println(aylar[simdi.get(Calendar.MONTH)]);
            System.out.println(simdi.get(Calendar.DATE));
            System.out.println(simdi.get(Calendar.YEAR));

            bugun = Calendar.getInstance().get(Calendar.DATE);

            int dayofweek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

            if (dayofweek==7) {
                bugun = bugun+2;
            }else if (dayofweek==1) {
                bugun = bugun+1;
            }
            System.out.println(bugun);
            for (int i = 0;i<liste2.size();i++){
                String[] parcala = liste2.get(i).split(" ");
                try {
                    int gun = Integer.parseInt(parcala[0]);
                    if (bugun == gun){
                        increment = i/6;
                        return i;
                    }
                }catch (NumberFormatException ignored){
                }
            }
            return 0;
        }
    }
}
