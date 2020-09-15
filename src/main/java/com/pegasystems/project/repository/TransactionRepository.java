package com.pegasystems.project.repository;

import org.springframework.stereotype.Repository;

import com.pegasystems.project.model.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Enter description of class here.
 * 
 * @author PAULD26
 *
 */

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{

}
