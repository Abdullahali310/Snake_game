package ai.poolvision.ultra;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private Game game;
    private TextView status;
    private TextView powerLabel;
    private int power=56, sideSpin=0, topSpin=0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(7,14,24));
        getWindow().setNavigationBarColor(Color.rgb(7,14,24));
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(12),dp(12),dp(12));
        root.setBackgroundColor(Color.rgb(7,14,24));
        TextView title=label("POOL VISION  /  ULTRA",23,0xfff6f9ff);
        title.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
        root.addView(title);
        status=label("SIMULATION LAB  •  V1",12,0xff72eab8);
        root.addView(status);
        game=new Game();
        root.addView(game,new LinearLayout.LayoutParams(-1,0,1));
        powerLabel=label("POWER  "+power+"%",14,0xffeaf2f6);
        root.addView(powerLabel);
        SeekBar powerBar=bar(0,100,power);
        powerBar.setOnSeekBarChangeListener(new Change(){public void onProgressChanged(SeekBar s,int p,boolean f){power=Math.max(5,p);powerLabel.setText("POWER  "+power+"%");game.invalidate();}});
        root.addView(powerBar);
        LinearLayout spinRow=new LinearLayout(this);
        spinRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout spinA=new LinearLayout(this), spinB=new LinearLayout(this);
        spinA.setOrientation(LinearLayout.VERTICAL);spinB.setOrientation(LinearLayout.VERTICAL);
        spinA.addView(label("SIDE SPIN",12,0xffc0d1dc));
        SeekBar sb=bar(0,200,100);
        sb.setOnSeekBarChangeListener(new Change(){public void onProgressChanged(SeekBar s,int p,boolean f){sideSpin=p-100;game.invalidate();}});
        spinA.addView(sb);
        spinB.addView(label("TOP / BACK SPIN",12,0xffc0d1dc));
        SeekBar tb=bar(0,200,100);
        tb.setOnSeekBarChangeListener(new Change(){public void onProgressChanged(SeekBar s,int p,boolean f){topSpin=p-100;game.invalidate();}});
        spinB.addView(tb);
        spinRow.addView(spinA,new LinearLayout.LayoutParams(0,-2,1));
        spinRow.addView(spinB,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(spinRow);
        LinearLayout actions=new LinearLayout(this);
        addButton(actions,"SHOOT",0xff50e9b0,0xff10271c,()->game.shoot());
        addButton(actions,"RESET",0xff21364b,Color.WHITE,()->{game.reset();status.setText("SIMULATION LAB  •  V1");});
        addButton(actions,"LINES",0xff21364b,Color.WHITE,()->{game.lines=!game.lines;game.invalidate();});
        root.addView(actions);
        TextView note=label("Drag balls to reposition • tap table to aim • practice physics only",11,0xff91a4b4);
        note.setGravity(Gravity.CENTER);note.setPadding(0,dp(10),0,0);
        root.addView(note);
        setContentView(root);
    }
    int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    TextView label(String t,int size,int color){TextView v=new TextView(this);v.setText(t);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(4),dp(3),dp(4),dp(3));return v;}
    SeekBar bar(int min,int max,int progress){SeekBar v=new SeekBar(this);v.setMax(max);v.setProgress(progress);v.setProgressTintList(android.content.res.ColorStateList.valueOf(0xff53eab4));v.setThumbTintList(android.content.res.ColorStateList.valueOf(0xff53eab4));return v;}
    void addButton(LinearLayout row,String name,int color,int ink,Runnable action) {
        Button b=new Button(this);
        b.setText(name);b.setAllCaps(false);b.setTextColor(ink);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        b.setOnClickListener(v->action.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(53),1);
        p.setMargins(dp(3),0,dp(3),0);row.addView(b,p);
    }
    abstract static class Change implements SeekBar.OnSeekBarChangeListener {
        public void onStartTrackingTouch(SeekBar s){}
        public void onStopTrackingTouch(SeekBar s){}
    }
    static class Ball {
        float x,y,vx,vy,sx,sy; final int color;
        boolean pocketed;
        Ball(float x,float y,int c){this.x=x;this.y=y;this.color=c;}
        Ball cloneBall(){Ball a=new Ball(x,y,color);a.vx=vx;a.vy=vy;a.sx=sx;a.sy=sy;a.pocketed=pocketed;return a;}
    }
    class Game extends View {
        final Paint p=new Paint(3); final ArrayList<Ball> balls=new ArrayList<>();
        float ax=720,ay=260,scale=1,offX=0,offY=0;
        int drag=-1; boolean moving=false,lines=true;
        final int[] palette={0xffe9edef,0xfff9c84a,0xff4589ef,0xffee5d64,0xff9a68e7,0xfff48c48,0xff49d9b4,0xffdd62c6};
        final float left=55,right=945,top=78,bottom=522,r=12;
        Game(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);reset();}
        void reset(){
            moving=false;balls.clear();
            balls.add(new Ball(255,300,palette[0]));
            int k=1;
            for(int row=0;row<3;row++){
                for(int n=0;n<=row;n++){
                    balls.add(new Ball(600+row*27,300+(n-row/2f)*26,palette[k++%palette.length]));
                }
            }
            ax=740;ay=300;invalidate();
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            c.drawColor(0xff07101b);
            float w=getWidth(),h=getHeight();
            scale=Math.min(w/1000f,h/610f);
            offX=(w-1000f*scale)/2;offY=(h-610f*scale)/2;
            c.save();c.translate(offX,offY);c.scale(scale,scale);
            p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(0xff132d35);
            c.drawRoundRect(15,38,985,562,30,30,p);
            p.setColor(0xffae8150);c.drawRoundRect(28,50,972,551,24,24,p);
            p.setColor(0xff163d39);c.drawRoundRect(left,top,right,bottom,10,10,p);
            p.setShader(new LinearGradient(0,top,0,bottom,0xff1a5145,0xff092a2a,Shader.TileMode.CLAMP));
            c.drawRoundRect(left,top,right,bottom,10,10,p);p.setShader(null);
            p.setColor(0xff0c1e21);
            for(float px:new float[]{left,(left+right)/2,right})
                for(float py:new float[]{top,bottom})c.drawCircle(px,py,21,p);
            p.setColor(0x55c1fff1);p.setStrokeWidth(1.5f);
            c.drawLine(300,top+16,300,bottom-16,p);
            p.setColor(0x88e8eee8);c.drawCircle(300,300,4,p);
            if(lines&&!moving)prediction(c);
            for(int i=0;i<balls.size();i++){
                Ball b=balls.get(i);if(b.pocketed)continue;
                p.setColor(0x7f000000);c.drawCircle(b.x+3,b.y+4,r+2,p);
                p.setShader(new RadialGradient(b.x-4,b.y-6,21,
                    new int[]{0xffffffff,b.color,0xff26313d},new float[]{0f,.35f,1f},Shader.TileMode.CLAMP));
                c.drawCircle(b.x,b.y,r,p);p.setShader(null);
                if(i>0){p.setColor(0xbbffffff);c.drawCircle(b.x,b.y,4,p);}
            }
            if(!moving&&!balls.get(0).pocketed){
                Ball cue=balls.get(0);float dx=ax-cue.x,dy=ay-cue.y;
                float d=(float)Math.hypot(dx,dy);
                if(d>0){dx/=d;dy/=d;
                    p.setStrokeWidth(2);p.setColor(0xfff8e4bc);
                    c.drawLine(cue.x-dx*23,cue.y-dy*23,cue.x-dx*(85+power),cue.y-dy*(85+power),p);
                    p.setColor(0xffe8bf70);c.drawCircle(cue.x-dx*(85+power),cue.y-dy*(85+power),5,p);
                }
            }
            p.setColor(0xffecf4ee);p.setTextSize(17);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
            c.drawText("POOL VISION  •  PHYSICS SIMULATOR",57,28,p);
            p.setTextSize(13);p.setColor(0xff96afb2);
            c.drawText("Model approximation • not connected to 8 Ball Pool",57,591,p);
            c.restore();
            if(moving){simulateStep(balls);if(hasMotion())postInvalidateDelayed(16);else {moving=false;invalidate();}}
        }
        void prediction(Canvas c){
            ArrayList<Ball> test=new ArrayList<>();
            for(Ball b:balls)test.add(b.cloneBall());
            float dx=ax-test.get(0).x,dy=ay-test.get(0).y,d=(float)Math.hypot(dx,dy);
            if(d<1)return;
            float speed=2.8f+power*0.11f;
            Ball cue=test.get(0);cue.vx=dx/d*speed;cue.vy=dy/d*speed;
            cue.sx=sideSpin/100f;cue.sy=topSpin/100f;
            float[] px=new float[test.size()],py=new float[test.size()];
            for(int i=0;i<test.size();i++){px[i]=test.get(i).x;py[i]=test.get(i).y;}
            for(int t=0;t<240;t++){
                simulateStep(test);
                if(t%3!=0)continue;
                for(int i=0;i<test.size();i++){
                    Ball b=test.get(i);
                    if(b.pocketed)continue;
                    p.setStrokeWidth(i==0?2.8f:1.9f);
                    p.setColor(i==0?0xaa65f9d5:0x887dc5fa);
                    c.drawLine(px[i],py[i],b.x,b.y,p);
                    px[i]=b.x;py[i]=b.y;
                }
            }
            for(int i=0;i<test.size();i++){
                Ball b=test.get(i);if(b.pocketed)continue;
                p.setColor(i==0?0xff65f9d5:0xff88bdfb);
                c.drawCircle(b.x,b.y,4,p);
            }
        }
        boolean hasMotion(){for(Ball b:balls)if(Math.abs(b.vx)+Math.abs(b.vy)>.016)return true;return false;}
        void shoot(){
            if(moving)return;Ball cue=balls.get(0);
            if(cue.pocketed){reset();return;}
            float dx=ax-cue.x,dy=ay-cue.y,d=(float)Math.hypot(dx,dy);
            if(d<2){ax=cue.x+100;ay=cue.y;dx=100;dy=0;d=100;}
            float speed=2.8f+power*0.11f;
            cue.vx=dx/d*speed;cue.vy=dy/d*speed;
            cue.sx=sideSpin/100f;cue.sy=topSpin/100f;
            moving=true;status.setText("SHOT IN MOTION  •  SIMULATING");invalidate();
        }
        void simulateStep(ArrayList<Ball> bs){
            for(Ball b:bs){
                if(b.pocketed)continue;
                if(Math.abs(b.vx)+Math.abs(b.vy)<.017){b.vx=0;b.vy=0;continue;}
                // Empirical spin response for training only; not calibrated to the game.
                float curve=b.sx*.0017f;
                float vx=b.vx,vy=b.vy;
                b.vx=vx-vy*curve;b.vy=vy+vx*curve;
                b.x+=b.vx;b.y+=b.vy;
                float damp=.985f+b.sy*.002f;
                b.vx*=damp;b.vy*=damp;b.sx*=.991f;b.sy*=.992f;
                if(b.x<left+r||b.x>right-r){
                    b.x=Math.max(left+r,Math.min(right-r,b.x));
                    b.vx=-b.vx*.91f;b.vy+=b.sx*.20f;
                }
                if(b.y<top+r||b.y>bottom-r){
                    b.y=Math.max(top+r,Math.min(bottom-r,b.y));
                    b.vy=-b.vy*.91f;b.vx+=b.sx*.20f;
                }
                for(float pocketX:new float[]{left,(left+right)/2,right}) {
                    if((pocketX==(left+right)/2 || pocketX==left || pocketX==right)
                       && (Math.hypot(b.x-pocketX,b.y-top)<16 || Math.hypot(b.x-pocketX,b.y-bottom)<16)){
                        b.pocketed=true;b.vx=b.vy=0;break;
                    }
                }
            }
            for(int i=0;i<bs.size();i++){
                Ball a=bs.get(i);if(a.pocketed)continue;
                for(int j=i+1;j<bs.size();j++){
                    Ball b=bs.get(j);if(b.pocketed)continue;
                    float dx=b.x-a.x,dy=b.y-a.y;
                    float dd=dx*dx+dy*dy;
                    if(dd<=.0001f||dd>=4*r*r)continue;
                    float dist=(float)Math.sqrt(dd),nx=dx/dist,ny=dy/dist;
                    float shift=(2*r-dist)*.5f;
                    a.x-=nx*shift;a.y-=ny*shift;b.x+=nx*shift;b.y+=ny*shift;
                    float rel=(b.vx-a.vx)*nx+(b.vy-a.vy)*ny;
                    if(rel<0){
                        float imp=-rel*.95f;
                        a.vx-=imp*nx;a.vy-=imp*ny;
                        b.vx+=imp*nx;b.vy+=imp*ny;
                        b.sx+=a.sx*.08f;
                    }
                }
            }
        }
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            float x=(e.getX()-offX)/scale,y=(e.getY()-offY)/scale;
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(moving)return true;
                drag=-1;
                for(int i=0;i<balls.size();i++){
                    Ball b=balls.get(i);
                    if(!b.pocketed&&Math.hypot(x-b.x,y-b.y)<25){drag=i;break;}
                }
                if(drag==-1){ax=x;ay=y;}invalidate();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                if(drag>=0){Ball b=balls.get(drag);b.x=Math.max(left+r,Math.min(right-r,x));b.y=Math.max(top+r,Math.min(bottom-r,y));}
                else{ax=x;ay=y;}
                invalidate();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){drag=-1;invalidate();return true;}
            return true;
        }
    }
}
