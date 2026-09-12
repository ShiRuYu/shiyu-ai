package com.shiyu.ai.common.storage.api;

import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.lease.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import java.io.InputStream;

/**
 * {@code StorageObject} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
 * @param inputStream inputStream 属性，表示该记录组件承载的数据。
 * @param name 名称，表示该记录组件承载的数据。
 * @param contentType 内容类型，表示该记录组件承载的数据。
 * @param size 大小，表示该记录组件承载的数据。
 */
public record StorageObject(InputStream inputStream, String name, String contentType, long size) {}
