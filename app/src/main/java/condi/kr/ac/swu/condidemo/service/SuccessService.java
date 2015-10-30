package condi.kr.ac.swu.condidemo.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.Properties;

import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;

public class SuccessService extends Service {

    private String groups = Session.GROUPS;
    private String user = Session.ID;
    private String name = Session.NICKNAME;
    private String  goalkm;
    private Thread thread;
    private String dml;
    private String result;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        goalkm = pref.getString("goalkm", "");

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    dml = "select sum(currentwalk) as count from walk where groups="+groups;
                    result = NetworkAction.sendDataToServer("sum.php", dml);
                    if(result.equals("")||result.isEmpty())
                        result = "0";

                    if(result.equals(goalkm)) {
                        new AsyncTask() {
                            @Override
                            protected Object doInBackground(Object[] params) {
                                Properties p = new Properties();
                                String dml = "select * from member where id='"+user+"'";
                                p.setProperty("sender", user);
                                p.setProperty("sendername", name);
                                p.setProperty("type", "9");
                                return NetworkAction.sendDataToServer("gcm.php", p, dml);
                            }
                        }.execute();
                        break;
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(thread.isAlive())
            thread.stop();
    }
}
