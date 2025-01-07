package com.raffleease.raffleease.Exceptions.CustomExceptions;

import lombok.Getter;

@Getter
public class FileStorageException extends RuntimeException {
    public FileStorageException(String msg) { super(msg); }
}
