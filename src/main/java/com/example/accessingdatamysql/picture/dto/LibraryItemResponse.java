package com.example.accessingdatamysql.picture.dto;

public class LibraryItemResponse {

    private Integer id;
    private String label;
    private String category;
    private String imageUrl;
    private String discoveredAt;

    public LibraryItemResponse(){

    }

    public LibraryItemResponse(Integer id, String label, String category, String imageUrl, String discoveredAt) {
        this.id = id;
        this.label = label;
        this.category = category;
        this.imageUrl = imageUrl;
        this.discoveredAt = discoveredAt;
    }

    public Integer getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDiscoveredAt() {
        return discoveredAt;
    }
}
