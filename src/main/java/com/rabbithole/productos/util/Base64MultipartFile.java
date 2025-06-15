package com.rabbithole.productos.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;

/**
 * Implementación personalizada de MultipartFile para manejar archivos desde base64
 */
public class Base64MultipartFile implements MultipartFile {

    private final byte[] content;
    private final String name;
    private final String originalFilename;
    private final String contentType;

    public Base64MultipartFile(byte[] content, String name, String originalFilename, String contentType) {
        this.content = content;
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(dest)) {
            fileOutputStream.write(content);
        }
    }

    @Override
    public String toString() {
        return "Base64MultipartFile{" +
                "name='" + name + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentLength=" + (content != null ? content.length : 0) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Base64MultipartFile that = (Base64MultipartFile) o;
        return name.equals(that.name) &&
                originalFilename.equals(that.originalFilename) &&
                contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, originalFilename, contentType);
    }
}
