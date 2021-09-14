package com.jpabook.jpashop.repository.order.query;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


// >> 23. order, orderItem 싹 다 join에서 한 방에 다 가지고 온다.
@Data
public class OrderFlatDto {

	private Long orderId;
	private String name;
	private LocalDateTime orderDate;
	private OrderStatus orderStatus;
	private Address address;
	private List<OrderItemQueryDto> orderItems;

	private String itemName;
	private int orderPrice;
	private int count;


	public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address, List<OrderItemQueryDto> orderItems, String itemName, int orderPrice, int count) {
		this.orderId = orderId;
		this.name = name;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.address = address;
		this.orderItems = orderItems;
		this.itemName = itemName;
		this.orderPrice = orderPrice;
		this.count = count;
	}
}
