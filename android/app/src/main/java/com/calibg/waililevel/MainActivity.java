package com.calibg.waililevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.hardware.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity implements SensorEventListener {
    static final int PAPER=Color.rgb(244,242,233), INK=Color.rgb(36,37,31), YELLOW=Color.rgb(245,218,67), MUTED=Color.rgb(102,103,91);
    SensorManager manager; Sensor sensor; boolean listening=false,received=false,demo=false,filtered=false;
    int panelColor=Color.rgb(238,238,228);
    final float[] gravity=new float[3], matrix=new float[9];
    final Handler handler=new Handler(Looper.getMainLooper());
    TextView angle,verdict,quip,status,mode,actual; Button start; LinearLayout panel,demoPanel; Dial dial;
    final Runnable timeout=()->{if(listening&&!received){stop();show(null);start.setText("重新试试");mode.setText("暂未连接");status.setText("暂时收不到传感器数据，请重试或使用试玩模式。");}};

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        manager=(SensorManager)getSystemService(SENSOR_SERVICE);
        sensor=manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if(sensor==null) sensor=manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if(sensor==null) sensor=manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(PAPER);
        LinearLayout root=column();root.setPadding(dp(22),dp(16),dp(22),dp(24));scroll.addView(root);
        if(android.os.Build.VERSION.SDK_INT>=30) {
            getWindow().setDecorFitsSystemWindows(false);
            scroll.setOnApplyWindowInsetsListener((v,insets)->{ android.graphics.Insets i=insets.getInsets(WindowInsets.Type.systemBars());v.setPadding(i.left,i.top,i.right,i.bottom);return insets; });
        }
        setContentView(scroll);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=text("歪理研究所",20,true);header.addView(brand,new LinearLayout.LayoutParams(0,dp(50),1));
        Button about=button("说明",false);header.addView(about,new LinearLayout.LayoutParams(dp(70),dp(46)));root.addView(header);
        about.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("仪器有点歪理。")
            .setMessage("将手机放在物体表面，再慢慢倾斜。\n\n0–10° 显示 0°，超过 10° 显示实际角度减 10°。例如 15° → 5°、20° → 10°。\n\n这是娱乐水平仪，不用于施工或精密测量。安装后可离线使用，数据不上传。")
            .setPositiveButton("懂了，问题不大",null).show());
        TextView title=text("歪一点，怎么了？",32,true);add(root,title,20,8);
        TextView subtitle=text("一个过分宽容的水平仪。",13,false);subtitle.setTextColor(MUTED);add(root,subtitle,0,22);
        panel=column();panel.setPadding(dp(18),dp(14),dp(18),dp(18));panel.setBackground(shape(Color.rgb(238,238,228),18,true));root.addView(panel);
        mode=text("等待开工 · 传感器未启动",11,false);panel.addView(mode);
        dial=new Dial();panel.addView(dial,new LinearLayout.LayoutParams(-1,dp(210)));
        angle=text("—°",68,true);angle.setGravity(Gravity.CENTER);angle.setContentDescription("显示角度：未开始");panel.addView(angle);
        verdict=text("等你来歪",23,true);verdict.setGravity(Gravity.CENTER);panel.addView(verdict);
        quip=text("仪器已就位，态度很随意。",12,false);quip.setGravity(Gravity.CENTER);add(panel,quip,8,0);
        LinearLayout controls=new LinearLayout(this);controls.setPadding(0,dp(20),0,0);
        start=button("开始测量 ↗",true);LinearLayout.LayoutParams first=new LinearLayout.LayoutParams(0,dp(54),1);first.rightMargin=dp(10);controls.addView(start,first);
        Button preview=button("先试玩一下",false);controls.addView(preview,new LinearLayout.LayoutParams(dp(120),dp(54)));root.addView(controls);
        start.setOnClickListener(v->{if(listening){stop();show(null);start.setText("继续测量 ↗");mode.setText("已暂停");status.setText("休息一下，点击继续测量重新开始。");}else startSensors();});
        preview.setOnClickListener(v->startDemo());
        status=text("点击开始，慢慢倾斜手机。",12,false);status.setTextColor(MUTED);add(root,status,14,14);
        demoPanel=column();demoPanel.setVisibility(View.GONE);root.addView(demoPanel);
        actual=text("试玩 · 实际 15°",13,true);demoPanel.addView(actual);
        SeekBar slider=new SeekBar(this);slider.setMax(900);slider.setProgress(150);demoPanel.addView(slider,new LinearLayout.LayoutParams(-1,dp(44)));
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int progress,boolean user){if(demo){actual.setText("试玩 · 实际 "+TiltMath.format(progress/10.0)+"°");show(TiltMath.demo(progress/10.0));}}
            public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}
        });
        LinearLayout presets=new LinearLayout(this);demoPanel.addView(presets);
        for(int n:new int[]{0,10,15,20,45}){Button b=button(n+"°",false);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(45),1);p.setMargins(dp(2),0,dp(2),0);presets.addView(b,p);b.setOnClickListener(v->slider.setProgress(n*10));}
        preview.setOnClickListener(v->{startDemo();slider.setProgress(150);actual.setText("试玩 · 实际 15°");show(TiltMath.demo(15));});
        TextView foot=text("歪理研究所 · 结论仅供一乐",11,false);foot.setTextColor(MUTED);foot.setGravity(Gravity.CENTER);add(root,foot,25,0);
    }
    void startSensors(){
        stop();demo=false;demoPanel.setVisibility(View.GONE);show(null);
        if(sensor==null){mode.setText("设备无可用传感器");status.setText("这台设备无法读取倾斜，请使用试玩模式。");return;}
        listening=manager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_GAME);
        received=false;filtered=false;
        if(!listening){mode.setText("传感器连接失败");status.setText("请重试或使用试玩模式。");return;}
        mode.setText("正在连接动作传感器…");status.setText("将手机放平，再慢慢倾斜。");start.setText("暂停测量");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);handler.postDelayed(timeout,6000);
    }
    void stop(){manager.unregisterListener(this);listening=false;handler.removeCallbacks(timeout);getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);}
    void startDemo(){stop();demo=true;demoPanel.setVisibility(View.VISIBLE);mode.setText("试玩模式 · 非手机读数");start.setText("开始测量 ↗");status.setText("拖动滑杆，看看本所怎么解释。");}
    @Override protected void onPause(){super.onPause();if(listening){stop();show(null);mode.setText("已暂停");start.setText("继续测量 ↗");status.setText("点击继续测量，重新读取手机倾斜。");}}
    @Override protected void onDestroy(){stop();super.onDestroy();}
    @Override public void onAccuracyChanged(Sensor s,int a){}
    @Override public void onSensorChanged(SensorEvent e){
        if(!listening)return;
        float x,y,z;
        if(e.sensor.getType()==Sensor.TYPE_GAME_ROTATION_VECTOR){SensorManager.getRotationMatrixFromVector(matrix,e.values);x=matrix[6];y=matrix[7];z=matrix[8];}
        else {x=e.values[0];y=e.values[1];z=e.values[2];}
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))return;
        if(e.sensor.getType()==Sensor.TYPE_ACCELEROMETER){float[] raw={x,y,z};for(int i=0;i<3;i++)gravity[i]=filtered?.85f*gravity[i]+.15f*raw[i]:raw[i];filtered=true;x=gravity[0];y=gravity[1];z=gravity[2];}
        TiltMath.Reading r=TiltMath.fromGravity(x,y,z);if(r==null)return;
        if(!received){received=true;handler.removeCallbacks(timeout);mode.setText("手机感应中 · 实时测量");status.setText("慢慢倾斜手机，看看它什么时候才肯承认。");}
        show(r);
    }
    void show(TiltMath.Reading r){
        double value=r==null?Double.NaN:r.displayed;boolean level=value==0;
        String nextAngle=TiltMath.format(value)+"°";
        if(!nextAngle.contentEquals(angle.getText())){angle.setText(nextAngle);angle.setContentDescription(r==null?"显示角度：未开始":"显示角度："+TiltMath.format(value)+"度");}
        setTextIfChanged(verdict,r==null?"等你来歪":level?"这很水平。":value<=10?"有一点，但不多。":value<=35?"这回真有点歪。":"再歪就躺下了。");
        setTextIfChanged(quip,r==null?"仪器已就位，态度很随意。":level?"本所认为：问题不大。":value<=10?"给生活留一点误差。":value<=35?"本所已经尽力包容了。":"这个角度，本所也圆不回来了。");
        int nextColor=level?YELLOW:Color.rgb(238,238,228);
        if(nextColor!=panelColor){panelColor=nextColor;panel.setBackground(shape(nextColor,18,true));}
        dial.reading=r;dial.invalidate();
    }
    void setTextIfChanged(TextView view,String next){if(!next.contentEquals(view.getText()))view.setText(next);}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    TextView text(String value,int size,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(INK);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    Button button(String label,boolean dark){Button b=new Button(this);b.setText(label);b.setTextSize(13);b.setAllCaps(false);b.setTextColor(dark?PAPER:INK);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(6),0,dp(6),0);b.setBackground(shape(dark?INK:PAPER,10,true));return b;}
    GradientDrawable shape(int color,int radius,boolean border){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(border)d.setStroke(dp(2),INK);return d;}
    void add(LinearLayout parent,View child,int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(top),0,dp(bottom));parent.addView(child,p);}
    int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
    class Dial extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);TiltMath.Reading reading;
        Dial(){super(MainActivity.this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f+dp(3),r=Math.min(getWidth()/2f-dp(30),getHeight()/2f-dp(22));
            p.setStrokeWidth(dp(1));p.setStyle(Paint.Style.STROKE);p.setColor(0x5524251f);c.drawCircle(cx,cy,r,p);c.drawCircle(cx,cy,r*.6f,p);c.drawLine(cx-r-dp(12),cy,cx+r+dp(12),cy,p);c.drawLine(cx,cy-r-dp(12),cx,cy+r+dp(12),p);
            p.setStyle(Paint.Style.FILL);p.setTextSize(dp(10));p.setTextAlign(Paint.Align.CENTER);p.setColor(MUTED);c.drawText("前",cx,cy-r-dp(10),p);c.drawText("后",cx,cy+r+dp(15),p);
            float distance=reading==null?0:(float)Math.min(reading.displayed/60,1)*r*.65f;
            float x=cx+(reading==null?0:(float)reading.x*distance),y=cy+(reading==null?0:(float)reading.y*distance),face=dp(29);
            p.setColor(PAPER);c.drawCircle(x,y,face,p);p.setColor(INK);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));c.drawCircle(x,y,face,p);p.setStyle(Paint.Style.FILL);
            c.drawOval(x-dp(12),y-dp(8),x-dp(8),y+dp(1),p);c.drawRoundRect(x+dp(6),y-dp(5),x+dp(14),y-dp(2),dp(1),dp(1),p);
            p.setStyle(Paint.Style.STROKE);c.drawArc(x-dp(8),y+dp(2),x+dp(12),y+dp(13),10,140,false,p);
        }
    }
}
