package com.example.documentreader.Repository;

import com.example.documentreader.Model.StoreRequestResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface externalRefundRepository extends JpaRepository<StoreRequestResponseDTO, Integer> {

    @Query("SELECT t FROM StoreRequestResponseDTO t where t.apiconnect in :transactions")
    public List<StoreRequestResponseDTO> findAllByApiconnect(@Param("transactions") List<String> incomplete_transaction);
}
