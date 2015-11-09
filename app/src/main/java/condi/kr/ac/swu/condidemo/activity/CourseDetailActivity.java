package condi.kr.ac.swu.condidemo.activity;

import android.app.Application;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.GlobalApplication;
import condi.kr.ac.swu.condidemo.data.NetworkAction;

public class CourseDetailActivity extends BaseActivity {


    private int MAX_PAGE=6;
    private Fragment cur_fragment=new Fragment();
    private ViewPager viewPager;

    private String id;
    private String[] cids;
    private TextView info_each_name, info_each_km, txtCourseInfoDetail1, txtCourseInfoDetail2;
    private NetworkImageView imgCourseDetail;
    private ImageView icon_info_each_photo1, icon_info_each_photo2, icon_info_each_photo3, icon_info_each_photo4, icon_info_each_photo5, icon_info_each_photo6;

    private ImageButton btnBreforeCourseDetail, btnAfterCourseDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);
        initActionBar("코스 정보");

        viewPager=(ViewPager)findViewById(R.id.viewPager);
        viewPager.setAdapter(new adapter(getSupportFragmentManager()));

        id = getIntent().getStringExtra("id");
        cids = getIntent().getStringArrayExtra("cids");
        for (String s : cids)
            printErrorMsg("cids: "+s);

        int position = 0;
        for(int i =0; i < cids.length; i++) {
            if (id.equals(cids[i]))
                position = i;
        }
        printErrorMsg("position" + position);
        viewPager.setCurrentItem(R.layout.course_detail);

    }

    private class adapter extends FragmentPagerAdapter {

        public adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(final int position) {
            if(position<0 || MAX_PAGE<=position)
                return null;

            cur_fragment=new Fragment() {

                @Override
                public void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                }

                @Nullable
                @Override
                public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                    LinearLayout linearLayout=(LinearLayout)inflater.inflate(R.layout.course_detail, container, false);
                    initView(linearLayout, cids[position], position);

                    return linearLayout;
                }
            };

            return cur_fragment;
        }

        @Override
        public int getCount() {
            return MAX_PAGE;
        }
    }

    private void initView(LinearLayout linearLayout, String cid, int index) {
        info_each_name = (TextView) linearLayout.findViewById(R.id.info_each_name);
        info_each_km = (TextView) linearLayout.findViewById(R.id.info_each_km);
        txtCourseInfoDetail1 = (TextView) linearLayout.findViewById(R.id.txtCourseInfoDetail1);
        txtCourseInfoDetail2 = (TextView) linearLayout.findViewById(R.id.txtCourseInfoDetail2);
        imgCourseDetail = (NetworkImageView) linearLayout.findViewById(R.id.imgCourseDetail);

        icon_info_each_photo1 = (ImageView) linearLayout.findViewById(R.id.icon_info_each_photo1);
        icon_info_each_photo2 = (ImageView) linearLayout.findViewById(R.id.icon_info_each_photo2);
        icon_info_each_photo3 = (ImageView) linearLayout.findViewById(R.id.icon_info_each_photo3);
        icon_info_each_photo4 = (ImageView) linearLayout.findViewById(R.id.icon_info_each_photo4);
        icon_info_each_photo5 = (ImageView) linearLayout.findViewById(R.id.icon_info_each_photo5);
        icon_info_each_photo6 = (ImageView) linearLayout.findViewById(R.id.icon_info_each_photo6);

        btnBreforeCourseDetail = (ImageButton) linearLayout.findViewById(R.id.btnBreforeCourseDetail);
        btnAfterCourseDetail = (ImageButton) linearLayout.findViewById(R.id.btnAfterCourseDetail);

        btnBreforeCourseDetail.setOnClickListener(this);
        btnAfterCourseDetail.setOnClickListener(this);

        switch (index) {
            case 0:
                icon_info_each_photo1.setImageResource(R.drawable.info_each_circle_big);
                icon_info_each_photo1.getLayoutParams().height = 40;
                icon_info_each_photo1.getLayoutParams().width = 40;

                icon_info_each_photo2.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo3.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo4.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo5.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo6.setImageResource(R.drawable.info_each_circle_small);
                break;
            case 1:
                icon_info_each_photo1.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo2.setImageResource(R.drawable.info_each_circle_big);
                icon_info_each_photo2.getLayoutParams().height = 40;
                icon_info_each_photo2.getLayoutParams().width = 40;

                icon_info_each_photo3.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo4.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo5.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo6.setImageResource(R.drawable.info_each_circle_small);
                break;
            case 2:
                icon_info_each_photo1.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo2.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo3.setImageResource(R.drawable.info_each_circle_big);
                icon_info_each_photo3.getLayoutParams().height = 40;
                icon_info_each_photo3.getLayoutParams().width = 40;

                icon_info_each_photo4.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo5.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo6.setImageResource(R.drawable.info_each_circle_small);
                break;
            case 3:
                icon_info_each_photo1.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo2.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo3.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo4.setImageResource(R.drawable.info_each_circle_big);
                icon_info_each_photo4.getLayoutParams().height = 40;
                icon_info_each_photo4.getLayoutParams().width = 40;

                icon_info_each_photo5.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo6.setImageResource(R.drawable.info_each_circle_small);
                break;
            case 4:
                icon_info_each_photo1.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo2.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo3.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo4.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo5.setImageResource(R.drawable.info_each_circle_big);
                icon_info_each_photo5.getLayoutParams().height = 40;
                icon_info_each_photo5.getLayoutParams().width = 40;

                icon_info_each_photo6.setImageResource(R.drawable.info_each_circle_small);
                break;
            case 5:
                icon_info_each_photo1.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo2.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo3.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo4.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo5.setImageResource(R.drawable.info_each_circle_small);
                icon_info_each_photo6.setImageResource(R.drawable.info_each_circle_big);
                icon_info_each_photo6.getLayoutParams().height = 40;
                icon_info_each_photo6.getLayoutParams().width = 40;
                break;
        }

        setInfo(cid);
    }

    private void setInfo(final String cid) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String dml = "select * from course where id="+cid;
                return NetworkAction.sendDataToServer("course.php", dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                new AsyncTask() {
                    List<Properties> list;
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            list = NetworkAction.parse("course.xml", "course");
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);

                        info_each_name.setText(list.get(0).getProperty("name"));
                        info_each_km.setText(list.get(0).getProperty("km")+" KM");
                        setCourseImageURL(imgCourseDetail,list.get(0).getProperty("picture") );

                        String info1, info2;
                        if(list.get(0).getProperty("info").length()>20) {
                            info1 = list.get(0).getProperty("info").substring(0, 19);
                            info2 = list.get(0).getProperty("info").substring(19);
                        } else {
                            info1 = list.get(0).getProperty("info");
                            info2 = "";
                        }
                        txtCourseInfoDetail1.setText(info1);
                        txtCourseInfoDetail2.setText(info2);
                    }
                }.execute();
            }
        }.execute();
    }

    public void setCourseImageURL(final NetworkImageView imageView, final String imageURL) {
        if (imageView != null && imageURL != null) {
            Application app = GlobalApplication.getGlobalApplicationContext();
            if (app == null)
                throw new UnsupportedOperationException("needs com.kakao.GlobalApplication in order to use ImageLoader");
            imageView.setImageUrl("http://condi.swu.ac.kr:80/condi2/course/"+imageURL, ((GlobalApplication) app).getImageLoader());
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if(v==btnBreforeCourseDetail) {
            if(viewPager.getCurrentItem()==0)
                viewPager.setCurrentItem(MAX_PAGE-1);
            else
                viewPager.setCurrentItem(viewPager.getCurrentItem()-1);

        } else if(v==btnAfterCourseDetail) {
            if(viewPager.getCurrentItem()==MAX_PAGE-1)
                viewPager.setCurrentItem(0);
            else
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
        }
    }
}
