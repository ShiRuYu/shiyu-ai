package com.shiyu.ai.chat.product;

import com.shiyu.ai.common.core.utils.JSONUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/** Character Card v2 JSON and PNG tEXt metadata codec. No third-party card implementation is used. */
public final class CharacterCardCodec {
    private CharacterCardCodec() { }

    public static String toJson(CharacterCardV2 card) { return JSONUtils.toJsonString(card); }
    public static CharacterCardV2 fromJson(String json) { return JSONUtils.parseObject(json, CharacterCardV2.class); }

    public static byte[] toPng(CharacterCardV2 card, BufferedImage image) throws IOException {
        BufferedImage source = image == null ? new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) : image;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        if (!writers.hasNext()) throw new IOException("PNG writer unavailable");
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
            writer.setOutput(output);
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifierAdapter.specifier(source), null);
            var root = metadata.getAsTree("javax_imageio_png_1.0");
            var text = new IIOMetadataNode("tEXt");
            var entry = new IIOMetadataNode("tEXtEntry");
            entry.setAttribute("keyword", "chara");
            entry.setAttribute("value", toJson(card));
            text.appendChild(entry); root.appendChild(text); metadata.setFromTree("javax_imageio_png_1.0", root);
            writer.write(null, new javax.imageio.IIOImage(source, null, metadata), null);
            output.flush(); return bytes.toByteArray();
        } finally { writer.dispose(); }
    }

    public static CharacterCardV2 fromPng(byte[] png) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(png))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("PNG reader unavailable");
            ImageReader reader = readers.next();
            try { reader.setInput(input); IIOMetadata metadata = reader.getImageMetadata(0); var root = metadata.getAsTree("javax_imageio_png_1.0");
                var entries = ((org.w3c.dom.Element) root).getElementsByTagName("tEXtEntry");
                for (int i = 0; i < entries.getLength(); i++) if ("chara".equals(entries.item(i).getAttributes().getNamedItem("keyword").getNodeValue()))
                    return fromJson(entries.item(i).getAttributes().getNamedItem("value").getNodeValue());
                throw new IOException("character card metadata not found");
            } finally { reader.dispose(); }
        }
    }

    private static final class ImageTypeSpecifierAdapter {
        static javax.imageio.ImageTypeSpecifier specifier(BufferedImage image) { return javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image); }
    }
}
