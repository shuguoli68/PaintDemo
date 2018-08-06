package cn.sharelink.paintdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.qqtheme.framework.picker.ColorPicker;
import cn.qqtheme.framework.util.ConvertUtils;

public class MainActivity extends AppCompatActivity {

    private PaintView paintView;
    private final static String FILE_PATH = "PaintDemo";
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initMenu();
    }

    /**
     * 初始化
     */
    private void initView() {
        paintView = (PaintView) findViewById(R.id.activity_paint_pv);
    }

    /**
     * 初始化底部菜单
     */
    private void initMenu() {
        //撤销
        menuItemSelected(R.id.activity_paint_undo, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                paintView.undo();
            }
        });
        //恢复
        menuItemSelected(R.id.activity_paint_redo, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                paintView.redo();
            }
        });

        //颜色
        menuItemSelected(R.id.activity_paint_color, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                ColorPicker colorPicker = new ColorPicker(MainActivity.this);
                colorPicker.setInitColor(0xDD00DD);
                colorPicker.setOnColorPickListener(new ColorPicker.OnColorPickListener() {
                    @Override
                    public void onColorPicked(int pickedColor) {
                        String color = "#"+ConvertUtils.toColorString(pickedColor);
                        paintView.setPaintColor(color);
                    }
                });
                colorPicker.show();
            }
        });
        //清空
        menuItemSelected(R.id.activity_paint_clear, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                paintView.clearAll();
            }
        });

        //橡皮擦
        menuItemSelected(R.id.activity_paint_eraser, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                paintView.setEraserModel();
            }
        });

        //画笔
        menuItemSelected(R.id.activity_paint, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                bottomPopupMenu();
            }
        });

        //保存
        menuItemSelected(R.id.activity_paint_save, new MenuSelectedListener() {
            @Override
            public void onMenuSelected() {
                permissionRequest();
            }
        });
    }

    private void permissionRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        } else {
            doBack();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doBack();
            } else {
                Toast.makeText(this, "权限获取失败，无法保存数据！", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void doBack() {
        String path = Environment.getExternalStorageDirectory().getPath()
                + File.separator + FILE_PATH;
        String imgName = System.currentTimeMillis()+".jpg";
        if (paintView.saveImg(path,imgName)) {
            Toast.makeText(MainActivity.this, "保存成功\n"+path+File.separator+imgName,Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 选中底部 Menu 菜单项
     */
    private void menuItemSelected(int viewId, final MenuSelectedListener listener) {
        findViewById(viewId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMenuSelected();
            }
        });

    }
    int progress = 4;
    private void bottomPopupMenu(){
        View view = LayoutInflater.from(this).inflate(R.layout.layout_pen,null);
        final PenView penView = view.findViewById(R.id.penView);
        SeekBar penSeekBar = view.findViewById(R.id.pen_seekBar);
        penSeekBar.setMax(100);
        penSeekBar.setProgress(App.get(App.penSize,4));
        final TextView penStroke = view.findViewById(R.id.pen_stroke);
        penStroke.setText(App.get(App.penSize,4)+"");
        penSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                penStroke.setText(i+"");
                progress = i;
                penView.setPen((float) i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        view.findViewById(R.id.pen_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        view.findViewById(R.id.pen_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.setPaintSize((float) progress);
                App.set(App.penSize,progress);
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.menu_pen_anim);
        // 弹出窗口显示内容视图,默认以锚定视图的左下角为起点
        popupWindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_main, null), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        //菜单背景色
        ColorDrawable dw = new ColorDrawable(0xffffffff);
        popupWindow.setBackgroundDrawable(dw);
        //内容背景透明度
        backgroundAlpha(0.5f);
        //关闭事件，弹出窗口消失将背景透明度改回来
        popupWindow.setOnDismissListener(new popupDismissListener());
    }
    //内容背景透明度
    private void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }
    class popupDismissListener implements PopupWindow.OnDismissListener{
        public void onDismiss() {
            backgroundAlpha(1.0f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    interface MenuSelectedListener {
        void onMenuSelected();
    }
}
