package com.example.shortlink.service;

public interface LinkService {

    String addLink(String url);

    String getFullUrl(String code);
}
