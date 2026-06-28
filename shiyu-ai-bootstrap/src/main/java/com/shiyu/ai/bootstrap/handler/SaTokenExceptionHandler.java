package com.shiyu.ai.bootstrap.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.enums.BizResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 鍏ㄥ眬寮傚父澶勭悊鍣?
 * 缁熶竴澶勭悊璁よ瘉鍜屾巿鏉冪浉鍏崇殑寮傚父
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 鏈櫥褰曟垨 Token 鏃犳晥
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        String message = switch (e.getType()) {
            case NotLoginException.NOT_TOKEN -> "鏈彁渚涜璇佷护鐗?;
            case NotLoginException.INVALID_TOKEN -> "璁よ瘉浠ょ墝鏃犳晥鎴栧凡杩囨湡";
            case NotLoginException.TOKEN_TIMEOUT -> "璁よ瘉浠ょ墝宸茶繃鏈燂紝璇烽噸鏂扮櫥褰?;
            case NotLoginException.BE_REPLACED -> "鎮ㄧ殑璐﹀彿宸插湪鍏朵粬璁惧鐧诲綍锛岃閲嶆柊鐧诲綍";
            case NotLoginException.KICK_OUT -> "鎮ㄧ殑璐﹀彿宸茶寮哄埗涓嬬嚎";
            default -> "鏈櫥褰曟垨鐧诲綍宸插け鏁?;
        };
        
        log.warn("璁よ瘉澶辫触: type={}, message={}", e.getType(), message);
        return Result.fail(BizResultCode.UNAUTHORIZED, message);
    }

    /**
     * 鏉冮檺涓嶈冻
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("鏉冮檺涓嶈冻: permission={}", e.getPermission());
        return Result.fail(BizResultCode.FORBIDDEN, BizResultCode.FORBIDDEN.getMsg());
    }

    /**
     * 瑙掕壊涓嶈冻
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("瑙掕壊涓嶈冻: role={}", e.getRole());
        return Result.fail(BizResultCode.FORBIDDEN, "瑙掕壊鏉冮檺涓嶈冻锛岃鑱旂郴绠＄悊鍛?);
    }
}
