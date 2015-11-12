package condi.kr.ac.swu.condidemo.activity;

import android.app.Application;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.BackPressCloseHandler;
import condi.kr.ac.swu.condidemo.data.GlobalApplication;

public class GoalActivity extends BaseActivity {

    private NetworkImageView imgMap;
    private Button startNext;
    private final String SERVER_ADDRESS = "http://condi.swu.ac.kr:80/condi2/picture/";
    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);
        initActionBar("목표 완성");
        backPressCloseHandler = new BackPressCloseHandler(this);
        initView();
    }

    private void initView(){
        imgMap = (NetworkImageView) findViewById(R.id.imgMapNext);
        startNext = (Button) findViewById(R.id.startNext);
        setMapURL("map2.png");
    }

    public void setMapURL(final String mapImageURL) {
        if (imgMap != null && mapImageURL != null) {
            Application app = GlobalApplication.getGlobalApplicationContext();
            if (app == null)
                throw new UnsupportedOperationException("needs com.kakao.GlobalApplication in order to use ImageLoader");
            imgMap.setImageUrl(SERVER_ADDRESS+mapImageURL, ((GlobalApplication) app).getImageLoader());
        }
    }

    @Override
    public void onClick(View v) {
        if(v == startNext) {
            startActivity(new Intent(getApplicationContext(), StartNewActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(slidingMenu.isMenuShowing()) {
            slidingMenu.toggle();
            return;
        }

        backPressCloseHandler.onBackPressed();
    }
}
