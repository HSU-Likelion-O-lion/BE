package com.likelion.olion.domain.community.service;

public interface CommunityShareImageStorage {
    String store(Long shareId, byte[] pngBytes);
}
