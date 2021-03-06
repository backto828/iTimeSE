package com.chen.practice.itimese;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chen.practice.itimese.model.MyTicker;
import com.chen.practice.itimese.model.RepeatCycle;
import com.chen.practice.itimese.others.RepeatCycleDialog;
import com.chen.practice.itimese.others.Tools;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;

import static com.chen.practice.itimese.MainActivity.setStatusBarTransparent;
import static com.chen.practice.itimese.ui.home.HomeFragment.ADD_MODE;
import static com.chen.practice.itimese.ui.home.HomeFragment.MODIFY_MODE;

public class NewAndEditTickerActivity extends AppCompatActivity {
    //定义一个startActivityForResult（）方法用到的整型值
    private static final int requestCode = 2014;

    private EditText editTitle, editRemark;

    private TextView dateTextView;
    private TextView repeatDayTextView;

    private MyTicker myTicker;

    private int mode;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == this.requestCode) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    // 得到图片的路径
                    Uri uri = data.getData();
                    myTicker.imageUriPath = uri.toString();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_edit_ticker);

        // 状态栏透明
        setStatusBarTransparent(this, R.id.edit_time_app_bar_layout);

        editTitle = this.findViewById(R.id.edit_text_title);
        editRemark = this.findViewById(R.id.edit_text_remark);
        dateTextView = this.findViewById(R.id.text_view_date_detail);
        repeatDayTextView = this.findViewById(R.id.text_view_repeat_detail);

        Intent intent = getIntent();
        mode = intent.getIntExtra("mode", -1);

        // 数据初始化
        if (mode == MODIFY_MODE) {
            myTicker = (MyTicker) intent.getSerializableExtra("time");
            editTitle.setText(myTicker.title);
            editRemark.setText(myTicker.remark);
        } else {
            myTicker = new MyTicker();
            // 初始化日期
            Calendar c = Calendar.getInstance();
            setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
        }
        // 显示日期
        showSelectedDate();
        // 显示重复周期
        repeatDayTextView.setText(myTicker.repeatCycle.toString());

        // 获取颜色
        int color = intent.getIntExtra("color", 0x03A9F4);
        if (mode == ADD_MODE) {
            // 设置颜色
            ((ImageView) this.findViewById(R.id.image_view_edit_time)).setColorFilter(color);
        } else {
            // 图片
            if (!myTicker.imageUriPath.isEmpty()) {
                Bitmap bitmap = Tools.getBitmapFromUriString(this.getContentResolver(), myTicker.imageUriPath);
                if (bitmap != null) {
                    ((ImageView) this.findViewById(R.id.image_view_edit_time)).setImageBitmap(bitmap);
                } else {
                    ((ImageView) this.findViewById(R.id.image_view_edit_time)).setColorFilter(color);
                }
            } else {
                ((ImageView) this.findViewById(R.id.image_view_edit_time)).setColorFilter(color);
            }
        }

        final Toolbar toolbar = this.findViewById(R.id.tool_bar_edit_time);
        setSupportActionBar(toolbar);
        // 工具栏内容设置
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        // 工具栏按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 取消
                NewAndEditTickerActivity.this.finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                myTicker.title = editTitle.getText().toString();
                myTicker.remark = editRemark.getText().toString();

                if (myTicker.title.isEmpty()) {
                    Snackbar.make(toolbar, "标题不能为空", Snackbar.LENGTH_LONG).show();
                    return false;
                }

                // 保存数据
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("time", myTicker);
                bundle.putInt("mode", mode);
                intent.putExtras(bundle);
                NewAndEditTickerActivity.this.setResult(RESULT_OK, intent);

                NewAndEditTickerActivity.this.finish();
                return true;
            }
        });

        this.findViewById(R.id.date_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 选择日期
                Calendar c = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(NewAndEditTickerActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // 储存日期
                                setDate(year, monthOfYear + 1, dayOfMonth);
                                // 显示日期
                                showSelectedDate();
                            }
                        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
        this.findViewById(R.id.repeat_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 选择重复
                ArrayList<String> items = (ArrayList<String>) RepeatCycle.repeatDayItemLabel.clone();
                if (myTicker.repeatCycle.type == RepeatCycle.NONE) {
                    items.remove(items.size() - 1);
                }
                new AlertDialog.Builder(NewAndEditTickerActivity.this)
                        .setTitle("周期")
                        .setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which != RepeatCycle.repeatDayItemType.indexOf(RepeatCycle.CUSTOMIZE)) {
                                    myTicker.repeatCycle.setType(RepeatCycle.repeatDayItemType.get(which));
                                    repeatDayTextView.setText(myTicker.repeatCycle.toString());
                                } else {
                                    // 自定义日期
                                    RepeatCycleDialog repeatCycleDialog = new RepeatCycleDialog(NewAndEditTickerActivity.this, new RepeatCycleDialog.DialogEventListener() {
                                        @Override
                                        public void DialogEvent(int day) {
                                            myTicker.repeatCycle.setCustomizeDay(day);
                                            repeatDayTextView.setText(myTicker.repeatCycle.toString());
                                        }
                                    });
                                    repeatCycleDialog.setTitle("周期");
                                    repeatCycleDialog.show();
                                    dialog.dismiss();
                                }
                            }
                        })
                        .show();
            }
        });
        this.findViewById(R.id.background_image_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 选择图片
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, requestCode);
            }
        });
    }


     //点击非编辑区域收起键盘
     // 获取点击事件
    @CallSuper
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isShouldHideKeyBord(view, ev)) {
                hideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判定当前是否需要隐藏
     * 通过view和event的位置判断
     */
    protected boolean isShouldHideKeyBord(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            return !(ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom);
        }
        return false;
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setDate(int year, int month, int day) {
        myTicker.setDate(year, month, day);
    }

    private void showSelectedDate() {
        dateTextView.setText(myTicker.date.year + "年" + myTicker.date.month + "月" + myTicker.date.day + "日");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 设置菜单栏按钮图片
        getMenuInflater().inflate(R.menu.activity_edit_time_confirm, menu);
        return true;
    }
}


