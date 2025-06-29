package com.nimesh.service;

import com.nimesh.model.Unit;
import com.nimesh.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UnitService {
    
    @Autowired
    private UnitRepository unitRepository;
    
    @PostConstruct
    public void init() {
        // Create default units if they don't exist
        if (unitRepository.count() == 0) {
            createDefaultUnits();
        }
    }
    
    private void createDefaultUnits() {
        // Add common units
        Unit kilograms = new Unit("Kilogram", BigDecimal.ONE);
        Unit grams = new Unit("Gram", new BigDecimal("0.001"));
        Unit liters = new Unit("Liter", BigDecimal.ONE);
        Unit milliliters = new Unit("Milliliter", new BigDecimal("0.001"));
        Unit pieces = new Unit("Piece", BigDecimal.ONE);
        Unit dozen = new Unit("Dozen", new BigDecimal("12"));
        Unit packet = new Unit("Packet", BigDecimal.ONE);
        Unit box = new Unit("Box", BigDecimal.ONE);
        
        unitRepository.save(kilograms);
        unitRepository.save(grams);
        unitRepository.save(liters);
        unitRepository.save(milliliters);
        unitRepository.save(pieces);
        unitRepository.save(dozen);
        unitRepository.save(packet);
        unitRepository.save(box);
    }
    
    public List<Unit> getAllUnits() {
        return unitRepository.findAll();
    }
    
    public Optional<Unit> getUnitById(Long id) {
        return unitRepository.findById(id);
    }
    
    public Unit getUnitByName(String name) {
        return unitRepository.findByName(name);
    }
    
    public Unit saveUnit(Unit unit) {
        return unitRepository.save(unit);
    }
    
    public boolean deleteUnit(Long id) {
        if (unitRepository.existsById(id)) {
            unitRepository.deleteById(id);
            return true;
        }
        return false;
    }
}