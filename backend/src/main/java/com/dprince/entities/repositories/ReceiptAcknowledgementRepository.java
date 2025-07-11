package com.dprince.entities.repositories;

import com.dprince.entities.ReceiptAcknowledgement;
import com.dprince.entities.enums.CommunicationWay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptAcknowledgementRepository extends JpaRepository<ReceiptAcknowledgement, Long> {
    ReceiptAcknowledgement findFirstByReceiptIdAndCommunicationWayOrderByMessageSendingTimeDesc(Long receiptId, CommunicationWay communicationWay);
}
