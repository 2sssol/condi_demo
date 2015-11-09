package condi.kr.ac.swu.condidemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;
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

    // thread
    private Handler graphHandler = new Handler();
    int percent = 0;
    float currentKM = 0.00f;

    private TextView mytxt1, detailDate1, detailDate2;


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

        mytxt1 = (TextView) findViewById(R.id.mytxt1);
        detailDate1 = (TextView) findViewById(R.id.detailDate1);
        detailDate2 = (TextView) findViewById(R.id.detailDate2);

        mytxt1.setText(Session.NICKNAME+"님, 여정을 방금시작하셨군요!");
        detailDate1.setText(new SimpleDateFormat("dd").format(new Date()));
        detailDate2.setText(new SimpleDateFormat("E").format(new Date()));
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


    @Override
    protected void onPause() {
        super.onPause();
        if(broadcastReceiver.isOrderedBroadcast())
            unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            walk = Integer.parseInt(intent.getStringExtra("walk"));
            myStep.setText(String.format("%s", walk));

            currentKM = (float) (walk * 0.011559);//Math.round(currentStep * 0.011559 * 100)/100;
            percent = Math.round((currentKM / totalKM) * 100);

            if(percent<=100) {
                graphHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        myView.changePercentage(percent);
                        myView.invalidate();

                        txtPercent.setText(String.format("%s", percent));
                        txtCurrentKM.setText(String.format("%.2f", currentKM));

                        if (currentKM > courseKm1) {
                            if (currentKM > courseKm1 + courseKm2) {
                                if (currentKM > courseKm1 + courseKm2 + courseKm3) {
                                    if (currentKM > courseKm1 + courseKm2 + courseKm3 + courseKm4) {
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
            } // if

        } //onReceive
    }; //broadcastReceiver

}

