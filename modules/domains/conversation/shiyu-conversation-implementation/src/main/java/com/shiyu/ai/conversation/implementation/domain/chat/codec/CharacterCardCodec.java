package com.shiyu.ai.conversation.implementation.domain.chat.codec;

import com.shiyu.ai.conversation.implementation.domain.chat.model.CharacterCardV2;

import com.shiyu.ai.common.foundation.utils.JSONUtils;

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
 * 解析或编解码 Character Card 相关的外部内容和领域数据。
 */
public final class CharacterCardCodec {
    private CharacterCardCodec() {}

    /**
     * 构建或转换 Character Card 相关业务数据，并返回处理结果。
     *
     * @param card 用于完成本次业务处理的 card 参数。
     * @return 返回 Character Card 相关操作生成的结果数据。
     */
    public static String toJson(CharacterCardV2 card) {
        return JSONUtils.toJsonString(card);
    }

    /**
     * 执行 Character Card 相关业务数据，并返回处理结果。
     *
     * @param json 用于完成本次业务处理的 json 参数。
     * @return 返回 Character Card 相关操作生成的结果数据。
     */
    public static CharacterCardV2 fromJson(String json) {
        return JSONUtils.parseObject(json, CharacterCardV2.class);
    }

    /**
     * 构建或转换 Character Card 相关业务数据，并返回处理结果。
     *
     * @param card 用于完成本次业务处理的 card 参数。
     * @param image 用于完成本次业务处理的 image 参数。
     * @return 返回 Character Card 相关操作生成的结果数据。
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
     * 执行 Character Card 相关业务数据，并返回处理结果。
     *
     * @param png 用于完成本次业务处理的 png 参数。
     * @return 返回 Character Card 相关操作生成的结果数据。
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
     * 将 Image Type Specifier 在不同层之间进行适配、转换或组装。
     */
    private static final class ImageTypeSpecifierAdapter {
        static javax.imageio.ImageTypeSpecifier specifier(BufferedImage image) {
            return javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image);
        }
    }
}
