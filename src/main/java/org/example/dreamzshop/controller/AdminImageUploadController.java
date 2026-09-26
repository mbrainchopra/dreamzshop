package org.example.dreamzshop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Controller
public class AdminImageUploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    @Value("${app.upload.dir:uploads/images}")
    private String uploadDirectory;

    @GetMapping("/admin/image-upload")
    public String imageUploadPage(Model model, HttpServletRequest request) {
        addExistingImages(model, request);
        return "admin/image-upload";
    }

    @PostMapping("/admin/image-upload")
    public String uploadImage(
            @RequestParam("image") MultipartFile image,
            Model model,
            HttpServletRequest request) {

        if (image == null || image.isEmpty()) {
            model.addAttribute("error", "Please select an image to upload.");
            addExistingImages(model, request);
            return "admin/image-upload";
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            model.addAttribute("error", "Only JPG, PNG, WEBP, GIF and SVG images are allowed.");
            addExistingImages(model, request);
            return "admin/image-upload";
        }

        String originalName = StringUtils.cleanPath(
                image.getOriginalFilename() == null ? "image" : image.getOriginalFilename()
        );
        String extension = StringUtils.getFilenameExtension(originalName);

        if (extension == null || extension.isBlank()) {
            extension = extensionFor(contentType);
        }

        String filename = UUID.randomUUID() + "." + extension.toLowerCase();
        Path uploadPath = Path.of(uploadDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);

            Path destination = uploadPath.resolve(filename).normalize();

            // Extra protection against path traversal.
            if (!destination.getParent().equals(uploadPath)) {
                model.addAttribute("error", "Invalid image filename.");
                addExistingImages(model, request);
                return "admin/image-upload";
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            // This is a local application path, not an external internet image URL.
            String imagePath = "/uploads/" + filename;
            String browserUrl = request.getScheme() + "://"
                    + request.getServerName()
                    + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                    ? "" : ":" + request.getServerPort())
                    + imagePath;

            model.addAttribute("uploaded", true);
            model.addAttribute("imagePath", imagePath);
            model.addAttribute("browserUrl", browserUrl);
            model.addAttribute("originalName", originalName);
            model.addAttribute("fileSize", formatSize(image.getSize()));

        } catch (IOException exception) {
            model.addAttribute("error", "Unable to save the image. Please try again.");
        }

        addExistingImages(model, request);
        return "admin/image-upload";
    }

    
    private void addExistingImages(Model model, HttpServletRequest request) {
        Path uploadPath = Path.of(uploadDirectory).toAbsolutePath().normalize();
        List<ImageItem> images = new ArrayList<>();

        try {
            if (Files.exists(uploadPath) && Files.isDirectory(uploadPath)) {
                try (var stream = Files.list(uploadPath)) {
                    stream.filter(Files::isRegularFile)
                            .filter(path -> isAllowedExtension(path.getFileName().toString()))
                            .sorted(Comparator.comparing(
                                    path -> {
                                        try {
                                            return Files.getLastModifiedTime(path).toMillis();
                                        } catch (IOException e) {
                                            return 0L;
                                        }
                                    },
                                    Comparator.reverseOrder()))
                            .forEach(path -> {
                                String filename = path.getFileName().toString();
                                String imagePath = "/uploads/" + filename;
                                String browserUrl = request.getScheme() + "://"
                                        + request.getServerName()
                                        + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                                        ? "" : ":" + request.getServerPort())
                                        + imagePath;
                                long size = 0L;
                                try {
                                    size = Files.size(path);
                                } catch (IOException ignored) {
                                }
                                images.add(new ImageItem(filename, imagePath, browserUrl, formatSize(size)));
                            });
                }
            }
        } catch (IOException ignored) {
            // Keep the upload page usable even if the upload directory cannot be listed.
        }

        model.addAttribute("existingImages", images);
    }

    private boolean isAllowedExtension(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        return extension != null && Set.of("jpg", "jpeg", "png", "webp", "gif", "svg")
                .contains(extension.toLowerCase());
    }

    public record ImageItem(
            String filename,
            String imagePath,
            String browserUrl,
            String fileSize
    ) {}

    private String extensionFor(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "image/svg+xml" -> "svg";
            default -> "img";
        };
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
