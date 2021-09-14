package com.jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// >> 19. simplequery처럼 Entity를 가져오는 것이 아닌 특정 화면에 대한 DTO를 직접 가져오는 repository
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

	private final EntityManager em;

	public List<OrderQueryDto> findOrderQueryDtos() {

		// >> 19-1. 주문 현황을 가져오지만 orderItem은 리스트라 아직 가져와서 채우지 않음
		List<OrderQueryDto> result = findOrders();

		// >> 19-2. 주문 현황 하나씩 처리
		//  1) 한 개의 주문 ID에 있는 orderItems를 가져옴
		//  2) setOrderItems 를 채움
		result.forEach(o -> {
			List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
			o.setOrderItems(orderItems);
		});
		return result;
	}

	// >> 21. 쿼리가 딱 2개만 나감 ( order 가져올 때 ) ( orderItem, Item 가져올 때 )
	public List<OrderQueryDto> findAllByDto_optimization() {
		// 쿼리 1
		List<OrderQueryDto> result = findOrders();

		List<Long> orderIds = result.stream()
			.map(OrderQueryDto::getOrderId)
			.collect(Collectors.toList());

		// 쿼리 2
		//	in 절을 이용하여 orderItem들을 한 방 쿼리로 가져옴
		Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

		result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

		return result;

	}

	private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
		List<OrderItemQueryDto> orderItems = em.createQuery(
				"select new com.jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
					" from OrderItem oi" +
					" join oi.item i" +
					" where oi.order.id in :orderIds", OrderItemQueryDto.class)
			.setParameter("orderIds", orderIds)
			.getResultList();

		// orderId를 기준으로 orderItemQueryDto를 Map으로 변경 가능
		// ( 키, 값 ) 쌍으로 변경
		Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
			.collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
		return orderItemMap;
	}


	private List<OrderItemQueryDto> findOrderItems(Long orderId) {
		return em.createQuery(
				"select new com.jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
					" from OrderItem oi" +
					" join oi.item i" +
					" where oi.order.id = :orderId", OrderItemQueryDto.class)
			.setParameter("orderId", orderId)
			.getResultList();
	}

	private List<OrderQueryDto> findOrders() {
		return em.createQuery(
				"select new com.jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
					" from Order o" +
					" join o.member m" +
					" join o.delivery d", OrderQueryDto.class)
			.getResultList();
	}


	public List<OrderFlatDto> findAllByDto_flat() {
		return em.createQuery(
				"select new com.jpabook.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count" +
					" from Order o" +
					" join o.member m" +
					" join o.delivery d" +
					" join o.orderItems oi " +
					" join oi.item i", OrderFlatDto.class)
			.getResultList();
	}
}
