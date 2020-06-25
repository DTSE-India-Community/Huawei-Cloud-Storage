package com.huawei.cloud.example.model;

public class Item implements Comparable<Item>{
	private String name;
	private String data;
	private String date;
	private String path;
	private String image;
	
	public Item(String nameVal,String dateVal, String dt, String pathVal, String img)
	{
		name = nameVal;
		data = dateVal;
		date = dt;
		path = pathVal;
		image = img;
		
	}
	public String getName()
	{
		return name;
	}
	public String getData()
	{
		return data;
	}
	public String getDate()
	{
		return date;
	}
	public String getPath()
	{
		return path;
	}
	public String getImage() {
		return image;
	}
	
	public int compareTo(Item o) {
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase()); 
		else 
			throw new IllegalArgumentException();
	}
}
