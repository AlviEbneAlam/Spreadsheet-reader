package com.example.documentreader.Repository;

import com.example.documentreader.Model.External_Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface externalRefundRepository extends JpaRepository<External_Refund, Integer> {

    @Query("SELECT t FROM External_Refund t where t.apiconnect in :transactions")
    public List<External_Refund> findAllByApiconnect(@Param("transactions") List<String> incomplete_transaction);
}
