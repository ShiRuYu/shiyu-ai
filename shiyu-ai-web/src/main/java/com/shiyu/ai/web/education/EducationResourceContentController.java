package com.shiyu.ai.web.education;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.storage.FileStorageManager;
import com.shiyu.ai.common.storage.StorageObject;
import com.shiyu.ai.common.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Serves bundled education resources through the tenant-scoped Storage API. */
@RestController
@RequestMapping("/education-resources")
@RequiredArgsConstructor
@SaCheckPermission("edu:resource:list")
public class EducationResourceContentController {

    private final FileStorageManager storageManager;

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<InputStreamResource> open(@PathVariable String fileName) throws IOException {
        if (fileName.isBlank() || fileName.contains("..") || fileName.contains("/")
                || fileName.contains("\\")) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        Long tenantId = LoginContextHolder.getCurrentTenantId();
        if (tenantId == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        String namespace = "tenant/" + tenantId + "/education-resources";
        StoredFile file = storageManager.list(namespace).stream()
                .filter(candidate -> fileName.equals(candidate.name()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        StorageObject object = storageManager.open(file.key());
        MediaType contentType = mediaType(object.contentType());
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(object.name(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(object.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(object.inputStream()));
    }

    private MediaType mediaType(String contentType) {
        try {
            return MediaType.parseMediaType(contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType);
        } catch (IllegalArgumentException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
