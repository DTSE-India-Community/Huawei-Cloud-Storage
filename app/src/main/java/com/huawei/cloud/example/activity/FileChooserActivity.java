package com.huawei.cloud.example.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

import com.huawei.cloud.example.model.Item;
import com.huawei.cloud.example.R;
import com.huawei.cloud.example.adapter.FileArrayAdapter;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooserActivity extends ListActivity {

	private File currentDir;
    private FileArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        currentDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		showFileDirectories(currentDir);
    }
    private void showFileDirectories(File f)
    {
    	File[]dirs = f.listFiles(); 
		 this.setTitle("Current Dir: "+f.getName());
		 List<Item>dir = new ArrayList<Item>();
		 List<Item>fls = new ArrayList<Item>();
		 try{
			 for(File ff: dirs)
			 { 
				Date lastModDate = new Date(ff.lastModified()); 
				DateFormat formater = DateFormat.getDateTimeInstance();
				String date_modify = formater.format(lastModDate);
				if(ff.isDirectory()){
					
					
					File[] fbuf = ff.listFiles(); 
					int buf = 0;
					if(fbuf != null){ 
						buf = fbuf.length;
					} 
					else buf = 0; 
					String num_item = String.valueOf(buf);
					if(buf == 0) num_item = num_item + " item";
					else num_item = num_item + " items";
					
					//String formated = lastModDate.toString();
					dir.add(new Item(ff.getName(),num_item,date_modify,ff.getAbsolutePath(),"directory_icon"));
				}
				else
				{
					if(ff.getName().toLowerCase().endsWith("jpg")){
						fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"jpg_icon"));
					}else if(ff.getName().toLowerCase().endsWith("png")){
						fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"png_icon"));
					}else if(ff.getName().toLowerCase().endsWith("docx")){
						fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"docx_icon"));
					}else if(ff.getName().toLowerCase().endsWith("gif")){
						fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"gif_icon"));
					}else if(ff.getName().toLowerCase().endsWith("mp4")){
						fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"mp_icon"));
					}else if(ff.getName().toLowerCase().endsWith("xls")){
						fls.add(new Item(ff.getName(),ff.length() + " Byte", date_modify, ff.getAbsolutePath(),"xls_icon"));
					}else {
						fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "file_icon"));
					}
				}
			 }
		 }catch(Exception e)
		 {    
			 
		 }
		 Collections.sort(dir);
		 Collections.sort(fls);
		 dir.addAll(fls);
		 if(!f.getName().equalsIgnoreCase("sdcard"))
			 dir.add(0,new Item("..","Parent Directory","",f.getParent(),"directory_up"));
		 adapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.file_view,dir);
		 this.setListAdapter(adapter); 
    }
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Item item = adapter.getItem(position);
		if(item.getImage().equalsIgnoreCase("directory_icon")||item.getImage().equalsIgnoreCase("directory_up")){
				currentDir = new File(item.getPath());
			showFileDirectories(currentDir);
		}
		else
		{
			onFileClick(item);
		}
	}
    private void onFileClick(Item item)
    {
    	Intent intent = new Intent();
        intent.putExtra("GetPath",currentDir.toString());
        intent.putExtra("GetFileName",item.getName());
        setResult(RESULT_OK, intent);
        finish();
    }


}
