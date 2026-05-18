package com.example.accessingdatamysql.picture.dto;

import java.util.List;

public class DiscoveryStatsResponse {

    private List<DiscoveryCategoryStatsResponse> categories;

    public DiscoveryStatsResponse(){

    }

    public DiscoveryStatsResponse(List<DiscoveryCategoryStatsResponse> categories){
        this.categories = categories;
    }

    public List<DiscoveryCategoryStatsResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<DiscoveryCategoryStatsResponse> categories) {
        this.categories = categories;
    }
}
