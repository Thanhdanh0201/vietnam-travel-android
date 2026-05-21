package com.example.travel_backend.config;

/**
 * Tọa độ trùng carousel app — warmup cache khi backend khởi động.
 */
public final class WeatherFeaturedLocations {

    private WeatherFeaturedLocations() {
    }

    public record Entry(String key, String displayName, String subtitle, double lat, double lng) {
    }

    public static final Entry[] ALL = {
            new Entry("hanoi", "Hà Nội", "Thủ đô", 21.0285, 105.8542),
            new Entry("hcmc", "Hồ Chí Minh", "TP.HCM", 10.7769, 106.7009),
            new Entry("mui_ne", "Mũi Né", "Bình Thuận", 10.9574, 108.2987),
            new Entry("da_lat", "Đà Lạt", "Lâm Đồng", 11.9404, 108.4583),
            new Entry("da_nang", "Đà Nẵng", "Miền Trung", 16.0544, 108.2022),
    };
}
