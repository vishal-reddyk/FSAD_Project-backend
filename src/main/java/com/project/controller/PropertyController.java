package com.project.controller;

import com.project.entity.Property;
import com.project.repository.PropertyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/property")
@CrossOrigin(origins = "http://localhost:3000")
public class PropertyController {

    @Autowired
    private PropertyRepository propertyRepository;

    // ✅ SAVE PROPERTY
    @PostMapping
    public String saveProperty(@RequestBody Property property) {

        // 🔥 DEBUG PRINT (VERY IMPORTANT)
        System.out.println("DATA RECEIVED:");
        System.out.println(property.getCity());
        System.out.println(property.getArea());
        System.out.println(property.getAddress());
        System.out.println(property.getPincode());

        propertyRepository.save(property);

        return "Property Saved Successfully";
    }

    // ✅ GET ALL PROPERTIES
    @GetMapping
    public List<Property> getAll() {
        return propertyRepository.findAll();
    }

    // ✅ UPDATE PROPERTY
    @PutMapping("/{id}")
    public String updateProperty(@PathVariable Long id, @RequestBody Property updated) {
        Optional<Property> existingOpt = propertyRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return "Property not found";
        }

        Property existing = existingOpt.get();
        existing.setCity(updated.getCity());
        existing.setArea(updated.getArea());
        existing.setAddress(updated.getAddress());
        existing.setPincode(updated.getPincode());
        propertyRepository.save(existing);
        return "Property Updated Successfully";
    }

    // ✅ DELETE PROPERTY
    @DeleteMapping("/{id}")
    public String deleteProperty(@PathVariable Long id) {
        if (!propertyRepository.existsById(id)) {
            return "Property not found";
        }
        propertyRepository.deleteById(id);
        return "Property Deleted Successfully";
    }
}