package hankyung.tossinvoice.controller.support.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 컨트롤러 파라미터에서 SecurityContext의 principal(userId)을 바로 꺼내기 위한 단축 어노테이션입니다.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "T(hankyung.tossinvoice.security.jwt.JwtUtil).checkPrincipal(#this)")
public @interface UserId {
}
