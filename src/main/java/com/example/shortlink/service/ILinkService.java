package com.example.shortlink.service;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.entity.LinkEntity;

public interface ILinkService {

    LinkEntity addLink(CreateLinkRequest request);

    String getFullUrl(String code);
}
