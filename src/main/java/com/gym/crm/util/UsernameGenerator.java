package com.gym.crm.util;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class UsernameGenerator {
    public String generateBase(String firstName, String lastName) {
        return firstName + "." + lastName;
    }

    public String makeUnique(String base, Set<String> existing) {
        if (!existing.contains(base)) return base;
        int i = 1;
        while (existing.contains(base + i)) i++;
        return base + i;
    }
}