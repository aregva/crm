package com.gym.crm.util;

import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class UsernameGenerator {
    public String generateBase(String firstName, String lastName) {
        return firstName + "." + lastName;
    }

    public String makeUnique(String base, Predicate<String> usernameExists) {
        if (!usernameExists.test(base)) return base;
        int i = 1;
        while (usernameExists.test(base + i)) i++;
        return base + i;
    }
}
