package com.huawei.cloud.example.activity;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectAuthCredential;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.auth.HwIdAuthProvider;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.cloud.storage.core.*;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.agconnect.cloud.storage.core.UploadTask;
import com.huawei.cloud.example.util.Constant;
import com.huawei.cloud.example.R;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.Tasks;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.squareup.picasso.Picasso;
import com.thecode.aestheticdialogs.AestheticDialog;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PATH = 1001;
    Button btnFileBrowse, btnLogin, btnshowList;
    String curFileName;
    String curFilePath;
    TextView txtTitle,txtName;
    ImageView imgAvatar;
    AGCStorageManagement storageManagement;
    HuaweiIdAuthParams mHuaweiIdAuthParams, authParams;
    HuaweiIdAuthService service;
    ProgressDialog progressDialog;
    HashMap<String, List<String>> expandableStorageList = new HashMap<String, List<String>>();
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    AGConnectUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        AGConnectInstance.initialize(this);
        btnFileBrowse = findViewById(R.id.btnFileBrowse);
        btnshowList = findViewById(R.id.btnShowList);
        btnLogin = findViewById(R.id.btnLogin);
        txtTitle = findViewById(R.id.txtHeader);
        txtName = findViewById(R.id.txtName);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtTitle.setText("HUAWEI CLOUD STORAGE");
        ActivityCompat.requestPermissions(this, permissions, 1);
        storageManagement = AGCStorageManagement.getInstance();
        displayAvatar("");
        currentUser = AGConnectAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            displayInfo(null,currentUser);
        }

    }

    public void huaweiLogin(View view) {
        idTokenSignIn();
    }

    public void browseFileUpload(View view) {
        if(currentUser != null) {
            Intent intent1 = new Intent(MainActivity.this, FileChooserActivity.class);
            startActivityForResult(intent1, REQUEST_PATH);
        }else{
            AestheticDialog.showToaster(this,"You must login to use browse and upload the file...","",AestheticDialog.INFO);
        }
    }

    public void showTheList(View view) {
        if(currentUser != null){
            getFileList();
        }else{
            AestheticDialog.showToaster(this,"You must login to see the list...","",AestheticDialog.INFO);
        }

    }

    private void idTokenSignIn() {
        mHuaweiIdAuthParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken()
                .setAccessToken()
                .setProfile()
                .createParams();
        service = HuaweiIdAuthManager.getService(MainActivity.this, mHuaweiIdAuthParams);
        startActivityForResult(service.getSignInIntent(), Constant.REQUEST_SIGN_IN_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                progressDialog = ProgressDialog.show(MainActivity.this, "", "Uploading...");
                curFileName = data.getStringExtra("GetFileName");
                curFilePath = data.getStringExtra("GetPath");
              //  Toast.makeText(MainActivity.this, curFilePath + "/" + curFileName, Toast.LENGTH_LONG).show();
                uploadFile();
            }
        } else if (requestCode == Constant.REQUEST_SIGN_IN_LOGIN) {
            Task<AuthHuaweiId> authHuaweiIdTask;
            AuthHuaweiId huaweiAccount;
            authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {

                huaweiAccount = authHuaweiIdTask.getResult();
                displayInfo(huaweiAccount,null);
                Log.e("AGC", "HMS signIn Success");
                Log.e("AGC", "accessToken:" + huaweiAccount.getAccessToken());
                AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken());
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        printUserInfo();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e("AGC", "AGC Auth Error: " + e.getMessage());
                    }
                });

            } else {
                Toast.makeText(MainActivity.this, getString(R.string.sign_in_failed) + ((ApiException) authHuaweiIdTask.getException()).getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void printUserInfo() {
         currentUser = AGConnectAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("AGC", "User Null");
        } else {
            AestheticDialog.showEmotion(this,"Success","You have successfully SignIn...", AestheticDialog.SUCCESS);
            Log.i("AGC", "Anonymous:" + currentUser.isAnonymous());
            Log.i("AGC", "Uid:" + currentUser.getUid());
            Log.i("AGC", "DisplayName:" + currentUser.getDisplayName());
            Log.i("AGC", "PhotoUrl:" + currentUser.getPhotoUrl());
            Log.i("AGC", "Email:" + currentUser.getEmail());
            Log.i("AGC", "ProviderId:" + currentUser.getProviderId());
            Log.i("AGC", "ProviderMap:" + new JSONArray(currentUser.getProviderInfo()).toString());
        }
    }

    // Upload the selected file to cloud storage ...
    private void uploadFile() {
        String files = "";
        if (curFileName.toLowerCase().endsWith(".docx") || curFileName.toLowerCase().endsWith(".doc") ) {
            files = "DOCS/";
        } else if (curFileName.toLowerCase().endsWith("jpg") || curFileName.toLowerCase().endsWith(".png")) {
            files = "IMAGES/";
        } else if (curFileName.toLowerCase().endsWith("xlsx") || curFileName.toLowerCase().endsWith(".xls")) {
            files = "EXCELS/";
        }else if (curFileName.toLowerCase().endsWith("mp4")) {
            files = "VIDEOS/";
        }
        StorageReference reference = storageManagement.getStorageReference(files + curFileName);
        File filePath = new File(curFilePath + "/" + curFileName);
        if (filePath.isFile()) {
            UploadTask task = reference.putFile(filePath);
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception ex) {
                    Log.e("MainActivity", ex.getMessage());
                    progressDialog.dismiss();
                }

            }).addOnSuccessListener(new OnSuccessListener<UploadTask.UploadResult>() {
                @Override
                public void onSuccess(UploadTask.UploadResult uploadResult) {
                    Log.i("MainActivity", uploadResult.toString());
                    progressDialog.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.UploadResult>() {
                @Override
                public void onProgress(UploadTask.UploadResult uploadResult) {
                    System.out.println(String.format("progress: %f", (uploadResult.getBytesTransferred() * 1.0) / uploadResult.getTotalByteCount()));
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.UploadResult>() {
                @Override
                public void onPaused(UploadTask.UploadResult uploadResult) {

                }
            });

        }
    }

    // Get all the files from cloud storage ...
    public void getFileList() {
        progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading the list...");
        expandableStorageList.clear();
        if (storageManagement == null) {
            storageManagement = AGCStorageManagement.getInstance();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                StorageReference dirReference = storageManagement.getStorageReference();
                Task<ListResult> dirResultTask = dirReference.listAll();
                try {
                    ListResult dirResult = Tasks.await(dirResultTask);
                    List<StorageReference> dirList = dirResult.getDirList();

                    for (int i = 0; i < dirList.size(); i++) {
                        System.out.println("DIR List " + i + "：" + dirList.get(i).getPath());
                        StorageReference fileStorageRef = storageManagement.getStorageReference(dirList.get(i).getPath());
                        List<String> fileStorageList = new ArrayList<>();
                        Task<ListResult> fileResultTask = fileStorageRef.listAll();
                        ListResult fileResult = Tasks.await(fileResultTask);
                        List<StorageReference> fileList = fileResult.getFileList();
                        for (int j = 0; j < fileList.size(); j++) {
                            System.out.println("FILE List " + j + "：" + fileList.get(j).getName());
                            if (!fileList.get(j).getName().isEmpty() && fileList.get(j).getName() != null) {
                                fileStorageList.add(fileList.get(j).getName());
                            }
                        }
                        expandableStorageList.put(dirList.get(i).getPath(), fileStorageList);
                    }

                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, ShowListActivity.class);
                            intent.putExtra("map", expandableStorageList);
                            startActivity(intent);
                            finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();

                } finally {

                }
            }
        }).start();
    }

    private void displayInfo(AuthHuaweiId infoHuaweiId,AGConnectUser connectUser){
        if(connectUser!=null){
            if(connectUser.getDisplayName()!=null) {
                txtName.setText(new StringBuilder().append("NAME : ").append(connectUser.getDisplayName()).toString());
            }else{
                txtName.setText("NAME : ");
            }
            txtName.setTypeface(null, Typeface.BOLD);
           // Uri avatarUri = ;
            displayAvatar(connectUser.getPhotoUrl());
        }else{

            if(infoHuaweiId.getDisplayName()!=null) {
                txtName.setText(new StringBuilder().append("NAME : ").append(infoHuaweiId.getDisplayName()).toString());
            }else{
                txtName.setText("NAME : ");
            }
            txtName.setTypeface(null, Typeface.BOLD);
            Uri avatarUri = infoHuaweiId.getAvatarUri();
            displayAvatar(avatarUri.toString());
        }


    }
    private void displayAvatar(String avatarUri){
        if(avatarUri.isEmpty()){
            Picasso.get()
                    .load(R.drawable.avatar)
                    .into(imgAvatar);
        }else{
            Picasso.get().load(avatarUri).into(imgAvatar);
        }
    }
    private void clearInfo(){
        Picasso.get()
                .load(R.drawable.avatar)
                .into(imgAvatar);
        txtName.setText("");
    }

    public void signOut(View view){
        if(service!=null){
            Task<Void> signOutTask = service.signOut();

            signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    Toast.makeText(MainActivity.this, R.string.sign_out_completely, Toast.LENGTH_LONG).show();
                    clearInfo();
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    System.out.println("Exception " + e);
                }
            });
        }else if (currentUser != null) {
            AGConnectAuth.getInstance().signOut();
            Toast.makeText(MainActivity.this, R.string.sign_out_completely, Toast.LENGTH_LONG).show();
            clearInfo();
        }
    }

}
