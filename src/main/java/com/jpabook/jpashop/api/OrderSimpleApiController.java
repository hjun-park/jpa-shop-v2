package com.jpabook.jpashop.api;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import com.jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

	private final OrderRepository orderRepository;
	private final OrderSimpleQueryRepository orderSimpleQueryRepository;


	/*
		v3와 v4 우열을 가르기 힘든 이유
		 1) [재사용성] v3의 경우 내가 원하는 테이블을 이용하여 fetch join 후에 가져옴
		 2) [성능최적화] v4의 경우 sql 사용하여 원하는 대로 뽑아옴
		  - v4는 v3에 비해서 재사용성이 떨어짐
		  - v3는 fetch join 후에 원하는 것만 dto로 뽑아쓰면 됨

		 또 다른 문제점은 v3의 경우 엔티티를 뽑아와서 DTO로 변환하는 거지만,
		 v4의 경우 DTO가 바로 나오기 때문에 API 스펙이 나오면 다 뜯어 고쳐야 한다.

		 [방법] - Repository를 2개 둔다.
		  1) 순수한 엔티티만 조회하는 OrderRepository
		  2) 화면에 박히는 Dto가 뽑혀서 나오는 OrderSimpleQueryRepository
			  -> 엔티티 뽑아서 조합하는 것보다 복잡한 쿼리가 더 나은 경우
			  -> 조회 전용으로 쓴다.

		 [무엇을 선택하는 것이 좋을지?]
		 1) 엔티티 -> DTO 변환 방법 선택
		 2) 필요하면 패치 조인으로 최적화 ( 대부분 성능 이슈 해결 ) (v3)
		 3) 패치조인만으로도 성능 이슈가 있다 -> DTO 직접 조회 방법 선택 (v4)
		 4) 최후에는 JDBC Template ( 거의 없음 )

	 */

	// >> 07-1. N+1 문제(성능문제의 90% 원인)와 같은 성능 저하 문제를 해결하는 fetch join
	@GetMapping("api/v3/simple-orders")
	public List<OrderSimpleDto> ordersV3() {

		List<Order> orders = orderRepository.findAllWithMemberDelivery();

		// entity를 dto에 매핑
		List<OrderSimpleDto> result = orders.stream()
			.map(o -> new OrderSimpleDto(o))
			.collect(Collectors.toList());

		return result;
	}

	// >> 08. v4: JPA에서 DTO로 바로 조회
	@GetMapping("/api/v4/simple-orders")
	public List<OrderSimpleQueryDto> ordersV4() {
		return orderSimpleQueryRepository.findOrderDtos();
	}


	// ====================================
	// V3에서 사용
	// ====================================
	@Data
	static public class OrderSimpleDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;    // 주문시간
		private OrderStatus orderStatus;
		private Address address;

		public OrderSimpleDto(Order order) {
			this.orderId = order.getId();
			this.name = order.getMember().getName();
			this.orderDate = order.getOrderDate();
			this.orderStatus = order.getStatus();
			this.address = order.getDelivery().getAddress();
		}
	}

}
