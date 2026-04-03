package com.fhsh.daitda.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private int status;
    private String message;
    private T data;

    // 성공 - 기본 메시지
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(200, "요청이 성공적으로 처리되었습니다.", data);
    }

    // 성공 - 커스텀 메시지
    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>(200, message, data);
    }

    // 성공 - data가 없는 경우
    public static CommonResponse<Void> success() {
        return new CommonResponse<>(200, "요청이 성공적으로 처리되었습니다.", null);
    }
}