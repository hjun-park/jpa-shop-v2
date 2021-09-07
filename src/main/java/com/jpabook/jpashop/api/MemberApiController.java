package com.jpabook.jpashop.api;

// >> 01. [상황] 기존 사업이 잘 되어 어플로 만드려고 함
// 어플과 통신하기 위해 API를 만들 것이며, 기존 화면하고는 따로 분리해 둠
// 그 이유는 예외처리를 묶어서 편하게 하기 위함 ( 화면과 API는 서로 다르니까 )

import com.jpabook.jpashop.domain.Member;
import com.jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

	private final MemberService memberService;

	// >> 02. 회원 등록 API
	// @RequestBody : Json으로 온 Body를 member로 변환해 줌
	// 문제점: Member는 엔티티이고 다른 곳에서 정말 많이 쓰고 있다. 이게 바뀔 수도 있단 얘기
	// 만약 API를 설계하고 추후에 Member 내용이 바뀌면 api 스펙 자체가 바뀌는 큰 문제 발생
	// 따라서 MemberDTO를 따로 만드는 것이 필요하다.
	@PostMapping("/api/v1/members")
	public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}

	// >> 03. 별도의 DTO를 사용한 예제
	// 장점: api 스펙 영향 없이 수정이 가능하다.
	//      무엇이 필요해서 어떤걸 받는지 알 수 있다.
	@PostMapping("/api/v2/members")
	public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

		Member member = new Member();
		member.setName(request.getName());

		Long id = memberService.join(member);
		return new CreateMemberResponse(id);
	}

	// >> 04. 회원 정보 수정
	@PutMapping("/api/v2/members/{id}")
	public UpdateMemberResponse updateMemberV2(
			@PathVariable("id") Long id,
			@RequestBody @Valid UpdateMemberRequest request) {

		// 수정할 때는 가능한 변경감지를 사용할 것 ( update 메소드 내용 참고 )
		memberService.update(id, request.getName());
		Member findMember = memberService.findOne(id);
		return new UpdateMemberResponse(findMember.getId(), findMember.getName());
	}


	// >> 05. 회원 조회 API
	// 문제점: 단순 회원 정보만 원하는 건데, 엔티티가 직접 노출되면서
	// 모든 정보들이 노출된다. (궁금하지 않는 orders도 표현됨)
	@GetMapping("/api/v1/members")
	public List<Member> membersV1() {
		return memberService.findMembers();
	}


	// >> 06. 회원 조회 API (개선된버전)
	@GetMapping("/api/v2/members")
	public Result membersV2() {
		List<Member> findMembers = memberService.findMembers();
		List<MemberDto> members = findMembers.stream()
			.map(member -> new MemberDto(member.getName()))
			.collect(Collectors.toList());

		return new Result(members.size(), members);
	}







	// ============================================

	@Data
	@AllArgsConstructor
	static class Result<T> {
		private int count;
		private T data;
	}

	@Data
	@AllArgsConstructor
	static class MemberDto {
		private String name;
	}

	@Data
	static class UpdateMemberRequest {
		private String name;
	}

	// data랑 allargs는 최대한 쓰는걸 자제한다.
	@Data
	@AllArgsConstructor
	static class UpdateMemberResponse {
		private Long id;
		private String name;
	}


	@Data
	static class CreateMemberRequest {
		private String name;
	}

	// api 반환 값
	@Data
	static class CreateMemberResponse {
		private Long id;

		public CreateMemberResponse(Long id) {
			this.id = id;
		}
	}
}
