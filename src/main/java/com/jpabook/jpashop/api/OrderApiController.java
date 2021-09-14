package com.jpabook.jpashop.api;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Order;
import com.jpabook.jpashop.domain.OrderItem;
import com.jpabook.jpashop.domain.OrderStatus;
import com.jpabook.jpashop.repository.OrderRepository;
import com.jpabook.jpashop.repository.OrderSearch;
import com.jpabook.jpashop.repository.order.query.OrderFlatDto;
import com.jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import com.jpabook.jpashop.repository.order.query.OrderQueryDto;
import com.jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// >> 10. 컬렉션 조회 최적화 (객체 하나가 아닌 컬렉션 조회)
@RestController
@RequiredArgsConstructor
public class OrderApiController {

	private final OrderRepository orderRepository;
	private final OrderQueryRepository orderQueryRepository;

	// >> 11. v2. 엔티티 DTO 변환 -> 쿼리가 상당히 많이 날아감 비효율적
	@GetMapping("/api/v2/orders")
	public List<OrderDto> ordersV2() {
		List<Order> orders = orderRepository.findAllByString(new OrderSearch());
		List<OrderDto> result = orders.stream()
			.map(o -> new OrderDto(o))
			.collect(Collectors.toList());

		return result;
	}

	// >> 12. v3. 엔티티 DTO 변환 - 패치 조인 사용
	//    - 패치 조인만 사용했을 뿐이고 나머지는 v2와 코드가 유사하다.
	//    - 쿼리가 여러 번 나가는데 비해 v3에서는 쿼리가 1개만 나간다. (성능 우수)
	@GetMapping("/api/v3/orders")
	public List<OrderDto> ordersV3() {
		List<Order> orders = orderRepository.findAllWithItem();
		List<OrderDto> result = orders.stream()
			.map(OrderDto::new)
			.collect(Collectors.toList());

		return result;
	}

	// >> 15. 페이징과 한계 돌파 ( N+1 -> 1+1 )
	@GetMapping("/api/v3.1/orders")
	public List<OrderDto> ordersV3_page(
		@RequestParam(value = "offset", defaultValue = "0") int offset,
		@RequestParam(value = "limit", defaultValue = "100") int limit
	) {
		// @*ToOne 끼리 했기 때문에 페이징 가능
		List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);    // 쿼리 1번

		// >> 18. application.yml에서 batch 사이즈를 조절하면 조절이 된다.
		// >> 기존에서 items를 조회하면 4개가 조회되었지만 batch 사이즈 조정을 통해서
		// >> 한방쿼리로 IN 쿼리를 4개 가져온다. ( 정확하게 필요한 데이터를 짚어서 가져온다 )
		// >> 쿼리는 총 3번 날린다. ( order, orderItems, Items )
		List<OrderDto> result = orders.stream()    // orderItems 에서 1개 조회 ( item 2개 존재 )
			// Items 에서 또 2개 조회  ( 총 (1+2)*2 6번 )
			.map(OrderDto::new)
			.collect(Collectors.toList());

		return result;
	}

	// >> 19. JPA에서 DTO 직접 조회하는 방법
	// orderItems의 경우 order와 N:1 관계를 가지므로, join하면 데이터가 증가한다.
	// 그래서 1:1관계는 그냥 그대로 가져오지만, orderItem은 하나하나 매칭을 시켜주었다.
	// 그렇게 하면 총 3번의 쿼리가 실행 된다.
	// 결과적으로 N+1 문제가 발생한다.
	// 		findOrder에서 1번 실행하여 주문 내역 N개를 가져오고,
	//		findOrderItem에서 1개의 주문에 대해 1개의 orderItem만 가져오면 좋을텐데
	//		여기서는 주문 내역의 item 개수 N개에 대해 하나씩 쿼리를 날린다.
	@GetMapping("/api/v4/orders")
	public List<OrderQueryDto> ordersV4() {
		return orderQueryRepository.findOrderQueryDtos();
	}


	@GetMapping("/api/v5/orders")
	public List<OrderQueryDto> ordersV5() {
		return orderQueryRepository.findAllByDto_optimization();
	}

	// >> 22. 다 받는 DTO를 만들어 쿼리 한 번에 해결
	// 쿼리 한 번에 다 가지고 오지만 중복 문제 발생 ( 페이징 불가 )
	// 장점 : 쿼리 1개,  단점 : 쿼리 짜집기 필요, 페이징 불가
	@GetMapping("/api/v6/orders")
	public List<OrderQueryDto> ordersV6() {
		List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

		// flats 중복을 제거
		return flats.stream()
			// 실행순서: mapping -> groupingBy -> entrySet -> stream
			// collect grouping을 통해서 orderFlatDto를 orderQueryDto로
			.collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
				Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), Collectors.toList())
			)).entrySet().stream()
			.map(e -> new OrderQueryDto(e.getKey().getOrderId(),
				e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
				e.getKey().getAddress(), e.getValue()))
			.collect(Collectors.toList());
	}


	// ===================
	// v2에서 사용
	// ===================
	@Data
	static class OrderDto {

		private Long orderId;
		private String name;
		private LocalDateTime orderDate;
		private OrderStatus orderStatus;
		private Address address;
		private List<OrderItemDto> orderItems;

		public OrderDto(Order order) {
			this.orderId = order.getId();
			this.name = order.getMember().getName();
			this.orderDate = order.getOrderDate();
			this.orderStatus = order.getStatus();
			this.address = order.getDelivery().getAddress();
			this.orderItems = order.getOrderItems().stream()
				.map(OrderItemDto::new)
				.collect(Collectors.toList());
		}
	}

	@Data
	static class OrderItemDto {

		private String itemName;    // 상품명
		private int orderPrice;        // 주문가격
		private int count;            // 주문 수량

		public OrderItemDto(OrderItem orderItem) {
			this.itemName = orderItem.getItem().getName();
			this.orderPrice = orderItem.getOrderPrice();
			this.count = orderItem.getCount();
		}
	}
}
