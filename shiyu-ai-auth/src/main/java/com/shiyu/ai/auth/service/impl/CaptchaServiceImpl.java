package com.shiyu.ai.auth.service.impl;

import com.shiyu.ai.auth.service.CaptchaService;
import com.shiyu.ai.model.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 楠岃瘉鐮佹湇鍔″疄鐜扮被
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {
    
    /**
     * 楠岃瘉鐮佸瓨鍌紙浣跨敤 ConcurrentHashMap 淇濊瘉绾跨▼瀹夊叏锛?
     * key: 楠岃瘉鐮?key
     * value: 楠岃瘉鐮佷俊鎭紙鍖呭惈 code 鍜?expireTime锛?
     */
    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    
    /**
     * 楠岃瘉鐮佽繃鏈熸椂闂达紙5 鍒嗛挓锛?
     */
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000;
    
    /**
     * 楠岃瘉鐮佸瓧绗﹂泦锛堟帓闄ゅ鏄撴贩娣嗙殑瀛楃锛?, O, 1, I, l锛?
     */
    private static final String CAPTCHA_CHARS = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";
    
    /**
     * 楠岃瘉鐮侀暱搴?
     */
    private static final int CAPTCHA_LENGTH = 4;
    
    /**
     * 鍥剧墖瀹藉害
     */
    private static final int WIDTH = 120;
    
    /**
     * 鍥剧墖楂樺害
     */
    private static final int HEIGHT = 40;
    
    /**
     * 闅忔満鏁扮敓鎴愬櫒
     */
    private final Random random = new Random();
    
    /**
     * 鍐呴儴绫伙細楠岃瘉鐮佹暟鎹?
     */
    private static class CaptchaData {
        private final String code;
        private final long expireTime;
        
        public CaptchaData(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
    
    @Override
    public CaptchaVO generateCaptcha() {
        // 鐢熸垚闅忔満楠岃瘉鐮?
        String code = generateRandomCode();
        
        // 鐢熸垚鍞竴 key
        String key = generateCaptchaKey();
        
        // 鐢熸垚 SVG 鍥剧墖
        String svgImage = generateSvgCaptcha(code);
        
        // 瀛樺偍楠岃瘉鐮佷俊鎭?
        long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_TIME;
        captchaStore.put(key, new CaptchaData(code, expireTime));
        
        log.info("鐢熸垚楠岃瘉鐮侊細key={}, code={}, expireTime={}", key, code, expireTime);
        
        // 杩斿洖 CaptchaVO 瀵硅薄
        return new CaptchaVO(key, svgImage, CAPTCHA_EXPIRE_TIME / 1000); // 杞崲涓虹
    }
    
    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        CaptchaData captchaData = captchaStore.get(key);
        if (captchaData == null) {
            log.warn("楠岃瘉鐮佷笉瀛樺湪锛歬ey={}", key);
            return false;
        }
        
        // 妫€鏌ユ槸鍚﹁繃鏈?
        if (captchaData.isExpired()) {
            log.warn("楠岃瘉鐮佸凡杩囨湡锛歬ey={}", key);
            destroyCaptcha(key);
            return false;
        }
        
        // 楠岃瘉楠岃瘉鐮侊紙蹇界暐澶у皬鍐欙級
        boolean valid = captchaData.getCode().equalsIgnoreCase(code);
        if (valid) {
            log.info("楠岃瘉鐮侀獙璇佹垚鍔燂細key={}", key);
            // 楠岃瘉鎴愬姛鍚庣珛鍗抽攢姣?
            destroyCaptcha(key);
        } else {
            log.warn("楠岃瘉鐮侀敊璇細key={}, input={}, expected={}", key, code, captchaData.getCode());
        }
        
        return valid;
    }
    
    @Override
    public void destroyCaptcha(String key) {
        captchaStore.remove(key);
        log.debug("閿€姣侀獙璇佺爜锛歬ey={}", key);
    }
    
    /**
     * 鐢熸垚闅忔満楠岃瘉鐮?
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return sb.toString();
    }
    
    /**
     * 鐢熸垚鍞竴 key
     */
    private String generateCaptchaKey() {
        return "captcha_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }
    
    /**
     * 鐢熸垚 SVG 楠岃瘉鐮?
     */
    private String generateSvgCaptcha(String code) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" viewBox=\"0,0,").append(WIDTH).append(",").append(HEIGHT).append("\">");
        
        // 鑳屾櫙
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#f0f0f0\"/>");
        
        // 缁樺埗骞叉壈绾?
        drawNoiseLines(svg);
        
        // 缁樺埗骞叉壈鐐?
        drawNoisePoints(svg);
        
        // 缁樺埗楠岃瘉鐮佹枃瀛?
        drawCaptchaText(svg, code);
        
        // 缁樺埗璐濆灏旀洸绾垮共鎵?
        drawBezierCurves(svg);
        
        svg.append("</svg>");
        return svg.toString();
    }
    
    /**
     * 缁樺埗骞叉壈绾?
     */
    private void drawNoiseLines(StringBuilder svg) {
        int lineCount = 5;
        for (int i = 0; i < lineCount; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            String color = getRandomColor();
            svg.append("<line x1=\"").append(x1).append("\" y1=\"").append(y1)
                    .append("\" x2=\"").append(x2).append("\" y2=\"").append(y2)
                    .append("\" stroke=\"").append(color).append("\" stroke-width=\"").append(random.nextInt(2) + 1)
                    .append("\" opacity=\"0.5\"/>");
        }
    }
    
    /**
     * 缁樺埗骞叉壈鐐?
     */
    private void drawNoisePoints(StringBuilder svg) {
        int pointCount = 50;
        for (int i = 0; i < pointCount; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            String color = getRandomColor();
            svg.append("<circle cx=\"").append(x).append("\" cy=\"").append(y)
                    .append("\" r=\"").append(random.nextInt(2) + 1)
                    .append("\" fill=\"").append(color).append("\" opacity=\"0.5\"/>");
        }
    }
    
    /**
     * 缁樺埗楠岃瘉鐮佹枃瀛?
     */
    private void drawCaptchaText(StringBuilder svg, String code) {
        int fontSize = 28;
        int charWidth = WIDTH / (code.length() + 1);
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int x = charWidth * (i + 1) - 10;
            int y = HEIGHT / 2 + fontSize / 2 - 5;
            
            // 闅忔満鏃嬭浆瑙掑害锛?15 鍒?15 搴︼級
            double rotation = (random.nextInt(30) - 15) * Math.PI / 180.0;
            
            // 闅忔満棰滆壊
            String color = getRandomDarkColor();
            
            // 娣诲姞鏂囧瓧鍙樻崲鏁堟灉
            svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
                    .append("\" font-family=\"Arial, sans-serif\" font-size=\"").append(fontSize)
                    .append("\" font-weight=\"bold\" fill=\"").append(color).append("\"")
                    .append(" transform=\"rotate(").append(rotation * 180 / Math.PI).append(",").append(x).append(",").append(y).append(")\"");
            svg.append(">").append(c).append("</text>");
        }
    }
    
    /**
     * 缁樺埗璐濆灏旀洸绾垮共鎵?
     */
    private void drawBezierCurves(StringBuilder svg) {
        int curveCount = 3;
        for (int i = 0; i < curveCount; i++) {
            int x1 = random.nextInt(WIDTH / 2);
            int y1 = random.nextInt(HEIGHT);
            int ctrlX = random.nextInt(WIDTH);
            int ctrlY = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH / 2, WIDTH);
            int y2 = random.nextInt(HEIGHT);
            
            String color = getRandomLightColor();
            
            // 浣跨敤浜屾璐濆灏旀洸绾?
            svg.append("<path d=\"M").append(x1).append(",").append(y1)
                    .append(" Q").append(ctrlX).append(",").append(ctrlY)
                    .append(" ").append(x2).append(",").append(y2).append("\"")
                    .append(" stroke=\"").append(color).append("\"")
                    .append(" fill=\"none\" stroke-width=\"").append(random.nextInt(2) + 1)
                    .append("\" opacity=\"0.3\"/>");
        }
    }
    
    /**
     * 鑾峰彇闅忔満棰滆壊锛堟祬鑹诧級
     */
    private String getRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    /**
     * 鑾峰彇闅忔満娣辫壊棰滆壊锛堢敤浜庢枃瀛楋級
     */
    private String getRandomDarkColor() {
        int r = random.nextInt(128);
        int g = random.nextInt(128);
        int b = random.nextInt(128);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    /**
     * 鑾峰彇闅忔満娴呰壊棰滆壊锛堢敤浜庡共鎵扮嚎锛?
     */
    private String getRandomLightColor() {
        int r = 128 + random.nextInt(128);
        int g = 128 + random.nextInt(128);
        int b = 128 + random.nextInt(128);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
