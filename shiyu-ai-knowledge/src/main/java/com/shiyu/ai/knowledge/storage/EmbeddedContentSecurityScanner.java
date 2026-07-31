package com.shiyu.ai.knowledge.storage;

import com.shiyu.ai.common.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.unit.DataSize;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class EmbeddedContentSecurityScanner implements ContentSecurityScanner {

    private static final Set<String> DANGEROUS =
            Set.of("exe", "dll", "com", "bat", "cmd", "ps1", "sh", "jar", "msi", "js");
    private static final Set<String> ALLOWED =
            Set.of("pdf", "docx", "md", "markdown", "txt", "html", "htm");

    @Value("${shiyu.knowledge.upload.max-file-size:200MB}")
    private DataSize maxFileSize;

    @Override
    public void validate(String fileName, String contentType, byte[] content) {
        String extension = extension(fileName);
        if (DANGEROUS.contains(extension) || !ALLOWED.contains(extension)) {
            throw new ServiceException("不支持或存在风险的文件类型: " + extension);
        }
        if (content.length == 0 || content.length > maxFileSize.toBytes()) {
            throw new ServiceException("文件为空或超过大小限制");
        }
        if ("pdf".equals(extension) && !startsWith(content, "%PDF-".getBytes())) {
            throw new ServiceException("PDF 文件签名不正确");
        }
        if ("docx".equals(extension)) {
            validateZip(content);
        }
    }

    private void validateZip(byte[] content) {
        long expanded = 0;
        int entries = 0;
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = input.getNextEntry()) != null) {
                if (++entries > 10_000) {
                    throw new ServiceException("压缩文档包含过多条目");
                }
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    expanded += read;
                    if (expanded > Math.max(maxFileSize.toBytes(), content.length * 100L)) {
                        throw new ServiceException("压缩文档膨胀比例异常");
                    }
                }
            }
        } catch (IOException exception) {
            throw new ServiceException("压缩文档结构无效");
        }
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        if (content.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) {
            if (content[i] != signature[i]) return false;
        }
        return true;
    }

    private String extension(String name) {
        int index = name == null ? -1 : name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
