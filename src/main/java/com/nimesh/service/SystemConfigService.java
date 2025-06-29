package com.nimesh.service;

import org.springframework.stereotype.Service;
import java.util.prefs.Preferences;

@Service
public class SystemConfigService {
    
    private static final String PRICING_STRATEGY_KEY = "pricingStrategy";
    private final Preferences prefs = Preferences.userNodeForPackage(SystemConfigService.class);
    
    // Pricing strategy options
    public static final String FIFO_STRATEGY = "FIFO";
    public static final String LIFO_STRATEGY = "LIFO";
    public static final String AVERAGE_STRATEGY = "AVERAGE";
    
    public SystemConfigService() {
        // Load pricing strategy from preferences, default to FIFO
        if (prefs.get(PRICING_STRATEGY_KEY, null) == null) {
            prefs.put(PRICING_STRATEGY_KEY, FIFO_STRATEGY);
        }
    }
    
    public String getPricingStrategy() {
        return prefs.get(PRICING_STRATEGY_KEY, FIFO_STRATEGY);
    }
    
    public void setPricingStrategy(String pricingStrategy) {
        if (pricingStrategy == null || pricingStrategy.isEmpty()) {
            pricingStrategy = FIFO_STRATEGY;
        }
        
        if (!pricingStrategy.equals(FIFO_STRATEGY) && 
            !pricingStrategy.equals(LIFO_STRATEGY) && 
            !pricingStrategy.equals(AVERAGE_STRATEGY)) {
            pricingStrategy = FIFO_STRATEGY;
        }
        
        prefs.put(PRICING_STRATEGY_KEY, pricingStrategy);
    }
}