package com.jpabook.jpashop.repository.order.simplequery;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {

	private Long orderId;
	private String name;
	private LocalDateTime orderDate;	// 주문시간
	private OrderStatus orderStatus;
	private Address address;

	// 객체를 인자로 받는 생성자
//	public OrderSimpleQueryDto(Order order) {
//		this.orderId = order.getId();
//		this.name = order.getMember().getName();
//		this.orderDate = order.getOrderDate();
//		this.orderStatus = order.getStatus();
//		this.address = order.getDelivery().getAddress();
//	}

	// >> 10. [09] 과정에서 new를 쓰게되면 위처럼 인자로 엔티티로 받을 수 없다.
	public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
		this.orderId = orderId;
		this.name = name;
		this.orderDate = orderDate;
		this.orderStatus = orderStatus;
		this.address = address;
	}
}
