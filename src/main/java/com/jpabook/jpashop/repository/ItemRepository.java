package com.jpabook.jpashop.repository;

import com.jpabook.jpashop.domain.item.Item;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ItemRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Item item) {
        // id값이 없다는 건 완전 새로운 item을 저장한다는 뜻 (insert)
        if(item.getId() == null) {
            em.persist(item);
        } else {
            // dirty checking이 일어나서 기존의 item을 수정함 (update)
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
