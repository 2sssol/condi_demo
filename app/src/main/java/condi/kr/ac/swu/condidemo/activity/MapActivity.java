package condi.kr.ac.swu.condidemo.activity;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.android.volley.toolbox.NetworkImageView;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.GlobalApplication;

public class MapActivity extends BaseActivity {

    private NetworkImageView imgMap;
    private final String SERVER_ADDRESS = "http://condi.swu.ac.kr:80/condi2/picture/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initActionBar("지도 완성");
        imgMap = (NetworkImageView) findViewById(R.id.imgMap_map);
        setMapURL("map1.png");
    }

    public void setMapURL(final String mapImageURL) {
        if (imgMap != null && mapImageURL != null) {
            Application app = GlobalApplication.getGlobalApplicationContext();
            if (app == null)
                throw new UnsupportedOperationException("needs com.kakao.GlobalApplication in order to use ImageLoader");
            imgMap.setImageUrl(SERVER_ADDRESS+mapImageURL, ((GlobalApplication) app).getImageLoader());
        }
    }

}
