package com.javalearn.keypolling.repository;

import com.javalearn.keypolling.model.KeyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface KeyInfoRepository extends JpaRepository<KeyInfo, Long> {
}
