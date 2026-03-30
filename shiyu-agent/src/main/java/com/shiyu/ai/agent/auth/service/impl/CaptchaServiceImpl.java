package com.shiyu.ai.agent.auth.service.impl;

import com.shiyu.ai.agent.auth.service.CaptchaService;
import com.shiyu.ai.agent.domain.vo.CaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {
    
    /**
     * 验证码存储（使用 ConcurrentHashMap 保证线程安全）
     * key: 验证码 key
     * value: 验证码信息（包含 code 和 expireTime）
     */
    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    
    /**
     * 验证码过期时间（5 分钟）
     */
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000;
    
    /**
     * 验证码字符集（排除容易混淆的字符：0, O, 1, I, l）
     */
    private static final String CAPTCHA_CHARS = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ";
    
    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 4;
    
    /**
     * 图片宽度
     */
    private static final int WIDTH = 120;
    
    /**
     * 图片高度
     */
    private static final int HEIGHT = 40;
    
    /**
     * 随机数生成器
     */
    private final Random random = new Random();
    
    /**
     * 内部类：验证码数据
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
        // 生成随机验证码
        String code = generateRandomCode();
        
        // 生成唯一 key
        String key = generateCaptchaKey();
        
        // 生成 SVG 图片
        String svgImage = generateSvgCaptcha(code);
        
        // 存储验证码信息
        long expireTime = System.currentTimeMillis() + CAPTCHA_EXPIRE_TIME;
        captchaStore.put(key, new CaptchaData(code, expireTime));
        
        log.info("生成验证码：key={}, code={}, expireTime={}", key, code, expireTime);
        
        // 返回 CaptchaVO 对象
        return new CaptchaVO(key, svgImage, CAPTCHA_EXPIRE_TIME / 1000); // 转换为秒
    }
    
    @Override
    public boolean validateCaptcha(String key, String code) {
        if (key == null || code == null) {
            return false;
        }
        
        CaptchaData captchaData = captchaStore.get(key);
        if (captchaData == null) {
            log.warn("验证码不存在：key={}", key);
            return false;
        }
        
        // 检查是否过期
        if (captchaData.isExpired()) {
            log.warn("验证码已过期：key={}", key);
            destroyCaptcha(key);
            return false;
        }
        
        // 验证验证码（忽略大小写）
        boolean valid = captchaData.getCode().equalsIgnoreCase(code);
        if (valid) {
            log.info("验证码验证成功：key={}", key);
            // 验证成功后立即销毁
            destroyCaptcha(key);
        } else {
            log.warn("验证码错误：key={}, input={}, expected={}", key, code, captchaData.getCode());
        }
        
        return valid;
    }
    
    @Override
    public void destroyCaptcha(String key) {
        captchaStore.remove(key);
        log.debug("销毁验证码：key={}", key);
    }
    
    /**
     * 生成随机验证码
     */
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return sb.toString();
    }
    
    /**
     * 生成唯一 key
     */
    private String generateCaptchaKey() {
        return "captcha_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }
    
    /**
     * 生成 SVG 验证码
     */
    private String generateSvgCaptcha(String code) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT)
                .append("\" viewBox=\"0,0,").append(WIDTH).append(",").append(HEIGHT).append("\">");
        
        // 背景
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"#f0f0f0\"/>");
        
        // 绘制干扰线
        drawNoiseLines(svg);
        
        // 绘制干扰点
        drawNoisePoints(svg);
        
        // 绘制验证码文字
        drawCaptchaText(svg, code);
        
        // 绘制贝塞尔曲线干扰
        drawBezierCurves(svg);
        
        svg.append("</svg>");
        return svg.toString();
    }
    
    /**
     * 绘制干扰线
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
     * 绘制干扰点
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
     * 绘制验证码文字
     */
    private void drawCaptchaText(StringBuilder svg, String code) {
        int fontSize = 28;
        int charWidth = WIDTH / (code.length() + 1);
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            int x = charWidth * (i + 1) - 10;
            int y = HEIGHT / 2 + fontSize / 2 - 5;
            
            // 随机旋转角度（-15 到 15 度）
            double rotation = (random.nextInt(30) - 15) * Math.PI / 180.0;
            
            // 随机颜色
            String color = getRandomDarkColor();
            
            // 添加文字变换效果
            svg.append("<text x=\"").append(x).append("\" y=\"").append(y)
                    .append("\" font-family=\"Arial, sans-serif\" font-size=\"").append(fontSize)
                    .append("\" font-weight=\"bold\" fill=\"").append(color).append("\"")
                    .append(" transform=\"rotate(").append(rotation * 180 / Math.PI).append(",").append(x).append(",").append(y).append(")\"");
            svg.append(">").append(c).append("</text>");
        }
    }
    
    /**
     * 绘制贝塞尔曲线干扰
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
            
            // 使用二次贝塞尔曲线
            svg.append("<path d=\"M").append(x1).append(",").append(y1)
                    .append(" Q").append(ctrlX).append(",").append(ctrlY)
                    .append(" ").append(x2).append(",").append(y2).append("\"")
                    .append(" stroke=\"").append(color).append("\"")
                    .append(" fill=\"none\" stroke-width=\"").append(random.nextInt(2) + 1)
                    .append("\" opacity=\"0.3\"/>");
        }
    }
    
    /**
     * 获取随机颜色（浅色）
     */
    private String getRandomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    /**
     * 获取随机深色颜色（用于文字）
     */
    private String getRandomDarkColor() {
        int r = random.nextInt(128);
        int g = random.nextInt(128);
        int b = random.nextInt(128);
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    /**
     * 获取随机浅色颜色（用于干扰线）
     */
    private String getRandomLightColor() {
        int r = 128 + random.nextInt(128);
        int g = 128 + random.nextInt(128);
        int b = 128 + random.nextInt(128);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
