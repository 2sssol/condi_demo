package condi.kr.ac.swu.condidemo.activity;

import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import condi.kr.ac.swu.condidemo.R;
import condi.kr.ac.swu.condidemo.data.GlobalApplication;
import condi.kr.ac.swu.condidemo.data.NetworkAction;
import condi.kr.ac.swu.condidemo.data.Session;
import condi.kr.ac.swu.condidemo.service.AccSensor;
import condi.kr.ac.swu.condidemo.service.StartService;
import condi.kr.ac.swu.condidemo.service.SuccessService;
import condi.kr.ac.swu.condidemo.view.CircularNetworkImageView;
import condi.kr.ac.swu.condidemo.view.RoundImage;


public class SettingActivity extends BaseActivity {

    private TextView settingName, settingHeight;
    private EditText settingPassword1, settingPassword2;
    private Button settingBtnPassword, settingBtnLogout, settingBtnJoinDelete;
    private CircularNetworkImageView settingProfile;
    private ImageView settingProfile_default;
    private RoundImage roundImage;
    private ImageButton btnSettingPic;

    private String password1, password2;

    private boolean isDialogShow = false;
    private boolean isOk;

    /*
    * 파일 전송 관련 변수
    * */
    private String currentFilePath = "";
    private String filePath = null;
    private final String IMG_FILE_PREFIX = "IMG_";
    private final String IMG_FILE_SUFFIX = ".jpg";
    private final String ALBUM_NAME = "WalkingTogether";
    private final String SERVER = "http://condi.swu.ac.kr:80/condi2/profile/";
    private String profile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initActionBar("환경 설정");
        initView();
    }

    private void initView() {
        settingName = (TextView) findViewById(R.id.settingName);
        settingHeight = (TextView) findViewById(R.id.settingHeight);

        settingPassword1 = (EditText) findViewById(R.id.settingPassword1);
        settingPassword2 = (EditText) findViewById(R.id.settingPassword2);

        settingBtnPassword = (Button) findViewById(R.id.settingBtnPassword);
        settingBtnLogout = (Button) findViewById(R.id.settingBtnLogout);
        settingBtnJoinDelete = (Button) findViewById(R.id.settingBtnJoinDelete);
        btnSettingPic = (ImageButton) findViewById(R.id.btnSettingPic);

        settingProfile = (CircularNetworkImageView) findViewById(R.id.settingProfile);
        settingProfile_default = (ImageView) findViewById(R.id.settingProfile_default);

        setInfo();
    }

    private void setInfo() {
        settingName.setText(Session.NICKNAME);
        settingHeight.setText(Session.HEIGHT);

        if(Session.PROFILE.equals("")) {
            settingProfile_default.setVisibility(View.VISIBLE);
            settingProfile.setVisibility(View.INVISIBLE);
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.thumb_story);
            roundImage = new RoundImage(bm);
            settingProfile_default.setImageDrawable(roundImage);
        } else {
            settingProfile_default.setVisibility(View.INVISIBLE);
            settingProfile.setVisibility(View.VISIBLE);
            setProfileURL(Session.PROFILE);
        }
    }

    public void setProfileURL(final String profileImageURL) {
        if (settingProfile != null && profileImageURL != null) {
            Application app = GlobalApplication.getGlobalApplicationContext();
            if (app == null)
                throw new UnsupportedOperationException("needs com.kakao.GlobalApplication in order to use ImageLoader");
            settingProfile.setImageUrl("http://condi.swu.ac.kr:80/condi2/profile/" + profileImageURL, ((GlobalApplication) app).getImageLoader());
        }
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

            Toast.makeText(getApplicationContext(), "정보 수정이 완료되었습니다.", Toast.LENGTH_LONG).show();
            redirectMainActivity();

        }
    }

    /*
    * redirecMethod
    * */
    private void redirectMainActivity() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    private void redirectLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /*
    * click event
    * */
    @Override
    public void onClick(View v) {
        if(v == settingBtnPassword) {
            password1 = settingPassword1.getText().toString();
            password2 = settingPassword2.getText().toString();

            if(TextUtils.isEmpty(password1)) {
                password1 = Session.PASSWORD;
                password2 = password1;
            }

            if(!password1.equals(password2)){
                toastErrorMsg("새로운 비밀번호를 확인해주세요.");
            } else {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        String dml = "update member set password='"+password1+"' where id='"+Session.ID+"'";
                        return NetworkAction.sendDataToServer(dml);
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        super.onPostExecute(o);
                        new MyPHP().execute();
                    }
                }.execute();
            }

        } else if(v == settingBtnLogout) {
            showLogoutDialog();
        } else if(v == settingBtnJoinDelete) {
            showJoinOutDialog();
        } else if (v == btnSettingPic) {
            pickPictureIntent();
        }
    }

    /*
    * 다이어로그
    * */
    public void showJoinOutDialog() {

        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_default_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(850,450);

        TextView customDlgTxt_big = (TextView) dialog.findViewById(R.id.customDlgTxt_big);
        Button customDlgBtnOk = (Button) dialog.findViewById(R.id.customDlgBtnOk);
        Button customDlgBtnNo = (Button) dialog.findViewById(R.id.customDlgBtnNo);

        customDlgTxt_big.setText("정말로 회원탈퇴를 하시겠습니까?");
        customDlgBtnOk.setText("회원탈퇴");
        customDlgBtnNo.setText("취   소");

        customDlgBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
                isOk = true;
            }
        });

        customDlgBtnNo.setOnClickListener(new View.OnClickListener() {
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
                if(isOk) {
                    stopService(new Intent(getApplicationContext(), AccSensor.class));
                    stopService(new Intent(getApplicationContext(), StartService.class));
                    stopService(new Intent(getApplicationContext(), SuccessService.class));

                    new AsyncTask() {
                        @Override
                        protected Object doInBackground(Object[] params) {
                            String dml = "delete from member where id='"+Session.ID+"'";
                            return NetworkAction.sendDataToServer(dml);
                        }

                        @Override
                        protected void onPostExecute(Object o) {
                            super.onPostExecute(o);
                            Session.removeAllPreferences(getApplicationContext());
                            redirectLoginActivity();
                        }
                    }.execute();
                }
            }
        });

        dialog.show();
        isDialogShow = true;
    }

    public void showLogoutDialog() {

        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_default_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(850,450);

        TextView customDlgTxt_big = (TextView) dialog.findViewById(R.id.customDlgTxt_big);
        Button customDlgBtnOk = (Button) dialog.findViewById(R.id.customDlgBtnOk);
        Button customDlgBtnNo = (Button) dialog.findViewById(R.id.customDlgBtnNo);

        customDlgTxt_big.setText("정말로 로그아웃을 하시겠습니까?");
        customDlgBtnOk.setText("로그아웃");
        customDlgBtnNo.setText("취   소");

        customDlgBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShow = false;
                isOk = true;
            }
        });

        customDlgBtnNo.setOnClickListener(new View.OnClickListener() {
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
                if(isOk) {
                    stopService(new Intent(getApplicationContext(), AccSensor.class));
                    stopService(new Intent(getApplicationContext(), StartService.class));
                    stopService(new Intent(getApplicationContext(), SuccessService.class));

                    Session.removeAllPreferences(getApplicationContext());
                    redirectLoginActivity();
                }
            }
        });

        dialog.show();
        isDialogShow = true;
    }

    /*
    * ============================================파일 관련 메소드==================================================
    * */
    private void updateProfile() {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String dml = "update member set profile='"+profile + IMG_FILE_SUFFIX+"' where id='"+Session.ID+"'";
                return NetworkAction.sendDataToServer(dml);
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                new UploadFile().execute();
            }
        }.execute();
    }

    private class UploadFile extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            return uploadFile(filePath);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer==200) {
                toastErrorMsg("프로필 사진이 변경되었습니다.");
                new MyPHP().execute();
            }  else
                Toast.makeText(getApplicationContext(),"파일 업로드를 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /* 임시 파일 관리 */
    private String getAlbumName() {
        return ALBUM_NAME;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = new File (
                    Environment.getExternalStorageDirectory()
                            + "/dcim/"
                            + getAlbumName()
            );

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "디렉토리를 만들지 못했습니다.");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createTempFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File albumF = getAlbumDir();
        File mediaF = null;

        profile = IMG_FILE_PREFIX + timeStamp+"_";
        mediaF = File.createTempFile(profile, IMG_FILE_SUFFIX, albumF);

        return mediaF;
    }

    private File setUpTempFile() throws IOException {

        File f =  createTempFile();
        currentFilePath = f.getAbsolutePath();
        filePath = currentFilePath;

        return f;
    }
    /* 임시 파일 관리 */

    /*
    * 갤러리에서 사진 가져오기
    * */
    private void pickPictureIntent() {

        Intent galleryForImage = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        File f = null;
        try {
            f = setUpTempFile();
            currentFilePath = f.getAbsolutePath();

            galleryForImage.setType("image/*");              // 모든 이미지
            galleryForImage.putExtra("crop", "true");        // Crop기능 활성화
            galleryForImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            galleryForImage.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        startActivityForResult(galleryForImage, 1001);
    }

    /*
    * 결과값 처리
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1001 && resultCode == RESULT_OK ) {
            settingProfile_default.setVisibility(View.VISIBLE);
            settingProfile.setVisibility(View.INVISIBLE);
            Bitmap bm = BitmapFactory.decodeFile(currentFilePath);
            roundImage = new RoundImage(bm);
            settingProfile_default.setImageDrawable(roundImage);

            updateProfile();
            currentFilePath = null;
        } else {
            Toast.makeText(getApplicationContext(), "이미지를 불러오지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    /*
    * 파일 전송 메소드
    * */
    public int uploadFile(String sourceFileUri) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        int serverResponseCode = 0;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fileName);

        if (!sourceFile.isFile()) {
            Toast.makeText(getApplicationContext(), "파일이 없습니다.", Toast.LENGTH_LONG).show();
            Log.e("uploadFile", "Source File not exist :" +fileName);
            return 0;
        }
        else
        {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL("http://condi.swu.ac.kr:80/condi2/condi/"+"upload_file.php");

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(35000);
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                conn.setRequestProperty("uploaded_file", profile + IMG_FILE_SUFFIX);


                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=" + profile + IMG_FILE_SUFFIX + "" + lineEnd);


                //dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename="+ imageFileName + JPEG_FILE_SUFFIX + "" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                // 더이상 읽을 것이 없어질 때 까지 돌려
                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);   // 이 시점부터 파일이 전송

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "+ serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {
                            //isFileUploadOk = true;
                            // 리다이렉트이동
                            //redirectLoginActivity();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                //Toast.makeText(getApplicationContext(), "MalformedURLException 에러", Toast.LENGTH_LONG).show();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(SettingActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file Err", e.getMessage());
            }
            //dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

    /*
    * ===========================================/.파일 관련 메소드==================================================
    * */
}
