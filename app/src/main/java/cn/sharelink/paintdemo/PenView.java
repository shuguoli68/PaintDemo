package cn.sharelink.paintdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class PenView extends View{

    private Paint mPaint;

    public PenView(Context context) {
        super(context);
        init();
    }

    public PenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setStrokeWidth(App.get(App.penSize,4));
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);//使画笔更加圆润
        mPaint.setStrokeCap(Paint.Cap.ROUND);//同上
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(getMeasuredWidth()/2-100,getMeasuredHeight()/2,getMeasuredWidth()/2+100,getMeasuredHeight()/2,mPaint);
    }

    public void setPen(float stroke){
        mPaint.setStrokeWidth(stroke);
        invalidate();
    }
}
