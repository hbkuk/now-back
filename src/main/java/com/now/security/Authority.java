package com.now.security;

/**
 * 사용자 권한을 나타내는 enm
 */
public enum Authority {
    /**
     * 일반 회원 권한
     */
    USER("USER"),

    /**
     * 매니저 권한
     */
    MANAGER("MANAGER");

    private final String value;

    Authority(String value) {
        this.value = value;
    }

    /**
     * 권한을 나타내는 문자열 값을 반환
     *
     * @return 권한을 나타내는 문자열 값
     */
    public String getValue() {
        return value;
    }
}


