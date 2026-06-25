package com.gym.crm.security;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.rest.auth.RestUserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymUserDetailsService implements UserDetailsService {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public GymUserDetailsService(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadGymUserByUsername(username);
    }

    @Transactional(readOnly = true)
    public GymUserDetails loadGymUserByUsername(String username) throws UsernameNotFoundException {
        return traineeDao.findByUsername(username)
                .map(trainee -> new GymUserDetails(
                        trainee.getUsername(),
                        trainee.getPassword(),
                        trainee.isActive(),
                        RestUserRole.TRAINEE
                ))
                .or(() -> trainerDao.findByUsername(username)
                        .map(trainer -> new GymUserDetails(
                                trainer.getUsername(),
                                trainer.getPassword(),
                                trainer.isActive(),
                                RestUserRole.TRAINER
                        )))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
