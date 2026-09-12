package com.shiyu.ai.common.core.utils.file;

/** 媒体类型工具类 */
public class MimeTypeUtils {
    /**
     * IMAGE_PNG 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String IMAGE_PNG = "image/png";

    /**
     * IMAGE_JPG 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String IMAGE_JPG = "image/jpg";

    /**
     * IMAGE_JPEG 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String IMAGE_JPEG = "image/jpeg";

    /**
     * IMAGE_BMP 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String IMAGE_BMP = "image/bmp";

    /**
     * IMAGE_GIF 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String IMAGE_GIF = "image/gif";

    /**
     * IMAGE_EXTENSION 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png"};

    /**
     * FLASH_EXTENSION 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String[] FLASH_EXTENSION = {"swf", "flv"};

    /**
     * MEDIA_EXTENSION 属性，保存当前对象中的业务数据或协作依赖。
     */
    public static final String[] MEDIA_EXTENSION = {
        "swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg", "asf", "rm", "rmvb"
    };

    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb"};

    /** 音频扩展名 */
    public static final String[] AUDIO__EXTENSION = {
        "mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm"
    };

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
        // 图片
        "bmp",
        "gif",
        "jpg",
        "jpeg",
        "png",
        "doc",
        "docx",
        "xls",
        "xlsx",
        "ppt",
        "pptx",
        "html",
        "htm",
        "txt",
        // 压缩文件
        "rar",
        "zip",
        "gz",
        "bz2",
        // 视频格式
        "mp4",
        "avi",
        "rmvb",
        // 音频格式
        "mp3",
        "mp4",
        "mpeg",
        "mpga",
        "m4a",
        "wav",
        "webm",
        // pdf
        "pdf"
    };
}
