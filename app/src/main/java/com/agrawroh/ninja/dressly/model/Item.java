package com.agrawroh.ninja.dressly.model;

import java.util.List;

/**
 * @author agrawroh
 * @version v1.0
 */
public class Item {
    private long itemId;
    private String itemName;
    private String itemDescription;
    private String itemDetails;
    private List<String> itemImageURI;
    private String itemPrice;
    private String itemRating;
    private String itemLocation;
    private String itemSize;

    public Item() {
        /* Do Nothing */
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(String itemDetails) {
        this.itemDetails = itemDetails;
    }

    public List<String> getItemImageURI() {
        return itemImageURI;
    }

    public void setItemImageURI(List<String> itemImageURI) {
        this.itemImageURI = itemImageURI;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemRating() {
        return itemRating;
    }

    public void setItemRating(String itemRating) {
        this.itemRating = itemRating;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }
}
