package com.jpabook.jpashop.controller;

import com.jpabook.jpashop.domain.Address;
import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 회원가입 페이지 요청 시
    @GetMapping("/members/new")
    // Model: 컨트롤러에서 뷰로 넘어갈 때 데이터를 실어 넘김
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    // 회원가입 등록 시
    // Valid옵션은 MemberForm에서 @NotEmpty 이런 것들을 spring에서 validate 해준다.
    @PostMapping("/members/new")
    public String create(@Valid MemberForm memberForm, BindingResult result) {

        // Binding Result는 @Valid 결과 이상이 있다면 처리 가능하다.
        if(result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(memberForm.getCity(), memberForm.getStreet(), memberForm.getZipcode());

        Member member = new Member();
        member.setName(memberForm.getName());
        member.setAddress(address);

        memberService.join(member);
        // 회원가입 후 홈으로 보냄
        return "redirect:/";

    }

    @GetMapping("/members")
    public String list(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);

        return "members/memberList";
    }

}
