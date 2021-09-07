package com.jpabook.jpashop.controller;

import com.jpabook.jpashop.domain.item.Book;
import com.jpabook.jpashop.domain.item.Item;
import com.jpabook.jpashop.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm.html";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "items/itemList";
    }

    // 상품 수정
    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId")Long itemId, Model model) {
        // @PathVariable : {itemId}와 매핑

        Book item = (Book) itemService.findOne(itemId);

        // 엔티티를 보내는 것이 아니라 정보를 업데이트 하는 폼을 보내준다.
        BookForm form = new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping("/items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {

        /* Merge (병합) 방식 : */
        // dirty checking과는 다르게 준영속 상태의 엔티티를 영속 상태로 만들어준다.
        // 자세한 내용은 김영한 님 강의 참고(PDF북)
        // 병합은 조심해야할 점이 있다.
        // 변경 감지 기능 (dirty checking)은 일부 원하는 속성만 선택해서 변경할 수 있지만
        // 병합의 경우 모든 속성이 변경된다. 병합시 값이 없으면 null로 업데이트할 위험도 있다. ( 병합은 모든 필드를 교체함 )
        // 그러기 때문에 병합을 쓰기보다는 변경감지 기능을 쓰는 것이 실무에서 제일 좋다. ( 즉, merge를 쓰지 마라 )
//        Book book = new Book();
//        book.setIsbn(form.getIsbn());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());

//        itemService.saveItem(book);
        return "redirect:/items";
    }
}
