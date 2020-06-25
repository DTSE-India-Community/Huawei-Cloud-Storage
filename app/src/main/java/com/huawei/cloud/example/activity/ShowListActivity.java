package com.huawei.cloud.example.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.ListResult;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.cloud.example.R;
import com.huawei.cloud.example.adapter.CustomExpandableListAdapter;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.Tasks;
import com.thecode.aestheticdialogs.AestheticDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowListActivity extends AppCompatActivity {

    AGCStorageManagement storageManagement;
    HashMap<String, List<String>> expandableStorageList = new HashMap<String, List<String>>();
    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    TextView txtTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_list);
        txtTitle = findViewById(R.id.txtHeader);
        txtTitle.setText("STORAGE LIST");
        AGConnectInstance.initialize(this);
        storageManagement = AGCStorageManagement.getInstance();
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        Intent intent = getIntent();
        expandableStorageList = new HashMap<String, List<String>>();
        expandableStorageList = (HashMap<String, List<String>>) intent.getSerializableExtra("map");
        if(expandableStorageList.size() > 0) {
            showTheList();
        }else{
            AestheticDialog.showEmojiDark(ShowListActivity.this,"NO LIST","Please Upload Files and then come back here to see..",AestheticDialog.ERROR);
        }

    }



    public void showTheList(){
        System.out.println("Size >>>" + expandableStorageList.size());
        expandableListTitle = new ArrayList<String>(expandableStorageList.keySet());
        expandableListAdapter = new CustomExpandableListAdapter(ShowListActivity.this, expandableListTitle, expandableStorageList);
        expandableListView.setAdapter(expandableListAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ShowListActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
