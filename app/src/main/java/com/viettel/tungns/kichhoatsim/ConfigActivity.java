package com.viettel.tungns.kichhoatsim;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ConfigActivity extends AppCompatActivity implements OnItemClick, View.OnClickListener {
    private RecyclerView mRvPattern;
    private TextView mTvPhoneNumberCall;
    private TextView mTvPhoneNumberSms;
    private TextView mTvContentSms;
    private LinearLayout mLlPhoneNumberCall;
    private LinearLayout mLlPhoneNumberSms;
    private LinearLayout mLlContentSms;
    private Config config;
    private AlertDialog dialogActivation;
    private EditText mEdtActivation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initData();
        initView();
    }

    private void initData() {
        String configJSON = getIntent().getStringExtra("config");
        config = new Gson().fromJson(configJSON, Config.class);
    }

    private void initView() {
        mRvPattern = findViewById(R.id.rv_pattern);
        mTvPhoneNumberCall = findViewById(R.id.tv_phone_number_call);
        mTvPhoneNumberSms = findViewById(R.id.tv_phone_number_sms);
        mTvContentSms = findViewById(R.id.tv_content_sms);
        mLlPhoneNumberCall = findViewById(R.id.ll_phone_number_call);
        mLlPhoneNumberSms = findViewById(R.id.ll_phone_number_sms);
        mLlContentSms = findViewById(R.id.ll_content_sms);
        mTvPhoneNumberCall.setText(config.getPhoneNumberCall());
        String smsPhoneNumber = Utils.convertSmsPhoneNumberToString(config.getSmsPhoneNumberList());
        mTvPhoneNumberSms.setText(smsPhoneNumber);
        mTvContentSms.setText(config.getSmsContent());
        PatternListViewAdapter adapter = new PatternListViewAdapter(config.getScanPatternList(), this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mRvPattern.setLayoutManager(layoutManager);
        mRvPattern.addItemDecoration(decoration);
        mRvPattern.setAdapter(adapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                config.getScanPatternList().remove(position);
                mRvPattern.getAdapter().notifyDataSetChanged();

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRvPattern);
        mLlPhoneNumberCall.setOnClickListener(this);
        mLlPhoneNumberSms.setOnClickListener(this);
        mLlContentSms.setOnClickListener(this);
        dialogActivation = createDialogEditActivation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnAddConfig:
                final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_config, null);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                EditText mEdtPatternName = dialogView.findViewById(R.id.edt_pattern_name);
                                EditText mEdtPatternContent = dialogView.findViewById(R.id.edt_pattern_content);
                                try {
                                    Pattern pattern = Pattern.compile(mEdtPatternContent.getText().toString());
                                    String str0 = mEdtPatternContent.getText().toString().substring(mEdtPatternContent.getText().toString().indexOf("{") + 1, mEdtPatternContent.getText().toString().indexOf("}"));
                                    Integer.parseInt(str0);
                                } catch (Exception ex) {
                                    Toast.makeText(ConfigActivity.this, getString(R.string.wrong_pattern), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (mEdtPatternName.getText().toString().trim().length() == 0) {
                                    Toast.makeText(ConfigActivity.this, getString(R.string.wrong_name_pattern), Toast.LENGTH_SHORT).show();
                                } else {
                                    ScanPattern scanPattern = new ScanPattern(mEdtPatternName.getText().toString(), mEdtPatternContent.getText().toString());
                                    config.getScanPatternList().add(scanPattern);
                                    mRvPattern.getAdapter().notifyDataSetChanged();
                                    mRvPattern.scrollToPosition(config.getScanPatternList().size() - 1);
                                }
                            }
                        })
                        .setTitle("Thêm pattern")
                        .create();
                alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
                break;
            case R.id.mnSaveConfig:
                SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
                sharedPreferences.edit().putString("config", new Gson().toJson(config)).apply();
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(final int position) {
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_config, null);
        final EditText mEdtPatternName = dialogView.findViewById(R.id.edt_pattern_name);
        final EditText mEdtPatternContent = dialogView.findViewById(R.id.edt_pattern_content);
        mEdtPatternName.setText(config.getScanPatternList().get(position).getKey());
        mEdtPatternContent.setText(config.getScanPatternList().get(position).getValue());
        mEdtPatternName.setSelection(mEdtPatternName.getText().length());
        mEdtPatternContent.setSelection(mEdtPatternContent.getText().length());
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Pattern pattern = Pattern.compile(mEdtPatternContent.getText().toString());
                            String str0 = mEdtPatternContent.getText().toString().substring(mEdtPatternContent.getText().toString().indexOf("{") + 1, mEdtPatternContent.getText().toString().indexOf("}"));
                            Integer.parseInt(str0);
                        } catch (Exception ex) {
                            Toast.makeText(ConfigActivity.this, getString(R.string.wrong_pattern), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (mEdtPatternName.getText().toString().trim().length() == 0) {
                            Toast.makeText(ConfigActivity.this, getString(R.string.wrong_name_pattern), Toast.LENGTH_SHORT).show();
                        } else {
                            ScanPattern scanPattern = new ScanPattern(mEdtPatternName.getText().toString(), mEdtPatternContent.getText().toString());
                            config.getScanPatternList().set(position, scanPattern);
                            mRvPattern.getAdapter().notifyDataSetChanged();
                            mRvPattern.scrollToPosition(position);
                        }

                    }
                })
                .setTitle("Sửa pattern")
                .create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_phone_number_call:
                mEdtActivation.setText(mTvPhoneNumberCall.getText());
                mEdtActivation.setSelection(mEdtActivation.getText().length());
                dialogActivation.setTitle("Số điện thoại gọi để kích hoạt");
                dialogActivation.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        config.setPhoneNumberCall(mEdtActivation.getText().toString());
                        mTvPhoneNumberCall.setText(config.getPhoneNumberCall());
                    }
                });
                dialogActivation.show();
                break;
            case R.id.ll_phone_number_sms:
                mEdtActivation.setText(mTvPhoneNumberSms.getText());
                mEdtActivation.setSelection(mEdtActivation.getText().length());
                dialogActivation.setTitle("Số điện thoại gửi tin nhắn");
                dialogActivation.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String[] arrayPhoneSMS = mEdtActivation.getText().toString().split(";");
                        ArrayList<String> phoneSMSList = new ArrayList<>();
                        for (String phoneSMS : arrayPhoneSMS) {
                            phoneSMS = phoneSMS.trim();
                            phoneSMSList.add(phoneSMS);
                        }
                        config.setSmsPhoneNumberList(phoneSMSList);
                        mTvPhoneNumberSms.setText(mEdtActivation.getText().toString());
                    }
                });
                dialogActivation.show();
                break;
            case R.id.ll_content_sms:
                mEdtActivation.setText(mTvContentSms.getText());
                mEdtActivation.setSelection(mEdtActivation.getText().length());
                dialogActivation.setTitle("Nội dung tin nhắn");
                dialogActivation.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        config.setSmsContent(mEdtActivation.getText().toString());
                        mTvContentSms.setText(config.getSmsContent());
                    }
                });
                dialogActivation.show();
                break;
        }
    }

    private AlertDialog createDialogEditActivation() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_activation, null);
        mEdtActivation = dialogView.findViewById(R.id.edt_activation);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setNegativeButton("Hủy bỏ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setTitle("Sửa pattern")
                .create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return alertDialog;
    }
}
