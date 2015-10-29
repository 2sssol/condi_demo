package condi.kr.ac.swu.condidemo.activity;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.GlobalApplication;
import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;
import condi.kr.ac.swu.condidemo.view.CircularNetworkImageView;
import condi.kr.ac.swu.condidemo.view.CustomCircularRingView;
import condi.kr.ac.swu.condidemo.view.PatchPointView;
import condi.kr.ac.swu.condidemo.view.adapter.GroupListAdapter;

public class GroupActivity extends BaseActivity {

    private CustomCircularRingView myView;
    private PatchPointView patchPointView;

    private TextView txtTotalDate, txtTotalKM ; // 전체 일수, km
    private TextView txtPercent, txtCurrentDate, txtCurrentKM;

    private Button btnMap, btnTodolist;

    // 경로
    private TextView txtCourseName1, txtCourseName2, txtCourseName3, txtCourseName4;   // 나머지 코스 이름
    private float courseKm1, courseKm2, courseKm3, courseKm4;

    private ListView grouplv;
    private GroupListAdapter adapter;

    private int walk = 0;
    private int period = 0;
    private float totalKM;

    // my
    private CircularNetworkImageView p1;
    private TextView pname1, pcurrent1_km, pcurrent1_step, pkm1, pcourse1;

    // thread
    private Handler graphHandler = new Handler();
    private Thread th;
    int percent = 0;
    int currentStep = 0;
    long currentKM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        initActionBar("어울림");
        initView();
    }

    private void initView() {
        // graph
        myView = (CustomCircularRingView) findViewById(R.id.customCircularRingView);
        myView.changePercentage(0);
        myView.invalidate();

        patchPointView = (PatchPointView) findViewById(R.id.patchPointView);
        txtPercent = (TextView) findViewById(R.id.txtPercent);
        txtCurrentKM = (TextView) findViewById(R.id.txtCurrentKM);

        // 경로
        txtCourseName1 = (TextView) findViewById(R.id.txtCourseName1);
        txtCourseName2 = (TextView) findViewById(R.id.txtCourseName2);
        txtCourseName3 = (TextView) findViewById(R.id.txtCourseName3);
        txtCourseName4 = (TextView) findViewById(R.id.txtCourseName4);

        // 버튼
        btnMap = (Button) findViewById(R.id.btnMap);
        btnTodolist = (Button) findViewById(R.id.btnTodolist);

        // date
        txtTotalDate = (TextView) findViewById(R.id.txtTotalDate);
        txtTotalKM = (TextView) findViewById(R.id.txtTotalKM);
        txtCurrentDate = (TextView) findViewById(R.id.txtCurrentDate);
        setDateKM ();

        // my
        p1 = (CircularNetworkImageView) findViewById(R.id.p1);
        pname1 = (TextView) findViewById(R.id.pname1);
        pcurrent1_km = (TextView) findViewById(R.id.pcurrent1_km);
        pcurrent1_step = (TextView) findViewById(R.id.pcurrent1_step);
        pcourse1 = (TextView) findViewById(R.id.pcourse1);
        pkm1 = (TextView) findViewById(R.id.pkm1);
        setMy();
        setOther();

        setMyView();

        btnTodolist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PromiseActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

            }
        });
    }

    private void setDateKM () {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String dml = "select goaldays, goalkm, date_format(startdate,'%y%m%d') as startdate " +
                        "from groups " +
                        "where id="+ Session.GROUPS;
                return NetworkAction.sendDataToServer("goaldayskm.php", dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                String[] results;
                if(o.equals("error"))
                    printErrorMsg("error : cannot select goaldays, goalkm");
                else {
                    results = o.toString().split("/");
                    txtTotalDate.setText(results[0]);
                    txtTotalKM.setText(results[1]);
                    totalKM = Float.parseFloat(results[1]);

                    printErrorMsg("totalKM : " + totalKM);
                    /*
                    * current date
                    * */
                    try {
                        Date startDate = new SimpleDateFormat("yyMMdd").parse(results[2]);       // startdate
                        Date today = new Date();

                        period = startDate.compareTo(today);
                        txtCurrentDate.setText(Integer.toString(period));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    private void setMy() {
        setProfileURL(p1, Session.PROFILE);
        pname1.setText(Session.NICKNAME);
        setSensor();

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String dml = "select * from course where id in (select course from member where groups="+Session.GROUPS+")";
                return NetworkAction.sendDataToServer("course.php", dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if(!o.equals("error")) {
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

                            for(Properties p : list) {
                                if(p.getProperty("id").equals(Session.COURSE)) {
                                    pcourse1.setText(p.getProperty("name"));
                                    pkm1.setText(p.getProperty("km"));
                                }
                            }

                            double[] km = new double[list.size()];
                            int cnt = 0;
                            int sum = 0;
                            List<Integer> points = new ArrayList<Integer>();
                            for(Properties p : list) {
                                points.add(sum);
                                km[cnt] = Double.parseDouble(p.getProperty("km"));
                                sum +=(int)(km[cnt]/totalKM*100);

                                switch (cnt) {
                                    case 0 :
                                        txtCourseName1.setText(p.getProperty("name"));
                                        courseKm1 = Float.parseFloat(p.getProperty("km"));
                                        break;
                                    case 1 :
                                        txtCourseName2.setText(p.getProperty("name"));
                                        courseKm2 = Float.parseFloat(p.getProperty("km"));
                                        break;
                                    case 2 :
                                        txtCourseName3.setText(p.getProperty("name"));
                                        courseKm3 = Float.parseFloat(p.getProperty("km"));
                                        break;
                                    case 3 :
                                        txtCourseName4.setText(p.getProperty("name"));
                                        courseKm4 = Float.parseFloat(p.getProperty("km"));
                                        break;
                                }
                                cnt++;
                            }
                            patchPointView.setPercentToPoint(points);
                            patchPointView.invalidate();

                        }
                    }.execute();
                } else {
                    printErrorMsg("myView 에서 error 입니다.");
                }
            }
        }.execute();

    }

    private void setMyView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                while (percent < 100) {
                    String dml = "select sum(currentwalk) as count " +
                            "from walk " +
                            "where groups="+Session.GROUPS;
                    result = NetworkAction.sendDataToServer("sum.php", dml);
                    if(result.equals("")||result.isEmpty())
                        result = "0";
                    currentStep = Integer.parseInt(result);
                    currentKM = Math.round(currentStep * 0.011559 * 100)/100;
                    percent = (int)((float) currentKM / totalKM *100);

                    graphHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myView.changePercentage(percent);
                            myView.invalidate();

                            txtPercent.setText(Integer.toString(percent));
                            txtCurrentKM.setText(Float.toString(currentKM));

                            if(currentKM > courseKm1) {
                                if(currentKM > courseKm1+courseKm2) {
                                    if(currentKM > courseKm1+courseKm2+courseKm3) {
                                        if(currentKM > courseKm1+courseKm2+courseKm3+courseKm4) {
                                            toastErrorMsg("목표에 도달하셨습니다.");
                                        } else {
                                            txtCourseName1.setBackgroundResource(R.drawable.route_blank_filled);
                                            txtCourseName2.setBackgroundResource(R.drawable.route_blank_filled);
                                            txtCourseName3.setBackgroundResource(R.drawable.route_blank_filled);
                                            txtCourseName4.setBackgroundResource(R.drawable.route_blank_filled);
                                        }
                                    } else {
                                        txtCourseName1.setBackgroundResource(R.drawable.route_blank_filled);
                                        txtCourseName2.setBackgroundResource(R.drawable.route_blank_filled);
                                        txtCourseName3.setBackgroundResource(R.drawable.route_blank_filled);
                                    }
                                } else {
                                    txtCourseName1.setBackgroundResource(R.drawable.route_blank_filled);
                                    txtCourseName2.setBackgroundResource(R.drawable.route_blank_filled);
                                }
                            } else {
                                txtCourseName1.setBackgroundResource(R.drawable.route_blank_filled);
                            }
                        }
                    });

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void setOther() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String dml = "select m.id as mid, m.nickname as mname, m.profile as mprofile, " +
                        "c.id as cid, c.name as cname, c.km as ckm " +
                        "from member m, course c " +
                        "where m.course = c.id and m.groups="+Session.GROUPS+" and m.id!='"+Session.ID+"'";
                return NetworkAction.sendDataToServer("coursemember.php",dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(o.equals("success")) {
                    new AsyncTask() {
                        List<Properties> friends;
                        @Override
                        protected Object doInBackground(Object[] objects) {
                            try {
                                friends = NetworkAction.parse("coursemember.xml", "coursemember");
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

                            grouplv = (ListView) findViewById(R.id.groups_lv);
                            adapter = new GroupListAdapter(GroupActivity.this.getApplicationContext(), friends);
                            grouplv.setAdapter(adapter);
                        }
                    }.execute();
                }
            }
        }.execute();
    }

    /*
    * ================================================================== sensor ======================================================================
    * */
    private void setSensor() {
        registerReceiver(broadcastReceiver, new IntentFilter("condi.kr.ac.swu.condiproject.step"));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            walk = Integer.parseInt(intent.getStringExtra("walk"));
            pcurrent1_km.setText(String.format("%s", ( Math.round(walk * 0.011559 * 100)/100)));
            pcurrent1_step.setText(String.format("%s",walk));

            th = new Thread(new Runnable() {
                @Override
                public void run() {
                    DateFormat df = new SimpleDateFormat("yyMMdd");
                    df.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    String today = df.format(new Date());

                    String dml = "update walk set currentwalk="+ walk +" where user='"+ Session.ID+"' and date_format(today,'%y%m%d')=str_to_date('"+ today +"','%y%m%d')";
                    System.out.println("걸음 : "+ dml + "\n ==> "+NetworkAction.sendDataToServer(dml)+" : "+walk);

                }
            });
            th.start();
        }
    };

    public void setProfileURL(final CircularNetworkImageView profile, final String profileImageURL) {

        Application app = GlobalApplication.getGlobalApplicationContext();
        if (app == null)
            throw new UnsupportedOperationException("needs com.kakao.GlobalApplication in order to use ImageLoader");

        if (profile != null && profileImageURL != null) {
            if(profileImageURL.equals(""))
                profile.setImageUrl("http://condi.swu.ac.kr:80/condi2/profile/thumb_story.png", ((GlobalApplication) app).getImageLoader());
            else
                profile.setImageUrl("http://condi.swu.ac.kr:80/condi2/profile/"+profileImageURL, ((GlobalApplication) app).getImageLoader());
        } else  {
            profile.setImageUrl("http://condi.swu.ac.kr:80/condi2/profile/thumb_story.png", ((GlobalApplication) app).getImageLoader());
        }
    }
}
