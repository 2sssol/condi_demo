package condi.kr.ac.swu.condidemo.view.adapter;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.activity.GroupActivity;
import condi.kr.ac.swu.condidemo.data.GlobalApplication;
import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;
import condi.kr.ac.swu.condidemo.view.CircularNetworkImageView;


/**
 * Created by 8304 on 2015-10-24.
 */
public class GroupListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap<String, String>> data;
    private String result;
    private Handler handler;
    private boolean isDialogShow = false;
    private boolean isOk;

    public GroupListAdapter(Context context, List<Properties> data) {
        this.context = context;
        handler = new Handler();

        ArrayList<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        String key = "";

        for(Properties p : data) {
            map = new HashMap<String, String>();
            Enumeration keys = p.propertyNames();
            while(keys.hasMoreElements()) {
                key = keys.nextElement().toString();
                map.put(key, p.getProperty(key));
            }
            maps.add(map);
        }

        this.data = maps;
    }

    /*
    * 오버라이드
    * */
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null)
            convertView = inflater.inflate(R.layout.group_list, null);

        // 리소스와 연결
        CircularNetworkImageView group_picture = (CircularNetworkImageView) convertView.findViewById(R.id.group_picture);
        TextView group_name = (TextView) convertView.findViewById(R.id.group_name);
        TextView group_current_km = (TextView) convertView.findViewById(R.id.group_current_km);
        TextView group_current_step = (TextView) convertView.findViewById(R.id.group_current_step);
        TextView group_course = (TextView) convertView.findViewById(R.id.group_course);
        TextView group_km = (TextView) convertView.findViewById(R.id.group_km);
        ImageView groups_cock = (ImageView) convertView.findViewById(R.id.groups_cock);

        //텍스트 세팅
        group_name.setText(data.get(position).get("mname"));
        group_course.setText(data.get(position).get("cname"));
        group_km.setText(data.get(position).get("ckm"));

        //이미지세팅
        setProfileURL(group_picture, data.get(position).get("mprofile"));

        // button
        setButton(groups_cock, data.get(position).get("mid"), data.get(position).get("mname"));

        setCurrent(group_current_step, group_current_km, data.get(position).get("mid"), position);

        return convertView;
    }

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

    private void setCurrent(final TextView step, final TextView km, final String id, final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String dml = "select count(currentwalk) as count from walk where user='"+id+"'";
                String php = "";
                if(position==0)
                    php = "memberwalk0.php";
                else if(position==1)
                    php = "memberwalk1.php";
                else
                    php = "memberwalk2.php";

                while (true) {
                    result = NetworkAction.sendDataToServer(php, dml);
                    if(result.equals("") || result.isEmpty())
                        result = "0";

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            step.setText(result);
                            km.setText(Float.toString((float)Math.round(Integer.parseInt(result) * 0.011559 * 100)/100));
                        }
                    });

                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }).start();
    }

    private void setButton(ImageView button, final String receiver, final String name) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInvitedDialog(receiver, name);
            }
        });
    }

    public void showInvitedDialog(final String receiver, final String receivername) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_cock);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(850,450);

        TextView customDlgTxt_too_big_cock = (TextView) dialog.findViewById(R.id.customDlgTxt_too_big_cock);
        TextView customDlgTxt_small_cock = (TextView) dialog.findViewById(R.id.customDlgTxt_small_cock);
        Button customDlgBtnOk_cock = (Button) dialog.findViewById(R.id.customDlgBtnOk_cock);
        Button customDlgBtnNo_cock = (Button) dialog.findViewById(R.id.customDlgBtnNo_cock);

        customDlgTxt_too_big_cock.setText(receivername + " 님을 콕찌르기");
        customDlgBtnOk_cock.setText("부탁하기");
        customDlgBtnNo_cock.setText("격려하기");

        customDlgBtnOk_cock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
                isOk = true;
            }
        });

        customDlgBtnNo_cock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
                isOk = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isOk) { // 부탁하기
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

                            Toast.makeText(context, receivername + "님에게 '부탁하기'를 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                } else {    // 격려하기
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

                            Toast.makeText(context, receivername + "님에게 '격려하기'를 했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            }
        });

        dialog.show();
        isDialogShow = true;
    }
}
