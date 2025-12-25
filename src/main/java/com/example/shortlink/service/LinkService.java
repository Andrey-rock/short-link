package com.example.shortlink.service;

import com.example.shortlink.dto.CreateLinkRequest;

public interface LinkService {

    String addLink(CreateLinkRequest request);

    String getFullUrl(String code);
}
