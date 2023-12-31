/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.render;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.jfinal.kit.StrKit;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * QrCodeRender 生成二维码
 */
public class QrCodeRender extends Render {

	protected String content;
	protected int width;
	protected int height;
	protected ErrorCorrectionLevel errorCorrectionLevel;

	/**
	 * 构造方法，经测试不指定纠错参数时，默认使用的是 'L' 最低级别纠错参数
	 * @param content 二维码携带内容
	 * @param width 二维码宽度
	 * @param height 二维码高度
	 */
	public QrCodeRender(String content, int width, int height) {
		init(content, width, height, null);
	}

	/**
	 * 带有纠错级别参数的构造方法，生成带有 logo 的二维码采用纠错原理
	 * 使用 ErrorCorrectionLevel.H 参数提升纠错能力
	 *
	 * ErrorCorrectionLevel 是枚举类型，纠错能力从高到低共有四个级别：
	 *  H = ~30% correction
	 *  Q = ~25% correction
	 *  M = ~15% correction
	 *  L = ~7%
	 *
	 *  使用的时候直接这样：ErrorCorrectionLevel.H
	 */
	public QrCodeRender(String content, int width, int height, ErrorCorrectionLevel errorCorrectionLevel) {
		init(content, width, height, errorCorrectionLevel);
	}

	/**
	 * 带有纠错级别参数的构造方法，纠错能力从高到低共有四个级别：'H'、'Q'、'M'、'L'
	 */
	public QrCodeRender(String content, int width, int height, char errorCorrectionLevel) {
		init(content, width, height, errorCorrectionLevel);
	}

	protected void init(String content, int width, int height, char errorCorrectionLevel) {
		if (errorCorrectionLevel == 'H') {
			init(content, width, height, ErrorCorrectionLevel.H);
		} else if (errorCorrectionLevel == 'Q') {
			init(content, width, height, ErrorCorrectionLevel.Q);
		} else if (errorCorrectionLevel == 'M') {
			init(content, width, height, ErrorCorrectionLevel.M);
		} else if (errorCorrectionLevel == 'L') {
			init(content, width, height, ErrorCorrectionLevel.L);
		} else {
			throw new IllegalArgumentException("errorCorrectionLevel 纠错级别参数值，从高到低必须为： 'H'、'Q'、'M'、'L'");
		}
	}

	protected void init(String content, int width, int height, ErrorCorrectionLevel errorCorrectionLevel) {
		if (StrKit.isBlank(content)) {
			throw new IllegalArgumentException("content 不能为空");
		}
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("width 与 height 不能小于 0");
		}
		this.content = content;
		this.width = width;
		this.height = height;
		this.errorCorrectionLevel = errorCorrectionLevel;
	}

	public void render() {
		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/png");

		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		hints.put(EncodeHintType.MARGIN, 0);    //去掉白色边框，极度重要，否则二维码周围的白边会很宽
		if (errorCorrectionLevel != null) {
			hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
		}

		OutputStream os = null;
		try {
			// MultiFormatWriter 可支持多种格式的条形码，在此直接使用 QRCodeWriter，通过查看源码可知少创建一个对象
			// BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

			os = response.getOutputStream();
			// 经测试 200 X 200 大小的二维码使用 "png" 格式只有 412B，而 "jpg" 却达到 15KB
			MatrixToImageWriter.writeToStream(bitMatrix, "png", os);    // format: "jpg"、"png"
		} catch (IOException e) {	// ClientAbortException、EofException 直接或间接继承自 IOException
			close(os);
			String name = e.getClass().getSimpleName();
			if ("ClientAbortException".equals(name) || "EofException".equals(name)) {
			} else {
				throw new RenderException(e);
			}
		} catch (Exception e) {
			throw new RenderException(e);
		}
	}
}
