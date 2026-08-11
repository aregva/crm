package com.gym.crm.integration.workload;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "trainer-workload-service", configuration = WorkloadFeignConfig.class)
public interface TrainerWorkloadClient {

    @PostMapping("/api/trainer-workloads")
    void submitWorkload(@RequestBody TrainerWorkloadRequest request);
}
