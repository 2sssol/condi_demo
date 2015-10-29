package condi.kr.ac.swu.condidemo.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class AccSensor extends Service implements SensorEventListener {

    private SensorManager sm;
    private Sensor accSensor;

    public int MY_WALK_COUNT = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(accSensor != null)
            sm.unregisterListener(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(accSensor != null)
            sm.registerListener(this, accSensor,SensorManager.SENSOR_DELAY_GAME);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    if ((event.values[0] < -10) || (event.values[0] > 10)
                            || (event.values[1] < -10) || (event.values[1] > 10)
                            || (event.values[2] < -10) || (event.values[2] > 10)) {

                        ++MY_WALK_COUNT;
                        Intent intentResponse = new Intent("condi.kr.ac.swu.condiproject.step");
                        intentResponse.putExtra("walk", Integer.toString(MY_WALK_COUNT));
                        sendBroadcast(intentResponse);
                    }

                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




}
