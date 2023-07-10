package com.now.service;

import com.now.domain.user.User;
import com.now.dto.TokenClaims;
import com.now.dto.UserDuplicateInfo;
import com.now.exception.AuthenticationFailedException;
import com.now.exception.DuplicateUserException;
import com.now.repository.UserRepository;
import com.now.security.Authority;
import com.now.security.JwtTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@Slf4j
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private String appSalt;
    private JwtTokenService tokenProvider;
    private MessageSourceAccessor messageSource;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${now.password.salt}") String appSalt, JwtTokenService tokenProvider,
                       MessageSourceAccessor messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appSalt = appSalt;
        this.tokenProvider = tokenProvider;
        this.messageSource = messageSource;
    }

    /**
     * 사용자 정보를 등록하는 메서드
     *
     * @param user 등록할 사용자 정보
     * @throws DuplicateUserException 중복된 사용자 정보 예외
     */
    public void registerUser(User user) {
        UserDuplicateInfo duplicateUserInfo = duplicateUserCheck(user);

        if (duplicateUserInfo.existsAtLeastOneDuplicate()) {
            throw new DuplicateUserException(duplicateUserInfo.generateDuplicateFieldMessages());
        }

        userRepository.insert(user.updateByPassword(passwordEncoder.encode(appendSalt(user.getPassword()))));
    }

    /**
     * 사용자의 인증을 처리하고 JWT 토큰을 생성하여 반환
     *
     * @param user 로그인할 사용자 정보
     * @return 인증된 사용자에게 부여된 JWT(Json Web Token)
     * @throws AuthenticationFailedException 아이디 또는 비밀번호가 틀린 경우 발생하는 예외
     */
    public String generateAuthToken(User user) {
        User savedUser = userRepository.findById(user.getId());

        if (savedUser == null) {
            throw new AuthenticationFailedException(messageSource.getMessage("error.authentication.failed"));
        }

        if (!passwordEncoder.matches(appendSalt(user.getPassword()), savedUser.getPassword())) {
            throw new AuthenticationFailedException(messageSource.getMessage("error.authentication.failed"));
        }

        return tokenProvider.create(TokenClaims.create(Map.of(
                "id", user.getId(), "role", Authority.USER.getValue())));
    }

    /**
     * 사용자 아이디(authorId)에 해당하는 사용자를 조회 후 {@link User}를 반환
     *
     * @param authorId 조회할 사용자의 아이디
     * @return 사용자를 조회 후 {@link User}를 반환
     */
    public User findUserById(String authorId) {
        User user = userRepository.findById(authorId);
        if(user == null) {
            throw new AuthenticationFailedException(messageSource.getMessage("error.authentication.userNotFound"));
        }
        return user;
    }

    /**
     * 중복된 사용자 정보를 체크해서 {@link UserDuplicateInfo}를 반환
     *
     * @param user 체크할 사용자 정보
     * @return 중복된 사용자 정보 결과
     */
    private UserDuplicateInfo duplicateUserCheck(User user) {
        return UserDuplicateInfo.builder()
                .duplicateId(userRepository.existsById(user.getId()))
                .duplicateNickname(userRepository.existsByNickname(user.getNickname()))
                .build();
    }

    /**
     * 주어진 값에 암호화 솔트를 결합 후 문자열을 반환
     *
     * @param value 암호화 솔트를 추가할 값
     * @return 암호화 솔트가 추가된 값
     */
    private String appendSalt(String value) {
        return String.join("", appSalt, value);
    }
}
