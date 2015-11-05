package condi.kr.ac.swu.condidemo.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;

public class StartNewActivity extends RootActivity {

    private boolean isDialogShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_new);
        showDialog();
    }

    public void showDialog() {

        final Dialog dialog = new Dialog(StartNewActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(850,450);

        TextView dlgDefaultText_big = (TextView) dialog.findViewById(R.id.dlgDefaultText_big);
        TextView dlgDefaultText_small = (TextView) dialog.findViewById(R.id.dlgDefaultText_small);
        Button dlgOk = (Button) dialog.findViewById(R.id.dlgOk);

        dlgDefaultText_big.setText("새로운 여정을 시작합니다.");
        dlgDefaultText_small.setText("친구를 초대해 보세요.");
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
                updateGroups();
            }
        });

        dialog.show();
        isDialogShow = true;
    }

    private void updateGroups() {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object[] params) {
                String dml = "update groups set region='성북구', goaldays=0, goalkm=0, startdate=null where id="+Session.GROUPS;
                return NetworkAction.sendDataToServer(dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(o.equals("success")) {
                    new MyPHP().execute();
                } else{
                    toastErrorMsg("새로운 여정을 시작할 수 없습니다.");
                }
            }
        }.execute();
    }

    /*
    * 나의 최신 정보 로드
    * */
    private class MyPHP extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            Properties prop = new Properties();
            prop.setProperty("id", Session.getPreferences(getApplicationContext(), "id"));
            return NetworkAction.sendDataToServer("my.php", prop);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new getMyInfo().execute();
        }
    }

    private class getMyInfo extends AsyncTask<Void, Void, Void> {

        List<Properties> props;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                props = NetworkAction.parse("my.xml", "member");
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Session.removeAllPreferences(getApplicationContext());
            Session.savePreferences(getApplicationContext(), props.get(0));

            Toast.makeText(getApplicationContext(), "새로운 여정 '성북구'를 시작합니다!\n힘차게 걸어보세요!", Toast.LENGTH_LONG).show();
            redirectSelectRegionActivity();

        }
    }

    private void redirectSelectRegionActivity() {
        Intent intent = new Intent(getApplicationContext(), SelectRegionActivity.class);
        intent.putExtra("first", false);
        startActivity(intent);
        finish();
    }
}
