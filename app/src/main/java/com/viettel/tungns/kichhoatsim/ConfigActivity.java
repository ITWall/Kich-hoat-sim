package com.viettel.tungns.kichhoatsim;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ConfigActivity extends AppCompatActivity {
    private EditText mEdtJSONConfig;
    private EditText mEdtJSONCommand;
    private List<ConfigParameter> configParameterList;
    private List<Command> configCommandList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initData();
        initView();
    }

    private void initData() {
        String configParameterListJSON = getIntent().getStringExtra(MainActivity.CONFIG_PARAMETER_LIST);
        String configCommandListJSON = getIntent().getStringExtra(MainActivity.CONFIG_COMMAND_LIST);
        configParameterList = ConfigParameter.getListObjectFromString(configParameterListJSON);
        configCommandList = Command.getListObjectFromString(configCommandListJSON);
    }

    private void initView() {
        mEdtJSONConfig = findViewById(R.id.edt_JSON_config);
        mEdtJSONCommand = findViewById(R.id.edt_JSON_command);
        mEdtJSONConfig.setText(new Gson().toJson(configParameterList));
        mEdtJSONCommand.setText(new Gson().toJson(configCommandList));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnSaveConfig:
                String configJSON = mEdtJSONConfig.getText().toString();
                String commandJSON = mEdtJSONCommand.getText().toString();
                try {
                    configParameterList = ConfigParameter.getListObjectFromString(configJSON);
                    SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.CONFIG_PARAMETER_LIST, MODE_PRIVATE);
                    sharedPreferences.edit().putString(MainActivity.CONFIG_PARAMETER_LIST, configJSON).apply();
                    configCommandList = Command.getListObjectFromString(commandJSON);
                    SharedPreferences sharedPreferencesCommand = getSharedPreferences(MainActivity.CONFIG_COMMAND_LIST, MODE_PRIVATE);
                    sharedPreferencesCommand.edit().putString(MainActivity.CONFIG_COMMAND_LIST, commandJSON).apply();
                    onBackPressed();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.wrong_config), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
