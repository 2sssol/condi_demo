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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

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
import condi.kr.ac.swu.condidemo.view.CustomCircularRingView2;
import condi.kr.ac.swu.condidemo.view.PatchPointView;

public class MyActivity extends BaseActivity {

    private BarChart mChart;
    private ArrayList<String> xList = new ArrayList<String>();
    private ArrayList<Integer> yList = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        initActionBar("나의 걸음");
        initView();

    }

    private void initView() {
        mChart = (BarChart) findViewById(R.id.barchart);
        setChart();
    }

    private void setChart() {
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        mChart.setMaxVisibleValueCount(60);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        getData();
    }

    private void setData(int count) {
        getData();

        ArrayList<String> xVals = new ArrayList<String>();
        xVals = xList;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int cnt = 0;
        for(Integer i : yList) {
            if (cnt>=count)
                break;

            float currentWalk = (float) i;
            yVals1.add(new BarEntry(currentWalk, cnt));
            cnt++;

        }

        BarDataSet set1 = new BarDataSet(yVals1, "나의 걸음");
        set1.setBarSpacePercent(35f);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);
        data.setValueTextSize(10f);

        mChart.setData(data);
    }

    private void getData() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String dml = "select * from walk where user='"+Session.ID+"'";
                return NetworkAction.sendDataToServer("mywalk.php", dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                if(o.equals("success")) {
                    new AsyncTask() {

                        List<Properties> list;

                        @Override
                        protected Object doInBackground(Object[] params) {
                            try {
                                list = NetworkAction.parse("mywalk.xml", "walk");
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

                            Date today;
                            String todayToString = "";
                            int currentWalk = 0;

                            /*
                            * x,y 축 데이터 정의
                            * */
                            for (Properties p : list) {
                                try {
                                    today = new SimpleDateFormat("yyyy-MM-dd").parse(p.getProperty("today"));
                                    todayToString = new SimpleDateFormat("MM/dd").format(today);
                                    currentWalk = Integer.parseInt(p.getProperty("currentwalk"));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                xList.add(todayToString);
                                yList.add(currentWalk);
                            }


                            setData(yList.size());

                        }
                    }.execute();
                }
            }
        }.execute();
    }

}
