package org.example.dreamzshop.service;

import org.example.dreamzshop.entity.Order;

public interface InvoiceService {

    byte[] generateInvoice(Order order);
}