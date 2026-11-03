package com.calibg.waililevel;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int LEVEL_GREEN=Color.rgb(39,195,84);
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final float[] gravity=new float[3], matrix=new float[9];
    private SensorManager manager;
    private Sensor sensor;
    private boolean listening,received,filtered;
    private int backgroundColor=Color.BLACK;
    private LinearLayout root;
    private TextView angle,levelLabel,status;
    private Button retry;
    private LevelDial dial;
    private final Runnable timeout=()->{if(listening&&!received)fail("无法读取传感器，请重试");};

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        manager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensor==null)sensor=manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if(sensor==null)sensor=manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.BLACK);
        if(android.os.Build.VERSION.SDK_INT>=30){
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().setSystemBarsAppearance(0,android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            root.setOnApplyWindowInsetsListener((v,insets)->{
                android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(i.left,i.top,i.right,i.bottom);return insets;
            });
        }
        setContentView(root);
        TextView title=text("水平仪",20);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        root.addView(title,new LinearLayout.LayoutParams(-1,dp(70)));
        FrameLayout stage=new FrameLayout(this);
        root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        dial=new LevelDial();stage.addView(dial,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout reading=new LinearLayout(this);reading.setOrientation(LinearLayout.VERTICAL);reading.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams center=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);stage.addView(reading,center);
        angle=text("—°",108);angle.setTypeface(Typeface.create("sans-serif-thin",Typeface.NORMAL));
        angle.setIncludeFontPadding(false);reading.addView(angle,new LinearLayout.LayoutParams(-1,-2));
        levelLabel=text("",18);LinearLayout.LayoutParams label=new LinearLayout.LayoutParams(-1,dp(42));label.topMargin=dp(20);reading.addView(levelLabel,label);
        TextView instruction=text("将手机放在物体表面",15);instruction.setTextColor(0xffcccccc);
        root.addView(instruction,new LinearLayout.LayoutParams(-1,dp(54)));
        status=text("正在读取传感器…",12);status.setTextColor(0xffcccccc);
        root.addView(status,new LinearLayout.LayoutParams(-1,dp(28)));
        retry=new Button(this);retry.setText("重新测量");retry.setTextColor(Color.WHITE);retry.setTextSize(14);retry.setAllCaps(false);
        GradientDrawable shape=new GradientDrawable();shape.setColor(0xff252525);shape.setCornerRadius(dp(10));retry.setBackground(shape);
        LinearLayout.LayoutParams button=new LinearLayout.LayoutParams(dp(150),dp(46));button.gravity=Gravity.CENTER;button.topMargin=dp(12);root.addView(retry,button);
        retry.setVisibility(View.GONE);retry.setOnClickListener(v->startSensors());
        View spacer=new View(this);root.addView(spacer,new LinearLayout.LayoutParams(-1,dp(36)));
    }

    @Override protected void onResume(){super.onResume();startSensors();}
    @Override protected void onPause(){stopSensors();show(null);super.onPause();}
    @Override protected void onDestroy(){stopSensors();super.onDestroy();}
    private void startSensors(){
        stopSensors();show(null);received=false;filtered=false;
        retry.setVisibility(View.GONE);status.setVisibility(View.VISIBLE);setText(status,"正在读取传感器…");
        if(sensor==null){fail("当前设备不支持水平测量");return;}
        listening=manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
        if(!listening){fail("无法连接传感器，请重试");return;}
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler.postDelayed(timeout,6000);
    }
    private void stopSensors(){
        if(manager!=null)manager.unregisterListener(this);
        listening=false;handler.removeCallbacks(timeout);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void fail(String message){stopSensors();show(null);status.setVisibility(View.VISIBLE);setText(status,message);retry.setVisibility(View.VISIBLE);}
    @Override public void onAccuracyChanged(Sensor s,int accuracy){}
    @Override public void onSensorChanged(SensorEvent event){
        if(!listening)return;
        float x,y,z;
        if(event.sensor.getType()==Sensor.TYPE_GAME_ROTATION_VECTOR){
            SensorManager.getRotationMatrixFromVector(matrix,event.values);x=matrix[6];y=matrix[7];z=matrix[8];
        }else{x=event.values[0];y=event.values[1];z=event.values[2];}
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))return;
        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            float[] raw={x,y,z};for(int i=0;i<3;i++)gravity[i]=filtered?.85f*gravity[i]+.15f*raw[i]:raw[i];
            filtered=true;x=gravity[0];y=gravity[1];z=gravity[2];
        }
        TiltMath.Reading result=TiltMath.fromGravity(x,y,z);if(result==null)return;
        if(!received){received=true;handler.removeCallbacks(timeout);status.setVisibility(View.INVISIBLE);}
        show(result);
    }
    private void show(TiltMath.Reading result){
        boolean level=result!=null&&result.displayed==0;
        double value=result==null?Double.NaN:result.displayed;
        String next=TiltMath.format(value)+"°";
        if(!next.contentEquals(angle.getText())){
            angle.setText(next);angle.setContentDescription(result==null?"正在读取角度":"角度 "+TiltMath.format(value)+" 度");
        }
        setText(levelLabel,level?"水平":"");
        int color=level?LEVEL_GREEN:Color.BLACK;
        if(color!=backgroundColor){backgroundColor=color;root.setBackgroundColor(color);getWindow().setNavigationBarColor(color);}
        dial.reading=result;dial.invalidate();
    }
    private void setText(TextView view,String next){if(!next.contentEquals(view.getText()))view.setText(next);}
    private TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setGravity(Gravity.CENTER);return t;}
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private class LevelDial extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private TiltMath.Reading reading;
        LevelDial(){super(MainActivity.this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            float cx=getWidth()/2f,cy=getHeight()/2f,r=Math.min(getWidth()*.42f,getHeight()*.40f);
            boolean level=reading!=null&&reading.displayed==0;
            float distance=reading==null?0:(float)Math.min(reading.displayed/80,1)*r*.48f;
            float dx=reading==null?0:(float)reading.x*distance,dy=reading==null?0:(float)reading.y*distance;
            paint.setStyle(Paint.Style.FILL);paint.setColor(level?0x18ffffff:0xff202020);
            canvas.drawCircle(cx-dx*.5f,cy-dy*.5f,r,paint);
            paint.setColor(level?0x10ffffff:0x38ffffff);canvas.drawCircle(cx+dx*.5f,cy+dy*.5f,r,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1));paint.setColor(level?0xbbffffff:0xff777777);
            canvas.drawCircle(cx+dx*.5f,cy+dy*.5f,r,paint);
            paint.setStrokeWidth(dp(1));paint.setColor(0xffcccccc);
            canvas.drawLine(cx-r-dp(14),cy,cx-r+dp(14),cy,paint);
            canvas.drawLine(cx+r-dp(14),cy,cx+r+dp(14),cy,paint);
        }
    }
}
