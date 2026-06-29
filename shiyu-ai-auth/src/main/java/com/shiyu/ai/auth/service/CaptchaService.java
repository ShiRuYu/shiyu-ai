package com.shiyu.ai.auth.service;

import com.shiyu.ai.auth.vo.CaptchaVO;

/**
 * 楠岃瘉鐮佹湇鍔?
 * 鎻愪緵楠岃瘉鐮佺敓鎴愩€侀獙璇佸拰閿€姣佸姛鑳?
 */
public interface CaptchaService {
    
    /**
     * 鐢熸垚楠岃瘉鐮?
     * @return 楠岃瘉鐮佷俊鎭紙CaptchaVO 瀵硅薄锛?
     */
    CaptchaVO generateCaptcha();
    
    /**
     * 楠岃瘉楠岃瘉鐮?
     * @param key 楠岃瘉鐮?key
     * @param code 鐢ㄦ埛杈撳叆鐨勯獙璇佺爜
     * @return 楠岃瘉缁撴灉
     */
    boolean validateCaptcha(String key, String code);
    
    /**
     * 閿€姣侀獙璇佺爜锛堜娇鐢ㄥ悗绔嬪嵆閿€姣侊級
     * @param key 楠岃瘉鐮?key
     */
    void destroyCaptcha(String key);
}
