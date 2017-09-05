package com.gun.mert.yemeklist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
        } else {
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

    public class linkcek extends AsyncTask<Void,Void,Void> {
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
                Sheet datatypeSheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = datatypeSheet.iterator();

                label1:
                while (iterator.hasNext()) {
                    Row currentRow = iterator.next();
                    for (Cell currentCell : currentRow) {
                        if (currentCell.getCellType() == Cell.CELL_TYPE_STRING) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                if (Objects.equals(currentCell.getStringCellValue(), "GIDA MÜHENDİSİ") || Objects.equals(currentCell.getStringCellValue(), "gida muhendisi") || Objects.equals(currentCell.getStringCellValue(), "gıda muhendisi" )) {
                                    break label1;
                                }
                            }
                            mylist.add(currentCell.getStringCellValue());
                        }
                    }
                }
                tatilatici(mylist);
                listeduzenle(mylist);

            } catch (IOException e) {
                e.printStackTrace();
            }/*
            for (int i=1;i<mylist.size();i++){
                System.out.println(mylist.get(i)+": "+i);
            }*/
            return null;
        }

        private void tatilatici(ArrayList<String> mylist) {
            ArrayList bulunanindex = new ArrayList();
            int bulunan = 0;
            for (int i=1;i<mylist.size();i++){
                System.out.println(mylist.get(i)+": "+i);
                if(mylist.get(i).equals("RESMİ TATİL") || mylist.get(i).equals("RESMI TATIL") || mylist.get(i).equals("TATİL") ){
                    bulunanindex.add(i);
                    bulunan = i;
                }
            }
            if (bulunan == 14 || bulunan == 44 || bulunan == 74 || bulunan==104 ){
                for (int i =0;i<bulunanindex.size();i++){
                    mylist.add((Integer) bulunanindex.get(i)-8,"-");
                    mylist.add((Integer) bulunanindex.get(i)-3,"-");
                    mylist.add((Integer) bulunanindex.get(i)+7,"-");
                    mylist.add((Integer) bulunanindex.get(i)+12,"-");
                }
            }else if (bulunan == 15 || bulunan == 45 || bulunan == 75 || bulunan==105){
                for (int i =0;i<bulunanindex.size();i++){
                    mylist.add((Integer) bulunanindex.get(i)-3,"-");
                    mylist.add((Integer) bulunanindex.get(i)+5,"-");
                    mylist.add((Integer) bulunanindex.get(i)+9,"-");
                    mylist.add((Integer) bulunanindex.get(i)+13,"-");
                }
            }else if (bulunan == 16 || bulunan == 46 || bulunan == 76 || bulunan==106){
// TODO: 6.09.2017 Tatil kodu eklenecek 
            }else if (bulunan == 17 || bulunan == 47 || bulunan == 77 || bulunan==107){
// TODO: 6.09.2017 tatil kodu eklenecek 
            }else if (bulunan == 18 || bulunan == 48 || bulunan == 78 || bulunan==108){
// TODO: 6.09.2017 tatil kodu eklenecek 
            }
            /*for (int i=1;i<mylist.size();i++){
                System.out.println(mylist.get(i)+": "+i);
            }*/
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            list = (ListView) findViewById(R.id.listview);
            gunIndex = gunubul()/6;
            loadList(gunIndex);
            progressDialog.dismiss();
            textView.setVisibility(View.INVISIBLE);
        }

        public void listeduzenle(ArrayList<String> mylist) {
            int gunsayac=0,gunsayac2=0;
            int y=0;
            int lastindex=0,lastindex2=0,lastindex3=0;
            String tarih1 = "2017";
            String tarih2 = "2018";
            String tarih3 = "2019";
            for(int i=1;i<=5;i++){
                String[] parcala = mylist.get(i).split(" ");
                for(y=0;y<parcala.length;y++){
                    if(parcala[y].equals(tarih1) || parcala[y].equals(tarih2)|| parcala[y].equals(tarih3)){
                        gunsayac++;
                    }
                }
            }

            if(gunsayac > 1){
                for(int j=1;j<=gunsayac;j++)
                {
                    String str = mylist.get(j);
                    liste2.add(str);
                    str = mylist.get(j+gunsayac);
                    liste2.add(str);
                    str = mylist.get(j+gunsayac*2);
                    liste2.add(str);
                    str = mylist.get(j+gunsayac*3);
                    liste2.add(str);
                    str = mylist.get(j+gunsayac*4);
                    liste2.add(str);
                    str = mylist.get(j+gunsayac*5);
                    liste2.add(str);
                }
            }   else if(gunsayac == 1){
                for(int x=1;x<=6;x++)
                {
                    String str = mylist.get(x);
                    liste2.add(str);
                }
            }
            for(int j=gunsayac*6+1;j<=gunsayac*6+5;j++)
            {
                String str = mylist.get(j);
                liste2.add(str);
                str = mylist.get(j+5);
                liste2.add(str);
                str = mylist.get(j+10);
                liste2.add(str);
                str = mylist.get(j+15);
                liste2.add(str);
                str = mylist.get(j+20);
                liste2.add(str);
                str = mylist.get(j+25);
                liste2.add(str);
                lastindex=j+25;
            }

            for(int j=1+lastindex;j<=lastindex+5;j++)
            {
                String str = mylist.get(j);
                liste2.add(str);
                str = mylist.get(j+5);
                liste2.add(str);
                str = mylist.get(j+10);
                liste2.add(str);
                str = mylist.get(j+15);
                liste2.add(str);
                str = mylist.get(j+20);
                liste2.add(str);
                str = mylist.get(j+25);
                liste2.add(str);
                lastindex2=j+25;
            }


            for(int j=lastindex2+1;j<=lastindex2+5;j++)
            {
                String str = mylist.get(j);
                liste2.add(str);
                str = mylist.get(j+5);
                liste2.add(str);
                str = mylist.get(j+10);
                liste2.add(str);
                str = mylist.get(j+15);
                liste2.add(str);
                str = mylist.get(j+20);
                liste2.add(str);
                str = mylist.get(j+25);
                liste2.add(str);
                lastindex3=j+25;
            }
            if (lastindex3<121){

            }else {
                for (int i = lastindex3 + 1; i <= lastindex3 + 5; i++) {
                    String[] parcala = mylist.get(i).split(" ");
                    for (y = 0; y < parcala.length; y++) {
                        if (parcala[y].equals(tarih1) || parcala[y].equals(tarih2) || parcala[y].equals(tarih3)) {
                            gunsayac2++;
                        }
                    }
                }
                if (gunsayac2 > 1) {
                    for (int j = lastindex3 + 1; j <= lastindex3 + gunsayac2; j++) {
                        String str = mylist.get(j);
                        liste2.add(str);
                        str = mylist.get(j + gunsayac2);
                        liste2.add(str);
                        str = mylist.get(j + gunsayac2 * 2);
                        liste2.add(str);
                        str = mylist.get(j + gunsayac2 * 3);
                        liste2.add(str);
                        str = mylist.get(j + gunsayac2 * 4);
                        liste2.add(str);
                        str = mylist.get(j + gunsayac2 * 5);
                        liste2.add(str);
                    }
                } else if (gunsayac2 == 1) {
                    for (int x = lastindex3 + 1; x <= lastindex3 + 6; x++) {
                        String str = mylist.get(x);
                        liste2.add(str);
                    }
                }
            }

            TOTAL_LIST_ITEMS = liste2.size();
            pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE;
        }

        public int gunubul(){
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
                }catch (NumberFormatException e){
                    continue;
                }
            }
            return 0;
        }
    }
}
