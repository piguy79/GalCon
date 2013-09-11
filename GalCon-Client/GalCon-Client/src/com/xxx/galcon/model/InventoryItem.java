package com.xxx.galcon.model;


public class InventoryItem {
	
	public String sku;
	public double price;
	public String name;
	public int numCoins;
	
	public InventoryItem(String sku, double price, String name, int numCoins) {
		super();
		this.sku = sku;
		this.price = price;
		this.name = name;
		this.numCoins = numCoins;
	}
	

}
