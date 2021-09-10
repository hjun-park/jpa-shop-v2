package com.jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

	@PersistenceContext
	private EntityManager em;

	public List<OrderSimpleQueryDto> findOrderDtos() {
		// >> 09. 여기서 보면 쿼리 결과가 Dto 클래스하고 1:1 매핑 될 수는 없음
		// 이 경우 new 연산자를 써야 한다.
		return em.createQuery(
				"select new com.jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
					" from Order o" +
					" join o.member m" +
					" join o.delivery d", OrderSimpleQueryDto.class)
			.getResultList();
	}
}
