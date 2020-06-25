package com.huawei.cloud.example.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.DownloadTask;
import com.huawei.agconnect.cloud.storage.core.FileMetadata;
import com.huawei.agconnect.cloud.storage.core.ListResult;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.cloud.example.R;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hmf.tasks.Tasks;
import com.thecode.aestheticdialogs.AestheticDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Name of the project HMSCloudStorageAndHostingExample.
 * Created by Sanghati Mukherjee.
 * Huawei Technologies Co., Ltd.
 * sanghati.mukherjee@huawei.com
 */
public class CustomExpandableListAdapter extends BaseExpandableListAdapter {


    AGCStorageManagement storageManagement;
    String size = "";
    String contentType = "";
    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;

    public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                       HashMap<String, List<String>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        AGConnectInstance.initialize(context);
        storageManagement = AGCStorageManagement.getInstance();
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        ImageView imgDownload = convertView.findViewById(R.id.imgDownload);
        ImageView imgDelete = convertView.findViewById(R.id.imgDelete);
        ImageView imgInfo = convertView.findViewById(R.id.imgInfo);

        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteFile((String) getGroup(listPosition), expandedListText);
                List<String> deleteValue = new ArrayList<>();
                if (expandableListDetail.containsKey((String) getGroup(listPosition))) {
                    deleteValue = expandableListDetail.get((String) getGroup(listPosition));
                }
                for (int i = 0; i < deleteValue.size(); i++) {
                    if (deleteValue.get(i).equalsIgnoreCase(expandedListText)) {
                        deleteValue.remove(i);
                    }
                }
                expandableListDetail.put((String) getGroup(listPosition), deleteValue);
                notifyDataSetChanged();
            }
        });
        imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadFile((String) getGroup(listPosition), expandedListText);
            }
        });

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fileInfo((String) getGroup(listPosition), expandedListText);
            }
        });

        TextView expandedListTextView = convertView
                .findViewById(R.id.expandedListItem);
        System.out.println("FILES VALUE >>> " + expandedListText);
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle.replace("/", ""));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    // Delete the file from cloud storage ...
    private void deleteFile(String group, String expandedListText) {
        if (storageManagement == null) {
            storageManagement = AGCStorageManagement.getInstance();
        }
        AGConnectUser currentUser = AGConnectAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.i("AGC", "deleteFile...DisplayName:" + currentUser.getDisplayName() + " ,userId=" + currentUser.getUid());
        } else {
            Log.e("AGC", "deleteFile...User Null");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                StorageReference storageReference = storageManagement.getStorageReference(group.trim() + expandedListText);
                Task<Void> deleteTask = storageReference.delete();
                deleteTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(Task<Void> task) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                AestheticDialog.showToasterDark((Activity) context, "Successfully Deleted", expandedListText + " File Successfully Deleted...", AestheticDialog.SUCCESS);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                AestheticDialog.showToasterDark((Activity) context, "Delete Failed", expandedListText + " File Successfully Not Deleted...", AestheticDialog.ERROR);
                            }
                        });
                    }
                });

            }
        }).start();
    }

    // Download the file from cloud storage ...
    private void downloadFile(String group, String expandedListText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StorageReference reference = storageManagement.getStorageReference(group + expandedListText);
                String path = getAGCSdkDirPath();
                // Toast.makeText(MainActivity.this,path,Toast.LENGTH_LONG).show();
                String sufix = "";
                if (expandedListText.toLowerCase().endsWith(".docx")) {
                    sufix = ".docx";
                } else if (expandedListText.toLowerCase().endsWith(".doc")) {
                    sufix = ".docx";
                } else if (expandedListText.toLowerCase().endsWith("jpg")) {
                    sufix = ".jpg";
                } else if (expandedListText.toLowerCase().endsWith(".png")) {
                    sufix = ".png";
                } else if (expandedListText.toLowerCase().endsWith("xlsx")) {
                    sufix = ".xlsx";
                } else if (expandedListText.toLowerCase().endsWith("xls")) {
                    sufix = ".xls";
                } else if (expandedListText.toLowerCase().endsWith("mp4")) {
                    sufix = ".mp4";
                }
                File filePath = new File(path + expandedListText.replace(sufix, "").trim() + SystemClock.currentThreadTimeMillis() + sufix);

                DownloadTask task = reference.getFile(filePath);
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        AestheticDialog.showToasterDark((Activity) context, "Download Failed", "Download Failed", AestheticDialog.ERROR);
                    }
                }).addOnSuccessListener(new OnSuccessListener<DownloadTask.DownloadResult>() {
                    @Override
                    public void onSuccess(DownloadTask.DownloadResult downloadResult) {
                        AestheticDialog.showToasterDark((Activity) context, "Downloaded Successfully", expandedListText + " File Downloaded Successfully...", AestheticDialog.SUCCESS);
                    }
                });
            }
        }).start();
    }

    // Get File Metadata or details ...
    private void fileInfo(String group, String expandedListText) {
        if (storageManagement == null) {
            storageManagement = AGCStorageManagement.getInstance();
        }
        AGConnectUser currentUser = AGConnectAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.i("AGC", "deleteFile...DisplayName:" + currentUser.getDisplayName() + " ,userId=" + currentUser.getUid());
        } else {
            Log.e("AGC", "deleteFile...User Null");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StorageReference storageReference = storageManagement.getStorageReference(group.trim() + expandedListText);
                    Task<FileMetadata> metaTask = storageReference.getFileMetadata();
                    FileMetadata metadata = null;
                    metadata = Tasks.await(metaTask);
                    size = String.valueOf(metadata.getSize());
                    contentType = metadata.getContentType();
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            AestheticDialog.showToasterDark((Activity) context, expandedListText + " Details", "Size: " + size + " \n" + "Content Type: " + contentType, AestheticDialog.INFO);
                        }
                    });
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected String getAGCSdkDirPath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CloudStorageExample/";
        Log.i("AGC", "path=" + path);
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return path;
    }

}
