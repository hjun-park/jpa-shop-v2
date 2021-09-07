package com.jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded
    private Address address;

    // 연관관계 주인이 아닌 거울은 MappedBy를 집어넣어준다.
    // 대상은 필드 이름으로 지정
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
