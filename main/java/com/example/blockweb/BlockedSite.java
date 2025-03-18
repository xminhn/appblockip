package com.example.blockweb;

public class BlockedSite {
    private int a_id;          // "a" để lên đầu
    private String b_url;      // "b" tiếp theo
    private String c_mac;      // "c"
    private String d_startDay; // "d"
    private String e_startTime;// "e"
    private String f_endDay;   // "f"
    private String g_endTime;  // "g"

    public BlockedSite(int id, String url, String mac, String startDay, String startTime, String endDay, String endTime) {
        this.a_id = id;
        this.b_url = url;
        this.c_mac = mac;
        this.d_startDay = startDay;
        this.e_startTime = startTime;
        this.f_endDay = endDay;
        this.g_endTime = endTime;
    }

    // Cập nhật getters với tên mới
    public int getId() { return a_id; }
    public String getUrl() { return b_url; }
    public String getMac() { return c_mac; }
    public String getStartDay() { return d_startDay; }
    public String getStartTime() { return e_startTime; }
    public String getEndDay() { return f_endDay; }
    public String getEndTime() { return g_endTime; }
}