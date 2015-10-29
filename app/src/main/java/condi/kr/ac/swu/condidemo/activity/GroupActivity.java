package condi.kr.ac.swu.condidemo.activity;

import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private int cnt = 0;
    private int k = 0;
    private String dml1, dml2, dml3;
    private Handler h1 = new Handler();
    private Handler h2 = new Handler();
    private Handler h3 = new Handler();

    // my
    private CircularNetworkImageView p1, p2, p3, p4;
    private TextView
            pname1, pcurrent1_km, pcurrent1_step, pkm1, pcourse1,
            pname2, pcurrent2_km, pcurrent2_step, pkm2, pcourse2,
            pname3, pcurrent3_km, pcurrent3_step, pkm3, pcourse3,
            pname4, pcurrent4_km, pcurrent4_step, pkm4, pcourse4;
    private ImageView groups_cock2, groups_cock3, groups_cock4;
    private RelativeLayout friend1, friend2, friend3;
    private Properties ps;

    // thread
    private Handler graphHandler = new Handler();
    private Thread th;
    int percent = 0;
    int currentStep = 0;
    float currentKM = 0.00f;

    private boolean isDialogShow = false;
    private boolean isOK;

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

                        period = 1;//startDate.compareTo(today);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = "";
                while (percent <= 100) {
                    if(percent==100) {
                        showRoomDialog();
                        break;
                    }
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
                        protected void onPreExecute() {
                            super.onPreExecute();
                            p2 = (CircularNetworkImageView) findViewById(R.id.p2);
                            p3 = (CircularNetworkImageView) findViewById(R.id.p3);
                            p4 = (CircularNetworkImageView) findViewById(R.id.p4);

                            pname2 = (TextView) findViewById(R.id.pname2);
                            pname3 = (TextView) findViewById(R.id.pname3);
                            pname4 = (TextView) findViewById(R.id.pname4);

                            pcurrent2_km = (TextView) findViewById(R.id.pcurrent2_km);
                            pcurrent3_km = (TextView) findViewById(R.id.pcurrent3_km);
                            pcurrent4_km = (TextView) findViewById(R.id.pcurrent4_km);

                            pcurrent2_step = (TextView) findViewById(R.id.pcurrent2_step);
                            pcurrent3_step = (TextView) findViewById(R.id.pcurrent3_step);
                            pcurrent4_step = (TextView) findViewById(R.id.pcurrent4_step);

                            pcourse2 = (TextView) findViewById(R.id.pcourse2);
                            pcourse3 = (TextView) findViewById(R.id.pcourse3);
                            pcourse4 = (TextView) findViewById(R.id.pcourse4);

                            pkm2 = (TextView) findViewById(R.id.pkm2);
                            pkm3 = (TextView) findViewById(R.id.pkm3);
                            pkm4 = (TextView) findViewById(R.id.pkm4);

                            groups_cock2 = (ImageView) findViewById(R.id.groups_cock2);
                            groups_cock3 = (ImageView) findViewById(R.id.groups_cock3);
                            groups_cock4 = (ImageView) findViewById(R.id.groups_cock4);

                            friend1 = (RelativeLayout) findViewById(R.id.friend1);
                            friend2 = (RelativeLayout) findViewById(R.id.friend2);
                            friend3 = (RelativeLayout) findViewById(R.id.friend3);

                            friend1.setVisibility(View.INVISIBLE);
                            friend2.setVisibility(View.INVISIBLE);
                            friend3.setVisibility(View.INVISIBLE);

                        }

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

                            for(Properties p : friends) {
                                if(cnt==0) {
                                    friend1.setVisibility(View.VISIBLE);
                                    setOtherProfileURL(p2, p.getProperty("mprofile"));
                                    pname2.setText(p.getProperty("mname"));
                                    pcourse2.setText(p.getProperty("cname"));
                                    pkm2.setText(p.getProperty("ckm"));
                                    ps = p;
                                    groups_cock2.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            showRoomDialog(ps.getProperty("mid"), ps.getProperty("mname"));
                                        }
                                    });
                                    dml1 = "select sum(currentwalk) from walk where user = '"+p.getProperty("mid")+"'";
                                } else if(cnt==1) {
                                    friend2.setVisibility(View.VISIBLE);
                                    setOtherProfileURL(p3, p.getProperty("mprofile"));
                                    pname3.setText(p.getProperty("mname"));
                                    pcourse3.setText(p.getProperty("cname"));
                                    pkm3.setText(p.getProperty("ckm"));
                                    ps = p;
                                    groups_cock3.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            showRoomDialog(ps.getProperty("mid"), ps.getProperty("mname"));
                                        }
                                    });
                                    dml2 = "select sum(currentwalk) from walk where user = '"+p.getProperty("mid")+"'";
                                } else {
                                    friend3.setVisibility(View.VISIBLE);
                                    setOtherProfileURL(p4, p.getProperty("mprofile"));
                                    pname4.setText(p.getProperty("mname"));
                                    pcourse4.setText(p.getProperty("cname"));
                                    pkm4.setText(p.getProperty("ckm"));
                                    ps = p;
                                    groups_cock4.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            showRoomDialog(ps.getProperty("mid"), ps.getProperty("mname"));
                                        }
                                    });
                                    dml3 = "select sum(currentwalk) from walk where user = '"+p.getProperty("mid")+"'";
                                }

                                cnt++;
                            }

                            for(k = 0 ; k<cnt; k++) {
                                new Thread(new Runnable() {
                                    String result = "";
                                    @Override
                                    public void run() {
                                        Log.d("other's : " , k+"번 째");
                                        if(k==0) {
                                            while(true) {
                                                Log.d("other's : " , "쓰레드"+k);
                                                result = NetworkAction.sendDataToServer("memberwalk0.php", dml1);
                                                if (result.equals("") || result.isEmpty())
                                                    result = "0";

                                                h1.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pcurrent2_step.setText(result);
                                                        pcurrent2_km.setText(String.format("%s", Math.round(Integer.parseInt(result) * 0.011559 * 100) / 100));
                                                    }
                                                });

                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }


                                        }
                                        else if(k==1) {
                                            while(true) {
                                                Log.d("other's : " , "쓰레드"+k);
                                                result = NetworkAction.sendDataToServer("memberwalk1.php", dml2);
                                                if(result.equals("") || result.isEmpty())
                                                    result = "0";

                                                h2.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pcurrent3_step.setText(result);
                                                        pcurrent3_km.setText(String.format("%s", Math.round(Integer.parseInt(result) * 0.011559 * 100)/100));
                                                    }
                                                });

                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        else {
                                            while(true) {
                                                Log.d("other's : " , "쓰레드"+k);
                                                result = NetworkAction.sendDataToServer("memberwalk2.php", dml3);
                                                if(result.equals("") || result.isEmpty())
                                                    result = "0";

                                                h3.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pcurrent4_step.setText(result);
                                                        pcurrent4_km.setText(String.format("%s", Math.round(Integer.parseInt(result) * 0.011559 * 100)/100));
                                                    }
                                                });

                                                try {
                                                    Thread.sleep(1000);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }).start();
                            }

                            /*grouplv = (ListView) findViewById(R.id.groups_lv);
                            adapter = new GroupListAdapter(GroupActivity.this.getApplicationContext(), friends);
                            grouplv.setAdapter(adapter);*/
                        }
                    }.execute();
                }
            }
        }.execute();
    }

    public void setOtherProfileURL(final CircularNetworkImageView profile, final String profileImageURL) {

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

    /*
    * ================================================================== sensor ======================================================================
    * */
    private void setSensor() {
        registerReceiver(broadcastReceiver, new IntentFilter("condi.kr.ac.swu.condiproject.step"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
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

    public void showRoomDialog() {

        final Dialog dialog = new Dialog(GroupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(850,450);

        TextView dlgDefaultText_big = (TextView) dialog.findViewById(R.id.dlgDefaultText_big);
        TextView dlgDefaultText_small = (TextView) dialog.findViewById(R.id.dlgDefaultText_small);
        Button dlgOk = (Button) dialog.findViewById(R.id.dlgOk);

        dlgDefaultText_big.setText("목 표 달 성");
        dlgDefaultText_small.setText("목표에 달성했습니다!\n축하드립니다!");
        dlgOk.setText("확   인");

        dlgOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                startActivity(new Intent(getApplicationContext(), GoalActivity.class));
                finish();
            }
        });

        dialog.show();
        isDialogShow = true;
    }

    public void showRoomDialog(final String receiver, final String name) {

        final Dialog dialog = new Dialog(GroupActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_default_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(850,450);

        TextView dlgDefaultText_big = (TextView) dialog.findViewById(R.id.customDlgTxt_big);
        TextView dlgDefaultText_small = (TextView) dialog.findViewById(R.id.customDlgTxt_small);
        Button dlgOk = (Button) dialog.findViewById(R.id.customDlgBtnOk);
        Button dlgNo = (Button) dialog.findViewById(R.id.customDlgBtnNo);

        dlgDefaultText_big.setText(name+" 님을 콕 찌르기");
        dlgDefaultText_small.setText("'콕 찌르기'는 상대방에게 걸음을\n부탁하거나 격려하는 기능입니다.");
        dlgOk.setText("부탁하기");
        dlgNo.setText("격려하기");

        dlgOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
                isOK = true;
            }
        });

        dlgNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
                isOK = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(isOK) {
                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            Properties p = new Properties();
                            p.setProperty("sender", Session.ID);
                            p.setProperty("receiver", receiver);
                            p.setProperty("sendername", Session.NICKNAME);
                            p.setProperty("type", "2");
                            return NetworkAction.sendDataToServer("gcmp.php", p);
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);

                            Toast.makeText(getApplicationContext(), name + "님에게 '부탁하기'를 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                } else {
                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            Properties p = new Properties();
                            p.setProperty("sender", Session.ID);
                            p.setProperty("receiver", receiver);
                            p.setProperty("sendername", Session.NICKNAME);
                            p.setProperty("type", "11");
                            return NetworkAction.sendDataToServer("gcmp.php", p);
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);

                            Toast.makeText(getApplicationContext(), name + "님에게 '격려하기'를 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            }
        });

        dialog.show();
        isDialogShow = true;
    }

}
