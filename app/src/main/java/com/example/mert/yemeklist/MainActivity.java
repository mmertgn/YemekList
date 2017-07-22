package com.example.mert.yemeklist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnileri = (Button) findViewById(R.id.btn_ileri);
        btngeri = (Button) findViewById(R.id.btn_geri);
        btntoday = (Button) findViewById(R.id.btn_today);
        textView = (TextView) findViewById(R.id.textView);
        new linkcek().execute();
        btnileri.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                increment++;
                loadList(increment);
                CheckEnable();
            }
        });

        btngeri.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                increment--;
                loadList(increment);
                CheckEnable();
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
            progressDialog.setMessage("Liste İndiriliyor ve İşleniyor...");
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

        public void listeduzenle(ArrayList<String> mylist){
            for(int j=1;j<=5;j++)
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
            }
            for(int j=31;j<=35;j++)
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
            }
            for(int j=61;j<=65;j++)
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
            }
            for(int j=91;j<=95;j++)
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
            }/*
            for(int j=121;j<=126;j++)
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
            }*/
            System.out.println(liste2+"\n");
            TOTAL_LIST_ITEMS = liste2.size();
            pageCount = TOTAL_LIST_ITEMS/NUM_ITEMS_PAGE;
            System.out.println("page : " + pageCount);
        }

        public int gunubul(){
            bugun = Calendar.getInstance().get(Calendar.DATE);
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
