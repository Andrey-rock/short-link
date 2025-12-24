package com.example.shortlink.service.impl;

import com.example.shortlink.entity.LinkEntity;
import com.example.shortlink.repository.LinkRepository;
import com.example.shortlink.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;


    @Override
    public String addLink(String url) {
        String code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 6);
        LinkEntity linkEntity = new LinkEntity(code, url);
        linkRepository.save(linkEntity);
        return code;
    }

    @Override
    public String getFullUrl(String code) {
        LinkEntity entity = linkRepository.findByCode(code).orElseThrow(() -> new NoSuchElementException("страница не найдена"));
        return entity.getLink();
    }
}
