package org.magnum.mobilecloud.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    Collection<Video> findByName(String name);
    Collection<Video> findByDurationLessThan(long duration);
}
