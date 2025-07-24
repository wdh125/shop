package com.coffeeshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.coffeeshop.entity.TableEntity;

public interface TableRepository extends JpaRepository<TableEntity, Integer> {
}