package com.hmdp.controller;

import com.hmdp.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${cityhub.upload-dir}")
    private String uploadDir;

    @PostMapping("/blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) throws IOException {
        String original = image.getOriginalFilename();
        String suffix = original == null || !original.contains(".") ? "" :
                original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!suffix.matches("jpg|jpeg|png|gif|webp")) {
            return Result.fail("仅支持 jpg/jpeg/png/gif/webp 图片");
        }
        String name = UUID.randomUUID().toString();
        String relative = "blogs/" + (name.hashCode() & 15) + "/" + ((name.hashCode() >> 4) & 15)
                + "/" + name + "." + suffix;
        Path file = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relative);
        Files.createDirectories(file.getParent());
        image.transferTo(file.toFile());
        return Result.ok("/" + relative);
    }

    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) throws IOException {
        String relative = filename.startsWith("/imgs/") ? filename.substring(6) : filename;
        if (relative.startsWith("/")) relative = relative.substring(1);
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path file = root.resolve(relative).normalize();
        if (!file.startsWith(root.resolve("blogs")) || Files.isDirectory(file)) {
            return Result.fail("错误的文件名称");
        }
        Files.deleteIfExists(file);
        return Result.ok();
    }
}
