package com.jpabook.jpashop.service;

import com.jpabook.jpashop.domain.item.Book;
import com.jpabook.jpashop.domain.item.Item;
import com.jpabook.jpashop.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }


    // 상품 내용 수정
    @Transactional
    public void updateItem(Long itemId,  String name, int price, int stockQuantity) {
        // find로 찾아오면 영속 상태인 값이 나온다.
        Item findItem = itemRepository.findOne(itemId);

        // 아래처럼 값을 셋팅하게 되면
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);

        // @Transactional에 의해서 commit이 되고 flush를 날리게 된다.
        // flush를 날리면 변경된 부분을 찾게 되고 JPA는 그 변경된 부분에 대해
        // update 쿼리를 날리게 된다.

        // merge를 쓰기보다 변경 감지를 쓴다.

    }

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
