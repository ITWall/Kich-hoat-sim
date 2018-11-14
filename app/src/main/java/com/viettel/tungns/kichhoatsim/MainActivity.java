package com.viettel.tungns.kichhoatsim;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView mIvPickImage;
    private RecyclerView mRvSimInfo;
    private static final int REQUEST_PICK_IMAGE = 232;
    private static final int REQUEST_PERMISSION = 10;
    private Button mBtnCallActivate;
    private Button mBtnSendMesage;
    private Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, REQUEST_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        initData();
        super.onResume();
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        if (sharedPreferences.getString("config", "").equals("")) {
            Toast.makeText(this, "config is empty", Toast.LENGTH_SHORT).show();
            config = new Config();
            config.setPhoneNumberCall("900");
            config.setSmsContent("abc");
            config.getSmsPhoneNumberList().add("12345");
            config.getSmsPhoneNumberList().add("54321");
            config.getScanPatternList().add(new ScanPattern("Số thuê bao", "(\\d){10}"));
            config.getScanPatternList().add(new ScanPattern("Số seri SIM", "(\\d){19}"));
            config.getScanPatternList().add(new ScanPattern("PUK", "(\\d){8}"));
            config.getScanPatternList().add(new ScanPattern("PIN", "(\\d){4}"));
            String configJSON = new Gson().toJson(config);
            sharedPreferences.edit().putString("config", configJSON).apply();
        } else {
            String configJSON = getSharedPreferences("config", MODE_PRIVATE).getString("config", "");
            config = new Gson().fromJson(configJSON, Config.class);
        }
    }

    private void initView() {
        mIvPickImage = findViewById(R.id.iv_pick_image);
        mBtnCallActivate = findViewById(R.id.btn_call_activate);
        mBtnSendMesage = findViewById(R.id.btn_send_mesage);
        mRvSimInfo = findViewById(R.id.rv_sim_info);
        mIvPickImage.setOnClickListener(this);
        mBtnCallActivate.setOnClickListener(this);
        mBtnSendMesage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_pick_image:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICK_IMAGE);
                break;
            case R.id.btn_call_activate:
                Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + config.getPhoneNumberCall()));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Bạn chưa cấp quyền gọi điện cho ứng dụng để thực hiện chức năng này!", Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(intentCall);
                break;
            case R.id.btn_send_mesage:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Bạn chưa cấp quyền gửi tin nhắn cho ứng dụng để thực hiện chức năng này!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Intent intentSendMessage = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:"));
                    intentSendMessage.putExtra("sms_body", config.getSmsContent());
                    intentSendMessage.putExtra("address", Utils.convertSmsPhoneNumberToString(config.getSmsPhoneNumberList()));
                    intentSendMessage.setType("vnd.android-dir/mms-sms");
                    startActivity(intentSendMessage);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "Gửi tin nhắn thất bại, vui lòng thử lại!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_IMAGE) {
                if (data != null) {
                    Uri uri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        bitmap = rotateBitmap(bitmap, 90);
                        mIvPickImage.setImageBitmap(bitmap);
                        final Bitmap finalBitmap = bitmap;
                        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(finalBitmap);
                        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                                .getOnDeviceTextRecognizer();
                        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText visionText) {
                                mRvSimInfo.setVisibility(View.VISIBLE);
                                SimInfo simInfo = getInfoSim(visionText);
                                InfoRecyclerViewAdapter adapter = new InfoRecyclerViewAdapter(simInfo, MainActivity.this);
                                RecyclerView.LayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
                                mRvSimInfo.setLayoutManager(manager);
                                mRvSimInfo.setAdapter(adapter);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnConfig:
                Intent intent = new Intent(this, ConfigActivity.class);
                intent.putExtra("config", new Gson().toJson(config));
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private SimInfo getInfoSim(FirebaseVisionText visionText) {
        SimInfo simInfo = new SimInfo();
        Collections.sort(config.getScanPatternList(), new Comparator<ScanPattern>() {
            @Override
            public int compare(ScanPattern s0, ScanPattern s1) {
                String str0 = s0.getValue().substring(s0.getValue().indexOf("{") + 1, s0.getValue().indexOf("}"));
                String str1 = s1.getValue().substring(s1.getValue().indexOf("{") + 1, s1.getValue().indexOf("}"));
                return Integer.parseInt(str1) - Integer.parseInt(str0);
            }
        });
        for (FirebaseVisionText.TextBlock block : visionText.getTextBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                for (int i = 0; i < config.getScanPatternList().size(); i++) {
                    Pattern pattern = Pattern.compile(config.getScanPatternList().get(i).getValue());
                    Matcher matcher = pattern.matcher(line.getText());
                    if (matcher.find()) {
                        String info = matcher.group(0);
                        ArrayList<String> infoList;
                        if (simInfo.getMapInfo().get(config.getScanPatternList().get(i).getKey()) == null) {
                            infoList = new ArrayList<>();
                        } else {
                            infoList = simInfo.getMapInfo().get(config.getScanPatternList().get(i).getKey());
                        }
                        infoList.add(info);
                        simInfo.getMapInfo().put(config.getScanPatternList().get(i).getKey(), infoList);
                        break;
                    }
                }
            }
        }
        return simInfo;
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private ArrayAdapter<String> getAdapterSpinner(ArrayList<String> listChoice) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listChoice);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        return arrayAdapter;
    }
}
