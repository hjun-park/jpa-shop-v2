package com.jpabook.jpashop.domain.item;

import com.jpabook.jpashop.Category;
import com.jpabook.jpashop.domain.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 상속관계 전략
@DiscriminatorColumn(name = "dtype")    // book이면 어떻게 할거야 이런 것
@Getter @Setter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // Entity 자체가 해결할 수 있는 것들은
    // Entity 안에서 비즈니스 로직을 짜는 것이 좋다.
    //== 비즈니스 로직 ==//

    /*
        Stock 증가
     */
    public void addStock(int quantity) {
        this.stockQuantity = quantity;
    }

    /*
        Stock 감소
     */
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;

        // 남은 수량은 0보다 작을 수 없음
        if(restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;

    }



}
