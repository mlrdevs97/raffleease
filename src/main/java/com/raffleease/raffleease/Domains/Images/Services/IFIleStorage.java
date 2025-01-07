package com.raffleease.raffleease.Domains.Images.Services;

import org.springframework.web.multipart.MultipartFile;

public interface IFIleStorage {
    String save(MultipartFile file);

    byte[] load(String filePath);

    void delete(String filePath);
}
