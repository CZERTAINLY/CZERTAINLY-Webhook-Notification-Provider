package com.czertainly.np.webhook.dao.repository;

import com.czertainly.np.webhook.dao.entity.NotificationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationInstanceRepository extends JpaRepository<NotificationInstance, Long> {

    Optional<NotificationInstance> findByName(String name);

    Optional<NotificationInstance> findByUuid(UUID uuid);

}
