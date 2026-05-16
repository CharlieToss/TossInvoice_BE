package hankyung.tossinvoice.global.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import hankyung.tossinvoice.global.exception.CustomErrorResponse;
import hankyung.tossinvoice.global.exception.GlobalErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"errorCode", "message", "result"})
public class BaseResponse<T> {
    private final String errorCode;
    private final String message;
    private T result;

    public static <T> BaseResponse<T> success(final T data) {
        return new BaseResponse<>(null, "SUCCESS", data);
    }

    public static <T> BaseResponse<T> fail(CustomErrorResponse customErrorResponse) {
        return new BaseResponse<>(customErrorResponse.getErrorCode(), customErrorResponse.getMessage(), null);
    }
}