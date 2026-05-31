package cn.org.wang.exam.utils.captcha;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.RandomUtil;
import cn.org.wang.exam.config.SlideCaptchaConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * 滑块验证码工具类
 * 负责生成背景图、滑块图以及计算缺口位置
 */
@Slf4j
@Component
public class SlideCaptchaUtil {



    private final SlideCaptchaConfig config;
    private List<Resource> imageResources = new ArrayList<>();

    /**
     * 构造函数
     * @SuppressFBWarnings("EI_EXPOSE_REP2") - Spring依赖注入模式，配置类由Spring容器管理，不存在外部修改风险
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public SlideCaptchaUtil(SlideCaptchaConfig config) {
        this.config = config;
    }

    /**
     * 初始化加载图片资源
     */
    public void init() {
        try {
            String path = config.getImagePath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // 支持多种格式
            String[] extensions = {"*.jpg", "*.jpeg", "*.png", "*.webp"};
            for (String ext : extensions) {
                loadImageResources(resolver, path, ext);
            }
            log.info("滑块验证码背景图加载完成，共找到 {} 张图片", imageResources.size());
        } catch (Exception e) {
            log.error("初始化滑块验证码资源失败", e);
        }
    }
    
    /**
     * 加载指定格式的图片资源
     */
    private void loadImageResources(PathMatchingResourcePatternResolver resolver, String path, String extension) {
        try {
            Resource[] resources = resolver.getResources(path + extension);
            for (Resource resource : resources) {
                if (resource.exists() && resource.isReadable()) {
                    imageResources.add(resource);
                }
            }
        } catch (Exception e) {
            log.debug("未找到格式为 {} 的图片资源", extension);
        }
    }

    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;
    private static final int SLIDER_WIDTH = 45;
    private static final int SLIDER_HEIGHT = 40;
    private static final int CIRCLE_R = 5;

    /**
     * 生成滑块验证码数据
     *
     * @return 包含背景图、滑块图和坐标的结果对象
     */
    public SlideCaptchaData generate() {
        // 懒加载：如果资源列表为空，尝试初始化一次
        if (imageResources.isEmpty()) {
            init();
        }

        // 1. 获取背景图（优先加载本地图片，失败则回退到随机生成）
        BufferedImage bgImage = loadBackgroundImage();
        if (bgImage == null) {
            bgImage = generateRandomBackground();
        }

        // 2. 计算缺口位置
        // xGap ∈ [55, WIDTH - SLIDER_WIDTH - 15]
        // yGap ∈ [25, HEIGHT - SLIDER_HEIGHT - 20]
        int xGap = RandomUtil.randomInt(55, WIDTH - SLIDER_WIDTH - 15);
        int yGap = RandomUtil.randomInt(25, HEIGHT - SLIDER_HEIGHT - 20);

        // 3. 生成滑块图 (包含凸起，所以尺寸要略大)
        // 实际有效区域是 SLIDER_WIDTH x SLIDER_HEIGHT，加上两侧/上下的凸起半径 CIRCLE_R
        // 宽度 = 滑块宽度 + 左右各一个凸起半径 = 45 + 5*2 = 55
        // 高度 = 滑块高度 + 上下各一个凸起半径 = 40 + 5*2 = 50
        BufferedImage sliderImage = new BufferedImage(
                SLIDER_WIDTH + CIRCLE_R * 2,
                SLIDER_HEIGHT + CIRCLE_R * 2,
                BufferedImage.TYPE_INT_ARGB
        );
        // 4. 抠图与遮罩
        cutImageOptimized(bgImage, sliderImage, xGap, yGap);

        // 5. 转Base64
        String bgBase64 = toBase64(bgImage);
        String sliderBase64 = toBase64(sliderImage);

        return new SlideCaptchaData()
                .setBackgroundImageBase64("data:image/png;base64," + bgBase64)
                .setSliderImageBase64("data:image/png;base64," + sliderBase64)
                .setXGap(xGap)
                .setYGap(yGap);
    }

    /**
     * 加载背景图片
     */
    private BufferedImage loadBackgroundImage() {
        if (imageResources.isEmpty()) {
            return null;
        }
        try {
            int index = RandomUtil.randomInt(imageResources.size());
            Resource resource = imageResources.get(index);
            try (InputStream is = resource.getInputStream()) {
                BufferedImage originalImage = ImageIO.read(is);
                if (originalImage == null) {
                    log.warn("读取图片失败: {}", resource.getFilename());
                    return null;
                }
                // 缩放到指定大小
                BufferedImage resizedImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(originalImage, 0, 0, WIDTH, HEIGHT, null);
                g.dispose();
                return resizedImage;
            }
        } catch (Exception e) {
            log.warn("加载滑块背景图失败，将使用随机背景: {}", e.getMessage());
        }
        return null;
    }

    private BufferedImage generateRandomBackground() {
        BufferedImage bgImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bgImage.createGraphics();
        // 简单的动态背景：随机颜色渐变
        int r = RandomUtil.randomInt(150, 250);
        int g = RandomUtil.randomInt(150, 250);
        int b = RandomUtil.randomInt(150, 250);
        g2d.setColor(new Color(r, g, b));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        // 添加干扰线
        for (int i = 0; i < 20; i++) {
            g2d.setColor(new Color(RandomUtil.randomInt(255), RandomUtil.randomInt(255), RandomUtil.randomInt(255)));
            g2d.setStroke(new BasicStroke(RandomUtil.randomInt(1, 3)));
            g2d.drawLine(RandomUtil.randomInt(WIDTH), RandomUtil.randomInt(HEIGHT), RandomUtil.randomInt(WIDTH), RandomUtil.randomInt(HEIGHT));
        }
        g2d.dispose();
        return bgImage;
    }

    private void cutImageOptimized(BufferedImage bgImage, BufferedImage sliderImage, int x, int y) {
        int sliderW = sliderImage.getWidth();
        int sliderH = sliderImage.getHeight();

        Graphics2D sliderG = sliderImage.createGraphics();
        Graphics2D bgG = bgImage.createGraphics();

        // 抗锯齿
        sliderG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 定义拼图路径
        float w = SLIDER_WIDTH;
        float h = SLIDER_HEIGHT;
        float r = CIRCLE_R;

        // 构造路径
        GeneralPath path = new GeneralPath();
        path.moveTo(0, 0);

        // 上边 (带凸起)
        path.lineTo(w / 2 - r, 0);
        path.quadTo(w / 2, -r * 2, w / 2 + r, 0); // 凸起
        path.lineTo(w, 0);

        // 右边 (带凸起)
        path.lineTo(w, h / 2 - r);
        path.quadTo(w + r * 2, h / 2, w, h / 2 + r); // 凸起
        path.lineTo(w, h);

        // 下边
        path.lineTo(0, h);

        // 左边
        path.lineTo(0, 0);
        path.closePath();

        // 1. 绘制滑块：从背景图中提取像素
        // 遍历滑块区域的每一个像素
        for (int i = 0; i < sliderW; i++) {
            for (int j = 0; j < sliderH; j++) {
                // 对应原图坐标
                int originX = x + i;
                // 由于滑块图包含了上方的凸起（高度为r），所以滑块图的j=0对应原图的y-r位置
                int originY = y + j - (int)r;

                // 边界检查
                if (originX >= WIDTH || originY >= HEIGHT || originY < 0) continue;

                // 判断点是否在路径内
                // 滑块图坐标(i,j) 对应 path坐标(i, j-r)
                // 注意：path是基于(0,0)到(w,h)绘制的，包含上下凸起
                // 这里的path绘制时是从(0,0)开始的，包含了上方的凸起，所以path坐标系的(0,0)实际上对应滑块图的左上角
                // 但是在cutImageOptimized中，path的绘制逻辑是：
                // path.moveTo(0, 0); 这里的(0,0)是主体左上角
                // path.lineTo(w / 2 - r, 0); ... path.quadTo(..., -r * 2, ...); 上方凸起会延伸到y<0的区域
                // 所以path的y坐标范围是[-r, h+r]
                
                // 而滑块图sliderImage的高度是 h + 2*r
                // 我们在遍历sliderImage时，j从0到sliderH
                // 对应path的坐标应该是 j - r
                // 因为sliderImage的第0行对应path的y=-r行
                
                if (path.contains(i, j - r)) {
                    // 复制像素到滑块图
                    sliderImage.setRGB(i, j, bgImage.getRGB(originX, originY));

                    // 背景图对应位置变暗/模糊 (遮罩)
                    int rgb = bgImage.getRGB(originX, originY);
                    Color c = new Color(rgb);
                    // 降低亮度
                    Color dark = new Color((int) (c.getRed() * 0.4), (int) (c.getGreen() * 0.4), (int) (c.getBlue() * 0.4));
                    bgImage.setRGB(originX, originY, dark.getRGB());
                }
            }
        }

        // 优化：给缺口也添加一个内阴影或描边，使其更清晰（解决视觉大小不匹配问题）
        bgG.translate(x, y);
        bgG.setColor(new Color(255, 255, 255, 100)); // 半透明白色
        bgG.setStroke(new BasicStroke(1));
        bgG.draw(path);
        bgG.translate(-x, -y); // 恢复坐标系

        // 给滑块添加边框，使其更明显
        // 滑块图坐标系需要向下平移r，以匹配path坐标系
        sliderG.translate(0, r);
        sliderG.setColor(new Color(255, 255, 255, 200));
        sliderG.setStroke(new BasicStroke(1)); // 减小描边宽度，减少视觉误差
        sliderG.draw(path);

        sliderG.dispose();
        bgG.dispose();
    }

    private String toBase64(BufferedImage image) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImgUtil.writePng(image, stream);
            byte[] imageBytes = stream.toByteArray();
            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            // 只记录Base64长度，避免日志过大
            log.debug("图片转Base64成功，长度: {}", base64.length());
            return base64;
        } catch (Exception e) {
            log.error("图片转Base64失败", e);
            // 返回空字符串，避免接口崩溃
            return "";
        }
    }

    public static class SlideCaptchaData {
        private String token;
        private String backgroundImageBase64;
        private String sliderImageBase64;
        
        @JsonProperty("xGap")
        private int xGap;
        
        @JsonProperty("yGap")
        private int yGap;
        
        private long expireTime;

        public String getToken() {
            return token;
        }

        public SlideCaptchaData setToken(String token) {
            this.token = token;
            return this;
        }

        public String getBackgroundImageBase64() {
            return backgroundImageBase64;
        }

        public SlideCaptchaData setBackgroundImageBase64(String backgroundImageBase64) {
            this.backgroundImageBase64 = backgroundImageBase64;
            return this;
        }

        public String getSliderImageBase64() {
            return sliderImageBase64;
        }

        public SlideCaptchaData setSliderImageBase64(String sliderImageBase64) {
            this.sliderImageBase64 = sliderImageBase64;
            return this;
        }

        public int getXGap() {
            return xGap;
        }

        public SlideCaptchaData setXGap(int xGap) {
            this.xGap = xGap;
            return this;
        }

        public int getYGap() {
            return yGap;
        }

        public SlideCaptchaData setYGap(int yGap) {
            this.yGap = yGap;
            return this;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public SlideCaptchaData setExpireTime(long expireTime) {
            this.expireTime = expireTime;
            return this;
        }
    }
}