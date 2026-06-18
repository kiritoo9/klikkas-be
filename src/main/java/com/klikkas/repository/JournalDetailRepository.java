package com.klikkas.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klikkas.entity.JournalDetail;

public interface JournalDetailRepository extends
        JpaRepository<JournalDetail, UUID> {

}
