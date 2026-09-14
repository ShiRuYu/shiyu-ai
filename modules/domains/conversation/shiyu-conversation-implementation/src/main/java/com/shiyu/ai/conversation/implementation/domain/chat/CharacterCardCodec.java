package com.shiyu.ai.conversation.implementation.domain.chat;

import com.shiyu.ai.common.core.utils.JSONUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * 负责角色卡数据的编码、解码和格式校验。
 */
public final class CharacterCardCodec {
    private CharacterCardCodec() {}

    /**
     * {@code toJson} 将当前对象转换为目标表示形式。
     *
     * @param card 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static String toJson(CharacterCardV2 card) {
        return JSONUtils.toJsonString(card);
    }

    /**
     * {@code fromJson} 执行当前类型定义的业务操作。
     *
     * @param json 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static CharacterCardV2 fromJson(String json) {
        return JSONUtils.parseObject(json, CharacterCardV2.class);
    }

    /**
     * {@code toPng} 将当前对象转换为目标表示形式。
     *
     * @param card 参数值，用于执行当前操作。
     * @param image 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static byte[] toPng(CharacterCardV2 card, BufferedImage image) throws IOException {
        BufferedImage source =
                image == null ? new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) : image;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) throw new IOException("PNG writer unavailable");
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            IIOMetadata metadata =
                    writer.getDefaultImageMetadata(
                            ImageTypeSpecifierAdapter.specifier(source), null);
            var root = metadata.getAsTree("javax_imageio_png_1.0");
            var text = new IIOMetadataNode("tEXt");
            var entry = new IIOMetadataNode("tEXtEntry");
            entry.setAttribute("keyword", "chara");
            entry.setAttribute("value", toJson(card));
            text.appendChild(entry);
            root.appendChild(text);
            metadata.setFromTree("javax_imageio_png_1.0", root);
            writer.write(null, new javax.imageio.IIOImage(source, null, metadata), null);
            output.flush();
            return bytes.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    /**
     * {@code fromPng} 执行当前类型定义的业务操作。
     *
     * @param png 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public static CharacterCardV2 fromPng(byte[] png) throws IOException {
        try (ImageInputStream input =
                ImageIO.createImageInputStream(new ByteArrayInputStream(png))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("PNG reader unavailable");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                IIOMetadata metadata = reader.getImageMetadata(0);
                var root = metadata.getAsTree("javax_imageio_png_1.0");
                var entries = ((org.w3c.dom.Element) root).getElementsByTagName("tEXtEntry");
                for (int i = 0; i < entries.getLength(); i++)
                    if ("chara"
                            .equals(
                                    entries.item(i)
                                            .getAttributes()
                                            .getNamedItem("keyword")
                                            .getNodeValue()))
                        return fromJson(
                                entries.item(i)
                                        .getAttributes()
                                        .getNamedItem("value")
                                        .getNodeValue());
                throw new IOException("character card metadata not found");
            } finally {
                reader.dispose();
            }
        }
    }

    /**
     * {@code ImageTypeSpecifierAdapter} 承载会话模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    private static final class ImageTypeSpecifierAdapter {
        static javax.imageio.ImageTypeSpecifier specifier(BufferedImage image) {
            return javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image);
        }
    }
}
