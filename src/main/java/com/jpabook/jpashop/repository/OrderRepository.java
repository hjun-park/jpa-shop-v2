package com.jpabook.jpashop.repository;

import com.jpabook.jpashop.domain.Order;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

	@PersistenceContext
	private EntityManager em;

	public void save(Order order) {
		em.persist(order);
	}

	public Order findOne(Long id) {
		return em.find(Order.class, id);
	}

	/*
		JPA Critieria
		 - 권장 방법 X
	 */
	// 동적쿼리 JPQL를 자바 코드로 작성할 수 있게 제공하는 JPA 표준
	public List<Order> findAllByCriteria(OrderSearch orderSearch) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> o = cq.from(Order.class);
		Join<Object, Object> m = o.join("member", JoinType.INNER);

		List<Predicate> criteria = new ArrayList<>();

		// 주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
			criteria.add(status);
		}

		// 회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			Predicate name =
				cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
			criteria.add(name);
		}

		cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
		return query.getResultList();
	}

	public List<Order> findAllByString(OrderSearch orderSearch) {
		//language=JPAQL
		String jpql = "select o From Order o join o.member m";
		boolean isFirstCondition = true;
		//주문 상태 검색
		if (orderSearch.getOrderStatus() != null) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " o.status = :status";
		}
		//회원 이름 검색
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			if (isFirstCondition) {
				jpql += " where";
				isFirstCondition = false;
			} else {
				jpql += " and";
			}
			jpql += " m.name like :name";
		}
		TypedQuery<Order> query = em.createQuery(jpql, Order.class)
			.setMaxResults(1000); //최대 1000건
		if (orderSearch.getOrderStatus() != null) {
			query = query.setParameter("status", orderSearch.getOrderStatus());
		}
		if (StringUtils.hasText(orderSearch.getMemberName())) {
			query = query.setParameter("name", orderSearch.getMemberName());
		}
		return query.getResultList();
	}


	// >> 07-2. 동작방식
	/*
		(fetch join)
		order 조회하는데 member와 delivery를 join 하고 한 번에 다 땡겨온다.
		프록시를 사용하지 않고 Lazy를 다 무시하고 값을 다 가지고 온다.
		- fetch는 JPA에만 있는 문법
	 */
	public List<Order> findAllWithMemberDelivery() {
		return em.createQuery(
			"select o from Order o" +
				" join fetch o.member m" +
				" join fetch o.delivery d", Order.class
		).getResultList();
	}

	// >> 15-1. 페이징과 한계 돌파
	public List<Order> findAllWithMemberDelivery(int offset, int limit) {
		return em.createQuery(
				"select o from Order o" +
					" join fetch o.member m" +
					" join fetch o.delivery d", Order.class)
			.setFirstResult(offset)
			.setMaxResults(limit)
			.getResultList();
	}

	// >> 13. 패치 조인을 이용한 방식
	//  - 그냥 조인을 하게되면 조인에 의한 중복된 데이터 값이 존재함.
	//  - 현재 ORDER를 조회하는 거지만, 주문 ITEM이 4개이고, Join에 의해
	//  - 결과 row가 4개 발생하게 된다. 이 경우 distinct 를 사용한다.

	//  - 그냥 DB에 distinct를 사용하면 똑같이 4개의 row가 발생한다.
	//  - 하지만 JPA에서는 2개만 결과가 나오는데 그 이유는 객체 자체를 비교해서이다.
	//  - 가져온 Order 객체ID값이 같으면 중복으로 처리하고 중복을 제거해준다.
	public List<Order> findAllWithItem() {
		return em.createQuery(
				"select distinct o from Order o" +
					" join fetch o.member m" +
					" join fetch o.delivery d" +
					" join fetch o.orderItems oi" +
					" join fetch oi.item i", Order.class)
//			.getResultList();
			.setFirstResult(1)
			.setMaxResults(100)
			.getResultList();    // 위 처럼 하면 페이징
		// [컬렉션 상태에서 패치 조인하면 페이징이 안 된다]
		// >> 14. 결과가 2개밖에 없으니까 100개 끌어도 2개일거란 생각
		// >> FirstResult와 MaxResult를 사용하고 fetch join을 사용하면 문제 발생
		// >> fetch join을 썼는데 거기에 페이징 쿼리가 들어가버린 상황
		// >> 즉, 메모리에서 페이징 처리를 해버림

		// >> 만약 데이터가 10000개 있었다면 ?
		// >> 10000개를 다 퍼올린 다음에 페이징 처리를 하게 됨 ( Out of Memory )
		// >> 기대하는 결과는 총 2개지만, 결과는 중복을 가진 4개가 나와버림

		// >> [해결방법]
		// >> 1:다 fetch join 상태에서는 페이징을 하지 않는다.
		// >> order, member, delivery 1:1 이지만 orderItem은 1:다 인 상황
	}
}
