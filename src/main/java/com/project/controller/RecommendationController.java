package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.project.entity.Recommendation;
import com.project.repository.RecommendationRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RecommendationController {
    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "recommendations")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private RecommendationRepository recommendationRepository;

    private String normalizeRecommendationName(String name) {
        return name == null ? "" : name.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isLegacyDefaultImage(String imageUrl) {
        if (isBlank(imageUrl)) {
            return true;
        }

        return imageUrl.contains("placeholder.com")
                || imageUrl.contains("source.unsplash")
                || imageUrl.contains("picsum.photos")
                || imageUrl.contains("photos/236329/pexels-photo-236329")
                || imageUrl.contains("photos/1689731/pexels-photo-1689731")
                || imageUrl.contains("photos/106399/pexels-photo-106399")
                || imageUrl.contains("photos/2740951/pexels-photo-2740951")
                || imageUrl.contains("photos/1282310/pexels-photo-1282310")
                || imageUrl.contains("photos/2193300/pexels-photo-2193300");
    }

    private String storeImage(MultipartFile image, String suffix) throws IOException {
        String originalName = image.getOriginalFilename() == null ? "image" : Paths.get(image.getOriginalFilename()).getFileName().toString();
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + suffix + "_" + safeName;
        Files.createDirectories(UPLOAD_DIR);

        Path target = UPLOAD_DIR.resolve(fileName).normalize();
        try (InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/recommendations/" + fileName;
    }

    private String resolveImageValue(String explicitUrl, String recommendationName, int index) {
        return isBlank(explicitUrl) ? getDefaultImageUrl(recommendationName, index) : explicitUrl;
    }

    @GetMapping("/recommendations")
    public List<Recommendation> getRecommendations() {
        List<Recommendation> recommendations = recommendationRepository.findAll();
        if (recommendations.isEmpty()) {
            Recommendation r1 = new Recommendation();
            r1.setName("Kitchen Upgrade");
            r1.setCost("₹1.8L");
            r1.setGain("₹4.5L");
            r1.setDescription("Modern cabinets + fittings + chimney and better lighting for a premium finish.");
            r1.setImageUrl(getDefaultImageUrl("Kitchen Upgrade", 1));
            r1.setImageUrl2(getDefaultImageUrl("Kitchen Upgrade", 2));
            r1.setImageUrl3(getDefaultImageUrl("Kitchen Upgrade", 3));

            Recommendation r2 = new Recommendation();
            r2.setName("Bathroom Waterproofing");
            r2.setCost("₹45K");
            r2.setGain("₹1.2L");
            r2.setDescription("Stop seepage/leaks and prevent mold for better long-term value.");
            r2.setImageUrl(getDefaultImageUrl("Bathroom Waterproofing", 1));
            r2.setImageUrl2(getDefaultImageUrl("Bathroom Waterproofing", 2));
            r2.setImageUrl3(getDefaultImageUrl("Bathroom Waterproofing", 3));

            Recommendation r3 = new Recommendation();
            r3.setName("Living Room Flooring");
            r3.setCost("₹80K");
            r3.setGain("₹2L");
            r3.setDescription("Upgrade to premium flooring (wood/vinyl) for a modern look and easy maintenance.");
            r3.setImageUrl(getDefaultImageUrl("Living Room Flooring", 1));
            r3.setImageUrl2(getDefaultImageUrl("Living Room Flooring", 2));
            r3.setImageUrl3(getDefaultImageUrl("Living Room Flooring", 3));

            Recommendation r4 = new Recommendation();
            r4.setName("Exterior Paint Refresh");
            r4.setCost("₹60K");
            r4.setGain("₹1.5L");
            r4.setDescription("Fresh paint instantly improves first impression and protects surfaces.");
            r4.setImageUrl(getDefaultImageUrl("Exterior Paint Refresh", 1));
            r4.setImageUrl2(getDefaultImageUrl("Exterior Paint Refresh", 2));
            r4.setImageUrl3(getDefaultImageUrl("Exterior Paint Refresh", 3));

            Recommendation r5 = new Recommendation();
            r5.setName("Energy Efficient Lighting");
            r5.setCost("₹18K");
            r5.setGain("₹55K");
            r5.setDescription("LED + better placement improves brightness and reduces electricity bills.");
            r5.setImageUrl(getDefaultImageUrl("Energy Efficient Lighting", 1));
            r5.setImageUrl2(getDefaultImageUrl("Energy Efficient Lighting", 2));
            r5.setImageUrl3(getDefaultImageUrl("Energy Efficient Lighting", 3));

            Recommendation r6 = new Recommendation();
            r6.setName("Balcony Makeover");
            r6.setCost("₹25K");
            r6.setGain("₹90K");
            r6.setDescription("Simple seating + planters + flooring for a usable, attractive balcony.");
            r6.setImageUrl(getDefaultImageUrl("Balcony Makeover", 1));
            r6.setImageUrl2(getDefaultImageUrl("Balcony Makeover", 2));
            r6.setImageUrl3(getDefaultImageUrl("Balcony Makeover", 3));

            recommendationRepository.saveAll(Arrays.asList(r1, r2, r3, r4, r5, r6));
            return recommendationRepository.findAll();
        }
        boolean changed = false;
        for (Recommendation r : recommendations) {
            if (isLegacyDefaultImage(r.getImageUrl())) {
                r.setImageUrl(getDefaultImageUrl(r.getName(), 1));
                changed = true;
            }
            if (isLegacyDefaultImage(r.getImageUrl2())) {
                r.setImageUrl2(getDefaultImageUrl(r.getName(), 2));
                changed = true;
            }
            if (isLegacyDefaultImage(r.getImageUrl3())) {
                r.setImageUrl3(getDefaultImageUrl(r.getName(), 3));
                changed = true;
            }
        }
        if (changed) {
            recommendationRepository.saveAll(recommendations);
        }
        return recommendations;
    }

    private String getDefaultImageUrl(String name, int index) {
        String normalizedName = normalizeRecommendationName(name);

        if (normalizedName.contains("kitchen")) {
            if (index == 1) return "https://images.pexels.com/photos/1080721/pexels-photo-1080721.jpeg?auto=compress&cs=tinysrgb&w=1200";
            if (index == 2) return "https://images.pexels.com/photos/2062431/pexels-photo-2062431.jpeg?auto=compress&cs=tinysrgb&w=1200";
            return "https://images.pexels.com/photos/5824519/pexels-photo-5824519.jpeg?auto=compress&cs=tinysrgb&w=1200";
        }
        if (normalizedName.contains("bathroom")) {
            if (index == 1) return "https://images.pexels.com/photos/6585761/pexels-photo-6585761.jpeg?auto=compress&cs=tinysrgb&w=1200";
            if (index == 2) return "https://images.pexels.com/photos/5998138/pexels-photo-5998138.jpeg?auto=compress&cs=tinysrgb&w=1200";
            return "https://images.pexels.com/photos/6444256/pexels-photo-6444256.jpeg?auto=compress&cs=tinysrgb&w=1200";
        }
        if (normalizedName.contains("living") || normalizedName.contains("floor")) {
            if (index == 1) return "https://images.pexels.com/photos/6489127/pexels-photo-6489127.jpeg?auto=compress&cs=tinysrgb&w=1200";
            if (index == 2) return "https://images.pexels.com/photos/6957083/pexels-photo-6957083.jpeg?auto=compress&cs=tinysrgb&w=1200";
            return "https://images.pexels.com/photos/7534226/pexels-photo-7534226.jpeg?auto=compress&cs=tinysrgb&w=1200";
        }
        if (normalizedName.contains("exterior") || normalizedName.contains("paint")) {
            if (index == 1) return "https://images.pexels.com/photos/731082/pexels-photo-731082.jpeg?auto=compress&cs=tinysrgb&w=1200";
            if (index == 2) return "https://images.pexels.com/photos/280229/pexels-photo-280229.jpeg?auto=compress&cs=tinysrgb&w=1200";
            return "https://images.pexels.com/photos/1396122/pexels-photo-1396122.jpeg?auto=compress&cs=tinysrgb&w=1200";
        }
        if (normalizedName.contains("lighting") || normalizedName.contains("light")) {
            if (index == 1) return "https://images.pexels.com/photos/1112598/pexels-photo-1112598.jpeg?auto=compress&cs=tinysrgb&w=1200";
            if (index == 2) return "https://images.pexels.com/photos/112811/pexels-photo-112811.jpeg?auto=compress&cs=tinysrgb&w=1200";
            return "https://images.pexels.com/photos/6585607/pexels-photo-6585607.jpeg?auto=compress&cs=tinysrgb&w=1200";
        }
        if (normalizedName.contains("balcony")) {
            if (index == 1) return "https://images.pexels.com/photos/1248583/pexels-photo-1248583.jpeg?auto=compress&cs=tinysrgb&w=1200";
            if (index == 2) return "https://images.pexels.com/photos/6489117/pexels-photo-6489117.jpeg?auto=compress&cs=tinysrgb&w=1200";
            return "https://images.pexels.com/photos/6758774/pexels-photo-6758774.jpeg?auto=compress&cs=tinysrgb&w=1200";
        }

        if (index == 1) return "https://images.pexels.com/photos/1643383/pexels-photo-1643383.jpeg?auto=compress&cs=tinysrgb&w=1200";
        if (index == 2) return "https://images.pexels.com/photos/1571460/pexels-photo-1571460.jpeg?auto=compress&cs=tinysrgb&w=1200";
        return "https://images.pexels.com/photos/276724/pexels-photo-276724.jpeg?auto=compress&cs=tinysrgb&w=1200";
    }

    @PostMapping("/admin/recommendations")
    public String addRecommendation(
            @RequestParam("name") String name,
            @RequestParam("cost") String cost,
            @RequestParam("gain") String gain,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "imageUrl2", required = false) String imageUrl2,
            @RequestParam(value = "imageUrl3", required = false) String imageUrl3) {

        Recommendation rec = new Recommendation();
        rec.setName(name);
        rec.setCost(cost);
        rec.setGain(gain);
        rec.setDescription(description);

        try {
            // Handle Image 1
            if (image != null && !image.isEmpty()) {
                rec.setImageUrl(storeImage(image, ""));
            } else {
                rec.setImageUrl(resolveImageValue(imageUrl, name, 1));
            }
            
            // Handle Image 2
            if (image2 != null && !image2.isEmpty()) {
                rec.setImageUrl2(storeImage(image2, "_2"));
            } else {
                rec.setImageUrl2(resolveImageValue(imageUrl2, name, 2));
            }
            
            // Handle Image 3
            if (image3 != null && !image3.isEmpty()) {
                rec.setImageUrl3(storeImage(image3, "_3"));
            } else {
                rec.setImageUrl3(resolveImageValue(imageUrl3, name, 3));
            }
        } catch (IOException e) {
            return "Error uploading image";
        }

        recommendationRepository.save(rec);
        return "Recommendation added successfully";
    }

    @PutMapping("/admin/recommendations/{id}")
    public String updateRecommendation(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("cost") String cost,
            @RequestParam("gain") String gain,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "image2", required = false) MultipartFile image2,
            @RequestParam(value = "image3", required = false) MultipartFile image3,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "imageUrl2", required = false) String imageUrl2,
            @RequestParam(value = "imageUrl3", required = false) String imageUrl3) {

        Optional<Recommendation> optionalRecommendation = recommendationRepository.findById(id);
        if (optionalRecommendation.isEmpty()) {
            return "Recommendation not found";
        }

        Recommendation rec = optionalRecommendation.get();
        rec.setName(name);
        rec.setCost(cost);
        rec.setGain(gain);
        rec.setDescription(description);

        try {
            // Handle Image 1
            if (image != null && !image.isEmpty()) {
                rec.setImageUrl(storeImage(image, ""));
            } else {
                rec.setImageUrl(resolveImageValue(imageUrl, name, 1));
            }
            
            // Handle Image 2
            if (image2 != null && !image2.isEmpty()) {
                rec.setImageUrl2(storeImage(image2, "_2"));
            } else {
                rec.setImageUrl2(resolveImageValue(imageUrl2, name, 2));
            }
            
            // Handle Image 3
            if (image3 != null && !image3.isEmpty()) {
                rec.setImageUrl3(storeImage(image3, "_3"));
            } else {
                rec.setImageUrl3(resolveImageValue(imageUrl3, name, 3));
            }
        } catch (IOException e) {
            return "Error uploading image";
        }

        recommendationRepository.save(rec);
        return "Recommendation updated successfully";
    }

    @DeleteMapping("/admin/recommendations/{id}")
    public String deleteRecommendation(@PathVariable Long id) {
        if (!recommendationRepository.existsById(id)) {
            return "Recommendation not found";
        }
        recommendationRepository.deleteById(id);
        return "Recommendation deleted successfully";
    }
}
