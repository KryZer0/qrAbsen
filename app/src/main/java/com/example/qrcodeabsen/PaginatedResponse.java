package com.example.qrcodeabsen;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> data;
    private int current_page;
    private int last_page;

    public List<T> getData() { return data; }
    public int getCurrentPage() { return current_page; }
    public int getLastPage() { return last_page; }
}
