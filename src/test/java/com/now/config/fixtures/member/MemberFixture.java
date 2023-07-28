package com.now.config.fixtures.member;

import com.now.core.member.domain.Member;
import com.now.core.member.presentation.dto.MemberProfile;

public class MemberFixture {

    public static String SAMPLE_MEMBER_ID_1 = "honi132";
    public static String SAMPLE_PASSWORD_1 = "testPassword1!";

    public static Member createMember(String memberId) {
        return Member.builder()
                .id(memberId)
                .password("testPassword")
                .name("testName")
                .nickname("testNickName")
                .build();
    }

    public static Member createMember(String id, String password) {
        return Member.builder()
                .id(id)
                .password(password)
                .build();
    }

    public static Member createMemberProfile(String id, String nickname, String name) {
        return Member.builder()
                .id(id)
                .nickname(nickname)
                .name(name)
                .build();
    }
}
