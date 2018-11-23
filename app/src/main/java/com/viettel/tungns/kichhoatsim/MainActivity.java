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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnItemClick {
    private ImageView mIvPickImage;
    private RecyclerView mRvSimInfo;
    private static final int REQUEST_PICK_IMAGE = 232;
    private static final int REQUEST_PERMISSION = 10;
    private Button mBtnDoCommand;
    private List<ConfigParameter> configParameterList;
    private List<Command> configCommandList;
    public static final String CONFIG_PARAMETER_LIST = "ConfigParameterList";
    public static final String CONFIG_COMMAND_LIST = "ConfigCommandList";
    private SimInfo simInfo;
    org.apache.log4j.Logger log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS}, REQUEST_PERMISSION);
        }
        log = Log4jHelper.getLogger("MainActivity");
    }

    @Override
    protected void onResume() {
        initData();
        super.onResume();
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences(CONFIG_PARAMETER_LIST, MODE_PRIVATE);
        SharedPreferences sharedPreferencesCommand = getSharedPreferences(CONFIG_COMMAND_LIST, MODE_PRIVATE);
        if (sharedPreferences.getString(CONFIG_PARAMETER_LIST, "").equals("")) {
            ConfigParameter configParameterSTB = new ConfigParameter("STB", "Số thuê bao", 0, "(\\d){10}");
            ConfigParameter configParameterSeriSIM = new ConfigParameter("SeriSIM", "Số seri SIM", 1, "(\\d){19}");
            ConfigParameter configParameterPUK = new ConfigParameter("PUK", "PUK", 2, "(\\d){8}");
            ConfigParameter configParameterPIN = new ConfigParameter("PIN", "PIN", 3, "(\\d){4}");
            configParameterList = new ArrayList<>();
            configParameterList.add(configParameterSTB);
            configParameterList.add(configParameterSeriSIM);
            configParameterList.add(configParameterPUK);
            configParameterList.add(configParameterPIN);
            String configJSON = new Gson().toJson(configParameterList);
            sharedPreferences.edit().putString(CONFIG_PARAMETER_LIST, configJSON).apply();
        } else {
            String configParameterJSON = getSharedPreferences(CONFIG_PARAMETER_LIST, MODE_PRIVATE).getString(CONFIG_PARAMETER_LIST, "");
            configParameterList = ConfigParameter.getListObjectFromString(configParameterJSON);
        }
        if (sharedPreferencesCommand.getString(CONFIG_COMMAND_LIST, "").equals("")) {
            Content content = new Content(Constant.CALL, "900", Constant.CONSTANT, null);
            Command commandCall = new Command("Gọi 900", content);
            content = new Content(Constant.SMS, "123;456", Constant.CONSTANT, "abcxyz");
            Command commandSMS = new Command("Nhắn tin", content);
            configCommandList = new ArrayList<>();
            configCommandList.add(commandCall);
            configCommandList.add(commandSMS);
            content = new Content(Constant.CALL, "0", Constant.PARAMETER, null);
            Command commandCall2 = new Command("Gọi 0", content);
            configCommandList.add(commandCall2);
            String commandJSON = new Gson().toJson(configCommandList);
            sharedPreferencesCommand.edit().putString(CONFIG_COMMAND_LIST, commandJSON).apply();
        } else {
            String configCommandJSON = getSharedPreferences(CONFIG_COMMAND_LIST, MODE_PRIVATE).getString(CONFIG_COMMAND_LIST, "");
            configCommandList = Command.getListObjectFromString(configCommandJSON);
        }

    }

    private void initView() {
        mIvPickImage = findViewById(R.id.iv_pick_image);
        mBtnDoCommand = findViewById(R.id.btn_do_command);
        mRvSimInfo = findViewById(R.id.rv_sim_info);
        mIvPickImage.setOnClickListener(this);
        mBtnDoCommand.setOnClickListener(this);
        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(mIvPickImage);
        photoViewAttacher.update();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_pick_image:

            case R.id.btn_do_command:
                View v = LayoutInflater.from(this).inflate(R.layout.dialog_choose_command, null);
                RecyclerView mRvChooseCommand = v.findViewById(R.id.rv_choose_command);
                ChooseCommandAdapter adapter = new ChooseCommandAdapter(configCommandList, this);
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
                mRvChooseCommand.setAdapter(adapter);
                mRvChooseCommand.setLayoutManager(layoutManager);
                mRvChooseCommand.addItemDecoration(decoration);
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("Chọn hành động")
                        .setView(v)
                        .create();
                alertDialog.show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        log.info("onActivityResult");
        log.info("resultCode: " + resultCode);

        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_PICK_IMAGE) {
                    if (data != null) {
                        Uri uri = data.getData();
                        try {
                            log.info("data: " + data);
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            log.info("bitmap: " + bitmap);
                            log.info("bitmap height 2: " + bitmap.getWidth() + " - " + bitmap.getHeight());
//                        getAllText(bitmap, 0);
//                            bitmap = rotateBitmap(bitmap, 90);
                            mIvPickImage.setImageBitmap(bitmap);
                            if (bitmap.getWidth() > bitmap.getHeight()) {
                                Toast.makeText(this, "Hãy xoay lại ảnh", Toast.LENGTH_SHORT).show();
                            } else {
                                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                                FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                                        .getOnDeviceTextRecognizer();
                                textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText visionText) {
                                        log.info("vision text: " + visionText);
                                        mRvSimInfo.setVisibility(View.VISIBLE);
                                        Toast.makeText(MainActivity.this, ""+visionText.getText(), Toast.LENGTH_SHORT).show();
                                        simInfo = getInfoSim(visionText);
                                        InfoRecyclerViewAdapter adapter = new InfoRecyclerViewAdapter(simInfo, MainActivity.this);
                                        RecyclerView.LayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
                                        mRvSimInfo.setLayoutManager(manager);
                                        mRvSimInfo.setAdapter(adapter);
                                        log.info(visionText.getText());
//                                log.info("Info");
//                                log.warn("Warn");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            log.info("bitmap: " + bitmap.getHeight());
                        } catch (IOException e) {
                            log.info(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("___onActivityResult");
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
                intent.putExtra(CONFIG_PARAMETER_LIST, new Gson().toJson(configParameterList));
                intent.putExtra(CONFIG_COMMAND_LIST, new Gson().toJson(configCommandList));
                startActivity(intent);
            case R.id.mnChoose:
                Intent intentChoose = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intentChoose, REQUEST_PICK_IMAGE);
                log.info("Pick image");
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private SimInfo getInfoSim(FirebaseVisionText visionText) {
        SimInfo simInfo = new SimInfo();
        Collections.sort(configParameterList, new Comparator<ConfigParameter>() {
            @Override
            public int compare(ConfigParameter c0, ConfigParameter c1) {
                String str0 = c0.getPattern().substring(c0.getPattern().indexOf("{") + 1, c0.getPattern().indexOf("}"));
                String str1 = c1.getPattern().substring(c1.getPattern().indexOf("{") + 1, c1.getPattern().indexOf("}"));
                return Integer.parseInt(str1) - Integer.parseInt(str0);
            }
        });
        for (FirebaseVisionText.TextBlock block : visionText.getTextBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                for (int i = 0; i < configParameterList.size(); i++) {
                    Pattern pattern = Pattern.compile(configParameterList.get(i).getPattern());
                    Matcher matcher = pattern.matcher(line.getText());
                    if (matcher.find()) {
                        String info = matcher.group(0);
                        ArrayList<String> infoList;
                        if (simInfo.getMapInfo().get(configParameterList.get(i)) == null) {
                            infoList = new ArrayList<>();
                        } else {
                            infoList = simInfo.getMapInfo().get(configParameterList.get(i));
                        }
                        infoList.add(info);
                        simInfo.getMapInfo().put(configParameterList.get(i), infoList);
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

    @Override
    public void onItemClick(int position) {
        Content cmd = configCommandList.get(position).getContent();
        if (cmd.getAction().equalsIgnoreCase(Constant.CALL)) {
            if (cmd.getType().equalsIgnoreCase(Constant.PARAMETER)) {
                if (simInfo == null) {
                    Toast.makeText(this, "Cần quét ảnh để lấy thông tin trước", Toast.LENGTH_SHORT).show();
                    return;
                }
//                Toast.makeText(this, "" + subCmd[1].substring(1), Toast.LENGTH_SHORT).show();
                ConfigParameter configParameter = getConfigParameterByPosition(configParameterList, Integer.parseInt(cmd.getDestination()));
                if (configParameter != null) {
//                    Toast.makeText(this, "" + configParameter.toString(), Toast.LENGTH_SHORT).show();
                    ArrayList<String> telList = simInfo.getMapInfo().get(configParameter);
                    if (telList != null) {
                        call(telList.get(0));
                    } else {
//                        Toast.makeText(this, "tellList is null", Toast.LENGTH_SHORT).show();
                        TextView tv = findViewById(R.id.tv_map);
                        tv.setText(simInfo.getMapInfo().toString());
                    }
                } else {
                    Toast.makeText(this, "Position trong config không tồn tại", Toast.LENGTH_SHORT).show();
                }

            } else if (cmd.getType().equalsIgnoreCase(Constant.CONSTANT)) {
                call(cmd.getDestination());
            }
        } else if (cmd.getAction().equalsIgnoreCase(Constant.SMS)) {
            if (cmd.getType().equalsIgnoreCase(Constant.PARAMETER)) {

            } else if (cmd.getType().equalsIgnoreCase(Constant.CONSTANT)) {
                sendSMS(cmd.getSmsBody(), cmd.getDestination());
            }
        } else {
            Toast.makeText(this, "Lệnh không phù hợp, không thể thực thi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void call (String tel) {
        Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + tel));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bạn chưa cấp quyền gọi điện cho ứng dụng để thực hiện chức năng này!", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intentCall);
    }

    private void sendSMS (String body, String addresses) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bạn chưa cấp quyền gửi tin nhắn cho ứng dụng để thực hiện chức năng này!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intentSendMessage = new Intent(Intent.ACTION_VIEW);
            intentSendMessage.putExtra("sms_body", body);
            intentSendMessage.putExtra("address", addresses);
            intentSendMessage.setType("vnd.android-dir/mms-sms");
            startActivity(intentSendMessage);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Gửi tin nhắn thất bại, vui lòng thử lại!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private ConfigParameter getConfigParameterByPosition (List<ConfigParameter> configParameterList, int position) {
        for (ConfigParameter configParameter: configParameterList) {
            if (configParameter.getPosition() == position) {
//                Toast.makeText(this, "" + configParameter.toString(), Toast.LENGTH_SHORT).show();
                return configParameter;
            }
        }
        Toast.makeText(this, "Cannot find config parameter with position " + position, Toast.LENGTH_SHORT).show();
        return null;
    }

    public void getAllText(final Bitmap bitmap, final int degree) {
        final Bitmap finalBitmap = rotateBitmap(bitmap, degree);
        if (degree == 360) {
            return;
        }
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(finalBitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText visionText) {
                String text = "";
                for (FirebaseVisionText.TextBlock block : visionText.getTextBlocks()) {
                    for (FirebaseVisionText.Line line : block.getLines()) {
                        String textLine = line.getText();
                        text += textLine + "\n";
                    }
                }
                Toast.makeText(MainActivity.this, degree + " " + text, Toast.LENGTH_SHORT).show();
                int copyDegree = degree + 90;
                getAllText(finalBitmap, copyDegree);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}
