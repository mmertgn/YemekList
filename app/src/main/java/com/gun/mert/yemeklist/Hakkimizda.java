package com.gun.mert.yemeklist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class Hakkimizda extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element versionElement = new Element();
        versionElement.setTitle("Sürüm 1.5");

        Element adsElement = new Element();
        adsElement.setTitle("Mustafa Mert Gün");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.simge)
                .setDescription("Karabük Üni. yemekhanesinde verilen yemeklerin güncel listesinin bulunduğu uygulamamı kullandığınız için teşekkür ederim. Uygulama otomatik olarak okul tarafından yayınlanan programı çekip listelemektedir. Hiç bir zaman güncelliğini kaybetme gibi bir durum ortaya çıkarmayacaktır.\n")
                .addItem(versionElement)
                .addItem(adsElement)
                .addGroup("Bize Ulaşın")
                .addEmail("m.mertgn@gmail.com")
                .addWebsite("http://mertgun.net/")
                .addFacebook("mert.gun.125")
                .addTwitter("MertGn2")
                .addPlayStore("com.gun.mert.yemeklist")
                .addInstagram("mertgun2")
                .create();
        setContentView(aboutPage);
    }
}
