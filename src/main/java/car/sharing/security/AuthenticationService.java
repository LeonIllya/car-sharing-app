package car.sharing.security;

import car.sharing.dto.user.UserLoginRequestDto;
import car.sharing.dto.user.UserLoginResponseDto;
import car.sharing.model.User;
import car.sharing.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.email(),
                        requestDto.password())
        );

        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }

    public void authenticateWithTelegram(UserLoginRequestDto requestDto,
                                            Long telegramId) {
        final Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(requestDto.email(),
                requestDto.password())
        );
        User user = (User) authentication.getPrincipal();
        user.setTelegramId(telegramId);
        userRepository.save(user);
    }
}
