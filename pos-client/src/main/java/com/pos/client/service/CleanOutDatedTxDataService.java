package com.pos.client.service;

import com.pos.client.domain.entity.LocalOrderItem;
import com.pos.client.repository.LocalOrderItemRepository;
import com.pos.client.repository.LocalOrderPaymentRepository;
import com.pos.client.repository.LocalOrderRepository;
import com.pos.client.repository.OutboxEventRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CleanOutDatedTxDataService {
  private final LocalOrderItemRepository localOrderItemRepository;
  private final LocalOrderRepository localOrderRepository;
  private final LocalOrderPaymentRepository localOrderPaymentRepository;
  private final OutboxEventRepository outboxEventRepository;

  public void execute() {

  }
}
