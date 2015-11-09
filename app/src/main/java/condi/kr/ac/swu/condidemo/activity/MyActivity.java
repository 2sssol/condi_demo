package condi.kr.ac.swu.condidemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;
import condi.kr.ac.swu.condidemo.view.CustomCircularRingView;
import condi.kr.ac.swu.condidemo.view.CustomCircularRingView2;
import condi.kr.ac.swu.condidemo.view.PatchPointView;

public class MyActivity extends BaseActivity {

    private CustomCircularRingView2 myView;
    private PatchPointView patchPointView;

    private TextView txtTotalDate, txtTotalKM ; // 전체 일수, km
    private TextView txtPercent, txtCurrentDate, txtCurrentKM, myStep;

    private TextView txtCourseName1, txtCourseName2, txtCourseName3, txtCourseName4;   // 나머지 코스 이름
    private float courseKm1, courseKm2, courseKm3, courseKm4;

    private int walk = 0;
    private int period = 0;
    private float totalKM;

    private Thread viewThread;

    // thread
    private Handler graphHandler = new Handler();
    private Thread th;
    int percent = 0;
    int currentStep = 0;
    float currentKM = 0.00f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        initActionBar("나의 걸음");
        initView();
    }

    private void initView() {
        myView = (CustomCircularRingView2) findViewById(R.id.customCircularRingView2);
        myView.changePercentage(0);
        myView.invalidate();

        patchPointView = (PatchPointView) findViewById(R.id.patchPointView2);

        myStep = (TextView) findViewById(R.id.myStep);
        txtPercent = (TextView) findViewById(R.id.txtPercent2);
        txtCurrentKM = (TextView) findViewById(R.id.txtCurrentKM2);

        txtTotalDate = (TextView) findViewById(R.id.txtTotalDate2);
        txtTotalKM = (TextView) findViewById(R.id.txtTotalKM2);
        txtCurrentDate = (TextView) findViewById(R.id.txtCurrentDate2);
        setDateKM();

        // 경로
        txtCourseName1 = (TextView) findViewById(R.id.txtCourseName12);
        txtCourseName2 = (TextView) findViewById(R.id.txtCourseName22);
        txtCourseName3 = (TextView) findViewById(R.id.txtCourseName32);
        txtCourseName4 = (TextView) findViewById(R.id.txtCourseName42);

        setMy();
        setMyView();
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
                    txtTotalKM.setText(results[1].substring(0,4));
                    totalKM = Float.parseFloat(results[1]);

                    printErrorMsg("totalKM : " + totalKM);
                    /*
                    * current date
                    * */
                    //try {
                        //Date startDate = new SimpleDateFormat("yyMMdd").parse(results[2]);       // startdate
                        //Date today = new Date();

                        period = 1;//startDate.compareTo(today);
                        txtCurrentDate.setText(Integer.toString(period));
                    //} catch (ParseException e) {
                    //    e.printStackTrace();
                    //}
                }
            }
        }.execute();
    }

    private void setMy() {
        registerReceiver(broadcastReceiver, new IntentFilter("condi.kr.ac.swu.condiproject.step"));

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

                            float[] km = new float[list.size()];
                            int cnt = 0;
                            int sum = 0;
                            List<Integer> points = new ArrayList<Integer>();
                            for(Properties p : list) {
                                points.add(sum);
                                km[cnt] = Float.parseFloat(p.getProperty("km"));
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
        viewThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                while (percent <= 100 || !viewThread.isInterrupted()) {
                    String dml = "select sum(currentwalk) as count  from walk  where groups="+Session.GROUPS;
                    result = NetworkAction.sendDataToServer("sum.php", dml);
                    if(result.equals("")||result.isEmpty())
                        result = "0";
                    currentStep = Integer.parseInt(result);
                    currentKM = (float) (currentStep * 0.011559);//Math.round(currentStep * 0.011559 * 100)/100;
                    percent = Math.round((currentKM / totalKM) * 100);

                    graphHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            myView.changePercentage(percent);
                            myView.invalidate();

                            txtPercent.setText(String.format("%s",percent));
                            txtCurrentKM.setText(String.format("%.2f", currentKM));

                            if(currentKM > courseKm1) {
                                if(currentKM > courseKm1+courseKm2) {
                                    if(currentKM > courseKm1+courseKm2+courseKm3) {
                                        if(currentKM > courseKm1+courseKm2+courseKm3+courseKm4) {
                                            percent = 100;
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
        });

        viewThread.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(broadcastReceiver.isOrderedBroadcast())
            unregisterReceiver(broadcastReceiver);
        viewThread.interrupt();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            walk = Integer.parseInt(intent.getStringExtra("walk"));
            txtCurrentKM.setText(String.format("%s", ( Math.round(walk * 0.011559 * 100)/100)));
            myStep.setText(String.format("%s",walk));

            viewThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String result = "";
                    while (percent <= 100 || !viewThread.isInterrupted()) {

                        currentKM = (float) (walk * 0.011559);//Math.round(currentStep * 0.011559 * 100)/100;
                        percent = Math.round((currentKM / totalKM) * 100);

                        graphHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                myView.changePercentage(percent);
                                myView.invalidate();

                                txtPercent.setText(String.format("%s",percent));
                                txtCurrentKM.setText(String.format("%.2f", currentKM));

                                if(currentKM > courseKm1) {
                                    if(currentKM > courseKm1+courseKm2) {
                                        if(currentKM > courseKm1+courseKm2+courseKm3) {
                                            if(currentKM > courseKm1+courseKm2+courseKm3+courseKm4) {
                                                percent = 100;
                                            } else {
                                                txtCourseName1.setBackgroundResource(R.drawable.road_nametag_on);
                                                txtCourseName2.setBackgroundResource(R.drawable.road_nametag_on);
                                                txtCourseName3.setBackgroundResource(R.drawable.road_nametag_on);
                                                txtCourseName4.setBackgroundResource(R.drawable.road_nametag_on);
                                            }
                                        } else {
                                            txtCourseName1.setBackgroundResource(R.drawable.road_nametag_on);
                                            txtCourseName2.setBackgroundResource(R.drawable.road_nametag_on);
                                            txtCourseName3.setBackgroundResource(R.drawable.road_nametag_on);
                                        }
                                    } else {
                                        txtCourseName1.setBackgroundResource(R.drawable.road_nametag_on);
                                        txtCourseName2.setBackgroundResource(R.drawable.road_nametag_on);
                                    }
                                } else {
                                    txtCourseName1.setBackgroundResource(R.drawable.road_nametag_on);
                                } // if-else

                            }
                        }); // handler-post

                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } // try-catch

                    } // while
                } // run
            }); // Thread

            viewThread.start();
        } //onReceive
    }; //broadcastReceiver

}

