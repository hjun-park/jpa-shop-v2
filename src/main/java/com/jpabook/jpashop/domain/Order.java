package com.jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    // 연관관계 주인 ( FK를 가지고 있으므로 )
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // 매핑을 상대방 엔티티의 어떤거와 할건지?
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //== 연관관계 편의 메소드==// => 연관관계에 속한 것들 끼리 서로 등록해주기 위한 메소드
    public void setMember(Member member) {  // 멤버 셋팅할 때
        this.member = member;               // 주문의 멤버를 지정하고
        member.getOrders().add(this);       // 해당 멤버 주문에 해당 주문을 집어넣음
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    // order랑 delivry도 서로 연관관계 메소드
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }


    //== 주문 생성 메소드 ==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) { // 여러개
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);

        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    // 비즈니스 로직
    /*
        주문 취소
     */
    public void cancel() {
        if(delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //== 주문 조회 ==//
    /*
        전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();    // 주문가격과 수량을 곱하기 위해 getTotalPrice 사용
        }

        // 위의 문장은 아래의 Stream 으로 해석될 수 있다.
        // orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();

        return totalPrice;
    }




}
